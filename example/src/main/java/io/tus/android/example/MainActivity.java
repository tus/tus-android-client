package io.tus.android.example;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import io.tus.android.client.TusAndroidClient;
import io.tus.android.example.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.toString();
    private ActivityMainBinding binding;
    private TusAndroidClient tusAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // create the upload client - this allows us to submit new uploads and get updates on the status of previously submitted uploads
        // uploads we submitted previously will continue in the background, regardless of whether we initialise the TusAndroidClient next time
        tusAndroidClient = new TusAndroidClient(getApplicationContext(), Uri.parse("https://tusd.tusdemo.net/files/"), new File(getFilesDir().getPath() + "/internal-tus-files-folder/"));
        // get the latest info we have on the status of our uploads. This info may not be available immediately, so pass a callback - this will only be called once
        tusAndroidClient.getPendingUploadInfo(this::updateStatsDisplay);
        // register to receive ongoing updates as the upload status changes
        tusAndroidClient.addPendingUploadChangeListener(this::updateStatsDisplay);
        // register to be notified when individual uploads succeed:
        tusAndroidClient.addUploadSuccessListener(succeededUploadInfo -> Log.e(LOG_TAG, "upload " + succeededUploadInfo.id + " succeeded!"));

        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
            if (!uris.isEmpty()) {
                beginUpload(uris);
            }
        });

        binding.uploadButton.setOnClickListener(v -> {
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                    .build());
        });
    }

    private void updateStatsDisplay(@NonNull TusAndroidClient.UploadStateInfo uploadStateInfo) {
        // UploadStateInfo contains...
        // 1) uploadsSucceeded: all the uploads that have succeeded in the background after the app was last running, plus any uploads that have succeeded this time
        binding.buttonStatsNumSucceeded.setText(getString(R.string.num_succeeded, uploadStateInfo.uploadsSucceeded.size()));
        // 2) uploadsPending: all the uploads (submitted now or in previous times using the app) which have not yet succeeded of permanently failed
        // they may be running, or schedule to run in future
        Pair<Integer, Integer> scheduleAndRunningCount = countScheduledAndRunning(uploadStateInfo.uploadsPending);
        binding.buttonStatsNumScheduled.setText(getString(R.string.num_scheduled, scheduleAndRunningCount.first));
        binding.buttonStatsNumRunning.setText(getString(R.string.num_running, scheduleAndRunningCount.second));
        // 3) uploadsFailed: uploads that have failed permanently in the background after the app was last running, plus any uploads that have permanently failed this time
        // to permanently fail, we either received an unrecoverable error from TUS backend, or exceeded a time or retry limit
        binding.buttonStatsNumPermanentlyFailed.setText(getString(R.string.num_failed, uploadStateInfo.uploadsFailed.size()));

        // For each upload we have some information, including a unique generated id, and the upload metadata
        // In-progress uploads contain their state (SCHEDULED or RUNNING) and progress
        StringBuilder infoDisplay = new StringBuilder();
        for (TusAndroidClient.PendingUploadInfo info : uploadStateInfo.uploadsPending.values()) {
            infoDisplay.append("id: ").append(info.id).append("\n")
                    .append("     state: ").append(info.state).append("\n")
                    .append("     progress: ").append((int) info.progress).append("%\n");
            if (info.mostRecentFailureReasonIfAny != null) {
                infoDisplay.append("     previously failed because: ").append(info.mostRecentFailureDetailsIfAny);
            }
            infoDisplay.append("\n\n");
        }
        binding.buttonStatsAllInfo.setText(uploadStateInfo.uploadsPending.isEmpty() ? getString(R.string.stats_description) : infoDisplay.toString());
        updateProgressBar(uploadStateInfo.uploadsPending);
    }

    private Pair<Integer, Integer> countScheduledAndRunning(Map<String, TusAndroidClient.PendingUploadInfo> pendingUploadsInfo) {
        int scheduledCount = 0;
        int runningCount = 0;

        for (TusAndroidClient.PendingUploadInfo info : pendingUploadsInfo.values()) {
            switch (info.state) {
                case SCHEDULED:
                    ++scheduledCount;
                    break;
                case RUNNING:
                    ++runningCount;
                    break;
            }
        }
        return Pair.create(scheduledCount, runningCount);
    }

    private void updateProgressBar(Map<String, TusAndroidClient.PendingUploadInfo> pendingUploadsInfo) {
        // set the progress bar to represent all the pending uploads
        // e.g if there are 3 uploads pending, 2 are 90% done and one is 20% done
        // the progress bar will show overall we are 66% done
        double progress = 0;
        for (TusAndroidClient.PendingUploadInfo info : pendingUploadsInfo.values()) {
            progress += info.progress;
        }
        if (!pendingUploadsInfo.isEmpty()) {
            progress /= pendingUploadsInfo.size();
        }
        binding.progressBar.setProgress((int) progress);
    }

    private void beginUpload(Collection<Uri> uris) {
        AsyncTask.execute(() -> {
            for (Uri uri : uris) {
                try {
                    // when data is submitted for upload, it is copied into the storage directory specified when you created the TusAndroidClient
                    // it stays there until the upload either succeeds, or fails permanently
                    // we use Android's WorkManager to ensure the upload happens in the background when appropriate conditions (like being connected to the internet) are met
                    String id = tusAndroidClient.submitFileForUpload(uri);
                    Log.d(LOG_TAG, "file submitted, id: " + id);
                } catch (TusAndroidClient.FileSubmissionException e) {
                    // this error could occur if we're unable to copy the file locally
                    showError(e);
                }
            }
        });

    }

    private void showError(Exception e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Internal error");
        builder.setMessage(e.getMessage());
        AlertDialog dialog = builder.create();
        dialog.show();
        Log.e(LOG_TAG, "an error occurred", e);
    }
}
