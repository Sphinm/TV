package com.fongmi.android.tv.ui.custom;

public interface MicCallback {

    void onResults(String result);

    default void onError(String message) {
    }

    default void onEnd() {
    }
}
