package org.odk.collect.android.storage

import android.content.Context
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

class ProjectStoragePaths @AssistedInject constructor(
    context: Context,
    @Assisted private val projectId: String
) {
    private val externalFilesDir: String = context.getExternalFilesDir(null)!!.absolutePath

    val rootDir: String
        get() = externalFilesDir + File.separator + "projects" + File.separator + projectId
}
