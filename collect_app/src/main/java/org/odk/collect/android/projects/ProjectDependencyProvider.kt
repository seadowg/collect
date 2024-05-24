package org.odk.collect.android.projects

interface ProjectDependencyProvider<T> {
    fun get(projectId: String): T
}
