package org.odk.collect.android.injection.config;

import androidx.annotation.NonNull;

import org.odk.collect.android.entities.ProjectJsonFileEntitiesRepository;
import org.odk.collect.android.projects.ProjectDependencyProvider;
import org.odk.collect.android.storage.ProjectStoragePaths;
import org.odk.collect.entities.EntitiesRepository;

import dagger.assisted.AssistedFactory;

public interface ProjectDependencyProviders {
    @AssistedFactory
    interface EntitiesRepositoryProvider extends ProjectDependencyProvider<EntitiesRepository> {
        ProjectJsonFileEntitiesRepository get(@NonNull String projectId);
    }

    @AssistedFactory
    interface ProjectStoragePathsFactory extends ProjectDependencyProvider<ProjectStoragePaths> {
        ProjectStoragePaths get(String projectId);
    }
}
