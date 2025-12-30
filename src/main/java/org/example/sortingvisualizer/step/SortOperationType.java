package org.example.sortingvisualizer.step;

public enum SortOperationType {
    COMPARE, // 仅比较两个位置（不改变数组），主要用于高亮与统计比较次数
    SWAP,    // 交换两个位置（会改变数组），用于可视化“交换”与统计交换次数
    SET      // 把某个位置写成新值（会改变数组），用于计数/基数等“赋值型”算法
}
