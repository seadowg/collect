package org.odk.collect.android.storage;

import android.content.Context;

import java.io.File;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public class ProjectStoragePaths {

    private final String projectId;
    private final String externalFilesDir;

    @AssistedInject
    public ProjectStoragePaths(Context context, @Assisted String projectId) {
        this.externalFilesDir = context.getExternalFilesDir(null).getAbsolutePath();
        this.projectId = projectId;
    }

    public String getRootDir() {
        return externalFilesDir + File.separator + "projects" + File.separator + projectId;
    }
}
