package org.odk.collect.android.preferences;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.shared.permissions.ContextCompatPermissionsChecker;
import org.odk.collect.shared.permissions.PermissionsChecker;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.DeviceDetailsProvider;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.robolectric.annotation.LooperMode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_PHONENUMBER;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class FormMetadataFragmentTest {

    private final FakePhoneStatePermissionsProvider permissionsProvider = new FakePhoneStatePermissionsProvider();
    private final DeviceDetailsProvider deviceDetailsProvider = mock(DeviceDetailsProvider.class);

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker, StorageStateProvider storageStateProvider) {
                return permissionsProvider;
            }

            @Override
            public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
                return deviceDetailsProvider;
            }
        });
    }

    @Test
    public void recreating_doesntRequestPermissionsAgain() {
        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        assertThat(permissionsProvider.timesRequested, equalTo(1));

        scenario.recreate();
        assertThat(permissionsProvider.timesRequested, equalTo(1));
    }

    @Test
    public void recreating_whenPermissionsAcceptedPreviously_showsPermissionDependantPreferences() {
        when(deviceDetailsProvider.getDeviceId()).thenReturn("123456789");

        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionsProvider.grant();
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo("123456789"));
        });

        scenario.recreate();
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo("123456789"));
        });
    }

    @Test
    public void recreating_whenPermissionsGrantedPreviously_doesNotShowPermissionDependantPreferences() {
        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionsProvider.deny();
        scenario.recreate();
        verifyNoInteractions(deviceDetailsProvider);
    }

    @Test
    public void whenDeviceDetailsAreMissing_preferenceSummariesAreNotSet() {
        when(deviceDetailsProvider.getLine1Number()).thenReturn(null);
        when(deviceDetailsProvider.getDeviceId()).thenReturn(null);

        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionsProvider.grant();
        scenario.onFragment(fragment -> {
            String notSetMessage = fragment.getContext().getString(R.string.preference_not_available);

            assertThat(fragment.findPreference(KEY_METADATA_PHONENUMBER).getSummary(), equalTo(notSetMessage));
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo(notSetMessage));
        });
    }

    private static class FakePhoneStatePermissionsProvider extends PermissionsProvider {

        int timesRequested;
        private PermissionListener lastAction;
        private boolean granted;

        private FakePhoneStatePermissionsProvider() {
            super(new ContextCompatPermissionsChecker(InstrumentationRegistry.getInstrumentation().getTargetContext()), new StorageStateProvider());
        }

        @Override
        public void requestReadPhoneStatePermission(Activity activity, boolean displayPermissionDeniedDialog, @NonNull PermissionListener action) {
            timesRequested++;
            this.lastAction = action;
        }

        @Override
        public boolean isReadPhoneStatePermissionGranted() {
            return granted;
        }

        void grant() {
            granted = true;
            lastAction.granted();
        }

        void deny() {
            granted = false;
            lastAction.denied();
        }
    }
}
