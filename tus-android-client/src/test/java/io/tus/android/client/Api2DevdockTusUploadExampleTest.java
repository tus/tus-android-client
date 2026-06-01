package io.tus.android.client;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.shadows.ShadowContentResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUploader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.OFF)
public class Api2DevdockTusUploadExampleTest {
    private static final String PROVIDER_AUTHORITY = "io.tus.android.client.api2devdock";

    @Test
    public void uploadsAndroidContentUriToTransloaditAssembly() throws Exception {
        System.setProperty("http.strictPostRedirect", "true");

        final String scenarioPath = scenarioPath();
        if (!isRequired()) {
            Assume.assumeTrue(
                    "API2 devdock scenario is only required through the dedicated API2 QA task",
                    scenarioPath != null
            );
        }
        if (scenarioPath == null) {
            throw new IllegalStateException("API2_SDK_EXAMPLE_SCENARIO must be set");
        }

        final JSONObject scenario = loadScenario(scenarioPath);
        final JSONObject createResponse =
                scenario.getJSONObject("prepared").getJSONObject("createResponse");
        final Activity activity = Robolectric.setupActivity(Activity.class);
        final String uploadUrl = uploadWithTus(activity, scenario, createResponse);
        writeResult(uploadUrl);

        assertNotNull(uploadUrl);
    }

    private static String uploadWithTus(
            Activity activity,
            JSONObject scenario,
            JSONObject createResponse
    ) throws IOException, JSONException, ProtocolException {
        final JSONObject uploadConfig = scenario.getJSONObject("upload");
        final byte[] content = scenarioBytes(uploadConfig);
        final Uri uri = registerContentUri(activity, content);

        final SharedPreferences preferences = activity.getSharedPreferences(
                "api2-devdock-tus-upload",
                0
        );
        preferences.edit().clear().commit();

        final TusClient client = new TusClient();
        client.setUploadCreationURL(new URL(scalarString(
                resolveValue(uploadConfig.getJSONObject("tusUrl"), scenario, createResponse)
        )));
        client.enableResuming(new TusPreferencesURLStore(preferences));
        client.enableRemoveFingerprintOnSuccess();

        final TusAndroidUpload upload = new TusAndroidUpload(uri, activity);
        upload.setFingerprint(scenario.getString("scenarioId") + "-android-devdock-example");
        upload.setMetadata(uploadMetadata(uploadConfig, scenario, createResponse));

        final TusUploader uploader = client.resumeOrCreateUpload(upload);
        uploader.setChunkSize(content.length);
        int uploadedChunkSize;
        do {
            uploadedChunkSize = uploader.uploadChunk();
        } while (uploadedChunkSize > -1);
        uploader.finish();

        assertEquals(content.length, uploader.getOffset());
        assertNotNull(uploader.getUploadURL());

        return uploader.getUploadURL().toString();
    }

    private static Uri registerContentUri(Activity activity, byte[] content) throws IOException {
        final File source = new File(activity.getCacheDir(), "api2-devdock-upload.txt");
        Files.write(source.toPath(), content);

        final Uri uri = Uri.parse("content://" + PROVIDER_AUTHORITY + "/api2-devdock-upload.txt");
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

    private static JSONObject loadScenario(String scenarioPath) throws IOException, JSONException {
        final byte[] contents = Files.readAllBytes(Paths.get(scenarioPath));
        return new JSONObject(new String(contents, StandardCharsets.UTF_8));
    }

    private static String scenarioPath() {
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

    private static boolean isRequired() {
        return "true".equals(System.getProperty("api2DevdockTusUpload.required"));
    }

    private static void writeResult(String uploadUrl) throws IOException, JSONException {
        final String resultPath = System.getenv("API2_SDK_EXAMPLE_RESULT");
        if (resultPath == null || resultPath.isEmpty()) {
            return;
        }

        final JSONObject result = new JSONObject();
        result.put("uploadUrl", uploadUrl);
        Files.write(
                Paths.get(resultPath),
                (result.toString(2) + "\n").getBytes(StandardCharsets.UTF_8)
        );
    }

    private static byte[] scenarioBytes(JSONObject uploadConfig) throws JSONException {
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

    private static String scalarString(Object value) {
        if (JSONObject.NULL.equals(value)) {
            return "null";
        }

        return String.valueOf(value);
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
