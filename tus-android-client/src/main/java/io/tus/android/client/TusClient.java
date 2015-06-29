package io.tus.android.client;

import android.app.Activity;
import android.content.SharedPreferences;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class TusClient {
    public final static String TUS_VERSION = "1.0.0";

    private URL uploadCreationURL;
    private boolean resumingEnabled;
    private Activity activity;

    public TusClient() {

    }

    public TusClient(URL uploadCreationURL) {
        setUploadCreationURL(uploadCreationURL);
    }

    public void setUploadCreationURL(URL uploadCreationURL) {
        this.uploadCreationURL = uploadCreationURL;
    }

    public URL getUploadCreationURL() {
        return uploadCreationURL;
    }

    public void enableResuming(Activity activity) {
        resumingEnabled = true;
        this.activity = activity;
    }

    public void disableResuming() {
        resumingEnabled = false;
        this.activity = null;
    }

    public TusUploader createUpload(TusUpload upload) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) uploadCreationURL.openConnection();
        connection.setRequestMethod("POST");
        prepareConnection(connection);

        connection.addRequestProperty("Upload-Length", Long.toString(upload.getSize()));
        connection.connect();
        // TODO: error handling
        String urlStr = connection.getHeaderField("Location");
        URL uploadURL = new URL(urlStr);

        storeFingerprint(upload, uploadURL);

        return new TusUploader(this, uploadURL, upload.getInputStream(), 0);
    }

    public TusUploader resumeUpload(TusUpload upload) throws FingerprintNotFoundException, IOException {
        URL uploadURL = readFingerprint(upload);
        if(uploadURL == null) {
            throw new FingerprintNotFoundException(upload.getFingerprint());
        }

        HttpURLConnection connection = (HttpURLConnection) uploadURL.openConnection();
        connection.setRequestMethod("HEAD");
        prepareConnection(connection);

        connection.connect();
        // TODO: error handling (+ remove fingerprint)
        String offsetStr = connection.getHeaderField("Upload-Offset");
        long offset = Long.parseLong(offsetStr);

        return new TusUploader(this, uploadURL, upload.getInputStream(), offset);
    }

    public TusUploader resumeOrCreateUpload(TusUpload upload) throws IOException {
        try {
            return resumeUpload(upload);
        } catch(FingerprintNotFoundException e) {
            return createUpload(upload);
        }
    }

    public void prepareConnection(URLConnection connection) {
        connection.addRequestProperty("Tus-Resumable", TUS_VERSION);
        // TODO: add custom headers
    }

    private void storeFingerprint(TusUpload upload, URL uploadURL) {
        if(!resumingEnabled) {
            return;
        }

        String fingerprint = upload.getFingerprint();
        String url = uploadURL.toString();

        // Ignore empty fingerprints
        if(fingerprint.length() == 0) {
            return;
        }

        SharedPreferences pref = activity.getSharedPreferences("tus", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(fingerprint, url);
        editor.commit();
    }

    private URL readFingerprint(TusUpload upload) {
        if(!resumingEnabled) {
            return null;
        }

        String fingerprint = upload.getFingerprint();

        // Ignore empty fingerprints
        if(fingerprint.length() == 0) {
            return null;
        }

        SharedPreferences pref = activity.getSharedPreferences("tus", 0);
        String urlStr = pref.getString(fingerprint, "");

        // No entry was found
        if(urlStr.length() == 0) {
            return null;
        }

        // Ignore invalid URLs
        try {
            return new URL(urlStr);
        } catch(MalformedURLException e) {
            removeFingerprint(upload);
            return null;
        }

    }

    private void removeFingerprint(TusUpload upload) {
        if(!resumingEnabled) {
            return;
        }

        String fingerprint = upload.getFingerprint();

        // Ignore empty fingerprints
        if(fingerprint.length() == 0) {
            return;
        }

        SharedPreferences pref = activity.getSharedPreferences("tus", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(fingerprint);
        editor.commit();
    }
}
