package org.odk.collect.android.projects

import org.odk.collect.android.formmanagement.FormSourceProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.settings.SettingsProvider

/**
 * Provides all the basic/building block dependencies needed when performing logic inside a
 * project.
 */
data class ProjectDependencyModule(
    val projectId: String,
    val settingsProvider: SettingsProvider,
    val formsRepositoryProvider: FormsRepositoryProvider,
    val instancesRepositoryProvider: InstancesRepositoryProvider,
    val storagePathProvider: StoragePathProvider,
    val changeLockProvider: ChangeLockProvider,
    val formSourceProvider: FormSourceProvider,
    val savepointsRepositoryProvider: SavepointsRepositoryProvider,
    val entitiesRepositoryProvider: ProjectDependencyProvider<EntitiesRepository>
) {
    val generalSettings by lazy { settingsProvider.getUnprotectedSettings(projectId) }
    val formsRepository by lazy { formsRepositoryProvider.get(projectId) }
    val instancesRepository by lazy { instancesRepositoryProvider.get(projectId) }
    val formSource by lazy { formSourceProvider.get(projectId) }
    val formsLock by lazy { changeLockProvider.getFormLock(projectId) }
    val instancesLock by lazy { changeLockProvider.getInstanceLock(projectId) }
    val formsDir by lazy { storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId) }
    val cacheDir by lazy { storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId) }
    val entitiesRepository by lazy { entitiesRepositoryProvider.get(projectId) }
    val savepointsRepository by lazy { savepointsRepositoryProvider.get(projectId) }
}

class ProjectDependencyModuleFactory(
    private val settingsProvider: SettingsProvider,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val storagePathProvider: StoragePathProvider,
    private val changeLockProvider: ChangeLockProvider,
    private val formSourceProvider: FormSourceProvider,
    private val savepointsRepositoryProvider: SavepointsRepositoryProvider,
    private val entitiesRepositoryProvider: ProjectDependencyProvider<EntitiesRepository>,
) {
    fun create(projectId: String) = ProjectDependencyModule(
        projectId,
        settingsProvider,
        formsRepositoryProvider,
        instancesRepositoryProvider,
        storagePathProvider,
        changeLockProvider,
        formSourceProvider,
        savepointsRepositoryProvider,
        entitiesRepositoryProvider
    )
}
