package io.tus.android.client;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import io.tus.java.client.TusUpload;

public class TusAndroidUpload extends TusUpload {
    public TusAndroidUpload(Uri uri, Activity activity) throws FileNotFoundException {
        Cursor cursor = activity.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE, OpenableColumns.DISPLAY_NAME}, null, null, null);
        if(cursor == null) {
            throw new FileNotFoundException();
        }

        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        String name = cursor.getString(nameIndex);

        setSize(size);
        setInputStream(activity.getContentResolver().openInputStream(uri));

        setFingerprint(String.format("%s-%d", uri.toString(), size));

        Map<String, String> metadata = new HashMap<>();
        metadata.put("filename", name);
        setMetadata(metadata);

        cursor.close();
    }
}
