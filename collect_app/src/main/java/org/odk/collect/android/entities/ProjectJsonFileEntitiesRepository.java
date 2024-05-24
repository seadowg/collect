package org.odk.collect.android.entities;

import org.odk.collect.android.injection.config.ProjectDependencyProviders;

import java.io.File;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public class ProjectJsonFileEntitiesRepository extends JsonFileEntitiesRepository {

    @AssistedInject
    public ProjectJsonFileEntitiesRepository(@Assisted String projectId, ProjectDependencyProviders.ProjectStoragePathsFactory projectStoragePathsFactory) {
        super(new File(projectStoragePathsFactory.get(projectId).getRootDir()));
    }
}
