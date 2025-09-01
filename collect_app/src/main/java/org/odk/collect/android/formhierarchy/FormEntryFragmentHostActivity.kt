package org.odk.collect.android.formhierarchy

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormEntryViewModelFactory
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formentry.FormOpeningMode
import org.odk.collect.android.formentry.FormSessionRepository
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.location.LocationClient
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.printer.HtmlPrinter
import org.odk.collect.qrcode.zxing.QRCodeCreatorImpl
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FormEntryFragmentHostActivity : LocalizedActivity() {

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var formSessionRepository: FormSessionRepository

    @Inject
    lateinit var mediaUtils: MediaUtils

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var audioRecorder: AudioRecorder

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var entitiesRepositoryProvider: EntitiesRepositoryProvider

    @Inject
    lateinit var permissionsChecker: PermissionsChecker

    @Inject
    lateinit var fusedLocationClient: LocationClient

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var autoSendSettingsProvider: AutoSendSettingsProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var savepointsRepositoryProvider: SavepointsRepositoryProvider

    @Inject
    lateinit var instancesDataService: InstancesDataService

    @Inject
    lateinit var changeLockProvider: ChangeLockProvider

    private lateinit var sessionId: String
    private val viewModelFactory by lazy {
        FormEntryViewModelFactory(
            this,
            FormOpeningMode.EDIT_SAVED,
            sessionId,
            scheduler,
            formSessionRepository,
            mediaUtils,
            audioRecorder,
            projectsDataService,
            entitiesRepositoryProvider,
            settingsProvider,
            permissionsChecker,
            fusedLocationClient,
            permissionsProvider,
            autoSendSettingsProvider,
            formsRepositoryProvider,
            instancesRepositoryProvider,
            savepointsRepositoryProvider,
            QRCodeCreatorImpl(),
            HtmlPrinter(),
            instancesDataService,
            changeLockProvider
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)

        sessionId = if (savedInstanceState == null) {
            formSessionRepository.create()
        } else {
            savedInstanceState.getString(KEY_SESSION_ID)!!
        }

        this.supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(FormEntryFragment::class.java) { FormEntryFragment(viewModelFactory) }
            .build()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_entry_host_layout)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.setGraph(R.navigation.form_entry)
        setSupportActionBar(findViewById(org.odk.collect.androidshared.R.id.toolbar))
    }

    companion object {
        private const val KEY_SESSION_ID = "session_id"
    }
}
