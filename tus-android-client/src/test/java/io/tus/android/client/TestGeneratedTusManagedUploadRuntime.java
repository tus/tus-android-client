/*
 * Code generated from Transloadit API2 TUS protocol contracts; DO NOT EDIT.
 * If it looks wrong, please report the issue instead of editing this file by hand;
 * the source fix belongs in the protocol contract generator so all TUS clients stay in sync.
 */

package io.tus.android.client;

import android.app.Activity;
import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusExecutor;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests generated Android managed-upload scenarios against Android storage and Java client pieces.
 */
@RunWith(RobolectricTestRunner.class)
public class TestGeneratedTusManagedUploadRuntime {
    private static final GeneratedTusManagedUploadRuntimeCase[] CASES =
            new GeneratedTusManagedUploadRuntimeCase[] {
        new GeneratedTusManagedUploadRuntimeCase(
                new GeneratedTusManagedUploadRuntimeProfile(
                        "managedUploadDurableRetry",
                        "android",
                        "durable-os-scheduler",
                        "copy-to-owned-storage",
                        "platform-key-value-store"
                ),
                new GeneratedTusManagedUploadTransport(
                        "Location"
                ),
                new GeneratedTusManagedUploadTerminal(
                        "succeeded",
                        ""
                ),
                new GeneratedTusManagedUploadCleanup(
                        "remove-owned-source-after-success",
                        "remove-after-success"
                ),
                new GeneratedTusManagedUploadRetryPlan(
                        new String[] {
                        "pending",
                        "running",
                        "failed",
                        "running",
                        "succeeded",
                    },
                        new int[] {
                        0,
                    }
                ),
                new GeneratedTusManagedUploadInput(
                        "hello managed!",
                        7,
                        "managed-durable-retry-fingerprint",
                        "managed-durable-retry",
                        new GeneratedTusManagedUploadMetadata[] {
                        new GeneratedTusManagedUploadMetadata(
                                "filename",
                                "managed.txt"
                        ),
                    }
                ),
                new GeneratedTusManagedUploadAttempt[] {
                        new GeneratedTusManagedUploadAttempt(
                                0,
                                "failed",
                                new GeneratedTusManagedUploadFailure(
                                        "after-accepted-offset",
                                        "io-error",
                                        7
                                ),
                                new GeneratedTusManagedUploadRequest[] {
                                        new GeneratedTusManagedUploadRequest(
                                                "POST",
                                                "endpoint",
                                                0,
                                                201,
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Length",
                                                        "14"
                                                ),
                                            },
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Location",
                                                        "https://tus.io/uploads/managed-durable-retry"
                                                ),
                                            }
                                        ),
                                        new GeneratedTusManagedUploadRequest(
                                                "PATCH",
                                                "upload",
                                                7,
                                                204,
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Offset",
                                                        "0"
                                                ),
                                            },
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Offset",
                                                        "7"
                                                ),
                                            }
                                        ),
                                }
                        ),
                        new GeneratedTusManagedUploadAttempt(
                                1,
                                "succeeded",
                                null,
                                new GeneratedTusManagedUploadRequest[] {
                                        new GeneratedTusManagedUploadRequest(
                                                "HEAD",
                                                "upload",
                                                0,
                                                200,
                                                new GeneratedTusManagedUploadHeader[0],
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Length",
                                                        "14"
                                                ),
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Offset",
                                                        "7"
                                                ),
                                            }
                                        ),
                                        new GeneratedTusManagedUploadRequest(
                                                "PATCH",
                                                "upload",
                                                7,
                                                204,
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Offset",
                                                        "7"
                                                ),
                                            },
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Offset",
                                                        "14"
                                                ),
                                            }
                                        ),
                                }
                        ),
                }
        ),
        new GeneratedTusManagedUploadRuntimeCase(
                new GeneratedTusManagedUploadRuntimeProfile(
                        "managedUploadPermanentFailure",
                        "android",
                        "durable-os-scheduler",
                        "copy-to-owned-storage",
                        "platform-key-value-store"
                ),
                new GeneratedTusManagedUploadTransport(
                        "Location"
                ),
                new GeneratedTusManagedUploadTerminal(
                        "failed",
                        "unretryable-protocol-error"
                ),
                new GeneratedTusManagedUploadCleanup(
                        "retain-owned-source-after-permanent-failure",
                        "absent-after-permanent-failure"
                ),
                new GeneratedTusManagedUploadRetryPlan(
                        new String[] {
                        "pending",
                        "running",
                        "failed",
                    },
                        new int[0]
                ),
                new GeneratedTusManagedUploadInput(
                        "hello failure!",
                        7,
                        "managed-permanent-failure-fingerprint",
                        "managed-permanent-failure",
                        new GeneratedTusManagedUploadMetadata[] {
                        new GeneratedTusManagedUploadMetadata(
                                "filename",
                                "managed-permanent-failure.txt"
                        ),
                    }
                ),
                new GeneratedTusManagedUploadAttempt[] {
                        new GeneratedTusManagedUploadAttempt(
                                0,
                                "failed",
                                new GeneratedTusManagedUploadFailure(
                                        "during-protocol-request",
                                        "unretryable-protocol-error",
                                        -1
                                ),
                                new GeneratedTusManagedUploadRequest[] {
                                        new GeneratedTusManagedUploadRequest(
                                                "POST",
                                                "endpoint",
                                                0,
                                                400,
                                                new GeneratedTusManagedUploadHeader[] {
                                                new GeneratedTusManagedUploadHeader(
                                                        "Upload-Length",
                                                        "14"
                                                ),
                                            },
                                                new GeneratedTusManagedUploadHeader[0]
                                        ),
                                }
                        ),
                }
        ),
    };
    private static final GeneratedTusMethodOverride[] METHOD_OVERRIDES =
            new GeneratedTusMethodOverride[] {
        new GeneratedTusMethodOverride(
                "PATCH",
                "POST",
                "X-HTTP-Method-Override",
                "PATCH"
        ),
    };

    /**
     * Verifies Android managed uploads can persist state and resume through platform storage.
     */
    @Test
    public void shouldRunManagedUploadWithAndroidPlatformState() throws Exception {
        for (GeneratedTusManagedUploadRuntimeCase testCase : CASES) {
            Activity activity = Robolectric.setupActivity(Activity.class);
            SharedPreferences stateStore =
                    activity.getSharedPreferences(testCase.scenarioId + "-state", 0);
            SharedPreferences urlStorePreferences =
                    activity.getSharedPreferences(testCase.scenarioId + "-urls", 0);
            assertTrue(testCase.scenarioId, stateStore.edit().clear().commit());
            assertTrue(testCase.scenarioId, urlStorePreferences.edit().clear().commit());

            GeneratedTusManagedUploadServer server = new GeneratedTusManagedUploadServer(testCase);
            server.start();
            try {
                List<String> states = new ArrayList<String>();
                File source = writeSourceFile(testCase);
                File ownedSource = ownedSourceFile(testCase, source);
                copyDurableSource(testCase, source, ownedSource);
                recordState(testCase, states, stateStore, "pending");

                final TusPreferencesURLStore urlStore =
                        new TusPreferencesURLStore(urlStorePreferences);
                final TusClient client = new TusClient();
                client.setUploadCreationURL(server.endpointUrlFor(testCase));
                client.enableResuming(urlStore);
                client.enableRemoveFingerprintOnSuccess();

                TusExecutor executor =
                        managedExecutorFor(testCase, client, ownedSource, states, stateStore);
                GeneratedTusAndroidScheduler scheduler =
                        new GeneratedTusAndroidScheduler(testCase, stateStore);
                try {
                    Future<Boolean> future = scheduler.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return executor.makeAttempts();
                        }
                    });
                    assertTerminalResult(testCase, future);
                } finally {
                    scheduler.shutdown();
                }

                cleanupAfterTerminalState(testCase, ownedSource);

                assertArrayEquals(
                        testCase.scenarioId,
                        testCase.expectedStates,
                        states.toArray(new String[states.size()]));
                assertArrayEquals(
                        testCase.scenarioId,
                        testCase.expectedStates,
                        storedStates(stateStore));
                assertResumeUrlState(testCase, urlStore);
                assertOwnedSourceState(testCase, ownedSource);
                assertTrue(testCase.scenarioId, source.exists());
                source.delete();
            } finally {
                server.stop();
            }
        }
    }

    private void assertTerminalResult(
            GeneratedTusManagedUploadRuntimeCase testCase,
            Future<Boolean> future) throws Exception {
        try {
            boolean result = future.get();
            if (!"succeeded".equals(testCase.terminalState)) {
                throw new AssertionError(testCase.scenarioId + " expected terminal failure");
            }
            assertTrue(testCase.scenarioId, result);
        } catch (ExecutionException error) {
            if (!"failed".equals(testCase.terminalState)) {
                throw error;
            }
            assertTerminalFailure(testCase, error.getCause());
        }
    }

    private void assertTerminalFailure(
            GeneratedTusManagedUploadRuntimeCase testCase,
            Throwable error) {
        if ("unretryable-protocol-error".equals(testCase.terminalFailure)) {
            assertTrue(testCase.scenarioId, error instanceof ProtocolException);
            return;
        }
        if ("source-unavailable".equals(testCase.terminalFailure)) {
            assertTrue(testCase.scenarioId, error instanceof IOException);
            return;
        }
        if ("retry-policy-exhausted".equals(testCase.terminalFailure)) {
            assertTrue(
                    testCase.scenarioId,
                    error instanceof ProtocolException || error instanceof IOException);
            return;
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated terminal failure "
                        + testCase.terminalFailure);
    }

    private TusExecutor managedExecutorFor(
            final GeneratedTusManagedUploadRuntimeCase testCase,
            final TusClient client,
            final File ownedSource,
            final List<String> states,
            final SharedPreferences stateStore) {
        TusExecutor executor = new TusExecutor() {
            private int attemptIndex;

            @Override
            protected void makeAttempt() throws ProtocolException, IOException {
                GeneratedTusManagedUploadAttempt attempt = testCase.attempts[attemptIndex];
                attemptIndex += 1;
                recordState(testCase, states, stateStore, "running");

                try {
                    TusUpload upload = uploadFor(testCase, ownedSource);
                    TusUploader uploader = client.resumeOrCreateUpload(upload);
                    uploader.setChunkSize(testCase.input.chunkSize);
                    uploader.setRequestPayloadSize(testCase.input.chunkSize);
                    while (uploader.getOffset() < upload.getSize()) {
                        uploader.uploadChunk();
                        if (
                                isAfterAcceptedOffsetFailure(attempt)
                                && uploader.getOffset() == attempt.failure.afterAcceptedOffset) {
                            uploader.finish(false);
                            recordState(testCase, states, stateStore, attempt.stateAfterAttempt);
                            throw new IOException(attempt.failure.kind);
                        }
                    }
                    uploader.finish();
                    recordState(testCase, states, stateStore, attempt.stateAfterAttempt);
                } catch (ProtocolException error) {
                    recordDuringProtocolFailure(testCase, states, stateStore, attempt);
                    throw error;
                } catch (IOException error) {
                    recordDuringProtocolFailure(testCase, states, stateStore, attempt);
                    throw error;
                }
            }
        };
        executor.setDelays(testCase.retryDelays);
        return executor;
    }

    private boolean isAfterAcceptedOffsetFailure(GeneratedTusManagedUploadAttempt attempt) {
        return attempt.failure != null
                && "after-accepted-offset".equals(attempt.failure.phase);
    }

    private void recordDuringProtocolFailure(
            GeneratedTusManagedUploadRuntimeCase testCase,
            List<String> states,
            SharedPreferences stateStore,
            GeneratedTusManagedUploadAttempt attempt) {
        if (attempt.failure == null || !"during-protocol-request".equals(attempt.failure.phase)) {
            return;
        }

        recordState(testCase, states, stateStore, attempt.stateAfterAttempt);
    }

    private TusUpload uploadFor(
            GeneratedTusManagedUploadRuntimeCase testCase,
            File ownedSource) throws IOException {
        TusUpload upload = new TusUpload(ownedSource);
        upload.setFingerprint(testCase.input.fingerprint);
        upload.setMetadata(metadataFor(testCase.input.metadata));
        return upload;
    }

    private Map<String, String> metadataFor(GeneratedTusManagedUploadMetadata[] metadata) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (GeneratedTusManagedUploadMetadata entry : metadata) {
            result.put(entry.name, entry.value);
        }
        return result;
    }

    private void copyDurableSource(
            GeneratedTusManagedUploadRuntimeCase testCase,
            File source,
            File ownedSource) throws IOException {
        if (!"copy-to-owned-storage".equals(testCase.sourceDurability)) {
            throw new AssertionError(
                    testCase.scenarioId
                            + " uses unsupported generated source durability "
                            + testCase.sourceDurability);
        }

        copyFile(source, ownedSource);
        assertTrue(testCase.scenarioId, ownedSource.exists());
    }

    private void cleanupAfterTerminalState(
            GeneratedTusManagedUploadRuntimeCase testCase,
            File ownedSource) throws IOException {
        if (!"remove-owned-source-after-success".equals(testCase.ownedSourceCleanup)) {
            return;
        }

        if (ownedSource.exists() && !ownedSource.delete()) {
            throw new IOException("Could not delete generated owned source " + ownedSource);
        }
    }

    private void assertOwnedSourceState(
            GeneratedTusManagedUploadRuntimeCase testCase,
            File ownedSource) {
        if ("remove-owned-source-after-success".equals(testCase.ownedSourceCleanup)) {
            assertFalse(testCase.scenarioId, ownedSource.exists());
            return;
        }
        if ("retain-owned-source-after-permanent-failure".equals(testCase.ownedSourceCleanup)) {
            assertTrue(testCase.scenarioId, ownedSource.exists());
            ownedSource.delete();
            return;
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated owned-source cleanup "
                        + testCase.ownedSourceCleanup);
    }

    private void assertResumeUrlState(
            GeneratedTusManagedUploadRuntimeCase testCase,
            TusPreferencesURLStore urlStore) {
        if (
                "remove-after-success".equals(testCase.resumeUrlCleanup)
                || "absent-after-permanent-failure".equals(testCase.resumeUrlCleanup)) {
            assertNull(testCase.scenarioId, urlStore.get(testCase.input.fingerprint));
            return;
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated resume URL cleanup "
                        + testCase.resumeUrlCleanup);
    }

    private void recordState(
            GeneratedTusManagedUploadRuntimeCase testCase,
            List<String> states,
            SharedPreferences stateStore,
            String state) {
        if (!"platform-key-value-store".equals(testCase.stateBackend)) {
            throw new AssertionError(
                    testCase.scenarioId
                            + " uses unsupported generated state backend "
                            + testCase.stateBackend);
        }

        states.add(state);
        SharedPreferences.Editor editor = stateStore.edit();
        editor.putInt("state-count", states.size());
        for (int index = 0; index < states.size(); index += 1) {
            editor.putString("state-" + index, states.get(index));
        }
        assertTrue(testCase.scenarioId, editor.commit());
    }

    private String[] storedStates(SharedPreferences stateStore) {
        int count = stateStore.getInt("state-count", 0);
        String[] states = new String[count];
        for (int index = 0; index < count; index += 1) {
            states[index] = stateStore.getString("state-" + index, "");
        }
        return states;
    }

    private File writeSourceFile(GeneratedTusManagedUploadRuntimeCase testCase) throws IOException {
        File source = File.createTempFile(testCase.scenarioId, "-source.bin");
        FileOutputStream output = new FileOutputStream(source);
        try {
            output.write(testCase.input.content.getBytes(StandardCharsets.UTF_8));
        } finally {
            output.close();
        }
        return source;
    }

    private File ownedSourceFile(
            GeneratedTusManagedUploadRuntimeCase testCase,
            File source) {
        return new File(source.getParentFile(), testCase.scenarioId + "-android-owned.bin");
    }

    private void copyFile(File source, File destination) throws IOException {
        FileInputStream input = new FileInputStream(source);
        try {
            FileOutputStream output = new FileOutputStream(destination);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

    private static String offsetDiscoveryMethod() {
        for (GeneratedTusProtocolContract.GeneratedTusProtocolOperation operation
                : GeneratedTusProtocolContract.OPERATIONS) {
            if ("offset-discovery".equals(operation.role)) {
                return operation.method;
            }
        }

        throw new AssertionError("Missing generated offset-discovery operation");
    }

    private static final class GeneratedTusAndroidScheduler {
        private final ExecutorService worker = Executors.newSingleThreadExecutor();
        private final GeneratedTusManagedUploadRuntimeCase testCase;
        private final SharedPreferences stateStore;

        GeneratedTusAndroidScheduler(
                GeneratedTusManagedUploadRuntimeCase testCase,
                SharedPreferences stateStore) {
            this.testCase = testCase;
            this.stateStore = stateStore;
        }

        Future<Boolean> submit(Callable<Boolean> work) {
            if (!"durable-os-scheduler".equals(testCase.scheduler)) {
                throw new AssertionError(
                        testCase.scenarioId
                                + " uses unsupported generated scheduler "
                                + testCase.scheduler);
            }

            assertTrue(
                    testCase.scenarioId,
                    stateStore.edit().putString("scheduler", testCase.scheduler).commit());
            return worker.submit(work);
        }

        void shutdown() {
            worker.shutdownNow();
        }
    }

    private static final class GeneratedTusManagedUploadServer {
        private final ServerSocket serverSocket;
        private final GeneratedTusManagedUploadRuntimeCase testCase;
        private volatile boolean running;
        private Thread thread;

        GeneratedTusManagedUploadServer(GeneratedTusManagedUploadRuntimeCase testCase)
                throws IOException {
            this.testCase = testCase;
            this.serverSocket = new ServerSocket(0);
        }

        void start() {
            running = true;
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    serve();
                }
            });
            thread.start();
        }

        void stop() throws IOException, InterruptedException {
            running = false;
            serverSocket.close();
            if (thread != null) {
                thread.join(1000);
            }
        }

        URL endpointUrlFor(GeneratedTusManagedUploadRuntimeCase testCase) throws IOException {
            return new URL("http://127.0.0.1:" + serverSocket.getLocalPort() + "/files");
        }

        URL uploadUrlFor(GeneratedTusManagedUploadRuntimeCase testCase) throws IOException {
            return new URL(endpointUrlFor(testCase).toString() + "/" + testCase.input.uploadPath);
        }

        private void serve() {
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    handle(socket);
                } catch (SocketException error) {
                    if (running) {
                        throw new AssertionError(error);
                    }
                } catch (IOException error) {
                    throw new AssertionError(error);
                }
            }
        }

        private void handle(Socket socket) throws IOException {
            try {
                GeneratedTusHttpRequest httpRequest =
                        readHttpRequest(socket.getInputStream(), socket.getOutputStream());
                GeneratedTusManagedUploadRequest request = findRequest(httpRequest);
                if (request == null) {
                    respondNotFound(socket.getOutputStream());
                    return;
                }

                respond(socket.getOutputStream(), request);
            } finally {
                socket.close();
            }
        }

        private GeneratedTusManagedUploadRequest findRequest(GeneratedTusHttpRequest httpRequest)
                throws IOException {
            for (GeneratedTusManagedUploadAttempt attempt : testCase.attempts) {
                for (GeneratedTusManagedUploadRequest request : attempt.requests) {
                    if (matchesRequest(httpRequest, request)) {
                        return request;
                    }
                }
            }

            return null;
        }

        private boolean matchesRequest(
                GeneratedTusHttpRequest httpRequest,
                GeneratedTusManagedUploadRequest request) throws IOException {
            if (!pathFor(request).equals(httpRequest.path)) {
                return false;
            }
            if (httpRequest.bodySize != request.bodySize) {
                return false;
            }
            if (!methodMatches(httpRequest, request)) {
                return false;
            }
            for (GeneratedTusManagedUploadHeader header : request.requestHeaders) {
                if (!header.value.equals(headerValue(httpRequest.headers, header.name))) {
                    return false;
                }
            }

            return true;
        }

        private boolean methodMatches(
                GeneratedTusHttpRequest httpRequest,
                GeneratedTusManagedUploadRequest request) {
            if (request.method.equals(httpRequest.method)) {
                return true;
            }
            GeneratedTusMethodOverride methodOverride = methodOverrideFor(request.method);
            return methodOverride != null
                    && methodOverride.method.equals(httpRequest.method)
                    && methodOverride.headerValue.equals(
                            headerValue(httpRequest.headers, methodOverride.headerName));
        }

        private String pathFor(GeneratedTusManagedUploadRequest request) throws IOException {
            if ("endpoint".equals(request.url)) {
                return endpointUrlFor(testCase).getPath();
            }

            return uploadUrlFor(testCase).getPath();
        }

        private String responseHeaderValueFor(GeneratedTusManagedUploadHeader header)
                throws IOException {
            if (!testCase.locationHeaderName.equals(header.name)) {
                return header.value;
            }

            return uploadUrlFor(testCase).toString();
        }

        private void respond(OutputStream output, GeneratedTusManagedUploadRequest request)
                throws IOException {
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 ").append(request.statusCode).append(" Generated\r\n");
            for (GeneratedTusManagedUploadHeader header : request.responseHeaders) {
                response.append(header.name)
                        .append(": ")
                        .append(responseHeaderValueFor(header))
                        .append("\r\n");
            }
            response.append("Content-Length: 0\r\n");
            response.append("Connection: close\r\n");
            response.append("\r\n");
            output.write(response.toString().getBytes(StandardCharsets.UTF_8));
        }

        private GeneratedTusHttpRequest readHttpRequest(InputStream input, OutputStream output)
                throws IOException {
            ByteArrayOutputStream headerBytes = new ByteArrayOutputStream();
            int previousThird = -1;
            int previousSecond = -1;
            int previousFirst = -1;
            int current;
            while ((current = input.read()) != -1) {
                headerBytes.write(current);
                if (
                        previousThird == '\r'
                        && previousSecond == '\n'
                        && previousFirst == '\r'
                        && current == '\n') {
                    break;
                }
                previousThird = previousSecond;
                previousSecond = previousFirst;
                previousFirst = current;
            }

            String headerText = headerBytes.toString(StandardCharsets.UTF_8.name());
            String[] lines = headerText.split("\\r\\n");
            String[] requestLine = lines[0].split(" ");
            Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
            for (int index = 1; index < lines.length; index += 1) {
                String line = lines[index];
                if (line.length() == 0) {
                    continue;
                }
                int separator = line.indexOf(":");
                if (separator < 0) {
                    continue;
                }
                String name = line.substring(0, separator);
                String value = line.substring(separator + 1).trim();
                List<String> values = headers.get(name);
                if (values == null) {
                    values = new ArrayList<String>();
                    headers.put(name, values);
                }
                values.add(value);
            }

            if ("100-continue".equalsIgnoreCase(headerValue(headers, "Expect"))) {
                output.write("HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                output.flush();
            }

            int bodySize = drainRequestBody(input, headers);
            return new GeneratedTusHttpRequest(requestLine[0], requestLine[1], headers, bodySize);
        }

        private static int contentLength(Map<String, List<String>> headers) {
            String header = headerValue(headers, "Content-Length");
            if (header == null || header.length() == 0) {
                return 0;
            }

            return Integer.parseInt(header);
        }

        private static int drainRequestBody(InputStream input, Map<String, List<String>> headers)
                throws IOException {
            if ("chunked".equalsIgnoreCase(headerValue(headers, "Transfer-Encoding"))) {
                return drainChunkedRequestBody(input);
            }

            return drainFixedRequestBody(input, contentLength(headers));
        }

        private static int drainFixedRequestBody(InputStream input, int contentLength)
                throws IOException {
            int remaining = contentLength;
            byte[] buffer = new byte[8192];
            while (remaining > 0) {
                int read = input.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read == -1) {
                    break;
                }
                remaining -= read;
            }
            return contentLength - remaining;
        }

        private static int drainChunkedRequestBody(InputStream input) throws IOException {
            int bodySize = 0;
            while (true) {
                String line = readAsciiLine(input);
                int extensionIndex = line.indexOf(";");
                String sizeText = extensionIndex < 0 ? line : line.substring(0, extensionIndex);
                int chunkSize = Integer.parseInt(sizeText.trim(), 16);
                if (chunkSize == 0) {
                    drainChunkedTrailers(input);
                    return bodySize;
                }

                bodySize += drainFixedRequestBody(input, chunkSize);
                readAsciiLine(input);
            }
        }

        private static void drainChunkedTrailers(InputStream input) throws IOException {
            while (true) {
                String line = readAsciiLine(input);
                if (line.length() == 0) {
                    return;
                }
            }
        }

        private static String readAsciiLine(InputStream input) throws IOException {
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            int current;
            while ((current = input.read()) != -1) {
                if (current == '\n') {
                    break;
                }
                if (current != '\r') {
                    line.write(current);
                }
            }
            return line.toString(StandardCharsets.UTF_8.name());
        }

        private static void respondNotFound(OutputStream output) throws IOException {
            byte[] body = "No generated request matched".getBytes(StandardCharsets.UTF_8);
            output.write("HTTP/1.1 404 Generated\r\n".getBytes(StandardCharsets.UTF_8));
            output.write(("Content-Length: " + body.length + "\r\n").getBytes(StandardCharsets.UTF_8));
            output.write("Connection: close\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            output.write(body);
        }

        private static String headerValue(Map<String, List<String>> headers, String name) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase(name) || entry.getValue().isEmpty()) {
                    continue;
                }

                return entry.getValue().get(0);
            }

            return null;
        }

        private static GeneratedTusMethodOverride methodOverrideFor(String originalMethod) {
            for (GeneratedTusMethodOverride methodOverride : METHOD_OVERRIDES) {
                if (methodOverride.originalMethod.equals(originalMethod)) {
                    return methodOverride;
                }
            }

            return null;
        }

        private static final class GeneratedTusHttpRequest {
            final String method;
            final String path;
            final Map<String, List<String>> headers;
            final int bodySize;

            GeneratedTusHttpRequest(
                    String method,
                    String path,
                    Map<String, List<String>> headers,
                    int bodySize) {
                this.method = method;
                this.path = path;
                this.headers = headers;
                this.bodySize = bodySize;
            }
        }
    }

    private static final class GeneratedTusManagedUploadRuntimeCase {
        final String scenarioId;
        final String runtime;
        final String scheduler;
        final String sourceDurability;
        final String stateBackend;
        final String locationHeaderName;
        final String terminalState;
        final String terminalFailure;
        final String ownedSourceCleanup;
        final String resumeUrlCleanup;
        final String[] expectedStates;
        final int[] retryDelays;
        final String offsetDiscoveryMethod;
        final GeneratedTusManagedUploadInput input;
        final GeneratedTusManagedUploadAttempt[] attempts;

        GeneratedTusManagedUploadRuntimeCase(
                GeneratedTusManagedUploadRuntimeProfile profile,
                GeneratedTusManagedUploadTransport transport,
                GeneratedTusManagedUploadTerminal terminal,
                GeneratedTusManagedUploadCleanup cleanup,
                GeneratedTusManagedUploadRetryPlan retryPlan,
                GeneratedTusManagedUploadInput input,
                GeneratedTusManagedUploadAttempt[] attempts) {
            this.scenarioId = profile.scenarioId;
            this.runtime = profile.runtime;
            this.scheduler = profile.scheduler;
            this.sourceDurability = profile.sourceDurability;
            this.stateBackend = profile.stateBackend;
            this.locationHeaderName = transport.locationHeaderName;
            this.terminalState = terminal.state;
            this.terminalFailure = terminal.failure;
            this.ownedSourceCleanup = cleanup.ownedSource;
            this.resumeUrlCleanup = cleanup.resumeUrl;
            this.expectedStates = retryPlan.expectedStates;
            this.retryDelays = retryPlan.retryDelays;
            this.offsetDiscoveryMethod = offsetDiscoveryMethod();
            this.input = input;
            this.attempts = attempts;
        }
    }

    private static final class GeneratedTusManagedUploadTerminal {
        final String state;
        final String failure;

        GeneratedTusManagedUploadTerminal(String state, String failure) {
            this.state = state;
            this.failure = failure;
        }
    }

    private static final class GeneratedTusManagedUploadRuntimeProfile {
        final String scenarioId;
        final String runtime;
        final String scheduler;
        final String sourceDurability;
        final String stateBackend;

        GeneratedTusManagedUploadRuntimeProfile(
                String scenarioId,
                String runtime,
                String scheduler,
                String sourceDurability,
                String stateBackend) {
            this.scenarioId = scenarioId;
            this.runtime = runtime;
            this.scheduler = scheduler;
            this.sourceDurability = sourceDurability;
            this.stateBackend = stateBackend;
        }
    }

    private static final class GeneratedTusManagedUploadTransport {
        final String locationHeaderName;

        GeneratedTusManagedUploadTransport(String locationHeaderName) {
            this.locationHeaderName = locationHeaderName;
        }
    }

    private static final class GeneratedTusManagedUploadCleanup {
        final String ownedSource;
        final String resumeUrl;

        GeneratedTusManagedUploadCleanup(String ownedSource, String resumeUrl) {
            this.ownedSource = ownedSource;
            this.resumeUrl = resumeUrl;
        }
    }

    private static final class GeneratedTusManagedUploadRetryPlan {
        final String[] expectedStates;
        final int[] retryDelays;

        GeneratedTusManagedUploadRetryPlan(String[] expectedStates, int[] retryDelays) {
            this.expectedStates = expectedStates;
            this.retryDelays = retryDelays;
        }
    }

    private static final class GeneratedTusManagedUploadInput {
        final String content;
        final int chunkSize;
        final String fingerprint;
        final String uploadPath;
        final GeneratedTusManagedUploadMetadata[] metadata;

        GeneratedTusManagedUploadInput(
                String content,
                int chunkSize,
                String fingerprint,
                String uploadPath,
                GeneratedTusManagedUploadMetadata[] metadata) {
            this.content = content;
            this.chunkSize = chunkSize;
            this.fingerprint = fingerprint;
            this.uploadPath = uploadPath;
            this.metadata = metadata;
        }
    }

    private static final class GeneratedTusManagedUploadAttempt {
        final int attemptIndex;
        final String stateAfterAttempt;
        final GeneratedTusManagedUploadFailure failure;
        final GeneratedTusManagedUploadRequest[] requests;

        GeneratedTusManagedUploadAttempt(
                int attemptIndex,
                String stateAfterAttempt,
                GeneratedTusManagedUploadFailure failure,
                GeneratedTusManagedUploadRequest[] requests) {
            this.attemptIndex = attemptIndex;
            this.stateAfterAttempt = stateAfterAttempt;
            this.failure = failure;
            this.requests = requests;
        }
    }

    private static final class GeneratedTusManagedUploadFailure {
        final String phase;
        final String kind;
        final long afterAcceptedOffset;

        GeneratedTusManagedUploadFailure(String phase, String kind, long afterAcceptedOffset) {
            this.phase = phase;
            this.kind = kind;
            this.afterAcceptedOffset = afterAcceptedOffset;
        }
    }

    private static final class GeneratedTusManagedUploadRequest {
        final String method;
        final String url;
        final int bodySize;
        final int statusCode;
        final GeneratedTusManagedUploadHeader[] requestHeaders;
        final GeneratedTusManagedUploadHeader[] responseHeaders;

        GeneratedTusManagedUploadRequest(
                String method,
                String url,
                int bodySize,
                int statusCode,
                GeneratedTusManagedUploadHeader[] requestHeaders,
                GeneratedTusManagedUploadHeader[] responseHeaders) {
            this.method = method;
            this.url = url;
            this.bodySize = bodySize;
            this.statusCode = statusCode;
            this.requestHeaders = requestHeaders;
            this.responseHeaders = responseHeaders;
        }
    }

    private static final class GeneratedTusManagedUploadHeader {
        final String name;
        final String value;

        GeneratedTusManagedUploadHeader(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final class GeneratedTusManagedUploadMetadata {
        final String name;
        final String value;

        GeneratedTusManagedUploadMetadata(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static final class GeneratedTusMethodOverride {
        final String originalMethod;
        final String method;
        final String headerName;
        final String headerValue;

        GeneratedTusMethodOverride(
                String originalMethod,
                String method,
                String headerName,
                String headerValue) {
            this.originalMethod = originalMethod;
            this.method = method;
            this.headerName = headerName;
            this.headerValue = headerValue;
        }
    }
}
