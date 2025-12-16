package org.example.sortingvisualizer.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * 排序可视化面板
 * 负责绘制数组状态
 */
public class VisualizerPane extends Pane {

    private int[] array;
    private static final int BAR_GAP = 0; // 取消固定间隙，密集时更美观

    // 颜色常量
    private static final Color COLOR_DEFAULT = Color.web("#7db3ff");

    // 内边距
    private static final double PADDING_TOP = 10;
    private static final double PADDING_BOTTOM = 10;

    public VisualizerPane() {
        // 初始设置：更简洁的浅色背景
        this.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e6e6e6;");
    }

    /**
     * 设置要显示的数据
     * @param array 数据数组
     */
    public void setArray(int[] array) {
        // 允许空数组，避免初始化阶段 NPE
        this.array = (array == null) ? new int[0] : array.clone(); // 保护性拷贝
        draw();
    }

    /**
     * 更新特定索引的颜色 (用于动画)
     * @param index1 索引1
     * @param index2 索引2
     * @param color 颜色
     */
    public void highlight(int index1, int index2, Color color) {
        if (array == null) return;
        draw(index1, index2, color);
    }

    public void updateArray(int[] newArray) {
        this.array = (newArray == null) ? new int[0] : newArray.clone();
        draw();
    }

    private void draw() {
        draw(-1, -1, null);
    }

    private void draw(int idx1, int idx2, Color highlightColor) {
        this.getChildren().clear();
        if (array == null || array.length == 0) return;

        double width = this.getWidth();
        double height = this.getHeight();
        if (width <= 0 || height <= 0) return;

        // 绘制区域去掉上下内边距
        double innerHeight = height - PADDING_TOP - PADDING_BOTTOM;
        if (innerHeight <= 1) return;

        // 找到最大值用于归一化高度，确保不同数据量下都能充满画布
        int maxVal = 0;
        for (int val : array) maxVal = Math.max(maxVal, val);
        if (maxVal <= 0) maxVal = 100; // 默认防守值

        // 使用浮点数计算单位高度，避免整数除法导致的量化误差
        // 这样可以充分利用垂直空间，而不是被 floor 截断
        double unitPx = innerHeight / (double) maxVal;

        // 计算每个柱子的宽度，使用浮点数以避免累积误差
        double barWidth = (width - Math.max(0, (array.length - 1)) * BAR_GAP) / array.length;

        // 基线（面板底部往上 PADDING_BOTTOM 位置）
        double baseY = height - PADDING_BOTTOM;

        for (int i = 0; i < array.length; i++) {
            int v = array[i];
            // 不再强制限制值域到 100，而是根据 maxVal 归一化

            // 高度计算保留浮点精度，最后再决定渲染高度
            double barHeight = v * unitPx;
            if (barHeight < 1 && v > 0) barHeight = 1; // 只要有值至少显示1px

            // x 坐标也使用浮点数计算，避免整数取整导致的右侧留白
            double x = i * (barWidth + BAR_GAP);
            double y = baseY - barHeight;

            Rectangle rect = new Rectangle(x, y, barWidth, barHeight);

            if (i == idx1 || i == idx2) {
                rect.setFill(highlightColor);
            } else {
                rect.setFill(COLOR_DEFAULT);
            }

            this.getChildren().add(rect);
        }
    }
}
