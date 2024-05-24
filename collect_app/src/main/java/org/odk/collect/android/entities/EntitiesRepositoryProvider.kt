package org.odk.collect.android.entities

import org.odk.collect.android.storage.ProjectStoragePathsFactory
import org.odk.collect.entities.EntitiesRepository
import java.io.File

class EntitiesRepositoryProvider(private val projectStoragePathsFactory: ProjectStoragePathsFactory) {

    fun get(projectId: String): EntitiesRepository {
        val projectDir = File(projectStoragePathsFactory.create(projectId).rootDir)
        return JsonFileEntitiesRepository(projectDir)
    }
}
