package org.odk.collect.android.utilities;

import android.content.res.Resources;
import android.os.Environment;

import org.odk.collect.android.R;

import java.io.File;

public class ExternalStorageFileStore {

    private final Resources resources;
    private final ExternalStorage externalStorage;
    private final Logger logger;

    public ExternalStorageFileStore(Resources context, ExternalStorage externalStorage, Logger logger) {
        this.resources = context;
        this.externalStorage = externalStorage;
        this.logger = logger;
    }

    public Instance initialize() {
        String storageState = externalStorage.getState();
        if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
            String message = resources.getString(R.string.sdcard_unmounted, storageState);
            throw new RuntimeException(message);
        }

        for (String dirPath : dirs(externalStorage)) {
            createDir(dirPath, resources, logger);
        }

        return new Instance();
    }

    public class Instance {
        private Instance() {
        }

        public File newForm(String formName) {
            File file = new File(
                    formsPath(externalStorage) + File.separator + formName + ".xml"
            );

            int duplicateNumber = 2;
            while (file.exists()) {
                file = new File(
                        formsPath(externalStorage) + File.separator + formName + "_" + duplicateNumber + ".xml"
                );
                duplicateNumber++;
            }

            return file;
        }

        public File newMedia(String formName, String mediaFileName) {
            String mediaPath = formsPath(externalStorage) + File.separator + formName + "-media";
            createDir(mediaPath, resources, logger);

            return new File(
                    mediaPath + File.separator + mediaFileName
            );
        }
    }

    private static void createDir(String dirPath, Resources resources, Logger logger) {
        File path = new File(dirPath);

        if (path.exists()) {
            if (!path.isDirectory()) {
                String error = resources.getString(R.string.not_a_directory, path);
                logger.warning(error);
                throw new RuntimeException(error);
            }
        } else {
            boolean directoryCreated = path.mkdir();
            if (!directoryCreated) {
                String error = resources.getString(R.string.cannot_create_directory, path);
                logger.warning(error);
                throw new RuntimeException(error);
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
