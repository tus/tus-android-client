package io.tus.android.client;

public class ResumingNotEnabledException extends Exception {
    public ResumingNotEnabledException() {
        super("resuming not enabled for this client. use enableResuming() to do so");
    }
}
