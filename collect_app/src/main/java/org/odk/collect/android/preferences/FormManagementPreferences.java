/*
 * Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.formmanagement.FormUpdateMode;

import javax.inject.Inject;

import static org.odk.collect.android.analytics.AnalyticsEvents.AUTO_FORM_UPDATE_PREF_CHANGE;
import static org.odk.collect.android.preferences.AdminKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_DOWNLOAD;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_CONSTRAINT_BEHAVIOR;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;
import static org.odk.collect.android.preferences.utilities.PreferencesUtils.displayDisabled;

public class FormManagementPreferences extends BasePreferenceFragment {

    @Inject
    Analytics analytics;

    @Inject
    PreferencesProvider preferencesProvider;

    public static FormManagementPreferences newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);

        FormManagementPreferences formManagementPreferences = new FormManagementPreferences();
        formManagementPreferences.setArguments(bundle);

        return formManagementPreferences;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.form_management_preferences, rootKey);
        updatePreferences(getPreferenceManager().getSharedPreferences());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        updatePreferences(sharedPreferences);

        if (key.equals(KEY_AUTOMATIC_DOWNLOAD)) {
            String formUpdateCheckPeriod = sharedPreferences.getString(KEY_PERIODIC_FORM_UPDATES_CHECK, null);
            analytics.logEvent(AUTO_FORM_UPDATE_PREF_CHANGE, "Automatic form updates", sharedPreferences.getBoolean(KEY_AUTOMATIC_DOWNLOAD, false) + " " + formUpdateCheckPeriod);
        }

        if (key.equals(KEY_PERIODIC_FORM_UPDATES_CHECK)) {
            String formUpdateCheckPeriod = sharedPreferences.getString(KEY_PERIODIC_FORM_UPDATES_CHECK, null);
            analytics.logEvent(AUTO_FORM_UPDATE_PREF_CHANGE, "Periodic form updates check", formUpdateCheckPeriod);
        }
    }

    private void updatePreferences(SharedPreferences sharedPreferences) {
        final ListPreference constrainBehavior = findPreference(KEY_CONSTRAINT_BEHAVIOR);
        constrainBehavior.setEnabled(preferencesProvider.getAdminSharedPreferences().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM, true));

        String protocol = sharedPreferences.getString(KEY_PROTOCOL, null);
        String formUpdateMode = sharedPreferences.getString(KEY_FORM_UPDATE_MODE, null);

        Preference updateFrequencyPref = findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK);
        CheckBoxPreference automaticDownloadPref = findPreference(KEY_AUTOMATIC_DOWNLOAD);

        if (Protocol.parse(getActivity(), protocol) == Protocol.GOOGLE) {
            displayDisabled(findPreference(KEY_FORM_UPDATE_MODE), getString(R.string.manually));
            displayDisabled(automaticDownloadPref, false);
            updateFrequencyPref.setEnabled(false);
        } else {
            switch (FormUpdateMode.parse(getActivity(), formUpdateMode)) {
                case MANUAL:
                    displayDisabled(automaticDownloadPref, false);
                    updateFrequencyPref.setEnabled(false);
                    break;
                case PREVIOUSLY_DOWNLOADED_ONLY:
                    automaticDownloadPref.setEnabled(true);
                    automaticDownloadPref.setChecked(sharedPreferences.getBoolean(KEY_AUTOMATIC_DOWNLOAD, false));

                    updateFrequencyPref.setEnabled(true);
                    break;
                case MATCH_EXACTLY:
                    displayDisabled(automaticDownloadPref, true);
                    updateFrequencyPref.setEnabled(true);
                    break;
            }
        }
    }
}
