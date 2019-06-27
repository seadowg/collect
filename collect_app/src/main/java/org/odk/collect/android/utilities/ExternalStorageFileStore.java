package org.odk.collect.android.utilities;

import android.os.Environment;

import java.io.File;

public class ExternalStorageFileStore {

    private ExternalStorageFileStore() {
    }

    public static ExternalStorageFileStore initialize() {
        if (!isStorageMounted()) {
            throw new RuntimeException();
        }

        for (String dirPath : dirs()) {
            createDir(dirPath);
        }

        return new ExternalStorageFileStore();
    }

    public File newForm(String formName) {
        return new File(
                formsPath() + File.separator + formName + ".xml"
        );
    }

    public File newMedia(String formName, String mediaFileName) {
        String mediaPath = formsPath() + File.separator + formName + "-media";
        createDir(mediaPath);

        return new File(
                mediaPath + File.separator + mediaFileName
        );
    }

    private static void createDir(String dirPath) {
        File path = new File(dirPath);

        if (!path.exists() || !path.isDirectory()) {
            if (!path.mkdir()) {
                throw new RuntimeException();
            }
        }
    }

    private static boolean isStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private static String formsPath() {
        return odkPath() + File.separator + "forms";
    }

    private static String odkPath() {
        return Environment.getExternalStorageDirectory()
                + File.separator + "odk";
    }

    private static String[] dirs() {
        return new String[]{
                odkPath(),
                odkPath() + File.separator + "forms",
                odkPath() + File.separator + "instances",
                odkPath() + File.separator + ".cache",
                odkPath() + File.separator + "metadata",
                odkPath() + File.separator + "layers"
        };
    }
}
