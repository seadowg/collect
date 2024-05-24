package org.odk.collect.android.formmanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.odk.collect.android.formmanagement.download.FormDownloadException
import org.odk.collect.android.formmanagement.download.ServerFormDownloader
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.android.projects.ProjectDependencyModule
import org.odk.collect.android.projects.ProjectDependencyModuleFactory
import org.odk.collect.androidshared.data.AppState
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSourceException
import org.odk.collect.settings.keys.ProjectKeys
import java.io.File
import java.util.function.Supplier
import java.util.stream.Collectors

class FormsDataService(
    private val appState: AppState,
    private val notifier: Notifier,
    private val projectDependencyModuleFactory: ProjectDependencyModuleFactory,
    private val clock: Supplier<Long>
) {

    fun getForms(projectId: String): Flow<List<Form>> {
        return getFormsFlow(projectId)
    }

    fun isSyncing(projectId: String): LiveData<Boolean> {
        return getSyncingLiveData(projectId)
    }

    fun getServerError(projectId: String): LiveData<FormSourceException?> {
        return getServerErrorLiveData(projectId)
    }

    fun getDiskError(projectId: String): LiveData<String?> {
        return getDiskErrorLiveData(projectId)
    }

    fun clear(projectId: String) {
        getServerErrorLiveData(projectId).value = null
    }

    fun downloadForms(
        projectId: String,
        forms: List<ServerFormDetails>,
        progressReporter: (Int, Int) -> Unit,
        isCancelled: () -> Boolean
    ): Map<ServerFormDetails, FormDownloadException?> {
        val projectDependencyProvider = projectDependencyModuleFactory.create(projectId)
        val formDownloader =
            formDownloader(projectDependencyProvider, clock)

        return ServerFormUseCases.downloadForms(
            forms,
            projectDependencyProvider.formsLock,
            formDownloader,
            progressReporter,
            isCancelled
        )
    }

    /**
     * Downloads updates for the project's already downloaded forms. If Automatic download is
     * disabled the user will just be notified that there are updates available.
     */
    fun downloadUpdates(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                syncWithStorage(projectId)

                val serverFormsDetailsFetcher = serverFormsDetailsFetcher(projectDependencies)
                val formDownloader = formDownloader(projectDependencies, clock)

                try {
                    val serverForms: List<ServerFormDetails> =
                        serverFormsDetailsFetcher.fetchFormDetails()
                    val updatedForms =
                        serverForms.stream().filter { obj: ServerFormDetails -> obj.isUpdated }
                            .collect(Collectors.toList())
                    if (updatedForms.isNotEmpty()) {
                        if (projectDependencies.generalSettings.getBoolean(ProjectKeys.KEY_AUTOMATIC_UPDATE)) {
                            val results = ServerFormUseCases.downloadForms(
                                updatedForms,
                                projectDependencies.formsLock,
                                formDownloader
                            )

                            notifier.onUpdatesDownloaded(results, projectId)
                        } else {
                            notifier.onUpdatesAvailable(updatedForms, projectId)
                        }
                    }

                    syncWithDb(projectId)
                } catch (_: FormSourceException) {
                    // Ignored
                }
            }
        }
    }

    /**
     * Downloads new forms, updates existing forms and deletes forms that are no longer part of
     * the project's form list.
     */
    @JvmOverloads
    fun matchFormsWithServer(projectId: String, notify: Boolean = true): Boolean {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        return projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                startSync(projectId)
                syncWithStorage(projectId)

                val serverFormsDetailsFetcher = serverFormsDetailsFetcher(projectDependencies)
                val formDownloader = formDownloader(projectDependencies, clock)

                val serverFormsSynchronizer = ServerFormsSynchronizer(
                    serverFormsDetailsFetcher,
                    projectDependencies.formsRepository,
                    projectDependencies.instancesRepository,
                    formDownloader
                )

                val exception = try {
                    serverFormsSynchronizer.synchronize()
                    if (notify) {
                        notifier.onSync(null, projectId)
                    }

                    null
                } catch (e: FormSourceException) {
                    if (notify) {
                        notifier.onSync(e, projectId)
                    }

                    e
                }

                syncWithDb(projectId)
                finishSync(projectId, exception)
                exception == null
            } else {
                false
            }
        }
    }

    fun deleteForm(projectId: String, formId: Long) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        LocalFormUseCases.deleteForm(
            projectDependencies.formsRepository,
            projectDependencies.instancesRepository,
            formId
        )
        syncWithDb(projectId)
    }

    fun update(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        projectDependencies.formsLock.withLock { acquiredLock ->
            if (acquiredLock) {
                startSync(projectId)
                syncWithStorage(projectId)
                syncWithDb(projectId)
                finishSync(projectId)
            }
        }
    }

    private fun syncWithStorage(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        val error = LocalFormUseCases.synchronizeWithDisk(
            projectDependencies.formsRepository,
            projectDependencies.formsDir
        )

        getDiskErrorLiveData(projectId).postValue(error)
    }

    private fun startSync(projectId: String) {
        getSyncingLiveData(projectId).postValue(true)
    }

    private fun finishSync(projectId: String, exception: FormSourceException? = null) {
        getServerErrorLiveData(projectId).postValue(exception)
        getSyncingLiveData(projectId).postValue(false)
    }

    private fun syncWithDb(projectId: String) {
        val projectDependencies = projectDependencyModuleFactory.create(projectId)
        getFormsFlow(projectId).value = projectDependencies.formsRepository.all
    }

    private fun getFormsFlow(projectId: String): MutableStateFlow<List<Form>> {
        return appState.get("forms:$projectId", MutableStateFlow(emptyList()))
    }

    private fun getSyncingLiveData(projectId: String) =
        appState.get("$KEY_PREFIX_SYNCING:$projectId", MutableLiveData(false))

    private fun getServerErrorLiveData(projectId: String) =
        appState.get("$KEY_PREFIX_ERROR:$projectId", MutableLiveData<FormSourceException>(null))

    private fun getDiskErrorLiveData(projectId: String): MutableLiveData<String?> =
        appState.get("$KEY_PREFIX_DISK_ERROR:$projectId", MutableLiveData<String?>(null))

    companion object {
        const val KEY_PREFIX_SYNCING = "syncStatusSyncing"
        const val KEY_PREFIX_ERROR = "syncStatusError"
        const val KEY_PREFIX_DISK_ERROR = "diskError"
    }
}

private fun formDownloader(
    projectDependencyModule: ProjectDependencyModule,
    clock: Supplier<Long>
): ServerFormDownloader {
    return ServerFormDownloader(
        projectDependencyModule.formSource,
        projectDependencyModule.formsRepository,
        File(projectDependencyModule.cacheDir),
        projectDependencyModule.formsDir,
        FormMetadataParser(),
        clock,
        projectDependencyModule.entitiesRepository
    )
}

private fun serverFormsDetailsFetcher(
    projectDependencyModule: ProjectDependencyModule
): ServerFormsDetailsFetcher {
    return ServerFormsDetailsFetcher(
        projectDependencyModule.formsRepository,
        projectDependencyModule.formSource
    )
}
