package io.tus.android.client;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.robolectric.shadows.ShadowContentResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Api2DevdockScenario {
    private static final String PROVIDER_AUTHORITY = "io.tus.android.client.api2devdock";

    private Api2DevdockScenario() {
    }

    static final class UploadCallbackEventKinds {
        final String chunkComplete;
        final String progress;
        final String sourceClose;
        final String success;
        final String uploadUrlAvailable;

        UploadCallbackEventKinds(JSONObject eventKinds) throws JSONException {
            chunkComplete = eventKinds.getString("chunkComplete");
            progress = eventKinds.getString("progress");
            sourceClose = eventKinds.getString("sourceClose");
            success = eventKinds.getString("success");
            uploadUrlAvailable = eventKinds.getString("uploadUrlAvailable");
        }
    }

    static final class UploadCallbacksPlan {
        final List<String> allowedExtraEventKeyPrefixes;
        final List<List<String>> eventKeyAlternativeGroups;
        final UploadCallbackEventKinds eventKinds;
        final String eventKeyPartSeparator;
        final List<String> eventKeys;
        final String eventPolicyMatching;

        UploadCallbacksPlan(JSONObject uploadCallbacks) throws JSONException {
            allowedExtraEventKeyPrefixes = stringList(
                    uploadCallbacks.getJSONArray("allowedExtraEventKeyPrefixes")
            );
            eventKeyAlternativeGroups = stringListList(
                    uploadCallbacks.getJSONArray("eventKeyAlternativeGroups")
            );
            eventKinds = new UploadCallbackEventKinds(uploadCallbacks.getJSONObject("eventKinds"));
            eventKeyPartSeparator = uploadCallbacks.getString("eventKeyPartSeparator");
            eventKeys = stringList(uploadCallbacks.getJSONArray("eventKeys"));
            eventPolicyMatching = uploadCallbacks.getString("eventPolicyMatching");
        }
    }

    static final class TerminationPlan {
        final int expectedVerificationStatus;
        final String method;
        final int minimumDeleteRequestCount;
        final int stopAfterAcceptedBytes;
        final String verificationMethod;

        TerminationPlan(JSONObject termination) throws JSONException {
            expectedVerificationStatus = termination.getInt("expectedVerificationStatus");
            method = termination.getString("method");
            minimumDeleteRequestCount = termination.getInt("minimumDeleteRequestCount");
            stopAfterAcceptedBytes = termination.getInt("stopAfterAcceptedBytes");
            verificationMethod = termination.getString("verificationMethod");
        }
    }

    static final class RequestLifecycleHooksPlan {
        final List<String> expectedAfterResponseMethods;
        final List<Integer> expectedAfterResponseStatusCodes;
        final List<String> expectedBeforeRequestMethods;
        final List<String> ignoredRequestMethods;

        RequestLifecycleHooksPlan(JSONObject requestLifecycleHooks) throws JSONException {
            expectedAfterResponseMethods = stringList(
                    requestLifecycleHooks.getJSONArray("expectedAfterResponseMethods")
            );
            expectedAfterResponseStatusCodes = integerList(
                    requestLifecycleHooks.getJSONArray("expectedAfterResponseStatusCodes")
            );
            expectedBeforeRequestMethods = stringList(
                    requestLifecycleHooks.getJSONArray("expectedBeforeRequestMethods")
            );
            ignoredRequestMethods = stringList(
                    requestLifecycleHooks.getJSONArray("ignoredRequestMethods")
            );
        }
    }

    static final class RetryOffsetRecoveryFailAfterResponsePlan {
        final String message;
        final String method;
        final int occurrence;

        RetryOffsetRecoveryFailAfterResponsePlan(JSONObject failAfterResponse)
                throws JSONException {
            message = failAfterResponse.getString("message");
            method = failAfterResponse.getString("method");
            occurrence = failAfterResponse.getInt("occurrence");
        }
    }

    static final class RetryOffsetRecoveryRecoveryResponsePlan {
        final String method;
        final String offsetHeader;

        RetryOffsetRecoveryRecoveryResponsePlan(JSONObject recoveryResponse)
                throws JSONException {
            method = recoveryResponse.getString("method");
            offsetHeader = recoveryResponse.getString("offsetHeader");
        }
    }

    static final class RetryOffsetRecoveryPlan {
        final int expectedFailureCount;
        final int expectedRecoveredOffset;
        final int expectedRecoveryRequestCount;
        final List<String> expectedRequestMethods;
        final RetryOffsetRecoveryFailAfterResponsePlan failAfterResponse;
        final RetryOffsetRecoveryRecoveryResponsePlan recoveryResponse;

        RetryOffsetRecoveryPlan(JSONObject retryOffsetRecovery) throws JSONException {
            expectedFailureCount = retryOffsetRecovery.getInt("expectedFailureCount");
            expectedRecoveredOffset = retryOffsetRecovery.getInt("expectedRecoveredOffset");
            expectedRecoveryRequestCount =
                    retryOffsetRecovery.getInt("expectedRecoveryRequestCount");
            expectedRequestMethods =
                    stringList(retryOffsetRecovery.getJSONArray("expectedRequestMethods"));
            failAfterResponse = new RetryOffsetRecoveryFailAfterResponsePlan(
                    retryOffsetRecovery.getJSONObject("failAfterResponse")
            );
            recoveryResponse = new RetryOffsetRecoveryRecoveryResponsePlan(
                    retryOffsetRecovery.getJSONObject("recoveryResponse")
            );
        }
    }

    static TusAndroidUpload androidUpload(
            Activity activity,
            Uri uri,
            JSONObject scenario,
            JSONObject createResponse,
            String fingerprint
    ) throws IOException, JSONException {
        final TusAndroidUpload upload = new TusAndroidUpload(uri, activity);
        upload.setFingerprint(fingerprint);
        upload.setMetadata(uploadMetadata(
                scenario.getJSONObject("upload"),
                scenario,
                createResponse
        ));

        return upload;
    }

    static int fixedChunkSizeBytes(JSONObject uploadConfig) throws JSONException {
        final JSONObject chunkSize = uploadConfig.getJSONObject("chunkSize");
        final String kind = chunkSize.getString("kind");
        if (!"fixed-bytes".equals(kind)) {
            throw new IllegalArgumentException("unsupported chunk size kind " + kind);
        }

        return chunkSize.getInt("bytes");
    }

    static List<String> matchUploadCallbackEventKeys(
            UploadCallbacksPlan plan,
            List<String> actual
    ) {
        if (!"exact".equals(plan.eventPolicyMatching)
                && !"exact-except-allowed-extra-events".equals(plan.eventPolicyMatching)) {
            throw new IllegalArgumentException(
                    "unsupported upload callback event policy " + plan.eventPolicyMatching
            );
        }

        final List<String> matched = new ArrayList<String>();
        int expectedIndex = 0;
        for (String event : actual) {
            if (expectedIndex < plan.eventKeys.size()
                    && uploadCallbackEventMatchesExpected(plan, expectedIndex, event)) {
                matched.add(plan.eventKeys.get(expectedIndex));
                expectedIndex += 1;
                continue;
            }

            if ("exact-except-allowed-extra-events".equals(plan.eventPolicyMatching)
                    && hasAllowedUploadCallbackExtraEventPrefix(plan, event)) {
                continue;
            }

            throw new IllegalStateException(
                    "unexpected upload callback event "
                            + event
                            + " at expected index "
                            + expectedIndex
                            + "; expected "
                            + plan.eventKeys
                            + ", actual "
                            + actual
            );
        }

        if (expectedIndex != plan.eventKeys.size()) {
            throw new IllegalStateException(
                    "missing upload callback events after index "
                            + expectedIndex
                            + "; expected "
                            + plan.eventKeys
                            + ", actual "
                            + actual
            );
        }

        return matched;
    }

    static void requireFullFileChunkSize(JSONObject uploadConfig) throws JSONException {
        final Object chunkSize = uploadConfig.get("chunkSize");
        if (!"full-file".equals(chunkSize)) {
            throw new IllegalArgumentException("unsupported chunk size policy " + chunkSize);
        }
    }

    static JSONObject loadScenario(String scenarioPath) throws IOException, JSONException {
        final byte[] contents = Files.readAllBytes(Paths.get(scenarioPath));
        return new JSONObject(new String(contents, StandardCharsets.UTF_8));
    }

    static Uri registerContentUri(
            Activity activity,
            byte[] content,
            String sourceName
    ) throws IOException {
        final File source = new File(activity.getCacheDir(), sourceName);
        Files.write(source.toPath(), content);

        final Uri uri = Uri.parse("content://" + PROVIDER_AUTHORITY + "/" + sourceName);
        final Api2DevdockContentProvider provider = new Api2DevdockContentProvider(source);
        final ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = PROVIDER_AUTHORITY;
        provider.attachInfo(activity, providerInfo);
        ShadowContentResolver.registerProviderInternal(
                PROVIDER_AUTHORITY,
                provider
        );

        return uri;
    }

    static byte[] scenarioBytes(JSONObject uploadConfig) throws JSONException {
        final JSONObject source = uploadConfig.getJSONObject("source");
        final String kind = source.getString("kind");
        if (!"bytes".equals(kind)) {
            throw new IllegalArgumentException("unsupported source kind " + kind);
        }

        final String encoding = source.getString("encoding");
        if (!"utf8".equals(encoding)) {
            throw new IllegalArgumentException("unsupported source encoding " + encoding);
        }

        return source.getString("value").getBytes(StandardCharsets.UTF_8);
    }

    static String scenarioPath() {
        final String scenarioPath = System.getenv("API2_SDK_EXAMPLE_SCENARIO");
        if (scenarioPath != null && !scenarioPath.isEmpty()) {
            return scenarioPath;
        }

        final String defaultPath = "tus-android-client/api2-scenario.json";
        if (Files.exists(Paths.get(defaultPath))) {
            return defaultPath;
        }

        return null;
    }

    static URL tusUrl(
            JSONObject uploadConfig,
            JSONObject scenario,
            JSONObject createResponse
    ) throws JSONException, java.net.MalformedURLException {
        return new URL(scalarString(
                resolveValue(uploadConfig.getJSONObject("tusUrl"), scenario, createResponse)
        ));
    }

    static UploadCallbacksPlan uploadCallbacks(JSONObject scenario) throws JSONException {
        return new UploadCallbacksPlan(
                scenario.getJSONObject("upload").getJSONObject("uploadCallbacks")
        );
    }

    static TerminationPlan termination(JSONObject uploadConfig) throws JSONException {
        return new TerminationPlan(uploadConfig.getJSONObject("termination"));
    }

    static boolean uploadAddRequestId(JSONObject uploadConfig) throws JSONException {
        return uploadConfig.getBoolean("addRequestId");
    }

    static Map<String, Map<String, String>> uploadBodyHeadersByMethod(
            JSONObject uploadConfig
    ) throws JSONException {
        final JSONObject bodyHeadersByMethod = uploadConfig.getJSONObject("bodyHeadersByMethod");
        final Map<String, Map<String, String>> result =
                new LinkedHashMap<String, Map<String, String>>();
        final JSONArray methods = bodyHeadersByMethod.names();
        if (methods == null) {
            return result;
        }

        for (int index = 0; index < methods.length(); index++) {
            final String method = methods.getString(index);
            result.put(method, stringMap(bodyHeadersByMethod.getJSONObject(method)));
        }

        return result;
    }

    static Map<String, String> uploadHeaders(JSONObject uploadConfig) throws JSONException {
        return stringMap(uploadConfig.getJSONObject("headers"));
    }

    static String uploadRequestIdHeaderName(JSONObject uploadConfig) throws JSONException {
        return uploadConfig.getString("requestIdHeaderName");
    }

    static RequestLifecycleHooksPlan requestLifecycleHooks(JSONObject uploadConfig)
            throws JSONException {
        return new RequestLifecycleHooksPlan(uploadConfig.getJSONObject("requestLifecycleHooks"));
    }

    static RetryOffsetRecoveryPlan retryOffsetRecovery(JSONObject uploadConfig)
            throws JSONException {
        return new RetryOffsetRecoveryPlan(uploadConfig.getJSONObject("retryOffsetRecovery"));
    }

    static String uploadCallbackEventKey(UploadCallbacksPlan plan, String... parts) {
        final StringBuilder key = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            if (index > 0) {
                key.append(plan.eventKeyPartSeparator);
            }
            key.append(parts[index]);
        }

        return key.toString();
    }

    static String uploadCallbackEventKeyNumber(long value) {
        return Long.toString(value);
    }

    static String uploadCallbackEventKeyTotal(long value) {
        return scalarString(value);
    }

    static void writeResult(JSONObject result) throws IOException, JSONException {
        final String resultPath = System.getenv("API2_SDK_EXAMPLE_RESULT");
        if (resultPath == null || resultPath.isEmpty()) {
            return;
        }

        Files.write(
                Paths.get(resultPath),
                (result.toString(2) + "\n").getBytes(StandardCharsets.UTF_8)
        );
    }

    private static Object readPath(Object value, JSONArray pathParts) throws JSONException {
        Object current = value;
        for (int index = 0; index < pathParts.length(); index++) {
            final Object part = pathParts.get(index);
            if (current instanceof JSONObject && part instanceof String) {
                current = ((JSONObject) current).get((String) part);
                continue;
            }

            if (current instanceof JSONArray && part instanceof Number) {
                current = ((JSONArray) current).get(((Number) part).intValue());
                continue;
            }

            throw new IllegalArgumentException("cannot read scenario path part " + part);
        }

        return current;
    }

    private static Object resolveValue(
            JSONObject valueSpec,
            JSONObject scenario,
            JSONObject createResponse
    ) throws JSONException {
        if (valueSpec.has("value")) {
            return valueSpec.get("value");
        }

        final JSONObject source = valueSpec.getJSONObject("source");
        final String root = source.getString("root");
        final Object rootValue;
        if ("scenario".equals(root)) {
            rootValue = scenario;
        } else if ("createResponse".equals(root)) {
            rootValue = createResponse;
        } else {
            throw new IllegalArgumentException("unsupported scenario value root " + root);
        }

        return readPath(rootValue, source.getJSONArray("path"));
    }

    private static String scalarString(Object value) {
        if (JSONObject.NULL.equals(value)) {
            return "null";
        }

        return String.valueOf(value);
    }

    private static Map<String, String> uploadMetadata(
            JSONObject uploadConfig,
            JSONObject scenario,
            JSONObject createResponse
    ) throws JSONException {
        final JSONArray fields = uploadConfig.getJSONArray("metadata");
        final Map<String, String> metadata = new LinkedHashMap<String, String>();
        for (int index = 0; index < fields.length(); index++) {
            final JSONObject field = fields.getJSONObject(index);
            metadata.put(
                    field.getString("name"),
                    scalarString(resolveValue(
                            field.getJSONObject("value"),
                            scenario,
                            createResponse
                    ))
            );
        }

        return metadata;
    }

    private static boolean hasAllowedUploadCallbackExtraEventPrefix(
            UploadCallbacksPlan plan,
            String event
    ) {
        for (String prefix : plan.allowedExtraEventKeyPrefixes) {
            if (event.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> stringList(JSONArray values) throws JSONException {
        final List<String> result = new ArrayList<String>();
        for (int index = 0; index < values.length(); index++) {
            result.add(values.getString(index));
        }

        return result;
    }

    private static List<Integer> integerList(JSONArray values) throws JSONException {
        final List<Integer> result = new ArrayList<Integer>();
        for (int index = 0; index < values.length(); index++) {
            result.add(values.getInt(index));
        }

        return result;
    }

    private static List<List<String>> stringListList(JSONArray values) throws JSONException {
        final List<List<String>> result = new ArrayList<List<String>>();
        for (int index = 0; index < values.length(); index++) {
            result.add(stringList(values.getJSONArray(index)));
        }

        return result;
    }

    private static Map<String, String> stringMap(JSONObject values) throws JSONException {
        final Map<String, String> result = new LinkedHashMap<String, String>();
        final JSONArray names = values.names();
        if (names == null) {
            return result;
        }

        for (int index = 0; index < names.length(); index++) {
            final String name = names.getString(index);
            result.put(name, values.getString(name));
        }

        return result;
    }

    private static boolean uploadCallbackEventMatchesExpected(
            UploadCallbacksPlan plan,
            int expectedIndex,
            String event
    ) {
        if (plan.eventKeys.get(expectedIndex).equals(event)) {
            return true;
        }

        if (expectedIndex >= plan.eventKeyAlternativeGroups.size()) {
            return false;
        }

        final List<String> alternatives = plan.eventKeyAlternativeGroups.get(expectedIndex);
        for (String alternative : alternatives) {
            if (alternative.equals(event)) {
                return true;
            }
        }

        return false;
    }

    private static final class Api2DevdockContentProvider extends ContentProvider {
        private final File source;

        Api2DevdockContentProvider(File source) {
            this.source = source;
        }

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Cursor query(
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String sortOrder
        ) {
            final MatrixCursor cursor = new MatrixCursor(
                    new String[]{OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME}
            );
            cursor.addRow(new Object[]{source.length(), source.getName()});

            return cursor;
        }

        @Override
        public String getType(Uri uri) {
            return "text/plain";
        }

        @Override
        public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
            return ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY);
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            throw new UnsupportedOperationException();
        }
    }
}
