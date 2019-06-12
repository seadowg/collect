package org.odk.collect.android.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(RobolectricTestRunner.class)
public class ExternalStorageFormFileStoreTest {

    @Test
    public void newForm_returnsAnXMLFile_withTheFormName_andInTheFormDirectory() {
        ExternalStorageFormFileStore store = new ExternalStorageFormFileStore();

        File file = store.newForm("bestForm");
        assertThat(file, notNullValue());
        assertThat(file.getAbsolutePath(), equalTo(
                getExternalStorageDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "bestForm.xml"));
    }
}