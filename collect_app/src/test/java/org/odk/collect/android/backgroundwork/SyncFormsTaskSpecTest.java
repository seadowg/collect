package org.odk.collect.android.backgroundwork;

import android.app.Application;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.odk.collect.android.formmanagement.matchexactly.ServerFormsSynchronizer;
import org.odk.collect.android.formmanagement.matchexactly.SyncStatusAppState;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.BooleanChangeLock;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.FormSource;
import org.odk.collect.forms.FormSourceException;

import java.util.function.Supplier;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(AndroidJUnit4.class)
public class SyncFormsTaskSpecTest {

    private final ServerFormsSynchronizer serverFormsSynchronizer = mock(ServerFormsSynchronizer.class);
    private final SyncStatusAppState syncStatusAppState = mock(SyncStatusAppState.class);
    private final Notifier notifier = mock(Notifier.class);
    private final Analytics analytics = mock(Analytics.class);
    private final BooleanChangeLock changeLock = new BooleanChangeLock();

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public FormDownloader providesFormDownloader(FormSource formSource, FormsRepositoryProvider formsRepositoryProvider, StoragePathProvider storagePathProvider, Analytics analytics) {
                return mock(FormDownloader.class); // We don't want to build this dependency for `ServerFormsSynchronizer`
            }

            @Override
            public ChangeLock providesFormsChangeLock() {
                return changeLock;
            }

            @Override
            public ServerFormsSynchronizer providesServerFormSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepositoryProvider formsRepositoryProvider, FormDownloader formDownloader, InstancesRepositoryProvider instancesRepositoryProvider, FastExternalItemsetsRepository fastExternalItemsetsRepository) {
                return serverFormsSynchronizer;
            }

            @Override
            public SyncStatusAppState providesServerFormSyncRepository(Context context) {
                return syncStatusAppState;
            }

            @Override
            public Notifier providesNotifier(Application application, SettingsProvider settingsProvider) {
                return notifier;
            }

            @Override
            public Analytics providesAnalytics(Application application) {
                return analytics;
            }
        });

        CollectHelpers.setupDemoProject();
    }

    @Test
    public void setsRepositoryToSyncing_runsSync_thenSetsRepositoryToNotSyncingAndNotifies() throws Exception {
        InOrder inOrder = inOrder(syncStatusAppState, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        inOrder.verify(syncStatusAppState).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusAppState).finishSync(null);

        verify(notifier).onSync(null);
    }

    @Test
    public void logsAnalytics() {
        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(analytics).logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, "Success");
    }

    @Test
    public void whenSynchronizingFails_setsRepositoryToNotSyncingAndNotifiesWithError() throws Exception {
        FormSourceException exception = new FormSourceException.FetchError();
        doThrow(exception).when(serverFormsSynchronizer).synchronize();
        InOrder inOrder = inOrder(syncStatusAppState, serverFormsSynchronizer);

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        inOrder.verify(syncStatusAppState).startSync();
        inOrder.verify(serverFormsSynchronizer).synchronize();
        inOrder.verify(syncStatusAppState).finishSync(exception);

        verify(notifier).onSync(exception);
    }

    @Test
    public void whenSynchronizingFails_logsAnalytics() throws Exception {
        FormSourceException exception = new FormSourceException.FetchError();
        doThrow(exception).when(serverFormsSynchronizer).synchronize();

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verify(analytics).logEvent(AnalyticsEvents.MATCH_EXACTLY_SYNC_COMPLETED, "FETCH_ERROR");
    }

    @Test
    public void whenChangeLockLocked_doesNothing() {
        changeLock.lock();

        SyncFormsTaskSpec taskSpec = new SyncFormsTaskSpec();
        Supplier<Boolean> task = taskSpec.getTask(ApplicationProvider.getApplicationContext());
        task.get();

        verifyNoInteractions(serverFormsSynchronizer);
        verifyNoInteractions(syncStatusAppState);
        verifyNoInteractions(notifier);
    }
}
