package org.odk.collect.android.injection.config;

import androidx.annotation.NonNull;

import org.odk.collect.android.entities.JsonFileEntitiesRepository;
import org.odk.collect.android.projects.PathBasedProjectDependencyProvider;
import org.odk.collect.android.projects.ProjectDependencyProvider;
import org.odk.collect.android.storage.ProjectStoragePaths;
import org.odk.collect.entities.EntitiesRepository;

import java.io.File;

import javax.inject.Inject;

import dagger.assisted.AssistedFactory;

public interface ProjectDependencyProviders {
    class EntitiesRepositoryProvider extends PathBasedProjectDependencyProvider<EntitiesRepository> {

        @Inject
        public EntitiesRepositoryProvider(ProjectStoragePathsFactory projectStoragePathsFactory) {
            super(projectStoragePathsFactory);
        }

        @Override
        public EntitiesRepository get(@NonNull ProjectStoragePaths paths) {
            return new JsonFileEntitiesRepository(new File(paths.getRootDir()));
        }
    }

    @AssistedFactory
    interface ProjectStoragePathsFactory extends ProjectDependencyProvider<ProjectStoragePaths> {
        ProjectStoragePaths get(String projectId);
    }
}
