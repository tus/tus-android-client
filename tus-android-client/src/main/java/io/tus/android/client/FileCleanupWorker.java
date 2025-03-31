package io.tus.android.client;


import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;

/**
 * This worker deletes the file from internal storage, where it was kept until the upload succeeded.
 */
public class FileCleanupWorker extends Worker {

    // INPUT
    public static final String PATH_TO_FILE_TO_DELETE = "PATH_TO_FILE_TO_DELETE";

    // OUTPUT
    public static final String FAILURE_REASON_ENUM = "FAILURE_REASON_ENUM";

    public enum FailureReason {
        ILLEGAL_ARGUMENT,
        NOT_DELETED
    }

    public FileCleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String fileLocation = getInputData().getString(PATH_TO_FILE_TO_DELETE);
        if (fileLocation == null) {
            return Result.failure(new Data.Builder()
                    .putInt(FAILURE_REASON_ENUM, FailureReason.ILLEGAL_ARGUMENT.ordinal()).build());
        }
        String path = Uri.parse(fileLocation).getPath();
        if (path == null) {
            return Result.failure(new Data.Builder()
                    .putInt(FAILURE_REASON_ENUM, FailureReason.ILLEGAL_ARGUMENT.ordinal()).build());
        }
        if (new File(path).delete()) {
            return Result.success();
        } else {
            return Result.failure(new Data.Builder()
                    .putInt(FAILURE_REASON_ENUM, FailureReason.NOT_DELETED.ordinal()).build());
        }
    }

    public static Data createInputData(@NonNull File file) {
        Data.Builder builder = new Data.Builder();
        builder.putString(FileCleanupWorker.PATH_TO_FILE_TO_DELETE, file.getPath());
        return builder.build();
    }
}