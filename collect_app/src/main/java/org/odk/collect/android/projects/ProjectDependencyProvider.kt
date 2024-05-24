package org.odk.collect.android.projects

import org.odk.collect.android.injection.config.ProjectDependencyProviders.ProjectStoragePathsFactory
import org.odk.collect.android.storage.ProjectStoragePaths

interface ProjectDependencyProvider<T> {
    fun get(projectId: String): T
}

abstract class PathBasedProjectDependencyProvider<T>(
    private val projectStoragePathsFactory: ProjectStoragePathsFactory
) : ProjectDependencyProvider<T> {

    abstract fun get(paths: ProjectStoragePaths): T

    override fun get(projectId: String): T {
        return get(projectStoragePathsFactory.get(projectId))
    }
}
