package io.tus.android.client;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import androidx.work.WorkRequest;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class TusAndroidClient {

    /*package*/ static final String TUS_SHARED_PREFS_NAME = "TUS_SHARED_PREF";

    private static final String TUS_UPLOAD_TAG = "tus-upload";
    private static final String TUS_UPLOAD_ID_TAG_PREFIX = "upload-id:";
    private static final String DELETE_FILE_TAG = "delete-file";
    private static final int CHUNK_SIZE = 1024;
    private static final String LOG_TAG = TusAndroidClient.class.toString();

    private final Context mContext;
    private final Uri mUploadUri;
    private final File mStorageDirectory;

    private final Object mWorkInfoUpdateLock = new Object();
    @Nullable // null before we get the info on our pending workers for the first time
    private UploadStateInfo mLastUpdatedUploadState;
    private final Collection<UploadInfoChangedCallback> mCallbacksAwaitingOneTimeInfo = new CopyOnWriteArrayList<>();
    private final Collection<UploadInfoChangedCallback> mCallbacksAlwaysAwaitingInfo = new CopyOnWriteArrayList<>();
    private final Collection<UploadSucceededCallback> mCallbacksAlwaysAwaitingSuccess = new CopyOnWriteArrayList<>();
    private final Collection<UploadFailedPermanentlyCallback> mCallbacksAlwaysAwaitingFailure = new CopyOnWriteArrayList<>();


    public static class UploadStateInfo {
        @NonNull
        public final Map<String, SucceededUploadInfo> uploadsSucceeded;
        @NonNull
        public final Map<String, PendingUploadInfo> uploadsPending;
        @NonNull
        public final Map<String, PermanentlyFailedUploadInfo> uploadsFailed;

        private UploadStateInfo(@NonNull Map<String, SucceededUploadInfo> uploadsSucceeded, @NonNull Map<String, PendingUploadInfo> uploadsPending, @NonNull Map<String, PermanentlyFailedUploadInfo> uploadsFailed) {
            this.uploadsSucceeded = Collections.unmodifiableMap(uploadsSucceeded);
            this.uploadsPending = Collections.unmodifiableMap(uploadsPending);
            this.uploadsFailed = Collections.unmodifiableMap(uploadsFailed);
        }
    }

    public static abstract class UploadInfo {
        @NonNull
        public final String id;
        @NonNull
        public final Map<String, String> metadata;

        protected UploadInfo(@NonNull String id, @NonNull Map<String, String> metadata) {
            this.id = id;
            this.metadata = Collections.unmodifiableMap(metadata);
        }
    }

    public static class SucceededUploadInfo extends UploadInfo {

        protected SucceededUploadInfo(@NonNull String id, @NonNull Map<String, String> metadata) {
            super(id, metadata);
        }
    }

    public static class PendingUploadInfo extends UploadInfo {
        public enum State {
            SCHEDULED, RUNNING
        }

        @NonNull
        public final State state;
        public final double progress;
        @Nullable
        public final TusUploadWorker.FailureReason mostRecentFailureReasonIfAny;
        @Nullable
        public final String mostRecentFailureDetailsIfAny;

        private PendingUploadInfo(@NonNull String id, @NonNull State state, @NonNull Map<String, String> metadata, double progress, @Nullable TusUploadWorker.FailureReason mostRecentFailureReasonIfAny, @Nullable String mostRecentFailureDetailsIfAny) {
            super(id, metadata);
            this.state = state;
            this.progress = progress;
            this.mostRecentFailureReasonIfAny = mostRecentFailureReasonIfAny;
            this.mostRecentFailureDetailsIfAny = mostRecentFailureDetailsIfAny;
        }
    }

    public static class PermanentlyFailedUploadInfo extends UploadInfo{

        @Nullable
        public final TusUploadWorker.FailureReason mostRecentFailureReason;
        @Nullable
        public final String mostRecentFailureDetails;

        private PermanentlyFailedUploadInfo(@NonNull String id, @NonNull Map<String, String> metadata, @Nullable TusUploadWorker.FailureReason mostRecentFailureReason, @Nullable String mostRecentFailureDetails) {
            super(id, metadata);
            this.mostRecentFailureReason = mostRecentFailureReason;
            this.mostRecentFailureDetails = mostRecentFailureDetails;
        }
    }

    public interface UploadInfoChangedCallback {
        void onUpdatedUploadInfoAvailable(@NonNull UploadStateInfo uploadStateInfo);
    }

    public interface UploadSucceededCallback {
        void onUploadSucceeded(@NonNull SucceededUploadInfo succeededUploadInfo);
    }

    public interface UploadFailedPermanentlyCallback {
        void onUploadFailed(@NonNull PermanentlyFailedUploadInfo failedUploadInfo);
    }

    public TusAndroidClient(@NonNull Context applicationContext, @NonNull Uri uploadUrl, @NonNull File storageDirectory) {
        Objects.requireNonNull(applicationContext, "application context is required");
        Objects.requireNonNull(uploadUrl, "server upload url is required");
        Objects.requireNonNull(storageDirectory, "storage directory is required");

        mContext = applicationContext.getApplicationContext();
        mUploadUri = uploadUrl;
        mStorageDirectory = storageDirectory;

        LiveData<List<WorkInfo>> pendingUploads = WorkManager.getInstance(mContext).getWorkInfosLiveData(WorkQuery.Builder
                .fromTags(Collections.singletonList(TUS_UPLOAD_TAG)).addStates(Arrays.asList(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.FAILED, WorkInfo.State.CANCELLED, WorkInfo.State.SUCCEEDED)).build());
        pendingUploads.observeForever(workInfos -> {
            UploadStateInfo oldState = mLastUpdatedUploadState;
            Collection<String> idsToRemoveFromDataStore;
            synchronized (mWorkInfoUpdateLock) {
                idsToRemoveFromDataStore = updateStateForWorkInfos(workInfos);
            }
            BetweenWorkDataStore.get(mContext).clearData(idsToRemoveFromDataStore);

            notifyListenersAsAppropriate(oldState, Objects.requireNonNull(mLastUpdatedUploadState));
        });
    }

    private void notifyListenersAsAppropriate(@Nullable UploadStateInfo oldState, @NonNull UploadStateInfo newState) {
        notifyOneTimeUpdateListeners(newState);
        notifyUpdateListeners(newState);
        if (oldState != null) {
            if (newState.uploadsSucceeded.size() > oldState.uploadsSucceeded.size()) {
                for (SucceededUploadInfo entry : newState.uploadsSucceeded.values()) {
                    if (!oldState.uploadsSucceeded.containsKey(entry.id)) {
                        notifySuccessListeners(entry);
                    }
                }
            }
            if (newState.uploadsFailed.size() > oldState.uploadsFailed.size()) {
                for (PermanentlyFailedUploadInfo entry : newState.uploadsFailed.values()) {
                    if (!oldState.uploadsFailed.containsKey(entry.id)) {
                        notifyPermanentFailureListeners(entry);
                    }
                }
            }
        }
    }

    private void notifyOneTimeUpdateListeners(@NonNull UploadStateInfo newState) {
        for (UploadInfoChangedCallback callback : mCallbacksAwaitingOneTimeInfo) {
            callback.onUpdatedUploadInfoAvailable(newState);
        }
        mCallbacksAwaitingOneTimeInfo.clear();
    }

    private void notifyUpdateListeners(@NonNull UploadStateInfo newState) {
        for (UploadInfoChangedCallback callback : mCallbacksAlwaysAwaitingInfo) {
            callback.onUpdatedUploadInfoAvailable(newState);
        }
    }

    private void notifySuccessListeners(@NonNull SucceededUploadInfo succeededUploadInfo) {
        for (UploadSucceededCallback callback : mCallbacksAlwaysAwaitingSuccess) {
            callback.onUploadSucceeded(succeededUploadInfo);
        }
    }

    private void notifyPermanentFailureListeners(@NonNull PermanentlyFailedUploadInfo failedUploadInfo) {
        for (UploadFailedPermanentlyCallback callback : mCallbacksAlwaysAwaitingFailure) {
            callback.onUploadFailed(failedUploadInfo);
        }
    }

    private Collection<String> updateStateForWorkInfos(@NonNull List<WorkInfo> workInfos) {
        // To our succeededUploads and permanentlyFailedUploads we add entries - existing entries will
        // exist in memory in this client. so we know that jobs 1, 3 and 5 succeeded this time,
        // even after they cease to be in the list of work infos
        // but they will not be persisted outside the life of the application (otherwise we'd run out of space)
        // next time we launch, jobs 1, 3 and 5 are forgotten
        Map<String, SucceededUploadInfo> succeededUploads = new HashMap<>();
        if (mLastUpdatedUploadState != null) {
            succeededUploads.putAll(mLastUpdatedUploadState.uploadsSucceeded);
        }
        Map<String, PermanentlyFailedUploadInfo> permanentlyFailedUploads = new HashMap<>();
        if (mLastUpdatedUploadState != null) {
            permanentlyFailedUploads.putAll(mLastUpdatedUploadState.uploadsFailed);
        }

        // for the pending uploads, the latest list of workInfos from WorkManager is always the source of truth, we will replace them each time
        Map<String, PendingUploadInfo> currentlyPendingUploads = new HashMap<>();

        // as we process the work infos, we will keep track of the ids of jobs that have succeeded of failed permanently.
        // When done we'll remove stored info for these jobs, to free memory space
        Collection<String> succeededOrPermanentlyFailedUploadIds = new ArrayList<>();

        for (WorkInfo workInfo : workInfos) {
            UploadInfo uploadInfo = fromWorkInfo(workInfo);
            if (uploadInfo != null) {
                if (uploadInfo instanceof SucceededUploadInfo) {
                    succeededUploads.put(uploadInfo.id, (SucceededUploadInfo) uploadInfo);
                    // keep track of the ids of completed jobs, we will delete them later
                    succeededOrPermanentlyFailedUploadIds.add(uploadInfo.id);
                } else if (uploadInfo instanceof PendingUploadInfo) {
                    currentlyPendingUploads.put(uploadInfo.id, (PendingUploadInfo) uploadInfo);
                } else if (uploadInfo instanceof PermanentlyFailedUploadInfo) {
                    permanentlyFailedUploads.put(uploadInfo.id, (PermanentlyFailedUploadInfo) uploadInfo);
                    // keep track of the ids of completed jobs, we will delete them later
                    succeededOrPermanentlyFailedUploadIds.add(uploadInfo.id);
                }
            }
        }
        mLastUpdatedUploadState = new UploadStateInfo(succeededUploads, currentlyPendingUploads, permanentlyFailedUploads);

        return succeededOrPermanentlyFailedUploadIds;
    }

    @Nullable
    // null if the work info is missing what we need, or we are in a bad state with no auxiliary data stored
    private UploadInfo fromWorkInfo(@NonNull WorkInfo workInfo) {
        String id = idFromWorkInfo(workInfo);
        if (id == null) {
            Log.e(LOG_TAG, "ignoring worker with bad state, missing id tag " + workInfo.getId());
            return null;
        }

        Map<String, String> metadata = BetweenWorkDataStore.get(mContext).getData(id);
        if (metadata == null) {
            // ignoring worker with bad state: stored metadata is missing or malformed.
            // this happens often when the succeeded work info continues to be returned from the
            // work info query after we have cleared its data
            return null;
        }

        Double progress = workInfo.getProgress().getDouble(TusUploadWorker.UPLOAD_PROGRESS_DOUBLE, 0);

        TusUploadWorker.FailureReason failureReason = null;
        String failureDetails = null;
        Map<String, String> data = BetweenWorkDataStore.get(mContext).getData(TusUploadWorker.KEY_WORKER_ID_PREFIX + workInfo.getId());
        if (data != null && data.containsKey(TusUploadWorker.KEY_STATUS)) {
            if (TusUploadWorker.STATUS_FAILED_NO_RETRY.equals(data.get(TusUploadWorker.KEY_STATUS)) || TusUploadWorker.STATUS_FAILED_WILL_RETRY.equals(data.get(TusUploadWorker.KEY_STATUS))) {
                String reasonEnumOrdinal = data.get(TusUploadWorker.KEY_FAILURE_REASON_ENUM);
                if (reasonEnumOrdinal == null) {
                    Log.e(LOG_TAG, "worker with bad state: stored data is missing or malformed. id:" + workInfo.getId());
                    return null;
                }
                failureReason = TusUploadWorker.FailureReason.values()[Integer.parseInt(reasonEnumOrdinal)];
                failureDetails = data.get(TusUploadWorker.KEY_FAILURE_DETAILS);
            }
        }

        switch (workInfo.getState()) {
            case ENQUEUED:
                return new PendingUploadInfo(id, PendingUploadInfo.State.SCHEDULED, metadata, progress, failureReason, failureDetails);
            case RUNNING:
                return new PendingUploadInfo(id, PendingUploadInfo.State.RUNNING, metadata, progress, failureReason, failureDetails);
            case SUCCEEDED:
                return new SucceededUploadInfo(id, metadata);
            case FAILED:
                return new PermanentlyFailedUploadInfo(id, metadata, failureReason, failureDetails);
            case BLOCKED:
                // unexpected, should never happen as we filter out this state
                // (and because the upload work is never blocked by any other work)
                Log.e(LOG_TAG, "worker in unexpected state: " + workInfo.getId() + " state: " + workInfo.getState());
                return null;
            case CANCELLED:
                // unexpected, should never happen as we do not cancel workers
                Log.e(LOG_TAG, "worker in unexpected state: " + workInfo.getId() + " state: " + workInfo.getState());
                return null;
            default:
                // unexpected, covered all states above
                Log.e(LOG_TAG, "worker in unexpected state: " + workInfo.getId() + " state: " + workInfo.getState());
                return null;
        }
    }

    @Nullable // null if the WorkInfo was not created correctly for us to get our id
    private static String idFromWorkInfo(@NonNull WorkInfo workInfo) {
        for (String tag : workInfo.getTags()) {
            if (tag.startsWith(TUS_UPLOAD_ID_TAG_PREFIX)) {
                return tag.substring(TUS_UPLOAD_ID_TAG_PREFIX.length());
            }
        }
        return null;
    }

    public void getPendingUploadInfo(@NonNull UploadInfoChangedCallback callback) {
        Objects.requireNonNull(callback, "info may not be available immediately, must specify callback");

        boolean notifyInstead = true;
        synchronized (mWorkInfoUpdateLock) {
            if (mLastUpdatedUploadState == null) {
                mCallbacksAwaitingOneTimeInfo.add(callback);
                notifyInstead = false;
            }
        }

        if (notifyInstead) {
            callback.onUpdatedUploadInfoAvailable(mLastUpdatedUploadState);
        }
    }

    /**
     * @param callback to receive updates about changes to scheduled or in progress uploads. This will be called immediately if we have any information about the current state of pending uploads.
     */
    public void addPendingUploadChangeListener(@NonNull UploadInfoChangedCallback callback) {
        Objects.requireNonNull(callback, "must specify callback");
        if (mLastUpdatedUploadState != null) {
            callback.onUpdatedUploadInfoAvailable(mLastUpdatedUploadState);
        }
        mCallbacksAlwaysAwaitingInfo.add(callback);
    }

    public void removePendingUploadChangeListener(@NonNull UploadInfoChangedCallback callback) {
        Objects.requireNonNull(callback, "must specify callback");
        mCallbacksAlwaysAwaitingInfo.remove(callback);
    }

    /**
     * @param callback will be notified of the next and all subsequent successful uploads, but not of anything that succeeded previously.
     */
    public void addUploadSuccessListener(@NonNull UploadSucceededCallback callback) {
        Objects.requireNonNull(callback, "must specify callback");
        mCallbacksAlwaysAwaitingSuccess.add(callback);
    }

    public void removeUploadSuccessListener(@NonNull UploadSucceededCallback callback) {
        Objects.requireNonNull(callback, "must specify callback");
        mCallbacksAlwaysAwaitingSuccess.remove(callback);
    }

    /**
     * @param callback will be notified of the next and all subsequent permanently failed uploads, but not of anything that failed previously.
     */
    public void addUploadFailureListener(@NonNull UploadFailedPermanentlyCallback callback) {
        Objects.requireNonNull(callback, "must specify callback");
        mCallbacksAlwaysAwaitingFailure.add(callback);
    }

    public void removeUploadFailureListener(@NonNull UploadFailedPermanentlyCallback callback) {
        Objects.requireNonNull(callback, "must specify callback");
        mCallbacksAlwaysAwaitingFailure.remove(callback);
    }

    /**
     * Submits the file for upload with the default submission policy (any network, retries for 48 hrs, exponential backoff).
     * File is copied and stored locally until the upload succeeds.
     * @throws FileSubmissionException if there is a problem copying or storing the file
     */
    @WorkerThread
    public String submitFileForUpload(@NonNull Uri fileUri) throws FileSubmissionException {
        return submitFileForUpload(fileUri, null, null, null);
    }

    /**
     * Submits the file for upload. File is copied and stored locally until the upload succeeds.
     * @param submissionPolicy controls number of retries, retry interval, and whether we limit uploads to unmetered networks
     * @throws FileSubmissionException if there is a problem copying or storing the file
     */
    @WorkerThread
    public String submitFileForUpload(@NonNull Uri fileUri, @Nullable SubmissionPolicy submissionPolicy) throws FileSubmissionException {
        return submitFileForUpload(fileUri, null, null, submissionPolicy);
    }

    /**
     * Submits the file for upload. File is copied and stored locally until the upload succeeds.
     * @param metadata to accompany the upload
     * @param customHeaders to accompany the upload
     * @param submissionPolicy controls number of retries, retry interval, and whether we limit uploads to unmetered networks
     * @throws FileSubmissionException if there is a problem copying or storing the file
     */
    @WorkerThread
    public String submitFileForUpload(@NonNull Uri fileUri, @Nullable Map<String, String> metadata, @Nullable Map<String, String> customHeaders, @Nullable SubmissionPolicy submissionPolicy) throws FileSubmissionException {
        Objects.requireNonNull(fileUri, "file uri is required");
        InputStream fileInputStream;
        try {
            fileInputStream = mContext.getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            throw new FileSubmissionException("unable to copy file, file not found", e);
        }
        return submitFileForUpload(fileInputStream, metadata, customHeaders, submissionPolicy);
    }

    /**
     * Submits data in the given input stream for upload using the default submission policy (any network, retries for 48 hrs, exponential backoff). Data is copied and stored locally until the upload succeeds.
     * @throws FileSubmissionException if there is a problem copying or storing the file
     */
    @WorkerThread
    public String submitFileForUpload(@NonNull InputStream fileInput) throws FileSubmissionException {
        return submitFileForUpload(fileInput, null, null, null);
    }

    /**
     * Submits data in the given input stream for upload. Data is copied and stored locally until the upload succeeds.
     * @param submissionPolicy controls number of retries, retry interval, and whether we limit uploads to unmetered networks
     * @throws FileSubmissionException if there is a problem copying or storing the file
     */
    @WorkerThread
    public String submitFileForUpload(@NonNull InputStream fileInput, @Nullable SubmissionPolicy submissionPolicy) throws FileSubmissionException {
        return submitFileForUpload(fileInput, null, null, submissionPolicy);
    }

    /**
     * Submits data in the given input stream for upload. Data is copied and stored locally until the upload succeeds.
     * @param metadata to accompany the upload
     * @param customHeaders to accompany the upload
     * @param submissionPolicy controls number of retries, retry interval, and whether we limit uploads to unmetered networks
     * @throws FileSubmissionException if there is a problem copying or storing the file
     */
    @WorkerThread
    public String submitFileForUpload(@NonNull InputStream fileInput, @Nullable Map<String, String> metadata, @Nullable Map<String, String> customHeaders, @Nullable SubmissionPolicy submissionPolicy) throws FileSubmissionException {
        Objects.requireNonNull(fileInput, "input stream is required");
        metadata = metadata == null ? Collections.emptyMap() : metadata;
        customHeaders = customHeaders == null ? Collections.emptyMap() : customHeaders;
        submissionPolicy = submissionPolicy == null ? new SubmissionPolicy.Builder().build() : submissionPolicy;

        try {
            if (!mStorageDirectory.exists()) {
                try {
                    if (!mStorageDirectory.mkdirs()) {
                        throw new FileSubmissionException("unable to create storage directory");
                    }
                } catch (SecurityException e) {
                    throw new FileSubmissionException("no permission to access storage directory", e);
                }
            }
            String fileName = UUID.randomUUID().toString();
            File file = new File(mStorageDirectory, fileName);
            try (OutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * CHUNK_SIZE];
                int read;
                while ((read = fileInput.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                output.flush();
            }

            // we have it now at file, try to upload it
            return submitForUpload(fileName, file, metadata, customHeaders, submissionPolicy);
        } catch (IOException e) {
            throw new FileSubmissionException("unable to copy file", e);
        } finally {
            try {
                fileInput.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String submitForUpload(@NonNull String uniqueId, @NonNull File file, @NonNull Map<String, String> metadata, @NonNull Map<String, String> customHeaders, @NonNull SubmissionPolicy submissionPolicy) {
        Constraints uploadTaskConstraints = new Constraints.Builder()
                .setRequiredNetworkType(submissionPolicy.uploadCriteria.equals(SubmissionPolicy.UploadCriteria.UNMETERED_ONLY) ? NetworkType.UNMETERED : NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest uploadFile =
                new OneTimeWorkRequest.Builder(TusUploadWorker.class)
                        .setInputData(TusUploadWorker.createInputData(mUploadUri, file, metadata, customHeaders, submissionPolicy))
                        .setConstraints(uploadTaskConstraints)
                        .addTag(TUS_UPLOAD_TAG)
                        .addTag(TUS_UPLOAD_ID_TAG_PREFIX + uniqueId)
                        .build();

        OneTimeWorkRequest deleteFile =
                new OneTimeWorkRequest.Builder(FileCleanupWorker.class)
                        .setInputData(FileCleanupWorker.createInputData(file))
                        .addTag(DELETE_FILE_TAG)
                        .build();

        BetweenWorkDataStore.get(mContext).storeData(uniqueId, metadata);
        WorkManager.getInstance(mContext).beginUniqueWork(uniqueId, ExistingWorkPolicy.REPLACE, uploadFile).then(deleteFile).enqueue();
        return uniqueId;
    }

    public static class FileSubmissionException extends Exception {
        public FileSubmissionException(String message) {
            super(message);
        }

        public FileSubmissionException(Exception cause) {
            super(cause);
        }

        public FileSubmissionException(String message, Exception cause) {
            super(message, cause);
        }
    }

    public static class SubmissionPolicy {
        public enum UploadCriteria {
            ANY_NETWORK,
            UNMETERED_ONLY
        }

        public enum BackoffType {
            LINEAR,
            EXPONENTIAL
        }


        public final UploadCriteria uploadCriteria;
        public final long backoffIntervalMillis;
        public final BackoffType backoffType;
        @Nullable // null indicates no stop-by date
        public final DateTime stopDate;
        @Nullable // null indicates no cap on retries
        public final Integer maxRetries;

        public static class Builder {
            private UploadCriteria mUploadCriteria = UploadCriteria.ANY_NETWORK;
            private long mBackoffIntervalMillis = WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS;
            private BackoffType mBackoffType = BackoffType.EXPONENTIAL;
            public DateTime mStopDate = DateTime.now().plusHours(48);
            public Integer mMaxRetries = null;

            public Builder withUploadCriteria(@NonNull UploadCriteria uploadCriteria) {
                mUploadCriteria = uploadCriteria;
                return this;
            }

            public Builder withBackoffCriteria(int intervalMillis, @NonNull BackoffType type) {
                if (intervalMillis <= 0) {
                    throw new IllegalArgumentException("interval must be > 0");
                }
                mBackoffIntervalMillis = intervalMillis;
                mBackoffType = type;
                return this;
            }

            public Builder withInfiniteRetries() {
                mStopDate = null;
                mMaxRetries = null;
                return this;
            }

            public Builder withMaximumRetries(int maximumRetries) {
                mStopDate = null;
                mMaxRetries = maximumRetries;
                return this;
            }

            public Builder withStopDate(@NonNull DateTime stopDate) {
                mStopDate = stopDate;
                mMaxRetries = null;
                return this;
            }

            public SubmissionPolicy build() {
                return new SubmissionPolicy(this);
            }
        }

        private SubmissionPolicy(@NonNull Builder builder) {
            uploadCriteria = builder.mUploadCriteria;
            backoffIntervalMillis = builder.mBackoffIntervalMillis;
            backoffType = builder.mBackoffType;
            maxRetries = builder.mMaxRetries;
            stopDate = builder.mStopDate;
        }
    }
}