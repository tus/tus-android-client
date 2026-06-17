package io.tus.android.client;

import android.app.Activity;
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

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusCreationWithUploadExampleTest {
    @Test
    public void createsAndroidContentUriUploadWithCreationData() throws Exception {
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
        final JSONObject result = uploadWithCreationData(activity, scenario, createResponse);
        Api2DevdockScenario.writeResult(result);

        assertEquals(
                Api2DevdockScenario.scenarioBytes(scenario.getJSONObject("upload")).length,
                result.getInt("acceptedBytes")
        );
        assertNotNull(result.getString("uploadUrl"));
    }

    private static JSONObject uploadWithCreationData(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final byte[] content = Api2DevdockScenario.scenarioBytes(uploadConfig);
        Api2DevdockScenario.requireFullFileChunkSize(uploadConfig);
        if (!uploadConfig.getBoolean("uploadDataDuringCreation")) {
            throw new IllegalStateException(
                    "creation-with-upload scenario must set uploadDataDuringCreation"
            );
        }

        final Uri uri = Api2DevdockScenario.registerContentUri(
                activity,
                content,
                "api2-devdock-creation-with-upload.txt"
        );

        final TusClient client = new TusClient();
        client.setUploadCreationURL(Api2DevdockScenario.tusUrl(
                uploadConfig,
                scenario,
                createResponse
        ));

        final TusAndroidUpload upload = Api2DevdockScenario.androidUpload(
                activity,
                uri,
                scenario,
                createResponse,
                scenario.getString("scenarioId") + "-android-creation-with-upload"
        );

        final TusUploader uploader = client.createUploadWithData(upload, content.length);
        uploader.finish();

        assertEquals(content.length, uploader.getOffset());
        assertNotNull(uploader.getUploadURL());

        return new JSONObject()
                .put("acceptedBytes", (int) uploader.getOffset())
                .put("uploadUrl", uploader.getUploadURL().toString());
    }

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusCreationWithUpload.required"));
    }
}
