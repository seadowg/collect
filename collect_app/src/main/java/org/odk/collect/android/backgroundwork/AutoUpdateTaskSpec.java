/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.previouslydownloaded.ServerFormsUpdateChecker;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.notifications.Notifier;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.utilities.MultiFormDownloader;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_DOWNLOAD;

public class AutoUpdateTaskSpec implements TaskSpec {

    @Inject
    ServerFormsUpdateChecker serverFormsUpdateChecker;

    @Inject
    StorageMigrationRepository storageMigrationRepository;

    @Inject
    MultiFormDownloader multiFormDownloader;

    @Inject
    Notifier notifier;

    @Inject
    PreferencesProvider preferencesProvider;

    @Inject
    @Named("FORMS")
    ChangeLock changeLock;

    @NotNull
    @Override
    public Supplier<Boolean> getTask(@NotNull Context context) {
        DaggerUtils.getComponent(context).inject(this);

        return () -> {
            List<ServerFormDetails> newUpdates = serverFormsUpdateChecker.check();

            if (!newUpdates.isEmpty()) {
                if (preferencesProvider.getGeneralSharedPreferences().getBoolean(KEY_AUTOMATIC_DOWNLOAD, false)) {
                    changeLock.withLock(acquiredLock -> {
                        if (acquiredLock) {
                            final HashMap<ServerFormDetails, String> result = multiFormDownloader.downloadForms(newUpdates, null);
                            notifier.onUpdatesDownloaded(result);
                        }

                        return null;
                    });
                } else {
                    notifier.onUpdatesAvailable();
                }
            }

            return true;
        };
    }

    @NotNull
    @Override
    public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
        return Adapter.class;
    }

    public static class Adapter extends WorkerAdapter {

        public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
            super(new AutoUpdateTaskSpec(), context, workerParams);
        }
    }

}