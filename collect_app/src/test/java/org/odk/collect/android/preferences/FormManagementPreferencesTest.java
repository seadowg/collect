package org.odk.collect.android.preferences;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.analytics.AnalyticsEvents.AUTO_FORM_UPDATE_PREF_CHANGE;
import static org.odk.collect.android.formmanagement.FormUpdateMode.MANUAL;
import static org.odk.collect.android.formmanagement.FormUpdateMode.MATCH_EXACTLY;
import static org.odk.collect.android.formmanagement.FormUpdateMode.PREVIOUSLY_DOWNLOADED_ONLY;
import static org.odk.collect.android.injection.DaggerUtils.getComponent;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_DOWNLOAD;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class FormManagementPreferencesTest {

    private SharedPreferences prefs;
    private Context context;

    private final Analytics analytics = mock(Analytics.class);

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        prefs = getComponent(context).preferencesProvider().getGeneralSharedPreferences();

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public Analytics providesAnalytics(Application application, GeneralSharedPreferences generalSharedPreferences) {
                return analytics;
            }
        });
    }

    @Test
    public void whenGoogleDriveUsedAsServer_showsUpdateModeAsManual_andDisablesPrefs() {
        prefs.edit().putString(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context)).apply();
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).getSummary(), is(context.getString(R.string.manually)));
            assertThat(prefs.getString(KEY_FORM_UPDATE_MODE, ""), is(MATCH_EXACTLY.getValue(context)));

            assertThat(f.findPreference(KEY_FORM_UPDATE_MODE).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_DOWNLOAD).isEnabled(), is(false));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_disablesPrefs() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(false));
            assertThat(f.findPreference(KEY_AUTOMATIC_DOWNLOAD).isEnabled(), is(false));
        });
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_disablesPrefs() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_DOWNLOAD).isEnabled(), is(true));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_disablesPrefs() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            assertThat(f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).isEnabled(), is(true));
            assertThat(f.findPreference(KEY_AUTOMATIC_DOWNLOAD).isEnabled(), is(false));
        });
    }

    @Test
    public void whenMatchExactlyEnabled_andAutomaticDownloadDisabled_showsAutomaticDownloadAsChecked() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_DOWNLOAD, false).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_DOWNLOAD);
            assertThat(automaticDownload.isChecked(), is(true));
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_DOWNLOAD, true), is(false));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MANUAL.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_DOWNLOAD, true).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_DOWNLOAD);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_DOWNLOAD, false), is(true));
        });
    }

    @Test
    public void whenGoogleDriveUsedAsServer_andAutomaticDownloadEnabled_showsAutomaticDownloadAsNotChecked() {
        prefs.edit().putString(KEY_PROTOCOL, Protocol.GOOGLE.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_DOWNLOAD, true).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_DOWNLOAD);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_DOWNLOAD, false), is(true));
        });
    }

    @Test
    public void whenManualUpdatesEnabled_andAutomaticDownloadDisabled_settingToPreviouslyDownloaded_resetsAutomaticDownload() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();
        prefs.edit().putBoolean(KEY_AUTOMATIC_DOWNLOAD, false).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            ListPreference updateMode = f.findPreference(KEY_FORM_UPDATE_MODE);
            updateMode.setValue(PREVIOUSLY_DOWNLOADED_ONLY.getValue(context));
            shadowOf(getMainLooper()).idle();

            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_DOWNLOAD);
            assertThat(automaticDownload.isChecked(), is(false));
            assertThat(prefs.getBoolean(KEY_AUTOMATIC_DOWNLOAD, true), is(false));
        });
    }

    @Test
    public void changingAutomaticDownload_logsAnalytics() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, PREVIOUSLY_DOWNLOADED_ONLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            CheckBoxPreference automaticDownload = f.findPreference(KEY_AUTOMATIC_DOWNLOAD);

            automaticDownload.setChecked(true);
            verify(analytics).logEvent(AUTO_FORM_UPDATE_PREF_CHANGE, "Automatic form updates", "true every_fifteen_minutes");

            automaticDownload.setChecked(false);
            verify(analytics).logEvent(AUTO_FORM_UPDATE_PREF_CHANGE, "Automatic form updates", "false every_fifteen_minutes");
        });
    }

    @Test
    public void changingAutomaticUpdateFrequency_logsAnalytics() {
        prefs.edit().putString(KEY_FORM_UPDATE_MODE, MATCH_EXACTLY.getValue(context)).apply();

        FragmentScenario<FormManagementPreferences> scenario = FragmentScenario.launch(FormManagementPreferences.class);
        scenario.onFragment(f -> {
            ListPreference automaticDownload = f.findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK);

            automaticDownload.setValue(f.getString(R.string.every_24_hours_value));
            verify(analytics).logEvent(AUTO_FORM_UPDATE_PREF_CHANGE, "Periodic form updates check", "every_24_hours");
        });
    }
}