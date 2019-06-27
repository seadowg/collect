package org.odk.collect.android.utilities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static android.os.Environment.MEDIA_MOUNTED;
import static android.os.Environment.MEDIA_UNMOUNTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ExternalStorageFileStoreTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String[] dirs;
    private ExternalStorageFileStore store;
    private FakeExternalStorage externalStorage;

    @Before
    public void setup() throws Exception {
        externalStorage = new FakeExternalStorage();
        dirs = new String[]{
                odkPath(),
                odkPath() + File.separator + "forms",
                odkPath() + File.separator + "instances",
                odkPath() + File.separator + ".cache",
                odkPath() + File.separator + "metadata",
                odkPath() + File.separator + "layers"
        };

        store = new ExternalStorageFileStore(RuntimeEnvironment.application.getResources(), externalStorage);
    }

    @Test
    public void initialize_createsDirectories() {
        store.initialize();

        Arrays.stream(dirs).forEach(dirPath -> {
            File dir = new File(dirPath);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
        });
    }

    @Test
    public void initialize_whenSomeDirectoriesAlreadyExist_createsTheNonExistentDirectories() {
        assertTrue(new File(dirs[0]).mkdir());
        assertTrue(new File(dirs[1]).mkdir());

        store.initialize();

        Arrays.stream(dirs).forEach(dirPath -> {
            File dir = new File(dirPath);
            assertTrue(dir.exists());
            assertTrue(dir.isDirectory());
        });
    }

    @Test
    public void initialize_whenFilesExistWithSamePath_throwsRuntimeExceptionWithMessage() throws Exception {
        assertTrue(new File(odkPath()).createNewFile());

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(RuntimeEnvironment.application.getString(
                R.string.not_a_directory,
                externalStorage.getDirectory() + File.separator + "odk"));
        store.initialize();
    }

    @Test
    public void initialize_whenExternalStorageNotMounted_throwsRuntimeExceptionWithMessage() {
        externalStorage.setState(MEDIA_UNMOUNTED);

        expectedException.expect(RuntimeException.class);
        String expectedMessage = RuntimeEnvironment.application.getString(
                R.string.sdcard_unmounted,
                MEDIA_UNMOUNTED);
        expectedException.expectMessage(expectedMessage);

        store.initialize();
    }

    @Test
    public void initialize_whenCreatingDirectoriesFails_throwsRuntimeExceptionWithMessage() {
        assertTrue(externalStorage.getDirectory().delete());

        expectedException.expect(RuntimeException.class);
        String expectedMessage = RuntimeEnvironment.application.getString(
                R.string.cannot_create_directory,
                externalStorage.getDirectory() + File.separator + "odk");
        expectedException.expectMessage(expectedMessage);

        store.initialize();
    }

    @Test
    public void newForm_returnsAnXMLFile_withTheFormName_andInTheFormDirectory() {
        ExternalStorageFileStore.Instance instance = store.initialize();

        File file = instance.newForm("bestForm");
        assertThat(file, notNullValue());
        assertThat(file.getAbsolutePath(), equalTo(
                externalStorage.getDirectory() + File.separator +
                        "odk" + File.separator +
                        "forms" + File.separator +
                        "bestForm.xml"));
    }

    @Test
    public void newMedia_whenThereIsNoMediaDirectory_createsIt() {
        ExternalStorageFileStore.Instance instance = store.initialize();
        instance.newMedia("myform", "media.mp3");

        File mediaDirectory = new File(externalStorage.getDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media");
        assertThat(mediaDirectory.exists(), is(true));
    }

    @Test
    public void newMedia_whenTheMediaDirectoryExists_doesNothing() {
        assertTrue(new File(externalStorage.getDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media").mkdirs());

        ExternalStorageFileStore.Instance instance = store.initialize();
        instance.newMedia("myform", "media.mp3");

        File mediaDirectory = new File(externalStorage.getDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media");
        assertThat(mediaDirectory.exists(), is(true));
    }

    @Test
    public void newMedia_whenTheFileExistsWithSamePathAsMediaDirectory_throwsRuntimeException() throws IOException {
        final ExternalStorageFileStore.Instance instance = store.initialize();

        String mediaPath = externalStorage.getDirectory() + File.separator +
                "odk" + File.separator +
                "forms" + File.separator +
                "myform-media";
        assertTrue(new File(mediaPath).createNewFile());

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(RuntimeEnvironment.application.getString(
                R.string.not_a_directory,
                mediaPath));
        instance.newMedia("myform", "media.mp3");
    }

    @Test
    public void newMedia_returnsANewFile_inTheFormsMediaDirectory() {
        ExternalStorageFileStore.Instance instance = store.initialize();

        File file = instance.newMedia("myform", "media.mp3");
        assertThat(file, notNullValue());
        assertThat(file.getAbsolutePath(), equalTo(
                externalStorage.getDirectory() + File.separator +
                        "odk" + File.separator +
                        "forms" + File.separator +
                        "myform-media" + File.separator +
                        "media.mp3"
        ));
    }

    private String odkPath() {
        return externalStorage.getDirectory() + File.separator + "odk";
    }

    private class FakeExternalStorage implements ExternalStorage {

        private final Path directory;
        private String state;

        FakeExternalStorage() throws IOException {
            this.directory = Files.createTempDirectory("temp");
            this.state = MEDIA_MOUNTED;
        }

        @Override
        public String getState() {
            return state;
        }

        @Override
        public File getDirectory() {
            return new File(directory.toString());
        }

        public void setState(String state) {

            this.state = state;
        }
    }
}