package org.example.sortingvisualizer.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataInputService {

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
