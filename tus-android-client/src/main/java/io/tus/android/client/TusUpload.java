package io.tus.android.client;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class TusUpload {
    private long size;
    private InputStream input;
    private String fingerprint;
    // TODO: Implement metadata header
    private Map<String, String> metadata;

    public TusUpload() {
    }

    public TusUpload(File file) throws FileNotFoundException {
        size = file.length();
        input = new FileInputStream(file);

        fingerprint = String.format("%s-%d", file.getAbsolutePath(), size);
    }

    public TusUpload(Uri uri, Activity activity) throws FileNotFoundException {
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        size = cursor.getLong(sizeIndex);
        input = activity.getContentResolver().openInputStream(uri);

        fingerprint = String.format("%s-%d", uri.toString(), size);

        cursor.close();
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public InputStream getInputStream() {
        return input;
    }

    public void setInputStream(InputStream inputStream) {
        input = inputStream;
    }
}
