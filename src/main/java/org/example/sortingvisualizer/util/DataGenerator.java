package org.example.sortingvisualizer.util;

import java.util.Random;

/**
 * 数据生成器工具类
 * 提供多种数据生成策略
 */
public class DataGenerator {

    private static final Random random = new Random();

    /**
     * 生成随机数据
     * @param size 数据规模
     * @param maxValue 最大值
     * @return 随机数组
     */
    public static int[] generateRandomData(int size, int maxValue) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = random.nextInt(maxValue) + 1;
        }
        return data;
    }

    /**
     * 生成近乎有序的数据 (少量乱序)
     * @param size 数据规模
     * @return 近乎有序数组
     */
    public static int[] generateNearlySortedData(int size) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i + 1;
        }
        // 随机交换几次
        int swapCount = (int) (size * 0.10); // 10% 的乱序
        for (int i = 0; i < swapCount; i++) {
            int idx1 = random.nextInt(size);
            int idx2 = random.nextInt(size);
            int temp = data[idx1];
            data[idx1] = data[idx2];
            data[idx2] = temp;
        }
        return data;
    }

    /**
     * 生成线性分布的随机数据 (洗牌后的梯度)
     * 值域为 1 到 size，保证每个元素高度唯一
     */
    public static int[] generateLinearShuffledData(int size) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i + 1;
        }
        // 洗牌 (Fisher-Yates Shuffle)
        for (int i = size - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = data[index];
            data[index] = data[i];
            data[i] = temp;
        }
        return data;
    }

    /**
     * 生成完全逆序的数据 (梯度递减)
     * @param size 数据规模
     * @return 逆序数组
     */
    public static int[] generateReversedData(int size) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = size - i;
        }
        return data;
    }

    /**
     * 生成有序数据 (梯度递增)
     */
    public static int[] generateSortedData(int size) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i + 1;
        }
        return data;
    }
}
