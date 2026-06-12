package com.example.agent.utils;

import java.util.HashMap;
import java.util.Map;

public class SimilarityUtils {

    /**
     * 计算两个文本的简单字频余弦相似度
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;

        Map<String, Integer> v1 = getWordFrequency(s1);
        Map<String, Integer> v2 = getWordFrequency(s2);

        double dotProduct = 0;
        for (String word : v1.keySet()) {
            if (v2.containsKey(word)) {
                dotProduct += v1.get(word) * v2.get(word);
            }
        }

        double normA = Math.sqrt(v1.values().stream().mapToDouble(i -> i * i).sum());
        double normB = Math.sqrt(v2.values().stream().mapToDouble(i -> i * i).sum());

        if (normA == 0 || normB == 0) return 0.0;
        return dotProduct / (normA * normB);
    }

    private static Map<String, Integer> getWordFrequency(String text) {
        Map<String, Integer> map = new HashMap<>();
        // 简单按字拆分进行词频统计
        for (String word : text.split("")) {
            if (!word.trim().isEmpty()) {
                map.put(word, map.getOrDefault(word, 0) + 1);
            }
        }
        return map;
    }
}