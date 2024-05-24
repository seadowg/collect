package org.odk.collect.android.injection.config

import dagger.assisted.AssistedFactory
import org.odk.collect.android.entities.JsonFileEntitiesRepository
import org.odk.collect.android.projects.ProjectDependencyProvider
import org.odk.collect.android.storage.ProjectStoragePaths
import org.odk.collect.entities.EntitiesRepository
import java.io.File
import javax.inject.Inject

object ProjectDependencyProviders {
    class EntitiesRepositoryProvider @Inject constructor(
        private val projectStoragePathsFactory: ProjectStoragePathsFactory
    ) : ProjectDependencyProvider<EntitiesRepository> {
        override fun get(projectId: String): EntitiesRepository {
            val paths = projectStoragePathsFactory.get(projectId)
            return JsonFileEntitiesRepository(File(paths.rootDir))
        }
    }

    @AssistedFactory
    interface ProjectStoragePathsFactory : ProjectDependencyProvider<ProjectStoragePaths> {
        override fun get(projectId: String): ProjectStoragePaths
    }
}
