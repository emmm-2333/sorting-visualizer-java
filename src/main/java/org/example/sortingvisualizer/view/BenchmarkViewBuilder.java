package org.example.sortingvisualizer.view;

import java.util.List;

import org.example.sortingvisualizer.model.PerformanceMetrics;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * 基准测试结果视图构建器：负责生成 TabPane（摘要/时间/内存/表格）。
 * <p>
 * 该类不依赖 Controller 状态，保持 UI 构建逻辑与交互流程松耦合。
 */
public final class BenchmarkViewBuilder {

    public TabPane buildResults(List<PerformanceMetrics> metrics, int size, String type) {
        TabPane tabPane = new TabPane();

        String typeText = (type == null || type.isBlank()) ? "数据" : type;
        String prefix = "[" + typeText + ", n=" + size + "] ";

        Tab summaryTab = new Tab(prefix + "摘要", createBenchmarkSummary(metrics, size, typeText));
        summaryTab.setClosable(false);

        Tab timeTab = new Tab(prefix + "时间对比", createTimeChart(metrics));
        timeTab.setClosable(false);

        Tab memoryTab = new Tab(prefix + "内存对比", createMemoryChart(metrics));
        memoryTab.setClosable(false);

        Tab tableTab = new Tab(prefix + "详细数据", createDetailTable(metrics));
        tableTab.setClosable(false);

        tabPane.getTabs().addAll(summaryTab, timeTab, memoryTab, tableTab);
        return tabPane;
    }

    private VBox createBenchmarkSummary(List<PerformanceMetrics> metrics, int size, String typeText) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14));

        int algoCount = (metrics == null) ? 0 : metrics.size();
        box.getChildren().add(new Label("数据类型：" + typeText));
        box.getChildren().add(new Label("数据规模 n：" + size));
        box.getChildren().add(new Label("参与算法数：" + algoCount));

        if (metrics == null || metrics.isEmpty()) {
            box.getChildren().add(new Label("暂无数据"));
            return box;
        }

        PerformanceMetrics fastest = metrics.get(0);
        PerformanceMetrics slowest = metrics.get(0);
        double sumTime = 0;
        double[] times = new double[metrics.size()];
        for (int i = 0; i < metrics.size(); i++) {
            PerformanceMetrics m = metrics.get(i);
            double t = m.getTimeElapsedMillis();
            times[i] = t;
            sumTime += t;
            if (t < fastest.getTimeElapsedMillis()) fastest = m;
            if (t > slowest.getTimeElapsedMillis()) slowest = m;
        }
        java.util.Arrays.sort(times);
        double median = (times.length % 2 == 1)
                ? times[times.length / 2]
                : (times[times.length / 2 - 1] + times[times.length / 2]) / 2.0;
        double avg = sumTime / times.length;

        box.getChildren().add(new Label("最快：" + fastest.algorithmName() + "  (" + fastest.getTimeElapsedMillis() + " ms)"));
        box.getChildren().add(new Label("最慢：" + slowest.algorithmName() + "  (" + slowest.getTimeElapsedMillis() + " ms)"));
        box.getChildren().add(new Label("平均：" + String.format(java.util.Locale.ROOT, "%.2f", avg) + " ms"));
        box.getChildren().add(new Label("中位数：" + String.format(java.util.Locale.ROOT, "%.2f", median) + " ms"));

        PerformanceMetrics minMem = metrics.get(0);
        PerformanceMetrics maxMem = metrics.get(0);
        for (PerformanceMetrics m : metrics) {
            if (m.getMemoryUsageMB() < minMem.getMemoryUsageMB()) minMem = m;
            if (m.getMemoryUsageMB() > maxMem.getMemoryUsageMB()) maxMem = m;
        }
        box.getChildren().add(new Label("内存最小：" + minMem.algorithmName() + "  (" + minMem.getMemoryUsageMB() + " MB)"));
        box.getChildren().add(new Label("内存最大：" + maxMem.algorithmName() + "  (" + maxMem.getMemoryUsageMB() + " MB)"));

        return box;
    }

    private BarChart<String, Number> createTimeChart(List<PerformanceMetrics> metrics) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("排序算法");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("耗时 (毫秒)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("算法耗时对比");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (metrics != null) {
            for (PerformanceMetrics m : metrics) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(m.algorithmName(), m.getTimeElapsedMillis());
                attachTooltip(data, m.algorithmName() + "\n" + String.format(java.util.Locale.ROOT, "%.2f ms", m.getTimeElapsedMillis()));
                series.getData().add(data);
            }
        }
        barChart.getData().add(series);
        return barChart;
    }

    private BarChart<String, Number> createMemoryChart(List<PerformanceMetrics> metrics) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("排序算法");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("内存占用 (MB)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("算法内存占用对比 (估算)");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (metrics != null) {
            for (PerformanceMetrics m : metrics) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(m.algorithmName(), m.getMemoryUsageMB());
                attachTooltip(data, m.algorithmName() + "\n" + String.format(java.util.Locale.ROOT, "%.2f MB", m.getMemoryUsageMB()));
                series.getData().add(data);
            }
        }
        barChart.getData().add(series);
        return barChart;
    }

    private TableView<PerformanceMetrics> createDetailTable(List<PerformanceMetrics> metrics) {
        TableView<PerformanceMetrics> table = new TableView<>();

        TableColumn<PerformanceMetrics, String> nameCol = new TableColumn<>("算法名称");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmName()));

        TableColumn<PerformanceMetrics, Number> timeCol = new TableColumn<>("耗时 (ms)");
        timeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getTimeElapsedMillis()));

        TableColumn<PerformanceMetrics, String> complexityCol = new TableColumn<>("平均时间复杂度");
        complexityCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmInfo().averageTimeComplexity()));

        TableColumn<PerformanceMetrics, String> spaceCol = new TableColumn<>("空间复杂度");
        spaceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmInfo().spaceComplexity()));

        TableColumn<PerformanceMetrics, String> stableCol = new TableColumn<>("稳定性");
        stableCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().algorithmInfo().isStable() ? "是" : "否"));

        table.getColumns().add(nameCol);
        table.getColumns().add(timeCol);
        table.getColumns().add(complexityCol);
        table.getColumns().add(spaceCol);
        table.getColumns().add(stableCol);

        if (metrics != null) {
            table.getItems().addAll(metrics);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private void attachTooltip(XYChart.Data<String, Number> data, String text) {
        if (data == null) return;
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(120));
        tooltip.setHideDelay(Duration.millis(40));

        ChangeListener<Node> listener = (obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip.install(newNode, tooltip);
            }
        };

        data.nodeProperty().addListener(listener);
        Node existing = data.getNode();
        if (existing != null) {
            Tooltip.install(existing, tooltip);
        }
    }
}
