package com.sac.speech;

public interface TextToSpeechCallback {
    void onStart();
    void onCompleted();
    void onError();
}
