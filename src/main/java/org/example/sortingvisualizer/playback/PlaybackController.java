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
        // 设置每一步自动回放的延迟（至少 1ms，避免 0 或负数）
        this.delayMillis = Math.max(1, delayMillis);

        // 速度在自动回放过程中变化时，需立刻更新定时推进周期。
        // 否则 timer 会继续按旧周期运行，用户会感觉“只有暂停/步进/重新开始才生效”。
        if (playing && player != null) {
            // 回放进行中：需要立刻用新 delay 重建定时器，否则用户会感觉速度设置“没生效”
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
        // 是否已加载录制数据（有 player 就表示 loaded）
        return player != null;
    }

    public boolean isPlaying() {
        return playing;
    }

    public int cursor() {
        // 当前步数（cursor 与 StepPlayer 语义一致：指向“下一步要执行的操作”）
        return (player == null) ? 0 : player.cursor();
    }

    public int size() {
        // 总步数
        return (player == null) ? 0 : player.size();
    }

    public boolean hasNext() {
        return player != null && player.hasNext();
    }

    public boolean hasPrev() {
        return player != null && player.hasPrev();
    }

    public int[] currentArray() {
        // 当前数组快照（用于 UI 渲染）
        return (player == null) ? new int[0] : player.currentArray();
    }

    public void load(RecordedSort recorded) {
        // 加载一组新的录制数据：会先 stop 清理旧状态，再创建新的 StepPlayer
        Objects.requireNonNull(recorded, "recorded");
        stop();
        this.player = new StepPlayer(recorded.initialArray(), recorded.operations());
        // 立即发一次更新：让 UI 能显示“准备开始”的画面/按钮状态
        emit(null);
    }

    public void start() {
        // 开始自动回放（如果已在播放则忽略）
        if (player == null) return;
        if (playing) return;
        playing = true;
        scheduleTick();
    }

    public void pause() {
        // 暂停自动回放：停止定时器，但不清除 player（保留当前 cursor/数组）
        playing = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        // 发一次更新（operation=null）：让上层 UI 有机会刷新“播放状态”显示
        emit(null);
    }

    public void stop() {
        // 停止并卸载：会清除 player，使得 isLoaded=false
        pause();
        player = null;
    }

    public SortOperation next() {
        // 手动下一步：不依赖定时器
        if (player == null) return null;
        SortOperation op = player.next(); // 可能返回 null（已到末尾）
        emit(op);                         // 推送快照给 UI
        return op;
    }

    public SortOperation prev() {
        // 手动上一步：撤销一步
        if (player == null) return null;
        SortOperation op = player.prev();
        emit(op);
        return op;
    }

    private void scheduleTick() {
        // 安排一次“到点推进一步”的 tick
        if (!playing || player == null) return;

        timer = new PauseTransition(Duration.millis(delayMillis));
        timer.setOnFinished(evt -> {
            if (!playing || player == null) return;

            // 到点：推进一步并通知 UI
            SortOperation op = player.next();
            emit(op);

            if (player.hasNext()) {
                // 还有下一步：继续安排下一次 tick
                scheduleTick();
            } else {
                // 已到末尾：停止播放并回调 onFinished（用于渲染完成态等）
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
        // emit 的职责：把 StepPlayer 当前状态打包成 PlaybackSnapshot 并交给上层（通常是 Controller/UI）
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
