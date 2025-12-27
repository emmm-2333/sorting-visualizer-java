package org.example.sortingvisualizer.playback;

import org.example.sortingvisualizer.step.SortOperation;

/**
 * 一次回放更新的快照。
 */
public record PlaybackSnapshot(
        int[] state,
        SortOperation operation,
        int cursor,
        int size,
        boolean hasPrev,
        boolean hasNext,
        boolean playing
) {
}
