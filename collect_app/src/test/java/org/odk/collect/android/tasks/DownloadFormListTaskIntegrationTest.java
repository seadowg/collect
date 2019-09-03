package org.odk.collect.android.tasks;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.GeneralKeys;
import org.robolectric.RobolectricTestRunner;

import java.util.Map;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class DownloadFormListTaskIntegrationTest {

    private MockWebServer server;

    @Before
    public void setup() throws Exception {
        server = mockWebServer();
    }

    @After
    public void teardown() throws Exception {
        if (server != null) {
            server.shutdown();
        }
    }

    @Test
    public void shouldProcessAndReturnAFormList() throws Exception {
        // given
        willRespondWith(RESPONSE);

        // when
        Collect application = Collect.getInstance();
        DownloadFormListTask task = new DownloadFormListTask(application.getComponent().downloadFormListUtils());
        final Map<String, FormDetails> fetched = task.doInBackground();

        // then
        assertEquals(2, fetched.size());

        // and
        FormDetails f1 = fetched.get("one");
        assertNull(f1.getErrorStr());
        assertEquals("The First Form", f1.getFormName());
        assertEquals("https://example.com/formXml?formId=one", f1.getDownloadUrl());
        assertNull(f1.getManifestUrl());
        assertEquals("one", f1.getFormID());
        assertNull(f1.getFormVersion());
        assertFalse(f1.isNewerFormVersionAvailable());
        assertFalse(f1.areNewerMediaFilesAvailable());

        // and
        FormDetails f2 = fetched.get("two");
        assertNull(f2.getErrorStr());
        assertEquals("The Second Form", f2.getFormName());
        assertEquals("https://example.com/formXml?formId=two", f2.getDownloadUrl());
        assertNull(f2.getManifestUrl());
        assertEquals("two", f2.getFormID());
        assertNull(f2.getFormVersion());
        assertFalse(f1.isNewerFormVersionAvailable());
        assertFalse(f1.areNewerMediaFilesAvailable());
    }

    protected void willRespondWith(String... rawResponses) {
        for (String rawResponse : rawResponses) {
            MockResponse response = new MockResponse();

            String[] parts = rawResponse.split("\r\n\r\n", 2);

            String[] headerLines = parts[0].split("\r\n");

            response.setStatus(headerLines[0]);

            for (int i = 1; i < headerLines.length; ++i) {
                String[] headerParts = headerLines[i].split(": ", 2);
                response.addHeader(headerParts[0], headerParts[1]);
            }

            response.setBody(parts[1]);

            server.enqueue(response);
        }
    }

    private static String join(String... strings) {
        StringBuilder bob = new StringBuilder();
        for (String s : strings) {
            bob.append(s).append('\n');
        }
        return bob.toString();
    }

    private static MockWebServer mockWebServer() throws Exception {
        MockWebServer server = new MockWebServer();
        server.start();
        configAppFor(server);
        return server;
    }

    private static void configAppFor(MockWebServer server) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(Collect.getInstance().getBaseContext()).edit();
        prefs.putString(GeneralKeys.KEY_SERVER_URL, server.url("/").toString());
        if (!prefs.commit()) {
            throw new RuntimeException("Failed to set up SharedPreferences for MockWebServer");
        }
    }

    private static final String RESPONSE = join(
            "HTTP/1.1 200 OK\r",
            "X-OpenRosa-Version: 1.0\r",
            "X-OpenRosa-Accept-Content-Length: 10485760\r",
            "Content-Type: text/xml; charset=utf-8\r",
            "X-Cloud-Trace-Context: cb84da0bfcb4da37910faf33b10ca190;o=1\r",
            "Date: Tue, 18 Apr 2017 15:45:03 GMT\r",
            "Server: Google Frontend\r",
            "Content-Length: 2235\r",
            "Alt-Svc: quic=\":443\"; ma=2592000; v=\"37,36,35\"\r",
            "Connection: close\r",
            "\r",
            "<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">",
            "<xform><formID>one</formID>",
            "<name>The First Form</name>",
            "<majorMinorVersion></majorMinorVersion>",
            "<version></version>",
            "<hash>md5:b71c92bec48730119eab982044a8adff</hash>",
            "<downloadUrl>https://example.com/formXml?formId=one</downloadUrl>",
            "</xform>",
            "<xform><formID>two</formID>",
            "<name>The Second Form</name>",
            "<majorMinorVersion></majorMinorVersion>",
            "<version></version>",
            "<hash>md5:4428adffbbec48771c9230119eab9820</hash>",
            "<downloadUrl>https://example.com/formXml?formId=two</downloadUrl>",
            "</xform>",
            "</xforms>");
}