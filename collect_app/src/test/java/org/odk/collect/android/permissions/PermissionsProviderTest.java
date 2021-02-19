package org.odk.collect.android.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;

import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.shared.permissions.PermissionsChecker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PermissionsProviderTest {
    private PermissionsChecker permissionsChecker;
    private StorageStateProvider storageStateProvider;
    private PermissionsProvider permissionsProvider;

    @Before
    public void setup() {
        permissionsChecker = mock(PermissionsChecker.class);
        storageStateProvider = mock(StorageStateProvider.class);
    }

    @Test
    public void whenStoragePermissionsGrantedAndScopedStorageNotUsed_shouldAreStoragePermissionsGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)).thenReturn(true);
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(false);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areStoragePermissionsGranted(), is(true));
    }

    @Test
    public void whenStoragePermissionsGrantedAndScopedStorageUsed_shouldAreStoragePermissionsGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)).thenReturn(true);
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(true);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areStoragePermissionsGranted(), is(true));
    }

    @Test
    public void whenStoragePermissionsNotGrantedAndScopedStorageNotUsed_shouldAreStoragePermissionsGrantedReturnFalse() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)).thenReturn(false);
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(false);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areStoragePermissionsGranted(), is(false));
    }

    @Test
    public void whenStoragePermissionsNotGrantedAndScopedStorageUsed_shouldAreStoragePermissionsGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)).thenReturn(false);
        when(storageStateProvider.isScopedStorageUsed()).thenReturn(true);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areStoragePermissionsGranted(), is(true));
    }

    @Test
    public void whenCameraPermissionGranted_shouldIsCameraPermissionGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)).thenReturn(true);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.isCameraPermissionGranted(), is(true));
    }

    @Test
    public void whenCameraPermissionNotGranted_shouldIsCameraPermissionGrantedReturnFalse() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)).thenReturn(false);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.isCameraPermissionGranted(), is(false));
    }

    @Test
    public void whenLocationPermissionsGranted_shouldAreLocationPermissionsGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(true);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areLocationPermissionsGranted(), is(true));
    }

    @Test
    public void whenLocationPermissionsNotGranted_shouldAreLocationPermissionsGrantedReturnFalse() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(false);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areLocationPermissionsGranted(), is(false));
    }

    @Test
    public void whenCameraAndAudioPermissionsGranted_shouldAreCameraAndRecordAudioPermissionsGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)).thenReturn(true);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areCameraAndRecordAudioPermissionsGranted(), is(true));
    }

    @Test
    public void whenCameraAndAudioPermissionsNotGranted_shouldAreCameraAndRecordAudioPermissionsGrantedReturnFalse() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)).thenReturn(false);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.areCameraAndRecordAudioPermissionsGranted(), is(false));
    }

    @Test
    public void whenGetAccountsPermissionGranted_shouldIsGetAccountsPermissionGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.GET_ACCOUNTS)).thenReturn(true);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.isGetAccountsPermissionGranted(), is(true));
    }

    @Test
    public void whenGetAccountsPermissionNotGranted_shouldIsGetAccountsPermissionGrantedReturnFalse() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.GET_ACCOUNTS)).thenReturn(false);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.isGetAccountsPermissionGranted(), is(false));
    }

    @Test
    public void whenReadPhoneStatePermissionGranted_shouldIsReadPhoneStatePermissionGrantedReturnTrue() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)).thenReturn(true);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.isReadPhoneStatePermissionGranted(), is(true));
    }

    @Test
    public void whenReadPhoneStatePermissionNotGranted_shouldIsReadPhoneStatePermissionGrantedReturnFalse() {
        when(permissionsChecker.isPermissionGranted(Manifest.permission.READ_PHONE_STATE)).thenReturn(false);
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        assertThat(permissionsProvider.isReadPhoneStatePermissionGranted(), is(false));
    }

    @Test
    public void whenRequestReadUriPermissionGranted_shouldGrantedBeCalled() {
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        Activity activity = mock(Activity.class);
        Uri uri = mock(Uri.class);
        ContentResolver contentResolver = mock(ContentResolver.class);

        PermissionListener permissionListener = mock(PermissionListener.class);
        permissionsProvider.requestReadUriPermission(activity, uri, contentResolver, permissionListener);
        verify(permissionListener).granted();
    }

    @Test
    public void whenRequestReadUriPermissionNotGranted_shouldUserBeAskedToGrantReadStoragePermission() {
        permissionsProvider = spy(new PermissionsProvider(permissionsChecker, storageStateProvider));

        Activity activity = mock(Activity.class);
        Uri uri = mock(Uri.class);
        ContentResolver contentResolver = mock(ContentResolver.class);
        when(contentResolver.query(uri, null, null, null, null)).thenThrow(SecurityException.class);

        PermissionListener permissionListener = mock(PermissionListener.class);
        permissionsProvider.requestReadUriPermission(activity, uri, contentResolver, permissionListener);
        verify(permissionsProvider).requestReadStoragePermission(any(), any());
    }

    @Test
    public void whenRequestReadUriPermissionThrowsAnyException_shouldDeniedBeCalled() {
        permissionsProvider = new PermissionsProvider(permissionsChecker, storageStateProvider);

        Activity activity = mock(Activity.class);
        Uri uri = mock(Uri.class);
        ContentResolver contentResolver = mock(ContentResolver.class);
        when(contentResolver.query(uri, null, null, null, null)).thenThrow(RuntimeException.class);

        PermissionListener permissionListener = mock(PermissionListener.class);
        permissionsProvider.requestReadUriPermission(activity, uri, contentResolver, permissionListener);
        verify(permissionListener).denied();
    }
}
