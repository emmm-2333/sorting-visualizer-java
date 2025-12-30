package org.example.sortingvisualizer.step;

public interface SortOperation {
    // 操作类型：用于 UI 决定高亮/统计/回显（例如比较/交换/赋值）
    SortOperationType type();

    // 第一个相关下标（比较/交换常用）；无意义时实现可以返回 -1
    int index1();

    // 第二个相关下标（比较/交换常用）；一元操作（SET）可返回 -1
    int index2();

    // 对数组执行该操作（回放“下一步”时调用）
    void apply(int[] array);

    // 撤销该操作（回放“上一步”时调用）
    void undo(int[] array);

    // 操作回显文本：通常基于“执行前快照”生成，保证用户看到的是当时的值
    String description(int[] arrayBeforeApply);
}
