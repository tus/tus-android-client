package io.tus.android.client;

public class FingerprintNotFoundException  extends Exception {
    public FingerprintNotFoundException(String fingerprint) {
        super("fingerprint not in storage found: " + fingerprint);
    }
}
