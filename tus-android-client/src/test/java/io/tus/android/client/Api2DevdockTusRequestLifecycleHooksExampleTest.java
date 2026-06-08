package io.tus.android.client;

import android.app.Activity;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.List;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusRequestLifecycleHooks;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusRequestLifecycleHooksExampleTest {
    @Test
    public void observesAndroidTusRequestLifecycleHooks() throws Exception {
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
        final JSONObject result = uploadWithRequestLifecycleHooks(
                activity,
                scenario,
                createResponse
        );
        Api2DevdockScenario.writeResult(result);

        assertNotNull(result.getString("uploadUrl"));
    }

    private static JSONObject uploadWithRequestLifecycleHooks(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        final Api2DevdockScenario.RequestLifecycleHooksPlan plan =
                Api2DevdockScenario.requestLifecycleHooks(uploadConfig);
        final List<String> beforeRequestMethods = new ArrayList<String>();
        final List<String> afterResponseMethods = new ArrayList<String>();
        final List<Integer> afterResponseStatusCodes = new ArrayList<Integer>();
        Api2DevdockScenario.requireFullFileChunkSize(uploadConfig);

        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-request-lifecycle-hooks.txt"
        );

        final SharedPreferences preferences = activity.getSharedPreferences(
                "api2-devdock-tus-request-lifecycle-hooks",
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
                        if (!plan.ignoredRequestMethods.contains(context.getMethod())) {
                            beforeRequestMethods.add(context.getMethod());
                        }
                    }
                },
                new TusRequestLifecycleHooks.AfterResponse() {
                    @Override
                    public void afterResponse(
                            TusRequestLifecycleHooks.RequestContext context
                    ) throws IOException {
                        if (!plan.ignoredRequestMethods.contains(context.getMethod())) {
                            afterResponseMethods.add(context.getMethod());
                            afterResponseStatusCodes.add(
                                    context.getConnection().getResponseCode()
                            );
                        }
                    }
                }
        ));

        final TusAndroidUpload upload = Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                scenario.getString("scenarioId") + "-android-request-lifecycle-hooks"
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
        assertStringList(
                beforeRequestMethods,
                plan.expectedBeforeRequestMethods,
                "before request methods"
        );
        assertStringList(
                afterResponseMethods,
                plan.expectedAfterResponseMethods,
                "after response methods"
        );
        assertIntegerList(
                afterResponseStatusCodes,
                plan.expectedAfterResponseStatusCodes,
                "after response status codes"
        );

        return new JSONObject()
                .put("afterResponseMethods", new JSONArray(afterResponseMethods))
                .put("afterResponseStatusCodes", new JSONArray(afterResponseStatusCodes))
                .put("beforeRequestMethods", new JSONArray(beforeRequestMethods))
                .put("uploadUrl", uploader.getUploadURL().toString());
    }

    private static void assertStringList(
            List<String> actual,
            List<String> expected,
            String label
    ) {
        if (actual.size() != expected.size()) {
            throw new IllegalStateException(
                    "request lifecycle hooks expected "
                            + label
                            + " "
                            + expected
                            + ", got "
                            + actual
            );
        }

        for (int index = 0; index < expected.size(); index++) {
            if (!actual.get(index).equals(expected.get(index))) {
                throw new IllegalStateException(
                        "request lifecycle hooks expected "
                                + label
                                + " "
                                + expected.get(index)
                                + " at index "
                                + index
                                + ", got "
                                + actual.get(index)
                );
            }
        }
    }

    private static void assertIntegerList(
            List<Integer> actual,
            List<Integer> expected,
            String label
    ) {
        if (actual.size() != expected.size()) {
            throw new IllegalStateException(
                    "request lifecycle hooks expected "
                            + label
                            + " "
                            + expected
                            + ", got "
                            + actual
            );
        }

        for (int index = 0; index < expected.size(); index++) {
            if (actual.get(index).intValue() != expected.get(index).intValue()) {
                throw new IllegalStateException(
                        "request lifecycle hooks expected "
                                + label
                                + " "
                                + expected.get(index)
                                + " at index "
                                + index
                                + ", got "
                                + actual.get(index)
                );
            }
        }
    }

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusRequestLifecycleHooks.required"));
    }
}
