package io.tus.android.client;

import android.app.Activity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class GeneratedTusProtocolContractTest {

    @Test
    public void shouldCoverResumeStoragePrimitive() throws Exception {
        GeneratedTusProtocolContract.GeneratedTusClientFeature feature =
                findFeature("singleUploadLifecycle");

        assertContains(feature.operationIds, "createTusUpload");
        assertContains(feature.operationIds, "getTusUploadOffset");
        assertContains(feature.operationIds, "patchTusUpload");
        assertContains(feature.primitives, "store-resume-url");

        Activity activity = Robolectric.setupActivity(Activity.class);
        TusPreferencesURLStore store = new TusPreferencesURLStore(
                activity.getSharedPreferences("generated-tus-contract-test", 0));
        URL url = new URL("https://tusd.tusdemo.net/files/generated");
        store.set("fingerprint", url);

        assertEquals(url, store.get("fingerprint"));
    }

    @Test
    public void shouldCoverManagedUploadProofCases() {
        for (GeneratedTusProtocolContract.GeneratedTusManagedUploadProofCase proofCase
                : GeneratedTusProtocolContract.MANAGED_UPLOAD_PROOF_CASES) {
            assertEquals("managedUpload", proofCase.featureId);
            assertEquals("feature-over-protocol", proofCase.layer);
            assertContains(
                    GeneratedTusProtocolContract.MANAGED_UPLOAD_SCENARIO_IDS,
                    proofCase.scenarioId);
            for (String primitive : proofCase.requiredPrimitives) {
                assertContains(GeneratedTusProtocolContract.MANAGED_UPLOAD_PRIMITIVES, primitive);
            }
            for (String featureId : proofCase.protocolFeatureIds) {
                findFeature(featureId);
            }
        }
    }

    private static GeneratedTusProtocolContract.GeneratedTusClientFeature findFeature(
            String featureId) {
        for (GeneratedTusProtocolContract.GeneratedTusClientFeature feature
                : GeneratedTusProtocolContract.CLIENT_FEATURES) {
            if (feature.featureId.equals(featureId)) {
                return feature;
            }
        }

        throw new AssertionError("Missing generated TUS client feature: " + featureId);
    }

    private static void assertContains(String[] values, String expected) {
        for (String value : values) {
            if (value.equals(expected)) {
                return;
            }
        }

        throw new AssertionError("Missing generated value: " + expected);
    }
}
