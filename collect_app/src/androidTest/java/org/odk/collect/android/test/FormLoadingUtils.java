/*
 * Copyright 2019 Nafundi
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

package org.odk.collect.android.test;

import android.content.Intent;
import android.content.res.AssetManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.apache.commons.io.IOUtils;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.ExternalStorageFileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

public class FormLoadingUtils {

    public static final String ALL_WIDGETS_FORM = "all-widgets.xml";

    private FormLoadingUtils() {

    }

    /**
     * Copies a form with the given file name and given associated media from the given assets
     * folder to the SD Card where it will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename, String formAssetPath, List<String> mediaFilenames) throws IOException {
        Collect application = ApplicationProvider.getApplicationContext();
        AppDependencyComponent component = application.getComponent();
        ExternalStorageFileStore.Instance externalStorageFileStore = component.externalStorageFileStore().initialize();

        if (!formAssetPath.isEmpty() && !formAssetPath.endsWith(File.separator)) {
            formAssetPath = formAssetPath + File.separator;
        }

        copyForm(externalStorageFileStore, formAssetPath, formFilename);

        if (mediaFilenames != null) {
            copyFormMediaFiles(externalStorageFileStore, formFilename, formAssetPath, mediaFilenames);
        }
    }

    /**
     * Copies a form with the given file name from the from the given assets folder to the SD Card
     * where it will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename, String formAssetPath) throws IOException {
        copyFormToSdCard(formFilename, formAssetPath, null);
    }

    /**
     * Copies a form with the given file name from the assets root to the SD Card where it
     * will be loaded by {@link FormLoaderTask}.
     */
    public static void copyFormToSdCard(String formFilename) throws IOException {
        copyFormToSdCard(formFilename, "", null);
    }

    public static IntentsTestRule<FormEntryActivity> getFormActivityTestRuleFor(String formFilename) {
        return new IntentsTestRule<FormEntryActivity>(FormEntryActivity.class) {
            @Override
            protected Intent getActivityIntent() {
                Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FormEntryActivity.class);
                intent.putExtra(EXTRA_TESTING_PATH, Collect.FORMS_PATH + formFilename);

                return intent;
            }

            @Override
            protected void afterActivityLaunched() {
                this.getActivity().setShouldOverrideAnimations(true);
                super.afterActivityLaunched();
            }
        };
    }

    private static void copyForm(ExternalStorageFileStore.Instance externalStorageFileStore, String formAssetPath, String formFilename) throws IOException {
        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
        InputStream inputStream = assetManager.open(formAssetPath + formFilename);

        File outFile = externalStorageFileStore.newForm(formFilename.replace(".xml", ""));
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    private static void copyFormMediaFiles(ExternalStorageFileStore.Instance externalStorageFileStore, String formFilename, String formAssetPath, List<String> mediaFilenames) throws IOException {
        AssetManager assetManager = InstrumentationRegistry.getInstrumentation().getContext().getAssets();

        for (String mediaFilename : mediaFilenames) {
            InputStream mediaInputStream = assetManager.open(formAssetPath + mediaFilename);
            File mediaOutFile = externalStorageFileStore.newMedia(formFilename.replace(".xml", ""), mediaFilename);
            OutputStream mediaOutputStream = new FileOutputStream(mediaOutFile);

            IOUtils.copy(mediaInputStream, mediaOutputStream);
        }
    }
}