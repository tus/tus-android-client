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
import java.net.URL;

import io.tus.java.client.FingerprintNotFoundException;
import io.tus.java.client.ProtocolException;
import io.tus.java.client.ResumingNotEnabledException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusResumeUploadExampleTest {
    @Test
    public void resumesAndroidContentUriUploadFromStoredUrl() throws Exception {
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
        final JSONObject result = uploadWithStoredResume(activity, scenario, createResponse);
        Api2DevdockScenario.writeResult(result);

        assertEquals(result.getString("firstUploadUrl"), result.getString("uploadUrl"));
    }

    private static JSONObject uploadWithStoredResume(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws FingerprintNotFoundException, IOException, JSONException, ProtocolException,
            ResumingNotEnabledException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final JSONObject resumeConfig = uploadConfig.getJSONObject("resume");
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-resume-upload.txt"
        );

        final SharedPreferences preferences = activity.getSharedPreferences(
                "api2-devdock-tus-resume-upload",
                0
        );
        assertTrue(preferences.edit().clear().commit());

        final TusPreferencesURLStore store = new TusPreferencesURLStore(preferences);
        final TusClient client = new TusClient();
        client.setUploadCreationURL(Api2DevdockScenario.tusUrl(
                uploadConfig,
                scenario,
                createResponse
        ));
        client.enableResuming(store);
        if (resumeConfig.getBoolean("removeFingerprintOnSuccess")) {
            client.enableRemoveFingerprintOnSuccess();
        }

        final String fingerprint = resumeConfig.getString("fingerprint");
        final TusUploader firstUploader = client.createUpload(Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                fingerprint
        ));
        firstUploader.setChunkSize(Api2DevdockScenario.fixedChunkSizeBytes(uploadConfig));
        final int firstAcceptedBytes = firstUploader.uploadChunk();
        assertEquals(resumeConfig.getInt("stopAfterAcceptedBytes"), firstAcceptedBytes);
        assertEquals(resumeConfig.getInt("stopAfterAcceptedBytes"), firstUploader.getOffset());
        assertNotNull(firstUploader.getUploadURL());
        final String firstUploadUrl = firstUploader.getUploadURL().toString();
        firstUploader.finish(false);

        final URL storedUploadUrl = store.get(fingerprint);
        assertNotNull(storedUploadUrl);
        assertEquals(firstUploadUrl, storedUploadUrl.toString());
        final int previousUploadCount = preferences.getAll().size();
        assertEquals(resumeConfig.getInt("expectedPreviousUploadCount"), previousUploadCount);

        final TusUploader resumedUploader = client.resumeUpload(Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                fingerprint
        ));
        resumedUploader.setChunkSize(content.length);
        int uploadedChunkSize;
        do {
            uploadedChunkSize = resumedUploader.uploadChunk();
        } while (uploadedChunkSize > -1);
        resumedUploader.finish();

        assertEquals(content.length, resumedUploader.getOffset());
        assertNotNull(resumedUploader.getUploadURL());
        final String uploadUrl = resumedUploader.getUploadURL().toString();
        final int remainingPreviousUploadCount = preferences.getAll().size();
        assertEquals(
                resumeConfig.getInt("expectedRemainingPreviousUploadCount"),
                remainingPreviousUploadCount
        );

        final JSONObject result = new JSONObject();
        result.put("firstUploadUrl", firstUploadUrl);
        result.put("previousUploadCount", previousUploadCount);
        result.put("remainingPreviousUploadCount", remainingPreviousUploadCount);
        result.put("uploadUrl", uploadUrl);

        return result;
    }

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusResumeUpload.required"));
    }
}
