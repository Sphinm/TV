package com.fongmi.android.tv.ui.activity.video;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.HorizontalGridView;

import com.bumptech.glide.request.transition.Transition;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.History;
import com.fongmi.android.tv.databinding.ActivityVideoBinding;
import com.fongmi.android.tv.impl.CustomTarget;
import com.fongmi.android.tv.ui.adapter.FlagAdapter;
import com.fongmi.android.tv.ui.custom.CustomKeyDownVod;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Traffic;

public class VideoOverlayUi {

    public interface PlayerView {
        String getDurationTime();

        String getPositionTime(long time);

        boolean isScrubbing();
    }

    private final ActivityVideoBinding binding;
    private final FragmentActivity activity;
    private final PlayerView player;
    private final Runnable hideControl;
    private final Runnable updateTraffic;
    private RelativeLayout.LayoutParams frameParams;
    private CustomKeyDownVod keyDown;
    private FlagAdapter flagAdapter;
    private View focus1;
    private View focus2;
    private boolean fullscreen;

    public VideoOverlayUi(ActivityVideoBinding binding, FragmentActivity activity, PlayerView player, Runnable hideControl, Runnable updateTraffic) {
        this.binding = binding;
        this.activity = activity;
        this.player = player;
        this.hideControl = hideControl;
        this.updateTraffic = updateTraffic;
    }

    public void initFrameParams(RelativeLayout.LayoutParams frameParams, CustomKeyDownVod keyDown, FlagAdapter flagAdapter) {
        this.frameParams = frameParams;
        this.keyDown = keyDown;
        this.flagAdapter = flagAdapter;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public View getFocus1(View fallback) {
        return focus1 == null || focus1.getVisibility() != View.VISIBLE ? fallback : focus1;
    }

    public View getFocus2(View fallback) {
        return focus2 == null || focus2.getVisibility() != View.VISIBLE || focus2 == binding.control.action.opening || focus2 == binding.control.action.ending ? binding.control.action.next : focus2;
    }

    public void setFocus1(View focus) {
        this.focus1 = focus;
    }

    public void setFocus2(View focus) {
        this.focus2 = focus;
    }

    public void enterFullscreen(View currentFocus, HorizontalGridView flagView) {
        focus1 = currentFocus;
        binding.video.requestFocus();
        binding.video.setForeground(null);
        binding.video.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        flagView.setSelectedPosition(flagAdapter.getPosition());
        keyDown.setFull(true);
        fullscreen = true;
        focus2 = null;
    }

    public void exitFullscreen(View fallbackFocus) {
        binding.video.setForeground(ResUtil.getDrawable(R.drawable.selector_video));
        binding.video.setLayoutParams(frameParams);
        fallbackFocus.requestFocus();
        keyDown.setFull(false);
        fullscreen = false;
        focus2 = null;
        hideInfo();
    }

    public void showProgress() {
        binding.progress.getRoot().setVisibility(View.VISIBLE);
        App.post(updateTraffic, 0);
        hideCenter();
        hideError();
    }

    public void hideProgress() {
        binding.progress.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(updateTraffic);
        Traffic.reset();
    }

    public void showError(String text) {
        binding.widget.error.setVisibility(View.VISIBLE);
        binding.widget.text.setText(text);
        hideProgress();
    }

    public void hideError() {
        binding.widget.error.setVisibility(View.GONE);
        binding.widget.text.setText("");
    }

    public void showInfo() {
        binding.widget.top.setVisibility(View.VISIBLE);
        binding.widget.center.setVisibility(View.VISIBLE);
        binding.widget.duration.setText(player.getDurationTime());
        binding.widget.position.setText(player.getPositionTime(0));
    }

    public void hideInfo() {
        binding.widget.top.setVisibility(View.GONE);
        binding.widget.center.setVisibility(View.GONE);
    }

    public void showControl(View view) {
        binding.control.getRoot().setVisibility(View.VISIBLE);
        view.requestFocus();
        scheduleHideControl();
    }

    public void hideControl() {
        binding.control.getRoot().setVisibility(View.GONE);
        App.removeCallbacks(hideControl);
    }

    public void hideCenter() {
        binding.widget.action.setImageResource(R.drawable.ic_widget_play);
        hideInfo();
    }

    public void setTraffic() {
        Traffic.setSpeed(binding.progress.traffic);
        App.post(updateTraffic, 1000);
    }

    public void scheduleHideControl() {
        if (player.isScrubbing()) return;
        App.post(hideControl, Constant.INTERVAL_HIDE);
    }

    public void onScrubbingChanged(boolean scrubbing) {
        if (scrubbing) App.removeCallbacks(hideControl);
        else if (binding.control.getRoot().getVisibility() == View.VISIBLE) scheduleHideControl();
    }

    public void onSeeking(long time) {
        binding.widget.center.setVisibility(View.VISIBLE);
        binding.widget.duration.setText(player.getDurationTime());
        binding.widget.position.setText(player.getPositionTime(time));
        binding.widget.action.setImageResource(time > 0 ? R.drawable.ic_widget_forward : R.drawable.ic_widget_rewind);
        hideProgress();
    }

    public void setArtwork(History history) {
        ImgUtil.load(activity, history.getVodPic(), new CustomTarget<>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                binding.player.setDefaultArtwork(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                binding.player.setDefaultArtwork(errorDrawable);
            }
        });
    }
}
