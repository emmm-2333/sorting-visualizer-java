package org.example.sortingvisualizer.playback;

import org.example.sortingvisualizer.step.SortOperation;

/**
 * 一次回放更新的快照。
 */
public record PlaybackSnapshot(
        int[] state,                // 当前回放后的数组状态（UI 用于绘制柱子）
        SortOperation operation,     // 本次“刚刚执行/撤销”的操作（用于回显文本与高亮）
        int cursor,                 // 当前游标：指向“下一个要执行”的操作索引
        int size,                   // 总操作数
        boolean hasPrev,            // 是否可以“上一步”（cursor > 0）
        boolean hasNext,            // 是否可以“下一步”（cursor < size）
        boolean playing             // 是否处于自动回放中（暂停/继续按钮状态）
) {
}
