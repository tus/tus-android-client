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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusExecutor;
import io.tus.java.client.TusRequestLifecycleHooks;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusRetryOffsetRecoveryExampleTest {
    @Test
    public void recoversAndroidTusOffsetAfterRetry() throws Exception {
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
        final JSONObject result = uploadWithRetryOffsetRecovery(
                activity,
                scenario,
                createResponse
        );
        Api2DevdockScenario.writeResult(result);

        assertEquals(
                scenario.getJSONObject("upload")
                        .getJSONObject("retryOffsetRecovery")
                        .getInt("expectedFailureCount"),
                result.getInt("simulatedFailureCount")
        );
        assertNotNull(result.getString("uploadUrl"));
    }

    private static JSONObject uploadWithRetryOffsetRecovery(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        final int chunkSize = Api2DevdockScenario.fixedChunkSizeBytes(uploadConfig);
        final Api2DevdockScenario.RetryOffsetRecoveryPlan plan =
                Api2DevdockScenario.retryOffsetRecovery(uploadConfig);
        final List<Integer> recoveredOffsets = new ArrayList<Integer>();
        final List<String> requestMethods = new ArrayList<String>();
        final int[] failureCandidateCount = new int[]{0};
        final int[] simulatedFailureCount = new int[]{0};

        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-retry-offset-recovery.txt"
        );

        final SharedPreferences preferences = activity.getSharedPreferences(
                "api2-devdock-tus-retry-offset-recovery",
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
                        requestMethods.add(context.getMethod());
                    }
                },
                new TusRequestLifecycleHooks.AfterResponse() {
                    @Override
                    public void afterResponse(
                            TusRequestLifecycleHooks.RequestContext context
                    ) throws IOException {
                        if (plan.recoveryResponse.method.equals(context.getMethod())) {
                            recoveredOffsets.add(readHeaderInt(
                                    context.getConnection(),
                                    plan.recoveryResponse.offsetHeader
                            ));
                        }

                        if (!plan.failAfterResponse.method.equals(context.getMethod())) {
                            return;
                        }

                        failureCandidateCount[0] += 1;
                        if (failureCandidateCount[0] != plan.failAfterResponse.occurrence) {
                            return;
                        }

                        simulatedFailureCount[0] += 1;
                        throw new IOException(plan.failAfterResponse.message);
                    }
                }
        ));

        final TusAndroidUpload upload = Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                scenario.getString("scenarioId") + "-android-retry-offset-recovery"
        );
        final String[] uploadUrl = new String[]{null};
        final long[] finalOffset = new long[]{0};
        final TusExecutor executor = new TusExecutor() {
            @Override
            protected void makeAttempt() throws ProtocolException, IOException {
                final TusUploader uploader = client.resumeOrCreateUpload(upload);
                uploader.setChunkSize(chunkSize);
                uploader.setRequestPayloadSize(chunkSize);
                int uploadedChunkSize;
                do {
                    uploadedChunkSize = uploader.uploadChunk();
                } while (uploadedChunkSize > -1);
                uploader.finish();
                uploadUrl[0] = uploader.getUploadURL().toString();
                finalOffset[0] = uploader.getOffset();
            }
        };
        executor.setDelays(new int[uploadConfig.getInt("retries")]);
        if (!executor.makeAttempts()) {
            throw new IOException("retry offset recovery was interrupted");
        }

        if (uploadUrl[0] == null) {
            throw new IllegalStateException(
                    "retry offset recovery TUS upload did not expose a URL"
            );
        }
        assertEquals(content.length, finalOffset[0]);
        assertEquals(plan.expectedFailureCount, simulatedFailureCount[0]);
        assertEquals(plan.expectedRecoveryRequestCount, recoveredOffsets.size());
        assertEquals(plan.expectedRecoveredOffset, recoveredOffsets.get(0).intValue());
        assertStringList(requestMethods, plan.expectedRequestMethods);

        return new JSONObject()
                .put("recoveredOffsets", new JSONArray(recoveredOffsets))
                .put("recoveryRequestCount", recoveredOffsets.size())
                .put("requestMethods", new JSONArray(requestMethods))
                .put("simulatedFailureCount", simulatedFailureCount[0])
                .put("uploadUrl", uploadUrl[0]);
    }

    private static int readHeaderInt(HttpURLConnection connection, String headerName) {
        final String value = connection.getHeaderField(headerName);
        final int offset;
        try {
            offset = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "retry offset recovery expected numeric "
                            + headerName
                            + " response header, got "
                            + value
            );
        }
        if (offset < 0) {
            throw new IllegalStateException(
                    "retry offset recovery expected non-negative offset, got " + offset
            );
        }

        return offset;
    }

    private static void assertStringList(List<String> actual, List<String> expected) {
        if (actual.size() != expected.size()) {
            throw new IllegalStateException(
                    "retry offset recovery expected request methods "
                            + expected
                            + ", got "
                            + actual
            );
        }

        for (int index = 0; index < expected.size(); index++) {
            if (!actual.get(index).equals(expected.get(index))) {
                throw new IllegalStateException(
                        "retry offset recovery expected request method "
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
        return "true".equals(System.getProperty("api2DevdockTusRetryOffsetRecovery.required"));
    }
}
