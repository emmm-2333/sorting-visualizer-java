package org.example.sortingvisualizer.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据输入服务类
 * 
 * 该类负责解析用户输入的字符串数据和从文件加载数据，将其转换为整数数组
 */
public class DataInputService {

    /**
     * 解析输入字符串，将其转换为整数数组
     * 
     * 此方法可以处理多种分隔符（空格、逗号、分号、制表符）以及换行符，
     * 将输入字符串中的数字提取出来组成整数数组
     * 
     * @param input 用户输入的字符串，可以包含数字和各种分隔符
     * @return 解析后的整数数组
     * @throws IllegalArgumentException 当输入为空或包含非整数值时抛出异常
     */
    public int[] parseInputString(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("输入为空");
        }
        String normalized = input.replaceAll("[\n\r]+", " ");
        String[] tokens = normalized.split("[ ,;\t]+");
        List<Integer> values = new ArrayList<>();
        for (String t : tokens) {
            if (t.isEmpty()) continue;
            try {
                int v = Integer.parseInt(t);
                values.add(v);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("包含非整数值: " + t);
            }
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("未解析到任何整数");
        }
        int[] arr = new int[values.size()];
        for (int i = 0; i < values.size(); i++) arr[i] = values.get(i);
        return arr;
    }

    /**
     * 从文件中加载数据并解析为整数数组
     * 
     * 此方法读取指定文件的内容，并调用parseInputString方法将内容解析为整数数组
     * 
     * @param file 要读取的文件对象
     * @return 解析后的整数数组
     * @throws IOException 当文件读取出现问题时抛出异常
     * @throws IllegalArgumentException 当文件为空或内容无法解析时抛出异常
     */
    public int[] loadFromFile(File file) throws IOException, IllegalArgumentException {
        if (file == null) {
            throw new IllegalArgumentException("未选择文件");
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return parseInputString(sb.toString());
    }
}