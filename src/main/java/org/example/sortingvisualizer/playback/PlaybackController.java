package org.example.sortingvisualizer.playback;

import java.util.Objects;
import java.util.function.Consumer;

import org.example.sortingvisualizer.step.RecordedSort;
import org.example.sortingvisualizer.step.SortOperation;
import org.example.sortingvisualizer.step.StepPlayer;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * 回放控制器：封装 StepPlayer + 自动回放定时逻辑。
 * <p>
 * 仅暴露 start/pause/next/prev/stop 与事件回调，避免 Controller 持有过多回放状态细节。
 */
public final class PlaybackController {

    private StepPlayer player;
    private PauseTransition timer;
    private boolean playing;
    private long delayMillis = 50;

    private Consumer<PlaybackSnapshot> onUpdate;
    private Runnable onFinished;

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = Math.max(1, delayMillis);

        // 速度在自动回放过程中变化时，需立刻更新定时推进周期。
        // 否则 timer 会继续按旧周期运行，用户会感觉“只有暂停/步进/重新开始才生效”。
        if (playing && player != null) {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            scheduleTick();
        }
    }

    public void setOnUpdate(Consumer<PlaybackSnapshot> onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    public boolean isLoaded() {
        return player != null;
    }

    public boolean isPlaying() {
        return playing;
    }

    public int cursor() {
        return (player == null) ? 0 : player.cursor();
    }

    public int size() {
        return (player == null) ? 0 : player.size();
    }

    public boolean hasNext() {
        return player != null && player.hasNext();
    }

    public boolean hasPrev() {
        return player != null && player.hasPrev();
    }

    public int[] currentArray() {
        return (player == null) ? new int[0] : player.currentArray();
    }

    public void load(RecordedSort recorded) {
        Objects.requireNonNull(recorded, "recorded");
        stop();
        this.player = new StepPlayer(recorded.initialArray(), recorded.operations());
        emit(null);
    }

    public void start() {
        if (player == null) return;
        if (playing) return;
        playing = true;
        scheduleTick();
    }

    public void pause() {
        playing = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        emit(null);
    }

    public void stop() {
        pause();
        player = null;
    }

    public SortOperation next() {
        if (player == null) return null;
        SortOperation op = player.next();
        emit(op);
        return op;
    }

    public SortOperation prev() {
        if (player == null) return null;
        SortOperation op = player.prev();
        emit(op);
        return op;
    }

    private void scheduleTick() {
        if (!playing || player == null) return;

        timer = new PauseTransition(Duration.millis(delayMillis));
        timer.setOnFinished(evt -> {
            if (!playing || player == null) return;

            SortOperation op = player.next();
            emit(op);

            if (player.hasNext()) {
                scheduleTick();
            } else {
                playing = false;
                if (timer != null) {
                    timer.stop();
                    timer = null;
                }
                if (onFinished != null) {
                    onFinished.run();
                }
            }
        });
        timer.play();
    }

    private void emit(SortOperation op) {
        if (player == null) return;
        if (onUpdate == null) return;
        onUpdate.accept(new PlaybackSnapshot(
                player.currentArray(),
                op,
                player.cursor(),
                player.size(),
                player.hasPrev(),
                player.hasNext(),
                playing
        ));
    }
}
