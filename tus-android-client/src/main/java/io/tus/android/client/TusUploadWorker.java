package io.tus.android.client;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.tus.java.client.ProtocolException;
import io.tus.java.client.TusClient;
import io.tus.java.client.TusUpload;
import io.tus.java.client.TusUploader;

/**
 * This worker performs the file upload to a TUS server.
 */
public class TusUploadWorker extends Worker {

    // OUTPUT: Progress
    public static final String UPLOAD_PROGRESS_DOUBLE = "UPLOAD_PROGRESS";
    // OUTPUT: Failure
    public static final String FAILURE_REASON_ENUM = "FAILURE_REASON_ENUM";
    public static final String FAILURE_DETAIL_MESSAGE = "FAILURE_DETAIL_MESSAGE";

    // INPUT
    private static final String PATH_TO_FILE_TO_UPLOAD = "PATH_TO_FILE_TO_UPLOAD";
    private static final String FILE_UPLOAD_URL = "FILE_UPLOAD_URL";
    private static final String METADATA_KEYS_ARRAY = "METADATA_KEYS_ARRAY";
    private static final String METADATA_VALUES_ARRAY = "METADATA_VALUES_ARRAY";
    private static final String CUSTOM_HEADERS_KEYS_ARRAY = "CUSTOM_HEADERS_KEYS_ARRAY";
    private static final String CUSTOM_HEADERS_VALUES_ARRAY = "CUSTOM_HEADERS_VALUES_ARRAY";
    private static final String DATE_TO_STOP_ISO8601 = "DATE_TO_STOP";
    private static final String MAX_RETRIES = "MAX_RETRIES";

    // stored data
    public static final String KEY_WORKER_ID_PREFIX = "worker-id-";
    public static final String KEY_STATUS = "key_status";
    // note: no 'success' status because we delete stored data when succeeds
    public static final String STATUS_STARTED = "status_started";
    public static final String STATUS_STOPPED = "status_stopped";
    public static final String STATUS_FAILED_WILL_RETRY = "status_failed_will_retry";
    public static final String STATUS_FAILED_NO_RETRY = "status_failed_no_retry";
    public static final String KEY_FAILURE_REASON_ENUM = "key_failure_reason";
    public static final String KEY_FAILURE_DETAILS = "key_failure_details";

    private static final int CHUNK_SIZE = 1024;

    public enum FailureReason {
        // important: these enums are sent & retrieved by their ordinal number. DO NOT CHANGE THE ORDER
        ILLEGAL_ARGUMENT,
        UPLOAD_FILE_NOT_FOUND,
        RECOVERABLE_PROTOCOL_ERROR,
        UNRECOVERABLE_PROTOCOL_ERROR,
        IO_ERROR
    }

    public static Data createInputData(@NonNull Uri uploadUri, @NonNull File file, @NonNull Map<String, String> metadata, @NonNull Map<String, String> customHeaders, @NonNull TusAndroidClient.SubmissionPolicy submissionPolicy) {
        Data.Builder builder = new Data.Builder();
        if (submissionPolicy.stopDate != null) {
            builder.putString(TusUploadWorker.DATE_TO_STOP_ISO8601, submissionPolicy.stopDate.toString());
        }
        if (submissionPolicy.maxRetries != null) {
            builder.putInt(TusUploadWorker.MAX_RETRIES, submissionPolicy.maxRetries);
        }
        builder.putString(TusUploadWorker.FILE_UPLOAD_URL, uploadUri.toString());
        builder.putString(TusUploadWorker.PATH_TO_FILE_TO_UPLOAD, file.getPath());
        builder.putStringArray(TusUploadWorker.CUSTOM_HEADERS_KEYS_ARRAY, customHeaders.keySet().toArray(new String[0]));
        builder.putStringArray(TusUploadWorker.CUSTOM_HEADERS_VALUES_ARRAY, customHeaders.values().toArray(new String[0]));
        builder.putStringArray(TusUploadWorker.METADATA_KEYS_ARRAY, metadata.keySet().toArray(new String[0]));
        builder.putStringArray(TusUploadWorker.METADATA_VALUES_ARRAY, metadata.values().toArray(new String[0]));
        return builder.build();
    }

