package org.odk.collect.android.utilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.MEDIA_UNMOUNTED;
import static android.os.Environment.getExternalStorageDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
    public void initialize_createsDirectories() {
        ExternalStorageFileStore.initialize();

        Arrays.stream(dirs).forEach(dirPath -> {
            File dir = new File(dirPath);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
        });
    }

    @Test
    public void initialize_whenSomeDirectoriesAlreadyExist_createsTheNonExistantDirectories() {
        assertTrue(new File(dirs[0]).mkdir());
        assertTrue(new File(dirs[1]).mkdir());

        ExternalStorageFileStore.initialize();

        Arrays.stream(dirs).forEach(dirPath -> {
            File dir = new File(dirPath);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
        });
    }

    @Test(expected = RuntimeException.class)
    public void initialize_whenFilesExistWithSamePath_throwsRuntimeException() throws Exception {
        assertTrue(new File(odkPath()).createNewFile());

        ExternalStorageFileStore.initialize();
    }

    @Test(expected = RuntimeException.class)
    public void initialize_whenExternalStorageNotMounted_throwsRuntimeException() {
        ShadowEnvironment.setExternalStorageState(MEDIA_UNMOUNTED);

        ExternalStorageFileStore.initialize();
    }

    @Test
    public void newForm_returnsAnXMLFile_withTheFormName_andInTheFormDirectory() {
        ExternalStorageFileStore store = ExternalStorageFileStore.initialize();

        File file = store.newForm("bestForm");
        assertThat(file, notNullValue());
        assertThat(file.getAbsolutePath(), equalTo(
                getExternalStorageDirectory() + File.separator +
                        "odk" + File.separator +
                        "forms" + File.separator +
                        "bestForm.xml"));
    }

    @Test
    public void newMedia_whenThereIsNoMediaDirectory_createsIt() {
        ExternalStorageFileStore store = ExternalStorageFileStore.initialize();
        store.newMedia("myform", "media.mp3");

        File mediaDirectory = new File(getExternalStorageDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media");
        assertThat(mediaDirectory.exists(), is(true));
    }

    @Test
    public void newMedia_whenTheMediaDirectoryExists_doesNothing() {
        assertTrue(new File(getExternalStorageDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media").mkdirs());

        ExternalStorageFileStore store = ExternalStorageFileStore.initialize();
        store.newMedia("myform", "media.mp3");

        File mediaDirectory = new File(getExternalStorageDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media");
        assertThat(mediaDirectory.exists(), is(true));
    }

    @Test(expected = RuntimeException.class)
    public void newMedia_whenTheFileExistsWithSamePath_throwsRuntimeException() throws IOException {
        ExternalStorageFileStore store = ExternalStorageFileStore.initialize();

        assertTrue(new File(getExternalStorageDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media").createNewFile());

        store.newMedia("myform", "media.mp3");
    }

    @Test
    public void newMedia_returnsANewFile_inTheFormsMediaDirectory() {
        ExternalStorageFileStore store = ExternalStorageFileStore.initialize();

        File file = store.newMedia("myform", "media.mp3");
        assertThat(file, notNullValue());
        assertThat(file.getAbsolutePath(), equalTo(
                getExternalStorageDirectory() + File.separator +
                        "odk" + File.separator +
                        "forms" + File.separator +
                        "myform-media" + File.separator +
                        "media.mp3"
        ));
    }

    private String odkPath() {
        return getExternalStorageDirectory() + File.separator + "odk";
    }
}