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
public class Api2DevdockTusUploadBodyHeadersExampleTest {
    @Test
    public void observesAndroidTusUploadBodyHeaders() throws Exception {
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
        final JSONObject result = uploadWithBodyHeaders(activity, scenario, createResponse);
        Api2DevdockScenario.writeResult(result);

        assertNotNull(result.getString("uploadUrl"));
    }

    private static JSONObject uploadWithBodyHeaders(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        final Map<String, Map<String, String>> expectedHeadersByMethod =
                Api2DevdockScenario.uploadBodyHeadersByMethod(uploadConfig);
        final Map<String, Map<String, String>> bodyHeadersByMethod =
                new LinkedHashMap<String, Map<String, String>>();
        Api2DevdockScenario.requireFullFileChunkSize(uploadConfig);

        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-upload-body-headers.txt"
        );

        final SharedPreferences preferences = activity.getSharedPreferences(
                "api2-devdock-tus-upload-body-headers",
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
        client.setRequestLifecycleHooks(new TusRequestLifecycleHooks(
                new TusRequestLifecycleHooks.BeforeRequest() {
                    @Override
                    public void beforeRequest(TusRequestLifecycleHooks.RequestContext context) {
                        final Map<String, String> expectedHeaders =
                                expectedHeadersByMethod.get(context.getMethod());
                        if (expectedHeaders == null) {
                            return;
                        }

                        bodyHeadersByMethod.put(
                                context.getMethod(),
                                observedBodyHeaders(
                                        context.getConnection(),
                                        context.getMethod(),
                                        expectedHeaders
                                )
                        );
                    }
                },
                null
        ));

        final TusAndroidUpload upload = Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                scenario.getString("scenarioId") + "-android-upload-body-headers"
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
        for (String method : expectedHeadersByMethod.keySet()) {
            if (!bodyHeadersByMethod.containsKey(method)) {
                throw new IllegalStateException(
                        "upload body headers did not observe " + method + " request"
                );
            }
        }

        return new JSONObject()
                .put("bodyHeadersByMethod", new JSONObject(bodyHeadersByMethod))
                .put("uploadUrl", uploader.getUploadURL().toString());
    }

    private static Map<String, String> observedBodyHeaders(
            HttpURLConnection connection,
            String method,
            Map<String, String> expectedHeaders
    ) {
        final Map<String, String> headers = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> entry : expectedHeaders.entrySet()) {
            final String value = connection.getRequestProperty(entry.getKey());
            if (value == null) {
                throw new IllegalStateException(
                        "upload body headers did not observe " + entry.getKey() + " on " + method
                );
            }

            headers.put(entry.getKey(), value);
        }

        return headers;
    }

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusUploadBodyHeaders.required"));
    }
}
