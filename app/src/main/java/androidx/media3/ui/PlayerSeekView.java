package androidx.media3.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;

public class PlayerSeekView extends FrameLayout {

    private final DefaultTimeBar timeBar;
    @Nullable
    private Player player;
    private final Player.Listener listener;

    public PlayerSeekView(Context context) {
        this(context, null);
    }

    public PlayerSeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        timeBar = new DefaultTimeBar(context, attrs);
        timeBar.setId(R.id.exo_progress);
        timeBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(timeBar);
        listener = new Player.Listener() {
            @Override
            public void onEvents(Player player, Player.Events events) {
                updateTimeBar(player);
            }
        };
        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                if (!canceled && player != null && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
                    player.seekTo(position);
                }
            }
        });
    }

    public TimeBar getTimeBar() {
        return timeBar;
    }

    public void setPlayer(@Nullable Player player) {
        if (this.player == player) return;
        if (this.player != null) this.player.removeListener(listener);
        this.player = player;
        if (player != null) {
            player.addListener(listener);
            updateTimeBar(player);
        } else {
            timeBar.setPosition(0);
            timeBar.setBufferedPosition(0);
            timeBar.setDuration(C.TIME_UNSET);
        }
    }

    private void updateTimeBar(Player player) {
        timeBar.setPosition(player.getContentPosition());
        timeBar.setBufferedPosition(player.getContentBufferedPosition());
        timeBar.setDuration(player.getDuration());
    }
}
