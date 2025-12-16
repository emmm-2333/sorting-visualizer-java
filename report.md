# 排序算法性能比较与动画演示系统 - 项目设计报告

## 1. 项目概述
本项目旨在开发一个基于 Java 的桌面应用程序，用于可视化演示多种排序算法的执行过程，并对其性能进行量化比较。系统将帮助用户直观理解算法原理，并分析不同算法在不同数据集下的效率差异。

## 2. 技术选型与开发环境
*   **编程语言**: Java 21 (利用新特性，如 Record, Switch 表达式等)
*   **构建工具**: Maven
*   **图形用户界面 (GUI)**: JavaFX
    *   *选择理由*: JavaFX 相比 Swing 拥有更现代的 UI 组件库、硬件加速的渲染引擎以及更强大的动画 API (Timeline, Transition)，非常适合实现流畅的排序动画。同时支持 FXML 实现界面与逻辑分离。
*   **图表库**: JavaFX Charts (内置) 或 JFreeChart (用于性能分析图表)

## 3. 系统架构与设计模式
本项目采用 **MVC (Model-View-Controller)** 架构模式，确保代码结构清晰、低耦合、易于维护和扩展。

*   **Model (模型层)**: 包含排序数据、算法实现、性能统计数据。
*   **View (视图层)**: 负责界面展示，包括动画画布、控制面板、图表展示。
*   **Controller (控制层)**: 处理用户交互，协调 Model 和 View。

**关键设计模式**:
*   **策略模式 (Strategy Pattern)**: 定义 `Sorter` 接口，所有排序算法（快排、归并、堆排等）实现该接口。这使得系统可以在运行时动态切换不同的排序算法，且易于添加新算法。
*   **观察者模式 (Observer Pattern) / 回调机制**: 排序算法在执行关键操作（比较、交换）时通知 View 层更新画面，实现解耦。
*   **工厂模式 (Factory Pattern)**: 用于根据用户选择创建具体的排序算法实例。

## 4. 项目目录结构规划
为了满足“结构规范简单，能后续加模块”的要求，建议采用以下包结构：

```
src/main/java/org/example/sortingvisualizer/
├── SortingVisualizerApp.java       // 程序入口
├── algorithm/                      // 核心算法模块
│   ├── Sorter.java                 // 排序算法接口
│   ├── impl/                       // 具体算法实现
│   │   ├── QuickSort.java
│   │   ├── MergeSort.java
│   │   ├── HeapSort.java
│   │   └── ...
│   └── SortStepListener.java       // 动画回调接口
├── model/                          // 数据模型
│   ├── SortData.java               // 封装待排序数组对象
│   └── PerformanceMetrics.java     // 性能指标记录
├── view/                           // UI 视图
│   ├── MainLayout.fxml             // 主界面布局 (可选)
│   ├── VisualizerPane.java         // 排序动画绘制面板
│   └── ChartPane.java              // 性能图表面板
├── controller/                     // 控制器
│   └── MainController.java         // 主逻辑控制
└── util/                           // 工具类
    ├── DataGenerator.java          // 数据生成器 (随机, 有序, 逆序)
    └── ThreadUtils.java            // 线程管理工具
```

## 5. 关键功能模块设计

### 5.1 存储结构与数据生成
*   使用泛型数组或 `int[]` 存储待排序数据。
*   `DataGenerator` 类提供多种生成策略：
    *   **随机数据**: 模拟一般情况。
    *   **近乎有序**: 测试插入排序等算法的优势。
    *   **完全逆序**: 测试算法的最差情况。
    *   **大量重复**: 测试三路快排等优化效果。

### 5.2 排序算法实现 (核心)
所有算法必须手动实现，严禁使用 `java.util.Arrays.sort`。
*   **快速排序 (Quick Sort)**: 实现递归版本，包含 Partition 操作。
*   **归并排序 (Merge Sort)**: 实现递归版本，包含 Merge 操作，注意辅助数组的使用。
*   **堆排序 (Heap Sort)**: 实现建堆 (Heapify) 和下沉 (SiftDown) 操作。
*   **其他**: 冒泡、选择、插入排序作为基础对比。

### 5.3 动画演示系统
*   **原理**: 算法执行时，不直接操作 UI。而是通过 `SortStepListener` 接口发送 `onCompare(index1, index2)` 和 `onSwap(index1, index2)` 事件。
*   **可视化**:
    *   使用矩形柱状图表示数字大小。
    *   **颜色编码**:
        *   红色: 正在比较的元素。
        *   绿色: 正在交换的元素。
        *   蓝色: 已排序完成的元素。
*   **速度控制**: 在事件回调中加入 `Thread.sleep(delay)` 来控制动画速度，该操作需在后台线程进行，避免阻塞 UI 线程 (JavaFX Application Thread)。

### 5.4 性能比较模块
*   **独立运行**: 性能测试时不开启动画（动画会严重拖慢速度），仅运行纯算法逻辑。
*   **多轮测试**: 对每种算法在同一数据集上运行多次取平均值。
*   **指标**:
    *   **时间复杂度**: 记录 System.nanoTime() 差值。
    *   **空间复杂度**: 估算辅助空间使用。
    *   **稳定性**: 验证排序前后相等元素的相对位置。

## 6. 下一步计划
1.  搭建 Maven 项目骨架，引入 JavaFX 依赖。
2.  创建上述包结构。
3.  实现基础的 `Sorter` 接口和 `BubbleSort` (作为测试)。
4.  搭建 JavaFX 主界面框架。

