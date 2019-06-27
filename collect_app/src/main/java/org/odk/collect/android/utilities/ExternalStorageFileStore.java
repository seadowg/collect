package org.odk.collect.android.utilities;

import android.os.Environment;

import java.io.File;

public class ExternalStorageFileStore {

    public void initialize() {
        if (!isStorageMounted()) {
            throw new RuntimeException();
        }

        for (String dirPath : dirs()) {
            createDir(dirPath);
        }
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

    private void createDir(String dirPath) {
        File path = new File(dirPath);

        if (!path.exists() || !path.isDirectory()) {
            if (!path.mkdir()) {
                throw new RuntimeException();
            }
        }
    }

    private boolean isStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private String formsPath() {
        return odkPath() + File.separator + "forms";
    }

    private String odkPath() {
        return Environment.getExternalStorageDirectory()
                + File.separator + "odk";
    }

    private String[] dirs() {
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
