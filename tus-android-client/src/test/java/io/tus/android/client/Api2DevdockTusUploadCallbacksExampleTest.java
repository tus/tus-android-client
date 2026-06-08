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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusUploadCallbacksExampleTest {
    @Test
    public void observesAndroidTusUploadCallbacks() throws Exception {
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
        final JSONObject result = uploadWithCallbacks(activity, scenario, createResponse);
        Api2DevdockScenario.writeResult(result);

        assertNotNull(result.getString("uploadUrl"));
    }

    private static JSONObject uploadWithCallbacks(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        final Api2DevdockScenario.UploadCallbacksPlan callbacks =
                Api2DevdockScenario.uploadCallbacks(scenario);
        final List<String> events = new ArrayList<String>();
        Api2DevdockScenario.requireFullFileChunkSize(uploadConfig);

        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-upload-callbacks.txt"
        );

        final SharedPreferences preferences = activity.getSharedPreferences(
                "api2-devdock-tus-upload-callbacks",
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

        final TusAndroidUpload upload = Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                scenario.getString("scenarioId") + "-android-upload-callbacks"
        );
        upload.setInputStream(new EventRecordingByteArrayInputStream(content, callbacks, events));

        final TusUploader uploader = client.resumeOrCreateUpload(upload);
        events.add(Api2DevdockScenario.uploadCallbackEventKey(
                callbacks,
                callbacks.eventKinds.uploadUrlAvailable
        ));
        uploader.setChunkSize(content.length);
        uploader.setProgressListener(new TusUploader.ProgressListener() {
            @Override
            public void onProgress(long bytesSent, long bytesTotal) {
                events.add(Api2DevdockScenario.uploadCallbackEventKey(
                        callbacks,
                        callbacks.eventKinds.progress,
                        Api2DevdockScenario.uploadCallbackEventKeyNumber(bytesSent),
                        Api2DevdockScenario.uploadCallbackEventKeyTotal(bytesTotal)
                ));
            }
        });
        uploader.setChunkCompleteListener(new TusUploader.ChunkCompleteListener() {
            @Override
            public void onChunkComplete(long chunkSize, long bytesAccepted, long bytesTotal) {
                events.add(Api2DevdockScenario.uploadCallbackEventKey(
                        callbacks,
                        callbacks.eventKinds.chunkComplete,
                        Api2DevdockScenario.uploadCallbackEventKeyNumber(chunkSize),
                        Api2DevdockScenario.uploadCallbackEventKeyNumber(bytesAccepted),
                        Api2DevdockScenario.uploadCallbackEventKeyTotal(bytesTotal)
                ));
            }
        });

        int uploadedChunkSize;
        do {
            uploadedChunkSize = uploader.uploadChunk();
        } while (uploadedChunkSize > -1);

        assertEquals(content.length, uploader.getOffset());
        assertNotNull(uploader.getUploadURL());

        uploader.finish(false);
        events.add(Api2DevdockScenario.uploadCallbackEventKey(
                callbacks,
                callbacks.eventKinds.success
        ));
        uploader.finish();

        final List<String> matchedEvents =
                Api2DevdockScenario.matchUploadCallbackEventKeys(callbacks, events);
        assertEquals(callbacks.eventKeys, matchedEvents);

        return new JSONObject()
                .put("eventKeys", new JSONArray(matchedEvents))
                .put("rawEventKeys", new JSONArray(events))
                .put("uploadUrl", uploader.getUploadURL().toString());
    }

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusUploadCallbacks.required"));
    }

    private static final class EventRecordingByteArrayInputStream extends ByteArrayInputStream {
        private final Api2DevdockScenario.UploadCallbacksPlan callbacks;
        private boolean closed;
        private final List<String> events;

        EventRecordingByteArrayInputStream(
                byte[] content,
                Api2DevdockScenario.UploadCallbacksPlan callbacks,
                List<String> events
        ) {
            super(content);
            this.callbacks = callbacks;
            this.events = events;
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                events.add(Api2DevdockScenario.uploadCallbackEventKey(
                        callbacks,
                        callbacks.eventKinds.sourceClose
                ));
                closed = true;
            }
            super.close();
        }
    }
}