    public TusUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        recordProgress(0);
        BetweenWorkDataStore.get(context).storeData(KEY_WORKER_ID_PREFIX + getId(), Collections.singletonMap(KEY_STATUS, STATUS_STARTED));
    }

    @NonNull
    @Override
    public Result doWork() {
        URL fileUploadUrl;
        try {
            fileUploadUrl = new URL(getInputData().getString(FILE_UPLOAD_URL));
        } catch (MalformedURLException e) {
            return recordWorkAborted(false, FailureReason.ILLEGAL_ARGUMENT, "invalid upload url: " + e.getMessage());
        }

        String fileLocation = getInputData().getString(PATH_TO_FILE_TO_UPLOAD);
        if (fileLocation == null) {
            return recordWorkAborted(false, FailureReason.ILLEGAL_ARGUMENT, "file path must be specified");
        }
        String fileLocationPath = Uri.parse(fileLocation).getPath();
        if (fileLocationPath == null) {
            return recordWorkAborted(false, FailureReason.ILLEGAL_ARGUMENT, "file path must be valid");
        }

        DateTime retryLimit = null;
        String limit = getInputData().getString(DATE_TO_STOP_ISO8601);
        if (limit != null) {
            try {
                retryLimit = DateTime.parse(limit);
            } catch (IllegalArgumentException e) {
                return recordWorkAborted(false, FailureReason.ILLEGAL_ARGUMENT, "malformed retry limit time: " + limit);
            }
        }
        Integer maxRetryCount = null;
        int retries = getInputData().getInt(MAX_RETRIES, -1);
        if (retries != -1) {
            maxRetryCount = retries;
        }

        Map<String, String> uploadMetadata;
        try {
            String[] metadataKeys = getInputData().getStringArray(METADATA_KEYS_ARRAY);
            String[] metadataValues = getInputData().getStringArray(METADATA_VALUES_ARRAY);
            uploadMetadata = reconstructMap(metadataKeys, metadataValues);
        } catch (IllegalArgumentException e) {
            return recordWorkAborted(false, FailureReason.ILLEGAL_ARGUMENT, "malformed metadata: " + e.getMessage());
        }

        Map<String, String> uploadCustomHeaders;
        try {
            String[] headerKeys = getInputData().getStringArray(CUSTOM_HEADERS_KEYS_ARRAY);
            String[] headerValues = getInputData().getStringArray(CUSTOM_HEADERS_VALUES_ARRAY);
            uploadCustomHeaders = reconstructMap(headerKeys, headerValues);
        } catch (IllegalArgumentException e) {
            return recordWorkAborted(false, FailureReason.ILLEGAL_ARGUMENT, "malformed headers: " + e.getMessage());
        }

        TusClient tusClient = new TusClient();
        tusClient.setUploadCreationURL(fileUploadUrl);
        tusClient.enableResuming(new TusPreferencesURLStore(getApplicationContext().getSharedPreferences(TusAndroidClient.TUS_SHARED_PREFS_NAME, Context.MODE_PRIVATE)));
        tusClient.setHeaders(uploadCustomHeaders);

        TusUpload upload;
        try {
            upload = new TusUpload(new File(fileLocationPath));
        } catch (FileNotFoundException e) {
            return recordWorkAborted(false, FailureReason.UPLOAD_FILE_NOT_FOUND, e.getMessage());
        }

        upload.setMetadata(uploadMetadata);

        try {
            TusUploader uploader = tusClient.resumeOrCreateUpload(upload);
            uploader.setChunkSize(CHUNK_SIZE);

            do {
                long totalBytes = upload.getSize();
                long bytesUploaded = uploader.getOffset();
                float progress = (float) bytesUploaded / totalBytes * 100;

                recordProgress(progress);

                if (isStopped()) {
                    uploader.finish();
                    BetweenWorkDataStore.get(getApplicationContext()).storeData(KEY_WORKER_ID_PREFIX + getId(), Collections.singletonMap(KEY_STATUS, STATUS_STOPPED));
                    return null; // result is ignored if we were stopped by the system
                }
            } while (uploader.uploadChunk() > -1);

            uploader.finish();
            BetweenWorkDataStore.get(getApplicationContext()).clearData(KEY_WORKER_ID_PREFIX + getId());
            return Result.success();
        } catch (ProtocolException e) {
            if (e.shouldRetry()) {
                return recordWorkAborted(eligibleToRetry(retryLimit, maxRetryCount), FailureReason.RECOVERABLE_PROTOCOL_ERROR, e.getMessage());
            }
            return recordWorkAborted(false, FailureReason.UNRECOVERABLE_PROTOCOL_ERROR, e.getMessage());
        } catch (IOException e) {
            return recordWorkAborted(eligibleToRetry(retryLimit, maxRetryCount), FailureReason.IO_ERROR, e.getMessage());
        }
    }

    private boolean eligibleToRetry(@Nullable DateTime retryLimit, @Nullable Integer maxRetryCount) {
        return (retryLimit == null || DateTime.now().isBefore(retryLimit)) && (maxRetryCount == null || maxRetryCount > getRunAttemptCount());
    }

    private void recordProgress(float progress) {
        setProgressAsync(new Data.Builder()
                .putDouble(UPLOAD_PROGRESS_DOUBLE, progress).build());
    }

    private Result recordWorkAborted(boolean willRetry, @NonNull FailureReason failureReason, @Nullable String failureDetails) {
        Map<String, String> data = new HashMap<>();
        data.put(KEY_STATUS, willRetry ? STATUS_FAILED_WILL_RETRY : STATUS_FAILED_NO_RETRY);
        data.put(KEY_FAILURE_REASON_ENUM, String.valueOf(failureReason.ordinal()));

        Data.Builder dataBuilder = new Data.Builder()
                .putInt(FAILURE_REASON_ENUM, failureReason.ordinal());

        if (failureDetails != null) {
            data.put(KEY_FAILURE_DETAILS, failureDetails);
            dataBuilder.putString(FAILURE_DETAIL_MESSAGE, failureDetails);
        }

        BetweenWorkDataStore.get(getApplicationContext()).storeData(KEY_WORKER_ID_PREFIX + getId(), data);
        return willRetry ? Result.retry() : Result.failure(dataBuilder.build());
    }

    @NonNull
    private static Map<String, String> reconstructMap(@Nullable String[] keys, @Nullable String[] values) {
        if (keys == null && values == null) {
            return Collections.emptyMap();
        }
        if (keys == null) {
            throw new IllegalArgumentException("missing keys, provided values");
        } else if (values == null) {
            throw new IllegalArgumentException("missing values, provided keys");
        } else if (keys.length != values.length) {
            throw new IllegalArgumentException("array miss-match: " + keys.length + " keys but " + values.length + " values");
        }
        Map<String, String> map = new HashMap<>(keys.length);
        for (int i = 0; i < keys.length; ++i) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}
