# 排序算法可视化与性能比较（JavaFX）

一个用 Java 17 + JavaFX + Maven 构建的桌面应用，用于“排序算法的动画演示与性能对比”。
核心思路是“先录制步骤，再回放”，因此支持暂停/继续、上一步/下一步、操作回显与次数统计；性能比较采用纯排序（不走动画），更贴近真实耗时。

## 特性

- 多种算法：快速、归并、堆、计数、基数、桶、选择、插入、希尔、冒泡等（含演示型：猴子/睡眠/珠）。
- 录制→回放：支持暂停/继续、上一步/下一步、当前步/总步与操作说明（比较/交换/写回）。
- 可视化：比较/交换/写回不同高亮色；完成态统一绿色；可选显示数值标签。
- 数据来源：随机/有序/逆序/部分有序生成，自定义文本输入，文件加载（.txt/.csv）。
- 性能比较：纯排序计时 + 内存占用估算；图表支持悬停提示（ms/MB）。
  
## 环境要求

- JDK 17（建议设置 `JAVA_HOME` 指向 JDK 17）
- Maven（建议 3.8+）

## 快速开始

```bash
mvn clean javafx:run
```

首次运行需联网以拉取 OpenJFX 依赖。
在 Windows 下可直接使用 CMD/PowerShell 运行上述命令。

## 使用说明

- 生成数据：选择“规模/类型”，点击“生成数据”。
- 自定义/文件：在文本框输入整数序列并“显示数据”，或“读取文件”加载 .txt/.csv。
- 开始排序：选择算法后点击“开始排序”。
  - 控制：暂停/继续、上一步/下一步；快捷键：←/→ 步进，Space 暂停/继续。
  - 回显与统计：显示当前步做了什么，以及比较/交换/写入的当前/总次数。
- 退出排序：点击“退出排序”恢复到排序前的数据。
- 性能比较：设定规模与类型后点击“性能比较”，查看时间/内存图表与明细表格。

## 项目结构

```
pom.xml
src/
  main/
    java/
      org/example/sortingvisualizer/
        SortingVisualizerApp.java
        algorithm/
          AlgorithmRegistry.java
          Sorter.java
          impl/  # 各排序算法实现
        controller/MainController.java
        playback/PlaybackController.java
        service/  # Benchmark/DataInput/StepRecording
        step/     # RecordedSort/SortOperation*
        view/     # VisualizerPane/BenchmarkViewBuilder
    resources/
      org/example/sortingvisualizer/view/
        MainLayout.fxml
        apple.css
```

