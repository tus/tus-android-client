package io.tus.android.client;

import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import io.tus.java.client.TusURLStore;

public class TusPreferencesURLStore implements TusURLStore {
    private final SharedPreferences preferences;

    public TusPreferencesURLStore(@NonNull SharedPreferences preferences) {
        Objects.requireNonNull(preferences, "must specify SharedPreferences");
        this.preferences = preferences;
    }

    public URL get(String fingerprint) {
        // Ignore empty fingerprints
        if(fingerprint == null || fingerprint.isEmpty()) {
            return null;
        }

        String urlStr = preferences.getString(fingerprint, "");

        // No entry was found
        if (urlStr.isEmpty()) {
            return null;
        }

        // Ignore invalid URLs
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            remove(fingerprint);
            return null;
        }
    }

    public void set(String fingerprint, URL url) {
        // Ignore empty fingerprints
        if (url == null || fingerprint == null || fingerprint.isEmpty()) {
            return;
        }
        preferences.edit().putString(fingerprint, url.toString()).apply();
    }

    public void remove(String fingerprint) {
        // Ignore empty fingerprints
        if (fingerprint == null || fingerprint.isEmpty()) {
            return;
        }
        preferences.edit().remove(fingerprint).apply();
    }
}
