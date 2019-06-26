package org.odk.collect.android.utilities;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;
import java.util.Arrays;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.MEDIA_UNMOUNTED;
import static android.os.Environment.getExternalStorageDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ExternalStorageFileStoreTest {

    private String[] dirs;

    @Before
    public void setup() {
        dirs = new String[]{
                odkPath(),
                odkPath() + File.separator + "forms",
                odkPath() + File.separator + "instances",
                odkPath() + File.separator + ".cache",
                odkPath() + File.separator + "metadata",
                odkPath() + File.separator + "layers"
        };

        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED);
    }

    @After
    public void teardown() {
        ShadowEnvironment.reset();
    }

    @Test
    public void createDirs_createsDirectories() {
        ExternalStorageFileStore store = new ExternalStorageFileStore();
        store.createDirs();

        Arrays.stream(dirs).forEach(dirPath -> {
            File dir = new File(dirPath);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
        });
    }

    @Test
    public void createDirs_whenDirectoriesAlreadyExist_doesNothing() {
        for (String dirPath : dirs) {
            new File(dirPath).mkdir();
        }

        ExternalStorageFileStore store = new ExternalStorageFileStore();
        store.createDirs();

        Arrays.stream(dirs).forEach(dirPath -> {
            File dir = new File(dirPath);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
        });
    }

    @Test(expected = RuntimeException.class)
    public void createDirs_whenFilesExistWithSamePath_throwsRuntimeException() throws Exception {
        new File(odkPath()).createNewFile();

        ExternalStorageFileStore store = new ExternalStorageFileStore();
        store.createDirs();
    }

    @Test(expected = RuntimeException.class)
    public void createDirs_whenExternalStorageNotMounted_throwsRuntimeException() {
        ShadowEnvironment.setExternalStorageState(MEDIA_UNMOUNTED);

        ExternalStorageFileStore store = new ExternalStorageFileStore();
        store.createDirs();
    }

    @NotNull
    private String odkPath() {
        return getExternalStorageDirectory() + File.separator + "odk";
    }

    @Test
    public void newForm_returnsAnXMLFile_withTheFormName_andInTheFormDirectory() {
        ExternalStorageFileStore store = new ExternalStorageFileStore();

        File file = store.newForm("bestForm");
        assertThat(file, notNullValue());
        assertThat(file.getAbsolutePath(), equalTo(
                getExternalStorageDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "bestForm.xml"));
    }
}