package org.odk.collect.android.formlists.blankformlist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.formmanagement.FormsUpdater
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState
import org.odk.collect.android.preferences.utilities.FormUpdateMode
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsDirDiskFormsSynchronizer
import org.odk.collect.androidtest.getOrAwaitValue
import org.odk.collect.forms.Form
import org.odk.collect.forms.FormSourceException
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.InMemSettings
import org.odk.collect.testshared.BooleanChangeLock
import org.odk.collect.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class BlankFormListViewModelTest {
    private val formsRepository = InMemFormsRepository()
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val syncRepository: SyncStatusAppState = mock()
    private val formsUpdater: FormsUpdater = mock()
    private val scheduler = FakeScheduler()
    private val generalSettings = InMemSettings()
    private val analytics: Analytics = mock()
    private val changeLockProvider: ChangeLockProvider = mock()
    private val formsDirDiskFormsSynchronizer: FormsDirDiskFormsSynchronizer = mock()
    private val projectId = "projectId"

    private val changeLock = BooleanChangeLock()
    private lateinit var viewModel: BlankFormListViewModel

    @Test
    fun `syncWithStorage should be triggered when viewModel is initialized`() {
        createViewModel()
        verify(formsDirDiskFormsSynchronizer).synchronizeAndReturnError()
    }

    @Test
    fun `syncWithStorage return correct result`() {
        whenever(formsDirDiskFormsSynchronizer.synchronizeAndReturnError()).thenReturn("Result text")
        createViewModel()
        assertThat(viewModel.syncResult.value?.value, `is`("Result text"))
    }

    @Test
    fun `syncWithStorage should not be triggered when viewModel is initialized if forms lock is locked`() {
        changeLock.lock()
        val formsDirDiskFormsSynchronizer: FormsDirDiskFormsSynchronizer = mock()
        createViewModel()

        verifyNoInteractions(formsDirDiskFormsSynchronizer)
    }

    @Test
    fun `syncWithServer when task finishes sets result to true`() {
        createViewModel()
        generalSettings.save(ProjectKeys.KEY_SERVER_URL, "https://sample.com")
        doReturn(true).whenever(formsUpdater).matchFormsWithServer(projectId)
        val result = viewModel.syncWithServer()
        scheduler.runBackground()
        assertThat(result.value, `is`(true))
    }

    @Test
    fun `syncWithServer when there is an error sets result to false`() {
        createViewModel()
        generalSettings.save(ProjectKeys.KEY_SERVER_URL, "https://sample.com")
        doReturn(false).whenever(formsUpdater).matchFormsWithServer(projectId)
        val result = viewModel.syncWithServer()
        scheduler.runBackground()
        assertThat(result.value, `is`(false))
    }

    @Test
    fun `isMatchExactlyEnabled returns correct value based on settings`() {
        createViewModel()

        generalSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)

        assertThat(viewModel.isMatchExactlyEnabled(), `is`(false))

        generalSettings.save(ProjectKeys.KEY_FORM_UPDATE_MODE, FormUpdateMode.MATCH_EXACTLY.getValue(context))

        assertThat(viewModel.isMatchExactlyEnabled(), `is`(true))
    }

    @Test
    fun `isSyncingWithServer follows repository isSyncing`() {
        createViewModel()

        val liveData = MutableLiveData(true)
        whenever(syncRepository.isSyncing(projectId)).thenReturn(liveData)

        val isSyncing = viewModel.isSyncingWithServer()
        assertThat(isSyncing.getOrAwaitValue(), `is`(true))
        liveData.value = false
        assertThat(isSyncing.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `isOutOfSyncWithServer follows repository syncError`() {
        createViewModel()

        val liveData = MutableLiveData<FormSourceException?>(FormSourceException.FetchError())
        whenever(syncRepository.getSyncError(projectId)).thenReturn(liveData)

        val outOfSync = viewModel.isOutOfSyncWithServer()
        assertThat(outOfSync.getOrAwaitValue(), `is`(true))
        liveData.value = null
        assertThat(outOfSync.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `isAuthenticationRequired follows repository syncError`() {
        createViewModel()

        val liveData = MutableLiveData<FormSourceException?>(FormSourceException.FetchError())
        whenever(syncRepository.getSyncError(projectId)).thenReturn(liveData)

        val authenticationRequired = viewModel.isAuthenticationRequired()
        assertThat(authenticationRequired.getOrAwaitValue(), `is`(false))
        liveData.value = FormSourceException.AuthRequired()
        assertThat(authenticationRequired.getOrAwaitValue(), `is`(true))
        liveData.value = null
        assertThat(authenticationRequired.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun `original list of forms should be fetched from database initially and sorted by name ASC`() {
        saveForms(BlankFormFixtures.blankForm1, BlankFormFixtures.blankForm2)
        createViewModel()

        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm1)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm2)
    }

    @Test
    fun `deleted forms should be ignored`() {
        saveForms(BlankFormFixtures.blankForm1, BlankFormFixtures.blankForm4)
        createViewModel()

        assertThat(viewModel.formsToDisplay.value!!.size, `is`(1))
        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm1)
    }

    @Test
    fun `list of forms should be sorted when sorting order is changed`() {
        saveForms(BlankFormFixtures.blankForm1, BlankFormFixtures.blankForm2, BlankFormFixtures.blankForm5)
        createViewModel()

        // Sort by name DESC
        viewModel.sortingOrder = 1

        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm2)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm1)
        assertFormItem(viewModel.formsToDisplay.value!![2], BlankFormFixtures.blankForm5)

        // Sort by date ASC
        viewModel.sortingOrder = 2

        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm1)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm2)
        assertFormItem(viewModel.formsToDisplay.value!![2], BlankFormFixtures.blankForm5)

        // Sort by date DESC
        viewModel.sortingOrder = 3

        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm5)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm2)
        assertFormItem(viewModel.formsToDisplay.value!![2], BlankFormFixtures.blankForm1)

        // Sort by name ASC
        viewModel.sortingOrder = 0

        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm5)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm1)
        assertFormItem(viewModel.formsToDisplay.value!![2], BlankFormFixtures.blankForm2)
    }

    @Test
    fun `list of forms should be filtered when filterText is changed`() {
        saveForms(BlankFormFixtures.blankForm1, BlankFormFixtures.blankForm2, BlankFormFixtures.blankForm3)
        createViewModel()

        viewModel.filterText = "2"

        assertThat(viewModel.formsToDisplay.value?.size, `is`(2))
        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm2)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm3)

        viewModel.filterText = "2x"

        assertThat(viewModel.formsToDisplay.value?.size, `is`(1))
        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm3)

        viewModel.filterText = ""

        assertThat(viewModel.formsToDisplay.value?.size, `is`(3))
        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm1)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm2)
        assertFormItem(viewModel.formsToDisplay.value!![2], BlankFormFixtures.blankForm3)
    }

    @Test
    fun `filtering and sorting should work together`() {
        saveForms(BlankFormFixtures.blankForm1, BlankFormFixtures.blankForm2, BlankFormFixtures.blankForm3)
        createViewModel()

        viewModel.filterText = "2"

        assertThat(viewModel.formsToDisplay.value?.size, `is`(2))
        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm2)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm3)

        viewModel.sortingOrder = 1

        assertThat(viewModel.formsToDisplay.value?.size, `is`(2))
        assertFormItem(viewModel.formsToDisplay.value!![0], BlankFormFixtures.blankForm3)
        assertFormItem(viewModel.formsToDisplay.value!![1], BlankFormFixtures.blankForm2)
    }

    private fun saveForms(vararg forms: Form) {
        formsRepository.deleteAll()

        forms.forEach {
            formsRepository.save(it)
        }
    }

    private fun createViewModel() {
        whenever(changeLockProvider.getFormLock(projectId)).thenReturn(changeLock)

        viewModel = BlankFormListViewModel(
            formsRepository,
            context,
            syncRepository,
            formsUpdater,
            scheduler,
            generalSettings,
            analytics,
            changeLockProvider,
            formsDirDiskFormsSynchronizer,
            projectId
        )

        scheduler.runBackground()
        scheduler.runBackground()
    }

    private fun assertFormItem(blankFormListItem: BlankFormListItem, form: Form) {
        assertThat(
            blankFormListItem,
            `is`(
                BlankFormListItem(
                    databaseId = form.dbId,
                    formId = form.formId,
                    formName = form.displayName,
                    formVersion = form.version ?: "",
                    geometryPath = form.geometryXpath ?: "",
                    dateOfCreation = form.date,
                    dateOfLastUsage = 0,
                    contentUri = FormsContract.getUri(projectId, form.dbId)
                )
            )
        )
    }
}
