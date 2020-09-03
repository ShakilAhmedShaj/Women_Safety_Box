package com.sac.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.sac.speech.ui.SpeechProgressView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Speech {

    private static final String LOG_TAG = Speech.class.getSimpleName();

    private static Speech instance = null;

    private SpeechRecognizer mSpeechRecognizer;
    private SpeechProgressView mProgressView;
    private String mCallingPackage;
    private boolean mPreferOffline = false;
    private boolean mGetPartialResults = true;
    private SpeechDelegate mDelegate;
    private boolean mIsListening = false;

    private final List<String> mPartialData = new ArrayList<>();
    private String mUnstableData;

    private DelayedOperation mDelayedStopListening;
    private Context mContext;

    private TextToSpeech mTextToSpeech;
    private final Map<String, TextToSpeechCallback> mTtsCallbacks = new HashMap<>();
    private Locale mLocale = Locale.getDefault();
    private float mTtsRate = 1.0f;
    private float mTtsPitch = 1.0f;
    private int mTtsQueueMode = TextToSpeech.QUEUE_FLUSH;
    private long mStopListeningDelayInMs = 10000;
    private long mTransitionMinimumDelay = 1200;
    private long mLastActionTimestamp;
    private List<String> mLastPartialResults = null;

    private final TextToSpeech.OnInitListener mTttsInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(final int status) {
            switch (status) {
                case TextToSpeech.SUCCESS:
                    Logger.info(LOG_TAG, "TextToSpeech engine successfully started");
                    break;

                case TextToSpeech.ERROR:
                    Logger.error(LOG_TAG, "Error while initializing TextToSpeech engine!");
                    break;

                default:
                    Logger.error(LOG_TAG, "Unknown TextToSpeech status: " + status);
                    break;
            }
        }
    };

    private UtteranceProgressListener mTtsProgressListener;

    private final RecognitionListener mListener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(final Bundle bundle) {
            mPartialData.clear();
            mUnstableData = null;
        }

        @Override
        public void onBeginningOfSpeech() {
            if (mProgressView != null)
                mProgressView.onBeginningOfSpeech();

            mDelayedStopListening.start(new DelayedOperation.Operation() {
                @Override
                public void onDelayedOperation() {
                    returnPartialResultsAndRecreateSpeechRecognizer();
                    Log.d("ReachedStop", "Stoppong");
                    //  mListenerDelay.onClick("1");
                }

                @Override
                public boolean shouldExecuteDelayedOperation() {
                    return true;
                }
            });
        }

        @Override
        public void onRmsChanged(final float v) {
            try {
                if (mDelegate != null)
                    mDelegate.onSpeechRmsChanged(v);
            } catch (final Throwable exc) {
                Logger.error(Speech.class.getSimpleName(),
                        "Unhandled exception in delegate onSpeechRmsChanged", exc);
            }

            if (mProgressView != null)
                mProgressView.onRmsChanged(v);
        }

        @Override
        public void onPartialResults(final Bundle bundle) {
            mDelayedStopListening.resetTimer();

            final List<String> partialResults = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            final List<String> unstableData = bundle.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");

            if (partialResults != null && !partialResults.isEmpty()) {
                mPartialData.clear();
                mPartialData.addAll(partialResults);
                mUnstableData = unstableData != null && !unstableData.isEmpty()
                        ? unstableData.get(0) : null;
                try {
                    if (mLastPartialResults == null || !mLastPartialResults.equals(partialResults)) {
                        if (mDelegate != null)
                            mDelegate.onSpeechPartialResults(partialResults);
                        mLastPartialResults = partialResults;
                    }
                } catch (final Throwable exc) {
                    Logger.error(Speech.class.getSimpleName(),
                            "Unhandled exception in delegate onSpeechPartialResults", exc);
                }
            }
        }

        @Override
        public void onResults(final Bundle bundle) {
            mDelayedStopListening.cancel();

            final List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            final String result;

            if (results != null && !results.isEmpty()
                    && results.get(0) != null && !results.get(0).isEmpty()) {
                result = results.get(0);
            } else {
                Logger.info(Speech.class.getSimpleName(), "No speech results, getting partial");
                result = getPartialResultsAsString();
            }

            mIsListening = false;

            try {
                if (mDelegate != null)
                    mDelegate.onSpeechResult(result.trim());
            } catch (final Throwable exc) {
                Logger.error(Speech.class.getSimpleName(),
                        "Unhandled exception in delegate onSpeechResult", exc);
            }

            if (mProgressView != null)
                mProgressView.onResultOrOnError();

            initSpeechRecognizer(mContext);
        }

        @Override
        public void onError(final int code) {
            Logger.error(LOG_TAG, "Speech recognition error", new SpeechRecognitionException(code));
            returnPartialResultsAndRecreateSpeechRecognizer();
        }

        @Override
        public void onBufferReceived(final byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            if (mProgressView != null)
                mProgressView.onEndOfSpeech();
        }

        @Override
        public void onEvent(final int i, final Bundle bundle) {

        }
    };

    private Speech(final Context context) {
        initSpeechRecognizer(context);
        initTts(context);
    }

    private Speech(final Context context, final String callingPackage) {
        initSpeechRecognizer(context);
        initTts(context);
        mCallingPackage = callingPackage;
    }

    private void initSpeechRecognizer(final Context context) {
        if (context == null)
            throw new IllegalArgumentException("context must be defined!");

        mContext = context;

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            if (mSpeechRecognizer != null) {
                try {
                    mSpeechRecognizer.destroy();
                } catch (final Throwable exc) {
                    Logger.debug(Speech.class.getSimpleName(),
                            "Non-Fatal error while destroying speech. " + exc.getMessage());
                } finally {
                    mSpeechRecognizer = null;
                }
            }

            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            mSpeechRecognizer.setRecognitionListener(mListener);
            initDelayedStopListening(context);

        } else {
            mSpeechRecognizer = null;
        }

        mPartialData.clear();
        mUnstableData = null;
    }

    private void initTts(final Context context) {
        if (mTextToSpeech == null) {
            mTtsProgressListener = new TtsProgressListener(mContext, mTtsCallbacks);
            mTextToSpeech = new TextToSpeech(context.getApplicationContext(), mTttsInitListener);
            mTextToSpeech.setOnUtteranceProgressListener(mTtsProgressListener);
            mTextToSpeech.setLanguage(mLocale);
            mTextToSpeech.setPitch(mTtsPitch);
            mTextToSpeech.setSpeechRate(mTtsRate);
        }
    }

    private void initDelayedStopListening(final Context context) {
        if (mDelayedStopListening != null) {
            mDelayedStopListening.cancel();
            mDelayedStopListening = null;
        }
//        Toast.makeText(context, "destroyed", Toast.LENGTH_SHORT).show();
        if (mListenerDelay != null) {
            mListenerDelay.onSpecifiedCommandPronounced("1");
        }
        mDelayedStopListening = new DelayedOperation(context, "delayStopListening", mStopListeningDelayInMs);
    }


    public static Speech init(final Context context) {
        if (instance == null) {
            instance = new Speech(context);
        }

        return instance;
    }


    public static Speech init(final Context context, final String callingPackage) {
        if (instance == null) {
            instance = new Speech(context, callingPackage);
        }

        return instance;
    }


    public synchronized void shutdown() {
        if (mSpeechRecognizer != null) {
            try {
                mSpeechRecognizer.stopListening();
            } catch (final Exception exc) {
                Logger.error(getClass().getSimpleName(), "Warning while de-initing speech recognizer", exc);
            }
        }

        if (mTextToSpeech != null) {
            try {
                mTtsCallbacks.clear();
                mTextToSpeech.stop();
                mTextToSpeech.shutdown();
            } catch (final Exception exc) {
                Logger.error(getClass().getSimpleName(), "Warning while de-initing text to speech", exc);
            }
        }

        unregisterDelegate();
        instance = null;
    }


    public static Speech getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Speech recognition has not been initialized! call init method first!");
        }

        return instance;
    }


    public void startListening(final SpeechDelegate delegate)
            throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException {
        startListening(null, delegate);
    }


    public void startListening(final SpeechProgressView progressView, final SpeechDelegate delegate)
            throws SpeechRecognitionNotAvailable, GoogleVoiceTypingDisabledException {
        if (mIsListening) return;

        if (mSpeechRecognizer == null)
            throw new SpeechRecognitionNotAvailable();

        if (delegate == null)
            throw new IllegalArgumentException("delegate must be defined!");

        if (throttleAction()) {
            Logger.debug(getClass().getSimpleName(), "Hey man calm down! Throttling start to prevent disaster!");
            return;
        }

        mDelegate = delegate;

        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, mGetPartialResults)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, mLocale.getLanguage())
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        if (mCallingPackage != null && !mCallingPackage.isEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mCallingPackage);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, mPreferOffline);
        }

        try {
            mSpeechRecognizer.startListening(intent);
        } catch (final SecurityException exc) {
            throw new GoogleVoiceTypingDisabledException();
        }

        mIsListening = true;
        updateLastActionTimestamp();

        try {
            if (mDelegate != null)
                mDelegate.onStartOfSpeech();
        } catch (final Throwable exc) {
            Logger.error(Speech.class.getSimpleName(),
                    "Unhandled exception in delegate onStartOfSpeech", exc);
        }

    }

    private void unregisterDelegate() {
        mDelegate = null;
        mProgressView = null;
    }

    private void updateLastActionTimestamp() {
        mLastActionTimestamp = new Date().getTime();
    }

    private boolean throttleAction() {
        return (new Date().getTime() <= (mLastActionTimestamp + mTransitionMinimumDelay));
    }


    public void stopListening() {
        if (!mIsListening) return;

        if (throttleAction()) {
            Logger.debug(getClass().getSimpleName(), "Hey man calm down! Throttling stop to prevent disaster!");
            return;
        }

        mIsListening = false;
        updateLastActionTimestamp();
        returnPartialResultsAndRecreateSpeechRecognizer();
    }

    private String getPartialResultsAsString() {
        final StringBuilder out = new StringBuilder("");

        for (final String partial : mPartialData) {
            out.append(partial).append(" ");
        }

        if (mUnstableData != null && !mUnstableData.isEmpty())
            out.append(mUnstableData);

        return out.toString().trim();
    }

    private void returnPartialResultsAndRecreateSpeechRecognizer() {
        mIsListening = false;
        try {
            if (mDelegate != null)
                mDelegate.onSpeechResult(getPartialResultsAsString());
        } catch (final Throwable exc) {
            Logger.error(Speech.class.getSimpleName(),
                    "Unhandled exception in delegate onSpeechResult", exc);
        }

//        if (mProgressView != null)
//            mProgressView.onResultOrOnError();

        // recreate the speech recognizer
        initSpeechRecognizer(mContext);
    }


    public boolean isListening() {
        return mIsListening;
    }


    public void say(final String message) {
        say(message, null);
    }


    public void say(final String message, final TextToSpeechCallback callback) {

        final String utteranceId = UUID.randomUUID().toString();

        if (callback != null) {
            mTtsCallbacks.put(utteranceId, callback);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(message, mTtsQueueMode, null, utteranceId);
        } else {
            final HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            mTextToSpeech.speak(message, mTtsQueueMode, params);
        }
    }


    public void stopTextToSpeech() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
    }


    public Speech setPreferOffline(final boolean preferOffline) {
        mPreferOffline = preferOffline;
        return this;
    }


    public Speech setGetPartialResults(final boolean getPartialResults) {
        mGetPartialResults = getPartialResults;
        return this;
    }


    public Speech setLocale(final Locale locale) {
        mLocale = locale;
        if (mTextToSpeech != null)
            mTextToSpeech.setLanguage(locale);
        return this;
    }


    public Speech setTextToSpeechRate(final float rate) {
        mTtsRate = rate;
        mTextToSpeech.setSpeechRate(rate);
        return this;
    }


    public Speech setTextToSpeechPitch(final float pitch) {
        mTtsPitch = pitch;
        mTextToSpeech.setPitch(pitch);
        return this;
    }

    public Speech setStopListeningAfterInactivity(final long milliseconds) {
        mStopListeningDelayInMs = milliseconds;
        initDelayedStopListening(mContext);
        return this;
    }


    public Speech setTransitionMinimumDelay(final long milliseconds) {
        mTransitionMinimumDelay = milliseconds;
        return this;
    }

    public Speech setTextToSpeechQueueMode(final int mode) {
        mTtsQueueMode = mode;
        return this;
    }

    private Speech.stopDueToDelay mListenerDelay;

    // define listener
    public interface stopDueToDelay {
        void onSpecifiedCommandPronounced(final String event);
    }

    // set the listener. Must be called from the fragment
    public void setListener(Speech.stopDueToDelay listener) {
        this.mListenerDelay = listener;
    }

}
