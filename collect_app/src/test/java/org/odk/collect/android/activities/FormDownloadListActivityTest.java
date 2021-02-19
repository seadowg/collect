package org.odk.collect.android.activities;

import android.app.Application;
import android.database.Cursor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.support.AlwaysGrantStoragePermissionsPermissionsProvider;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.dao.FormsDao;
import org.odk.collect.shared.permissions.PermissionsChecker;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.robolectric.shadows.ShadowEnvironment;

import static android.os.Environment.MEDIA_MOUNTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.RobolectricHelpers.overrideAppDependencyModule;

@RunWith(AndroidJUnit4.class)
public class FormDownloadListActivityTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock Analytics analytics;

    @Mock FormsDao formsDao;

    @Before public void setup() {
        overrideAppDependencyModule(new AppDependencyModule(analytics, formsDao));
        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED); // Required for ODK directories to be created
    }

    @Test
    public void tappingDownloadButton_logsAnalytics() {
        Cursor cursor = mock(Cursor.class);
        when(cursor.getCount()).thenReturn(0);
        when(formsDao.getFormsCursor()).thenReturn(cursor);

        ActivityScenario<FormDownloadListActivity> downloadActivity = ActivityScenario.launch(FormDownloadListActivity.class);

        downloadActivity.onActivity(activity -> {
            activity.findViewById(R.id.add_button).setEnabled(true);
            activity.findViewById(R.id.add_button).performClick();
            verify(analytics).logEvent(eq(AnalyticsEvents.FIRST_FORM_DOWNLOAD), any());
        });
    }

    private static class AppDependencyModule extends org.odk.collect.android.injection.config.AppDependencyModule {

        Analytics analytics;
        FormsDao formsDao;

        AppDependencyModule(Analytics analytics, FormsDao formsDao) {
            this.analytics = analytics;
            this.formsDao = formsDao;
        }

        @Override
        public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
            return analytics;
        }

        @Override
        public FormsDao provideFormsDao() {
            return formsDao;
        }

        @Override
        public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker, StorageStateProvider storageStateProvider) {
            return new AlwaysGrantStoragePermissionsPermissionsProvider(permissionsChecker, storageStateProvider);
        }

        @Override
        public StorageInitializer providesStorageInitializer() {
            return new AlwaysSuccessfullyCreateOdkDirsStorageInitializer();
        }
    }

    private static class AlwaysSuccessfullyCreateOdkDirsStorageInitializer extends StorageInitializer {
        @Override
        public void createOdkDirsOnStorage() {
            // do nothing
        }
    }
}
