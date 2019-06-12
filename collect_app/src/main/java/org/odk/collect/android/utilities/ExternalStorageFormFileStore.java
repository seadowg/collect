package org.odk.collect.android.utilities;

import org.odk.collect.android.application.Collect;

import java.io.File;

public class ExternalStorageFormFileStore {

    public File newForm(String formName) {
        return new File(formsPath() + File.separator + formName + ".xml");
    }

    public File newMedia(String formName) {
        throw new UnsupportedOperationException();
    }

    private String formsPath() {
        return Collect.FORMS_PATH;
    }
}
