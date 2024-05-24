package org.odk.collect.android.entities

import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.entities.EntitiesRepository
import java.io.File

class EntitiesRepositoryProvider(private val storagePathProvider: StoragePathProvider) {

    fun get(projectId: String): EntitiesRepository {
        val projectDir = File(storagePathProvider.getProjectRootDirPath(projectId))
        return JsonFileEntitiesRepository(projectDir)
    }
}
