package io.tus.android.client;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.FileNotFoundException;

import io.tus.java.client.TusUpload;

public class TusAndroidUpload extends TusUpload {
    public TusAndroidUpload(Uri uri, Activity activity) throws FileNotFoundException {
        Cursor cursor = activity.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);

        setSize(size);
        setInputStream(activity.getContentResolver().openInputStream(uri));

        setFingerprint(String.format("%s-%d", uri.toString(), size));

        cursor.close();
    }
}
