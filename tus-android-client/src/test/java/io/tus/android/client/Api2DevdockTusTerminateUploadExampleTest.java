package io.tus.android.client;

import android.app.Activity;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.ConscryptMode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusRequestLifecycleHooks;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusTerminateUploadExampleTest {
    @Test
    public void terminatesAndroidContentUriUpload() throws Exception {
        System.setProperty("http.strictPostRedirect", "true");

        final String scenarioPath = Api2DevdockScenario.scenarioPath();
        if (!isRequired()) {
            Assume.assumeTrue(
                    "API2 devdock scenario is only required through the dedicated API2 QA task",
                    scenarioPath != null
            );
        }
        if (scenarioPath == null) {
            throw new IllegalStateException("API2_SDK_EXAMPLE_SCENARIO must be set");
        }

        final JSONObject scenario = Api2DevdockScenario.loadScenario(scenarioPath);
        final JSONObject createResponse =
                scenario.getJSONObject("prepared").getJSONObject("createResponse");
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final JSONObject result = uploadAndTerminate(activity, scenario, createResponse);
        Api2DevdockScenario.writeResult(result);

        assertEquals(true, result.getBoolean("terminated"));
        assertNotNull(result.getString("uploadUrl"));
    }

    private static JSONObject uploadAndTerminate(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final Api2DevdockScenario.TerminationPlan termination =
                Api2DevdockScenario.termination(uploadConfig);
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        final int chunkSize = Api2DevdockScenario.fixedChunkSizeBytes(uploadConfig);
        final List<String> requestMethods = new ArrayList<String>();

        if (termination.stopAfterAcceptedBytes > content.length) {
            throw new IllegalStateException(
                    "terminate upload stop-after bytes "
                            + termination.stopAfterAcceptedBytes
                            + " exceeds content length "
                            + content.length
            );
        }

        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-terminate-upload.txt"
        );

        final TusClient client = new TusClient();
        client.setUploadCreationURL(Api2DevdockScenario.tusUrl(
                uploadConfig,
                scenario,
                createResponse
        ));
        client.setRequestLifecycleHooks(new TusRequestLifecycleHooks(
                new TusRequestLifecycleHooks.BeforeRequest() {
                    @Override
                    public void beforeRequest(TusRequestLifecycleHooks.RequestContext context) {
                        requestMethods.add(context.getMethod());
                    }
                },
                null
        ));

        final TusUploader uploader = client.createUpload(Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                scenario.getString("scenarioId") + "-android-terminate-upload"
        ));
        uploader.setChunkSize(chunkSize);
        uploader.setRequestPayloadSize(termination.stopAfterAcceptedBytes);
        final int uploadedChunkSize = uploader.uploadChunk();
        uploader.finish();

        assertEquals(termination.stopAfterAcceptedBytes, uploadedChunkSize);
        assertEquals(termination.stopAfterAcceptedBytes, uploader.getOffset());
        assertNotNull(uploader.getUploadURL());

        final URL uploadUrl = uploader.getUploadURL();
        client.terminateUpload(uploadUrl).disconnect();
        final int verificationStatus = verifyTerminatedUpload(
                client,
                termination.verificationMethod,
                uploadUrl
        );
        assertEquals(termination.expectedVerificationStatus, verificationStatus);

        return new JSONObject()
                .put("acceptedBytes", (int) uploader.getOffset())
                .put("deleteRequestCount", countMethod(requestMethods, termination.method))
                .put("requestMethods", new JSONArray(requestMethods))
                .put("terminated", true)
                .put("uploadUrl", uploadUrl.toString())
                .put("verificationStatus", verificationStatus);
    }

    private static int verifyTerminatedUpload(
            TusClient client,
            String method,
            URL uploadUrl
    ) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) uploadUrl.openConnection();
        try {
            connection.setRequestMethod(method);
            client.prepareConnection(connection);
            connection.connect();
            return connection.getResponseCode();
        } finally {
            connection.disconnect();
        }
    }

    private static int countMethod(List<String> methods, String expectedMethod) {
        int count = 0;
        for (String method : methods) {
            if (method.equals(expectedMethod)) {
                count += 1;
            }
        }

        return count;
    }

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusTerminateUpload.required"));
    }
}
