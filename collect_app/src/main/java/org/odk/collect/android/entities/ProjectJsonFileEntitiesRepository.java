package org.odk.collect.android.entities;

import org.odk.collect.android.storage.ProjectStoragePathsFactory;

import java.io.File;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

public class ProjectJsonFileEntitiesRepository extends JsonFileEntitiesRepository {

    @AssistedInject
    public ProjectJsonFileEntitiesRepository(@Assisted String projectId, ProjectStoragePathsFactory projectStoragePathsFactory) {
        super(new File(projectStoragePathsFactory.create(projectId).getRootDir()));
    }
}
