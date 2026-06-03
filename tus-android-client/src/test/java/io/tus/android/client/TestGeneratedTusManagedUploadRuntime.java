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
                        "available",
                        "platform-key-value-store",
                        new GeneratedTusManagedUploadNetwork(
                                "any-network",
                                "unmetered-network",
                                "start-upload-work"
                        )
                ),
                new GeneratedTusManagedUploadTransport(
                        "Location"
                ),
                new GeneratedTusManagedUploadOutcome(
                        "terminal",
                        "succeeded",
                        "",
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
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Length",
                                                                "14"
                                                        ),
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Metadata",
                                                                "filename bWFuYWdlZC50eHQ="
                                                        ),
                                                    }
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Location",
                                                                "https://tus.io/uploads/managed-durable-retry"
                                                        ),
                                                    }
                                                )
                                        ),
                                        new GeneratedTusManagedUploadRequest(
                                                "PATCH",
                                                "upload",
                                                7,
                                                204,
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Offset",
                                                                "0"
                                                        ),
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Content-Type",
                                                                "application/offset+octet-stream"
                                                        ),
                                                    }
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Offset",
                                                                "7"
                                                        ),
                                                    }
                                                )
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
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[0]
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
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
                                                )
                                        ),
                                        new GeneratedTusManagedUploadRequest(
                                                "PATCH",
                                                "upload",
                                                7,
                                                204,
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Offset",
                                                                "7"
                                                        ),
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Content-Type",
                                                                "application/offset+octet-stream"
                                                        ),
                                                    }
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Offset",
                                                                "14"
                                                        ),
                                                    }
                                                )
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
                        "available",
                        "platform-key-value-store",
                        new GeneratedTusManagedUploadNetwork(
                                "any-network",
                                "unmetered-network",
                                "start-upload-work"
                        )
                ),
                new GeneratedTusManagedUploadTransport(
                        "Location"
                ),
                new GeneratedTusManagedUploadOutcome(
                        "terminal",
                        "failed",
                        "unretryable-protocol-error",
                        ""
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
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Length",
                                                                "14"
                                                        ),
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Metadata",
                                                                "filename bWFuYWdlZC1wZXJtYW5lbnQtZmFpbHVyZS50eHQ="
                                                        ),
                                                    }
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        false,
                                                        new GeneratedTusManagedUploadHeader[0]
                                                )
                                        ),
                                }
                        ),
                }
        ),
        new GeneratedTusManagedUploadRuntimeCase(
                new GeneratedTusManagedUploadRuntimeProfile(
                        "managedUploadRetryPolicyExhausted",
                        "android",
                        "durable-os-scheduler",
                        "copy-to-owned-storage",
                        "available",
                        "platform-key-value-store",
                        new GeneratedTusManagedUploadNetwork(
                                "any-network",
                                "unmetered-network",
                                "start-upload-work"
                        )
                ),
                new GeneratedTusManagedUploadTransport(
                        "Location"
                ),
                new GeneratedTusManagedUploadOutcome(
                        "terminal",
                        "failed",
                        "retry-policy-exhausted",
                        ""
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
                        "running",
                        "failed",
                        "running",
                        "failed",
                    },
                        new int[] {
                        0,
                        0,
                    }
                ),
                new GeneratedTusManagedUploadInput(
                        "hello retries!",
                        7,
                        "managed-retry-exhausted-fingerprint",
                        "managed-retry-exhausted",
                        new GeneratedTusManagedUploadMetadata[] {
                        new GeneratedTusManagedUploadMetadata(
                                "filename",
                                "managed-retry-exhausted.txt"
                        ),
                    }
                ),
                new GeneratedTusManagedUploadAttempt[] {
                        new GeneratedTusManagedUploadAttempt(
                                0,
                                "failed",
                                new GeneratedTusManagedUploadFailure(
                                        "during-protocol-request",
                                        "retryable-protocol-error",
                                        -1
                                ),
                                new GeneratedTusManagedUploadRequest[] {
                                        new GeneratedTusManagedUploadRequest(
                                                "POST",
                                                "endpoint",
                                                0,
                                                500,
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Length",
                                                                "14"
                                                        ),
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Metadata",
                                                                "filename bWFuYWdlZC1yZXRyeS1leGhhdXN0ZWQudHh0"
                                                        ),
                                                    }
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        false,
                                                        new GeneratedTusManagedUploadHeader[0]
                                                )
                                        ),
                                }
                        ),
                        new GeneratedTusManagedUploadAttempt(
                                1,
                                "failed",
                                new GeneratedTusManagedUploadFailure(
                                        "during-protocol-request",
                                        "retryable-protocol-error",
                                        -1
                                ),
                                new GeneratedTusManagedUploadRequest[] {
                                        new GeneratedTusManagedUploadRequest(
                                                "POST",
                                                "endpoint",
                                                0,
                                                500,
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Length",
                                                                "14"
                                                        ),
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Metadata",
                                                                "filename bWFuYWdlZC1yZXRyeS1leGhhdXN0ZWQudHh0"
                                                        ),
                                                    }
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        false,
                                                        new GeneratedTusManagedUploadHeader[0]
                                                )
                                        ),
                                }
                        ),
                        new GeneratedTusManagedUploadAttempt(
                                2,
                                "failed",
                                new GeneratedTusManagedUploadFailure(
                                        "during-protocol-request",
                                        "retryable-protocol-error",
                                        -1
                                ),
                                new GeneratedTusManagedUploadRequest[] {
                                        new GeneratedTusManagedUploadRequest(
                                                "POST",
                                                "endpoint",
                                                0,
                                                500,
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        true,
                                                        new GeneratedTusManagedUploadHeader[] {
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Length",
                                                                "14"
                                                        ),
                                                        new GeneratedTusManagedUploadHeader(
                                                                "Upload-Metadata",
                                                                "filename bWFuYWdlZC1yZXRyeS1leGhhdXN0ZWQudHh0"
                                                        ),
                                                    }
                                                ),
                                                new GeneratedTusManagedUploadHeaderSet(
                                                        false,
                                                        new GeneratedTusManagedUploadHeader[0]
                                                )
                                        ),
                                }
                        ),
                }
        ),
        new GeneratedTusManagedUploadRuntimeCase(
                new GeneratedTusManagedUploadRuntimeProfile(
                        "managedUploadSourceUnavailable",
                        "android",
                        "durable-os-scheduler",
                        "copy-to-owned-storage",
                        "missing-before-durable-copy",
                        "platform-key-value-store",
                        new GeneratedTusManagedUploadNetwork(
                                "any-network",
                                "unmetered-network",
                                "start-upload-work"
                        )
                ),
                new GeneratedTusManagedUploadTransport(
                        "Location"
                ),
                new GeneratedTusManagedUploadOutcome(
                        "terminal",
                        "failed",
                        "source-unavailable",
                        ""
                ),
                new GeneratedTusManagedUploadCleanup(
                        "absent-after-source-unavailable",
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
                        "hello missing!",
                        7,
                        "managed-source-unavailable-fingerprint",
                        "managed-source-unavailable",
                        new GeneratedTusManagedUploadMetadata[] {
                        new GeneratedTusManagedUploadMetadata(
                                "filename",
                                "managed-source-unavailable.txt"
                        ),
                    }
                ),
                new GeneratedTusManagedUploadAttempt[] {
                        new GeneratedTusManagedUploadAttempt(
                                0,
                                "failed",
                                new GeneratedTusManagedUploadFailure(
                                        "before-protocol-request",
                                        "source-unavailable",
                                        -1
                                ),
                                new GeneratedTusManagedUploadRequest[] {

                                }
                        ),
                }
        ),
        new GeneratedTusManagedUploadRuntimeCase(
                new GeneratedTusManagedUploadRuntimeProfile(
                        "managedUploadNetworkConstraint",
                        "android",
                        "durable-os-scheduler",
                        "copy-to-owned-storage",
                        "available",
                        "platform-key-value-store",
                        new GeneratedTusManagedUploadNetwork(
                                "unmetered-network",
                                "metered-network",
                                "defer-until-network-constraint-satisfied"
                        )
                ),
                new GeneratedTusManagedUploadTransport(
                        "Location"
                ),
                new GeneratedTusManagedUploadOutcome(
                        "deferred",
                        "pending",
                        "",
                        "network-constraint-unsatisfied"
                ),
                new GeneratedTusManagedUploadCleanup(
                        "retain-owned-source-while-deferred",
                        "absent-while-deferred"
                ),
                new GeneratedTusManagedUploadRetryPlan(
                        new String[] {
                        "pending",
                    },
                        new int[0]
                ),
                new GeneratedTusManagedUploadInput(
                        "hello later!",
                        7,
                        "managed-network-constraint-fingerprint",
                        "managed-network-constraint",
                        new GeneratedTusManagedUploadMetadata[] {
                        new GeneratedTusManagedUploadMetadata(
                                "filename",
                                "managed-network-constraint.txt"
                        ),
                    }
                ),
                new GeneratedTusManagedUploadAttempt[] {

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
                recordState(testCase, states, stateStore, "pending");

                final TusPreferencesURLStore urlStore =
                        new TusPreferencesURLStore(urlStorePreferences);
                final TusClient client = new TusClient();
                client.setUploadCreationURL(server.endpointUrlFor(testCase));
                client.enableResuming(urlStore);
                client.enableRemoveFingerprintOnSuccess();

                try {
                    prepareSourceBeforeProtocol(testCase, source, ownedSource, states, stateStore);
                    GeneratedTusAndroidScheduler scheduler =
                            new GeneratedTusAndroidScheduler(testCase, stateStore);
                    try {
                        if (shouldDeferBeforeProtocol(testCase)) {
                            scheduler.deferUntilNetworkConstraintSatisfied();
                            assertDeferredResult(testCase);
                        } else {
                            TusExecutor executor =
                                    managedExecutorFor(
                                            testCase,
                                            client,
                                            ownedSource,
                                            states,
                                            stateStore);
                            Future<Boolean> future = scheduler.submit(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    return executor.makeAttempts();
                                }
                            });
                            assertTerminalResult(testCase, future);
                        }
                    } finally {
                        scheduler.shutdown();
                    }
                } catch (IOException error) {
                    if (!isSourceUnavailableBeforeProtocol(testCase)) {
                        throw error;
                    }
                    assertTerminalFailure(testCase, error);
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
                assertInputSourceState(testCase, source);
                assertProtocolRequestCount(testCase, server.requestCount());
            } finally {
                server.stop();
            }
        }
    }

    private void assertTerminalResult(
            GeneratedTusManagedUploadRuntimeCase testCase,
            Future<Boolean> future) throws Exception {
        if (!"terminal".equals(testCase.outcomeKind)) {
            throw new AssertionError(testCase.scenarioId + " expected deferred outcome");
        }

        try {
            boolean result = future.get();
            if (!"succeeded".equals(testCase.outcomeState)) {
                throw new AssertionError(testCase.scenarioId + " expected terminal failure");
            }
            assertTrue(testCase.scenarioId, result);
        } catch (ExecutionException error) {
            if (!"failed".equals(testCase.outcomeState)) {
                throw error;
            }
            assertTerminalFailure(testCase, error.getCause());
        }
    }

    private void assertTerminalFailure(
            GeneratedTusManagedUploadRuntimeCase testCase,
            Throwable error) {
        if ("unretryable-protocol-error".equals(testCase.outcomeFailure)) {
            assertTrue(testCase.scenarioId, error instanceof ProtocolException);
            return;
        }
        if ("source-unavailable".equals(testCase.outcomeFailure)) {
            assertTrue(testCase.scenarioId, error instanceof IOException);
            return;
        }
        if ("retry-policy-exhausted".equals(testCase.outcomeFailure)) {
            assertTrue(
                    testCase.scenarioId,
                    error instanceof ProtocolException || error instanceof IOException);
            return;
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated terminal failure "
                        + testCase.outcomeFailure);
    }

    private void assertDeferredResult(GeneratedTusManagedUploadRuntimeCase testCase) {
        if (
                !"deferred".equals(testCase.outcomeKind)
                || !"pending".equals(testCase.outcomeState)
                || !"network-constraint-unsatisfied".equals(testCase.outcomeReason)
                || !"defer-until-network-constraint-satisfied".equals(testCase.networkDecision)
                || networkConstraintSatisfied(testCase)) {
            throw new AssertionError(testCase.scenarioId + " expected deferred network outcome");
        }
    }

    private boolean networkConstraintSatisfied(GeneratedTusManagedUploadRuntimeCase testCase) {
        if ("offline".equals(testCase.currentNetwork)) {
            return false;
        }
        if ("any-network".equals(testCase.networkRequired)) {
            return "metered-network".equals(testCase.currentNetwork)
                    || "unmetered-network".equals(testCase.currentNetwork);
        }
        if ("unmetered-network".equals(testCase.networkRequired)) {
            return "unmetered-network".equals(testCase.currentNetwork);
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated network requirement "
                        + testCase.networkRequired);
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

    private void prepareSourceBeforeProtocol(
            GeneratedTusManagedUploadRuntimeCase testCase,
            File source,
            File ownedSource,
            List<String> states,
            SharedPreferences stateStore) throws IOException {
        if ("available".equals(testCase.sourceAvailability)) {
            copyDurableSource(testCase, source, ownedSource);
            return;
        }
        if ("missing-before-durable-copy".equals(testCase.sourceAvailability)) {
            GeneratedTusManagedUploadAttempt attempt = testCase.attempts[0];
            if (source.exists() && !source.delete()) {
                throw new IOException("Could not remove generated input source " + source);
            }
            recordState(testCase, states, stateStore, "running");
            try {
                copyDurableSource(testCase, source, ownedSource);
            } catch (IOException error) {
                recordState(testCase, states, stateStore, attempt.stateAfterAttempt);
                throw error;
            }
            throw new AssertionError(testCase.scenarioId + " unexpectedly prepared missing source");
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated source availability "
                        + testCase.sourceAvailability);
    }

    private boolean isSourceUnavailableBeforeProtocol(GeneratedTusManagedUploadRuntimeCase testCase) {
        return "source-unavailable".equals(testCase.outcomeFailure)
                && "missing-before-durable-copy".equals(testCase.sourceAvailability);
    }

    private boolean shouldDeferBeforeProtocol(GeneratedTusManagedUploadRuntimeCase testCase) {
        return "defer-until-network-constraint-satisfied".equals(testCase.networkDecision);
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
        if ("retain-owned-source-while-deferred".equals(testCase.ownedSourceCleanup)) {
            assertTrue(testCase.scenarioId, ownedSource.exists());
            ownedSource.delete();
            return;
        }
        if ("absent-after-source-unavailable".equals(testCase.ownedSourceCleanup)) {
            assertFalse(testCase.scenarioId, ownedSource.exists());
            return;
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated owned-source cleanup "
                        + testCase.ownedSourceCleanup);
    }

    private void assertInputSourceState(
            GeneratedTusManagedUploadRuntimeCase testCase,
            File source) {
        if ("missing-before-durable-copy".equals(testCase.sourceAvailability)) {
            assertFalse(testCase.scenarioId, source.exists());
            return;
        }

        assertTrue(testCase.scenarioId, source.exists());
        source.delete();
    }

    private void assertResumeUrlState(
            GeneratedTusManagedUploadRuntimeCase testCase,
            TusPreferencesURLStore urlStore) {
        if (
                "remove-after-success".equals(testCase.resumeUrlCleanup)
                || "absent-after-permanent-failure".equals(testCase.resumeUrlCleanup)
                || "absent-while-deferred".equals(testCase.resumeUrlCleanup)) {
            assertNull(testCase.scenarioId, urlStore.get(testCase.input.fingerprint));
            return;
        }

        throw new AssertionError(
                testCase.scenarioId
                        + " uses unsupported generated resume URL cleanup "
                        + testCase.resumeUrlCleanup);
    }

    private void assertProtocolRequestCount(
            GeneratedTusManagedUploadRuntimeCase testCase,
            int actualRequestCount) {
        assertTrue(
                testCase.scenarioId,
                actualRequestCount == expectedProtocolRequestCount(testCase));
    }

    private int expectedProtocolRequestCount(GeneratedTusManagedUploadRuntimeCase testCase) {
        int count = 0;
        for (GeneratedTusManagedUploadAttempt attempt : testCase.attempts) {
            count += attempt.requests.length;
        }
        return count;
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

        void deferUntilNetworkConstraintSatisfied() {
            if (!"durable-os-scheduler".equals(testCase.scheduler)) {
                throw new AssertionError(
                        testCase.scenarioId
                                + " uses unsupported generated scheduler "
                                + testCase.scheduler);
            }
            if (
                    !"defer-until-network-constraint-satisfied".equals(testCase.networkDecision)
                    || networkConstraintSatisfied(testCase)) {
                throw new AssertionError(testCase.scenarioId + " expected unsatisfied network");
            }

            assertTrue(
                    testCase.scenarioId,
                    stateStore.edit()
                            .putString("scheduler", testCase.scheduler)
                            .putString("network-required", testCase.networkRequired)
                            .putString("network-current", testCase.currentNetwork)
                            .commit());
        }

        private boolean networkConstraintSatisfied(GeneratedTusManagedUploadRuntimeCase testCase) {
            if ("offline".equals(testCase.currentNetwork)) {
                return false;
            }
            if ("any-network".equals(testCase.networkRequired)) {
                return "metered-network".equals(testCase.currentNetwork)
                        || "unmetered-network".equals(testCase.currentNetwork);
            }
            if ("unmetered-network".equals(testCase.networkRequired)) {
                return "unmetered-network".equals(testCase.currentNetwork);
            }

            throw new AssertionError(
                    testCase.scenarioId
                            + " uses unsupported generated network requirement "
                            + testCase.networkRequired);
        }

        void shutdown() {
            worker.shutdownNow();
        }
    }

    private static final class GeneratedTusManagedUploadServer {
        private final ServerSocket serverSocket;
        private final GeneratedTusManagedUploadRuntimeCase testCase;
        private volatile int requestCount;
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

        int requestCount() {
            return requestCount;
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
                requestCount += 1;
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
            if (!headersMatch(
                    httpRequest.headers,
                    request.requestHeaders,
                    GeneratedTusProtocolContract.DEFAULT_REQUEST_HEADERS)) {
                return false;
            }

            return true;
        }

        private boolean headersMatch(
                Map<String, List<String>> actualHeaders,
                GeneratedTusManagedUploadHeaderSet expectedHeaders,
                Map<String, String> defaultHeaders) {
            if (expectedHeaders.includesDefaultProtocolHeaders) {
                for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
                    if (!entry.getValue().equals(headerValue(actualHeaders, entry.getKey()))) {
                        return false;
                    }
                }
            }
            for (GeneratedTusManagedUploadHeader header : expectedHeaders.headers) {
                if (!header.value.equals(headerValue(actualHeaders, header.name))) {
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
            if (request.responseHeaders.includesDefaultProtocolHeaders) {
                for (Map.Entry<String, String> entry
                        : GeneratedTusProtocolContract.DEFAULT_RESPONSE_HEADERS.entrySet()) {
                    appendHeader(response, entry.getKey(), entry.getValue());
                }
            }
            for (GeneratedTusManagedUploadHeader header : request.responseHeaders.headers) {
                appendHeader(response, header.name, responseHeaderValueFor(header));
            }
            response.append("Content-Length: 0\r\n");
            response.append("Connection: close\r\n");
            response.append("\r\n");
            output.write(response.toString().getBytes(StandardCharsets.UTF_8));
        }

        private static void appendHeader(StringBuilder response, String name, String value) {
            response.append(name)
                    .append(": ")
                    .append(value)
                    .append("\r\n");
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
        final String sourceAvailability;
        final String stateBackend;
        final String networkRequired;
        final String currentNetwork;
        final String networkDecision;
        final String locationHeaderName;
        final String outcomeKind;
        final String outcomeState;
        final String outcomeFailure;
        final String outcomeReason;
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
                GeneratedTusManagedUploadOutcome outcome,
                GeneratedTusManagedUploadCleanup cleanup,
                GeneratedTusManagedUploadRetryPlan retryPlan,
                GeneratedTusManagedUploadInput input,
                GeneratedTusManagedUploadAttempt[] attempts) {
            this.scenarioId = profile.scenarioId;
            this.runtime = profile.runtime;
            this.scheduler = profile.scheduler;
            this.sourceDurability = profile.sourceDurability;
            this.sourceAvailability = profile.sourceAvailability;
            this.stateBackend = profile.stateBackend;
            this.networkRequired = profile.networkRequired;
            this.currentNetwork = profile.currentNetwork;
            this.networkDecision = profile.networkDecision;
            this.locationHeaderName = transport.locationHeaderName;
            this.outcomeKind = outcome.kind;
            this.outcomeState = outcome.state;
            this.outcomeFailure = outcome.failure;
            this.outcomeReason = outcome.reason;
            this.ownedSourceCleanup = cleanup.ownedSource;
            this.resumeUrlCleanup = cleanup.resumeUrl;
            this.expectedStates = retryPlan.expectedStates;
            this.retryDelays = retryPlan.retryDelays;
            this.offsetDiscoveryMethod = offsetDiscoveryMethod();
            this.input = input;
            this.attempts = attempts;
        }
    }

    private static final class GeneratedTusManagedUploadOutcome {
        final String kind;
        final String state;
        final String failure;
        final String reason;

        GeneratedTusManagedUploadOutcome(String kind, String state, String failure, String reason) {
            this.kind = kind;
            this.state = state;
            this.failure = failure;
            this.reason = reason;
        }
    }

    private static final class GeneratedTusManagedUploadRuntimeProfile {
        final String scenarioId;
        final String runtime;
        final String scheduler;
        final String sourceDurability;
        final String sourceAvailability;
        final String stateBackend;
        final String networkRequired;
        final String currentNetwork;
        final String networkDecision;

        GeneratedTusManagedUploadRuntimeProfile(
                String scenarioId,
                String runtime,
                String scheduler,
                String sourceDurability,
                String sourceAvailability,
                String stateBackend,
                GeneratedTusManagedUploadNetwork network) {
            this.scenarioId = scenarioId;
            this.runtime = runtime;
            this.scheduler = scheduler;
            this.sourceDurability = sourceDurability;
            this.sourceAvailability = sourceAvailability;
            this.stateBackend = stateBackend;
            this.networkRequired = network.required;
            this.currentNetwork = network.current;
            this.networkDecision = network.decision;
        }
    }

    private static final class GeneratedTusManagedUploadNetwork {
        final String required;
        final String current;
        final String decision;

        GeneratedTusManagedUploadNetwork(String required, String current, String decision) {
            this.required = required;
            this.current = current;
            this.decision = decision;
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
        final GeneratedTusManagedUploadHeaderSet requestHeaders;
        final GeneratedTusManagedUploadHeaderSet responseHeaders;

        GeneratedTusManagedUploadRequest(
                String method,
                String url,
                int bodySize,
                int statusCode,
                GeneratedTusManagedUploadHeaderSet requestHeaders,
                GeneratedTusManagedUploadHeaderSet responseHeaders) {
            this.method = method;
            this.url = url;
            this.bodySize = bodySize;
            this.statusCode = statusCode;
            this.requestHeaders = requestHeaders;
            this.responseHeaders = responseHeaders;
        }
    }

    private static final class GeneratedTusManagedUploadHeaderSet {
        final boolean includesDefaultProtocolHeaders;
        final GeneratedTusManagedUploadHeader[] headers;

        GeneratedTusManagedUploadHeaderSet(
                boolean includesDefaultProtocolHeaders,
                GeneratedTusManagedUploadHeader[] headers) {
            this.includesDefaultProtocolHeaders = includesDefaultProtocolHeaders;
            this.headers = headers;
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
