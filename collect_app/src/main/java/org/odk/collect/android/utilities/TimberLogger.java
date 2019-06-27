package org.odk.collect.android.utilities;

import timber.log.Timber;

public class TimberLogger implements Logger {

    @Override
    public void warning(String message) {
        Timber.w(message);
    }
}
