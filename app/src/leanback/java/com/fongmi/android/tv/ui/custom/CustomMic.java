package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.speech.VoskRecognizer;
import com.fongmi.android.tv.utils.KeyUtil;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.bassaer.library.MDColor;

public class CustomMic extends AppCompatImageView {

    private VoskRecognizer mRecognizer;
    private MicCallback mMicCallback;
    private FragmentActivity mActivity;
    private boolean mListen;
    private boolean mModelReady;

    public CustomMic(@NonNull Context context) {
        super(context);
    }

    public CustomMic(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(FragmentActivity activity, MicCallback callback) {
        mActivity = activity;
        mMicCallback = callback;
        VoskRecognizer.prepare(activity, () -> {
            mModelReady = true;
            setEnabled(true);
        }, message -> {
            mModelReady = false;
            setEnabled(false);
        });
    }

    public void start() {
        if (mActivity == null) return;
        if (!mModelReady) {
            dispatchError(getContext().getString(R.string.speech_model_loading));
            return;
        }
        PermissionUtil.requestAudio(mActivity, granted -> {
            if (!granted) {
                dispatchError(getContext().getString(R.string.speech_permission_denied));
                return;
            }
            if (mRecognizer == null) mRecognizer = new VoskRecognizer();
            mRecognizer.start(new VoskRecognizer.Callback() {
                @Override
                public void onStart() {
                    requestFocus();
                    updateUI(true);
                }

                @Override
                public void onPartial(String text) {
                }

                @Override
                public void onResult(String text) {
                    dispatchResult(text);
                }

                @Override
                public void onError(String message) {
                    dispatchError(message);
                }

                @Override
                public void onEnd() {
                    updateUI(false);
                    if (mMicCallback != null) mMicCallback.onEnd();
                }
            });
        });
    }

    public void stop() {
        if (mRecognizer != null) mRecognizer.stop();
        updateUI(false);
    }

    public void destroy() {
        if (mRecognizer != null) mRecognizer.cancel();
        mRecognizer = null;
    }

    private boolean isListen() {
        return mListen;
    }

    private void dispatchResult(String text) {
        updateUI(false);
        if (mMicCallback != null) mMicCallback.onResults(text == null ? "" : text.trim());
    }

    private void dispatchError(String message) {
        updateUI(false);
        if (mMicCallback != null) mMicCallback.onError(message);
    }

    private void updateUI(boolean listening) {
        mListen = listening;
        if (listening) {
            startAnimation(ResUtil.getAnim(R.anim.flicker));
            setColorFilter(MDColor.RED_500, PorterDuff.Mode.SRC_IN);
        } else {
            clearAnimation();
            setColorFilter(MDColor.WHITE, PorterDuff.Mode.SRC_IN);
        }
    }

    private boolean onBackKey(KeyEvent event) {
        if (!isListen() || !KeyUtil.isBackKey(event)) return false;
        stop();
        return true;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) start();
        else stop();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (onBackKey(event)) return true;
        return super.dispatchKeyEvent(event);
    }
}
