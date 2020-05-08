package org.odk.collect.android.activities.viewmodels;

import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.analytics.AnalyticsEvents;
import org.odk.collect.android.utilities.FileUtils;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;


public class FormDownloadListViewModelTest {
    private FormDownloadListViewModel viewModel;

    @Before public void setUp() {
        viewModel = new FormDownloadListViewModel();
    }

    @Test public void getDownloadAnalyticsEvent_returnsFirstDownload_whenNoFormsAreOnDevice() {
        assertThat(viewModel.getDownloadAnalyticsEvent(0), is(AnalyticsEvents.FIRST_FORM_DOWNLOAD));
    }

    @Test public void getDownloadAnalyticsEvent_returnsSubsequentDownload_whenFormsAreOnDevice() {
        assertThat(viewModel.getDownloadAnalyticsEvent(1), is(AnalyticsEvents.SUBSEQUENT_FORM_DOWNLOAD));
    }

    @Test public void getDownloadAnalyticsDescription_hasExpectedFormat() {
        String result = viewModel.getDownloadAnalyticsDescription("some string");
        assertThat(result.matches("[0-9]*/[0-9]*-.*"), is(true));
    }

    @Test public void getDownloadAnalyticsDescription_includesServerUrlHash() {
        String server = "a server url";
        String serverUrlHash = FileUtils.getMd5Hash(new ByteArrayInputStream(server.getBytes()));

        assertThat(viewModel.getDownloadAnalyticsDescription(server), containsString(serverUrlHash));
    }

    @Test public void getDownloadAnalyticsDescription_includesSelectedFormAndTotalFormCounts() {
        viewModel.addForm(new HashMap<>());
        viewModel.addForm(new HashMap<>());
        viewModel.addForm(new HashMap<>());
        viewModel.addForm(new HashMap<>());
        viewModel.addForm(new HashMap<>());

        viewModel.addSelectedFormId("foo");
        viewModel.addSelectedFormId("bar");

        assertThat(viewModel.getDownloadAnalyticsDescription("some string"), containsString("2/5"));
    }
}
