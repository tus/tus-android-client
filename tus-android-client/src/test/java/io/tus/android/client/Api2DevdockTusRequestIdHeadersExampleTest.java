package io.tus.android.client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;

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
import java.util.LinkedHashMap;
import java.util.Map;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusRequestLifecycleHooks;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusRequestIdHeadersExampleTest {
    @Test
    public void observesAndroidTusRequestIdHeaders() throws Exception {
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
        final JSONObject result = uploadWithRequestIdHeaders(activity, scenario, createResponse);
        Api2DevdockScenario.writeResult(result);

        assertNotNull(result.getString("uploadUrl"));
    }

    private static JSONObject uploadWithRequestIdHeaders(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        final String requestIdHeaderName =
                Api2DevdockScenario.uploadRequestIdHeaderName(uploadConfig);
        final Map<String, Map<String, String>> headersByMethod =
                new LinkedHashMap<String, Map<String, String>>();
        Api2DevdockScenario.requireFullFileChunkSize(uploadConfig);

        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-request-id-headers.txt"
        );

        final SharedPreferences preferences = activity.getSharedPreferences(
                "api2-devdock-tus-request-id-headers",
                0
        );
        assertTrue(preferences.edit().clear().commit());

        final TusClient client = new TusClient();
        client.setUploadCreationURL(Api2DevdockScenario.tusUrl(
                uploadConfig,
                scenario,
                createResponse
        ));
        client.enableResuming(new TusPreferencesURLStore(preferences));
        client.setHeaders(Api2DevdockScenario.uploadHeaders(uploadConfig));
        if (Api2DevdockScenario.uploadAddRequestId(uploadConfig)) {
            client.enableRequestIdHeader();
        }
        client.setRequestLifecycleHooks(new TusRequestLifecycleHooks(
                new TusRequestLifecycleHooks.BeforeRequest() {
                    @Override
                    public void beforeRequest(TusRequestLifecycleHooks.RequestContext context) {
                        if ("POST".equals(context.getMethod())
                                || "PATCH".equals(context.getMethod())) {
                            final Map<String, String> headers =
                                    new LinkedHashMap<String, String>();
                            headers.put(
                                    requestIdHeaderName,
                                    observedRequestIdHeader(
                                            context.getConnection(),
                                            context.getMethod(),
                                            requestIdHeaderName
                                    )
                            );
                            headersByMethod.put(context.getMethod(), headers);
                        }
                    }
                },
                null
        ));

        final TusAndroidUpload upload = Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                scenario.getString("scenarioId") + "-android-request-id-headers"
        );

        final TusUploader uploader = client.resumeOrCreateUpload(upload);
        uploader.setChunkSize(content.length);
        int uploadedChunkSize;
        do {
            uploadedChunkSize = uploader.uploadChunk();
        } while (uploadedChunkSize > -1);
        uploader.finish();

        assertEquals(content.length, uploader.getOffset());
        assertNotNull(uploader.getUploadURL());

        return new JSONObject()
                .put("headersByMethod", new JSONObject(headersByMethod))
                .put("uploadUrl", uploader.getUploadURL().toString());
    }

    private static String observedRequestIdHeader(
            HttpURLConnection connection,
            String method,
            String requestIdHeaderName
    ) {
        final String value = connection.getRequestProperty(requestIdHeaderName);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException(
                    "request ID headers did not observe "
                            + requestIdHeaderName
                            + " on "
                            + method
            );
        }

        return value;
    }

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusRequestIdHeaders.required"));
    }
}
