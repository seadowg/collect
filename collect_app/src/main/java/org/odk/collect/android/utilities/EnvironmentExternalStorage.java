package org.odk.collect.android.utilities;

import android.os.Environment;

import java.io.File;

public class EnvironmentExternalStorage implements ExternalStorage {

    @Override
    public String getState() {
        return Environment.getExternalStorageState();
    }

    @Override
    public File getDirectory() {
        return Environment.getExternalStorageDirectory();
    }
}
