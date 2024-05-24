package org.odk.collect.android.storage;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface ProjectStoragePathsFactory {
    ProjectStoragePaths create(String projectId);
}
