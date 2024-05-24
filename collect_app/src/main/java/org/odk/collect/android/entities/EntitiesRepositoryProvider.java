package org.odk.collect.android.entities;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface EntitiesRepositoryProvider {
    ProjectJsonFileEntitiesRepository get(String projectId);
}
