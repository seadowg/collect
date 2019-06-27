package org.odk.collect.android.utilities;

import android.content.Context;
import android.os.Environment;

import org.odk.collect.android.R;

import java.io.File;

public class ExternalStorageFileStore {

    private final Context context;

    public ExternalStorageFileStore(Context context) {
        this.context = context;
    }

    public Instance initialize() {
        String storageState = Environment.getExternalStorageState();
        if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
            String message = context.getString(R.string.sdcard_unmounted, storageState);
            throw new RuntimeException(message);
        }

        for (String dirPath : dirs()) {
            createDir(context, dirPath);
        }

        return new Instance();
    }

    public  class Instance {
        private Instance() {
        }

        public File newForm(String formName) {
            return new File(
                    formsPath() + File.separator + formName + ".xml"
            );
        }

        public File newMedia(String formName, String mediaFileName) {
            String mediaPath = formsPath() + File.separator + formName + "-media";
            createDir(context, mediaPath);

            return new File(
                    mediaPath + File.separator + mediaFileName
            );
        }
    }

    private static void createDir(Context context, String dirPath) {
        File path = new File(dirPath);

        if (!path.exists() || !path.isDirectory()) {
            if (!path.mkdir()) {
                throw new RuntimeException(context.getString(R.string.not_a_directory, path));
            }
        }
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
