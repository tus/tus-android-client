package io.tus.android.client;

import java.net.URL;

public interface TusURLStore {
    void set(String fingerprint, URL url);
    URL get(String fingerprint);
    void remove(String fingerprint);
}
