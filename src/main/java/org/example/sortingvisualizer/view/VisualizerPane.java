package org.example.sortingvisualizer.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * 排序可视化面板
 * 负责绘制数组状态
 */
public class VisualizerPane extends Pane {

    private int[] array;
    private static final int BAR_GAP = 2;
    private static final double MAX_BAR_WIDTH = 42;
    private boolean showLabels = true;

    // 颜色常量
    private static final Color COLOR_DEFAULT = Color.web("#7db3ff");
    private static final Color COLOR_STROKE = Color.web("#e6e6e6");

    // 内边距
    private static final double PADDING_TOP = 10;
    private static final double PADDING_BOTTOM = 10;

    public VisualizerPane() {
        this.getStyleClass().add("visualizer-pane");
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

    /**
     * 一次性渲染数组与高亮（避免 updateArray + highlight 的双重重绘）。
     */
    public void renderState(int[] newArray, int index1, int index2, Color highlightColor) {
        this.array = (newArray == null) ? new int[0] : newArray.clone();
        draw(index1, index2, highlightColor, null);
    }

    /**
     * 渲染“完成态”：所有柱子使用同一种颜色（例如排序完成的绿色）。
     */
    public void renderFinalState(int[] newArray, Color fillColor) {
        this.array = (newArray == null) ? new int[0] : newArray.clone();
        draw(-1, -1, null, fillColor);
    }

    public void setShowLabels(boolean show) {
        this.showLabels = show;
        draw();
    }

    private void draw() {
        draw(-1, -1, null, null);
    }

    private void draw(int idx1, int idx2, Color highlightColor) {
        draw(idx1, idx2, highlightColor, null);
    }

    private void draw(int idx1, int idx2, Color highlightColor, Color overrideFillColor) {
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

        // 计算每个柱子的宽度（考虑间距，并设置最大宽度，避免数据量很小时柱子过粗）
        double totalGap = Math.max(0, (array.length - 1)) * BAR_GAP;
        double rawBarWidth = (width - totalGap) / array.length;
        double barWidth = Math.min(Math.max(1, rawBarWidth), MAX_BAR_WIDTH);

        // 如果柱子总宽度小于画布，则居中显示
        double totalBarsWidth = array.length * barWidth + totalGap;
        double startX = Math.max(0, (width - totalBarsWidth) / 2.0);

        // 基线（面板底部往上 PADDING_BOTTOM 位置）
        double baseY = height - PADDING_BOTTOM;

        for (int i = 0; i < array.length; i++) {
            int v = array[i];
            // 不再强制限制值域到 100，而是根据 maxVal 归一化

            // 高度计算保留浮点精度，最后再决定渲染高度
            double barHeight = v * unitPx;
            if (barHeight < 1 && v > 0) barHeight = 1; // 只要有值至少显示1px

            // x 坐标：居中 + 间距
            double x = startX + i * (barWidth + BAR_GAP);
            double y = baseY - barHeight;

            Rectangle rect = new Rectangle(x, y, barWidth, barHeight);

            // Apple-ish：圆角 + 细描边 + 轻微区分度
            double arc = Math.min(10, barWidth);
            rect.setArcWidth(arc);
            rect.setArcHeight(arc);
            rect.setStroke(COLOR_STROKE);
            rect.setStrokeWidth(0.5);

            if ((i == idx1 || i == idx2) && highlightColor != null) {
                rect.setFill(highlightColor);
            } else if (overrideFillColor != null) {
                rect.setFill(overrideFillColor);
            } else {
                rect.setFill(colorForValue(v, maxVal));
            }

            this.getChildren().add(rect);

            if (showLabels && barWidth >= 16) {
                Text label = new Text(String.valueOf(v));
                label.setFill(Color.web("#333333"));
                label.setFont(Font.font(11));
                double labelX = x + 2;
                double labelY = y - 2;
                label.setX(labelX);
                label.setY(Math.max(labelY, 8));
                this.getChildren().add(label);
            }
        }
    }

    private Color colorForValue(int value, int maxValue) {
        if (maxValue <= 0) {
            return COLOR_DEFAULT;
        }
        double t = Math.min(1.0, Math.max(0.0, value / (double) maxValue));
        // Apple-ish：整体更明亮，避免“发黑”；同时保留轻微的深浅差异
        double hue = 211; // 接近系统蓝
        double saturation = 0.48;
        double brightness = 0.78 + 0.16 * t; // 0.78..0.94
        return Color.hsb(hue, saturation, brightness);
    }
}
