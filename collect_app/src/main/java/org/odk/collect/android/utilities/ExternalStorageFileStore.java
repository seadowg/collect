package org.odk.collect.android.utilities;

import android.content.res.Resources;
import android.os.Environment;

import org.odk.collect.android.R;

import java.io.File;

public class ExternalStorageFileStore {

    private final Resources resources;
    private final ExternalStorage externalStorage;

    public ExternalStorageFileStore(Resources context, ExternalStorage externalStorage) {
        this.resources = context;
        this.externalStorage = externalStorage;
    }

    public Instance initialize() {
        String storageState = externalStorage.getState();
        if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
            String message = resources.getString(R.string.sdcard_unmounted, storageState);
            throw new RuntimeException(message);
        }

        for (String dirPath : dirs(externalStorage)) {
            createDir(resources, dirPath);
        }

        return new Instance();
    }

    public class Instance {
        private Instance() {
        }

        public File newForm(String formName) {
            return new File(
                    formsPath(externalStorage) + File.separator + formName + ".xml"
            );
        }

        public File newMedia(String formName, String mediaFileName) {
            String mediaPath = formsPath(externalStorage) + File.separator + formName + "-media";
            createDir(resources, mediaPath);

            return new File(
                    mediaPath + File.separator + mediaFileName
            );
        }
    }

    private static void createDir(Resources resources, String dirPath) {
        File path = new File(dirPath);

        if (path.exists()) {
            if (!path.isDirectory()) {
                throw new RuntimeException(resources.getString(R.string.not_a_directory, path));
            }
        } else {
            boolean directoryCreated = path.mkdir();
            if (!directoryCreated) {
                throw new RuntimeException(resources.getString(R.string.cannot_create_directory, path));
            }
        }
    }

    private static String formsPath(ExternalStorage externalStorage) {
        return odkPath(externalStorage) + File.separator + "forms";
    }

    private static String odkPath(ExternalStorage externalStorage) {
        return externalStorage.getDirectory()
                + File.separator + "odk";
    }

    private static String[] dirs(ExternalStorage externalStorage) {
        return new String[]{
                odkPath(externalStorage),
                odkPath(externalStorage) + File.separator + "forms",
                odkPath(externalStorage) + File.separator + "instances",
                odkPath(externalStorage) + File.separator + ".cache",
                odkPath(externalStorage) + File.separator + "metadata",
                odkPath(externalStorage) + File.separator + "layers"
        };
    }
}
