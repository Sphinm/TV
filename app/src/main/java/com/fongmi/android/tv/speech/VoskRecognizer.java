package com.fongmi.android.tv.speech;

import android.content.Context;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.utils.Download;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.utils.Path;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VoskRecognizer {

    public interface Callback {
        void onStart();

        void onPartial(String text);

        void onResult(String text);

        void onError(String message);

        void onEnd();
    }

    private interface ModelListener {
        void onReady();

        void onError(String message);
    }

    private static final String MODEL_DIR = "model-cn";
    private static final String MODEL_ZIP = "vosk-model-small-cn-0.22.zip";
    private static final String MODEL_MARKER = "conf/model.conf";
    private static final float SAMPLE_RATE = 16000.0f;

    private static Model sharedModel;
    private static final AtomicBoolean loading = new AtomicBoolean(false);
    private static final List<ModelListener> waiters = new ArrayList<>();

    private final AtomicBoolean listening = new AtomicBoolean(false);
    private SpeechService speechService;
    private Callback callback;

    static {
        LibVosk.setLogLevel(LogLevel.INFO);
    }

    public static boolean isReady() {
        return sharedModel != null;
    }

    public static void prepare(Context context, Runnable onReady, ErrorListener onError) {
        if (sharedModel != null) {
            App.post(onReady);
            return;
        }
        synchronized (waiters) {
            waiters.add(new ModelListener() {
                @Override
                public void onReady() {
                    onReady.run();
                }

                @Override
                public void onError(String message) {
                    onError.onError(message);
                }
            });
        }
        if (!loading.compareAndSet(false, true)) return;
        Task.execute(() -> {
            try {
                ensureModelInstalled();
                sharedModel = new Model(getModelDir().getAbsolutePath());
                loading.set(false);
                dispatchReady();
            } catch (Exception e) {
                loading.set(false);
                dispatchError(e.getMessage() == null ? "model load failed" : e.getMessage());
            }
        });
    }

    private static File getModelDir() {
        return Path.files(MODEL_DIR);
    }

    private static boolean isModelInstalled() {
        return Path.exists(new File(getModelDir(), MODEL_MARKER));
    }

    private static void ensureModelInstalled() throws Exception {
        if (isModelInstalled()) return;
        File zip = Path.cache(MODEL_ZIP);
        Download.create(Constant.REMOTE_VOSK_MODEL, zip).tag("vosk-model").get();
        if (!Path.exists(zip)) throw new IOException("model download failed");
        Path.clear(getModelDir());
        FileUtil.zipDecompress(zip, Path.files());
        Path.clear(zip);
        if (!isModelInstalled()) throw new IOException("model unpack failed");
    }

    private static void dispatchReady() {
        List<ModelListener> pending;
        synchronized (waiters) {
            pending = new ArrayList<>(waiters);
            waiters.clear();
        }
        App.post(() -> {
            for (ModelListener listener : pending) listener.onReady();
        });
    }

    private static void dispatchError(String message) {
        List<ModelListener> pending;
        synchronized (waiters) {
            pending = new ArrayList<>(waiters);
            waiters.clear();
        }
        App.post(() -> {
            for (ModelListener listener : pending) listener.onError(message);
        });
    }

    public void start(Callback callback) {
        if (!isReady()) {
            notifyError(callback, "speech model not ready");
            notifyEnd(callback);
            return;
        }
        if (!listening.compareAndSet(false, true)) return;
        this.callback = callback;
        try {
            Recognizer recognizer = new Recognizer(sharedModel, SAMPLE_RATE);
            speechService = new SpeechService(recognizer, SAMPLE_RATE);
            speechService.startListening(new Listener());
            notifyStart(callback);
        } catch (IOException e) {
            listening.set(false);
            notifyError(callback, e.getMessage() == null ? "speech init failed" : e.getMessage());
            notifyEnd(callback);
        }
    }

    public void stop() {
        if (!listening.get()) return;
        if (speechService != null) speechService.stop();
    }

    public void cancel() {
        listening.set(false);
        shutdownService();
        notifyEnd(callback);
        callback = null;
    }

    private void shutdownService() {
        if (speechService == null) return;
        speechService.stop();
        speechService.shutdown();
        speechService = null;
    }

    private void finish() {
        if (!listening.getAndSet(false)) return;
        shutdownService();
        notifyEnd(callback);
        callback = null;
    }

    private static String parseText(String json, String key) {
        if (json == null || json.isEmpty()) return "";
        try {
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            if (!object.has(key)) return "";
            return object.get(key).getAsString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private void notifyStart(Callback target) {
        App.post(() -> {
            if (target != null) target.onStart();
        });
    }

    private void notifyPartial(Callback target, String text) {
        App.post(() -> {
            if (target != null) target.onPartial(text);
        });
    }

    private void notifyResult(Callback target, String text) {
        App.post(() -> {
            if (target != null) target.onResult(text);
        });
    }

    private void notifyError(Callback target, String message) {
        App.post(() -> {
            if (target != null) target.onError(message);
        });
    }

    private void notifyEnd(Callback target) {
        App.post(() -> {
            if (target != null) target.onEnd();
        });
    }

    public interface ErrorListener {
        void onError(String message);
    }

    private class Listener implements RecognitionListener {

        @Override
        public void onPartialResult(String hypothesis) {
            String text = parseText(hypothesis, "partial");
            if (!text.isEmpty()) notifyPartial(callback, text);
        }

        @Override
        public void onResult(String hypothesis) {
            String text = parseText(hypothesis, "text");
            if (!text.isEmpty()) notifyResult(callback, text);
            finish();
        }

        @Override
        public void onFinalResult(String hypothesis) {
            String text = parseText(hypothesis, "text");
            if (!text.isEmpty()) notifyResult(callback, text);
            finish();
        }

        @Override
        public void onError(Exception e) {
            notifyError(callback, e.getMessage() == null ? "speech failed" : e.getMessage());
            finish();
        }

        @Override
        public void onTimeout() {
            finish();
        }
    }
}
