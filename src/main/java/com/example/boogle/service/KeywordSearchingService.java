package com.example.boogle.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.example.boogle.keywordsearching.WebPage;
import com.example.boogle.keywordsearching.WebTree;
import com.example.boogle.model.Keyword;

public class KeywordSearchingService {
    private ArrayList<Keyword> Keywords;

    public KeywordSearchingService(String keyword) {
        this.Keywords = new ArrayList<Keyword>();
        loadKeywordsFromFile("keywords.txt");
        String[] words = keyword.split("\\s+"); // 以空白分隔
        for (String w : words) {
            Keywords.add(new Keyword(w.toUpperCase(), 10.0));
        }
    }

    private void loadKeywordsFromFile(String filePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String name = parts[0].trim().toUpperCase();
                    double weight = Double.parseDouble(parts[1].trim());
                    Keywords.add(new Keyword(name, weight));
                    System.out.printf("[LoadKeyword] name=%s, weight=%.2f%n", name, weight);
                }
            }
        } catch (Exception e) {
            System.err.println("Fail to get the keyword file：" + e.getMessage());
        }
    }

    public List<Keyword> getKeywords() {
        return Keywords;
    }

    public double calculateScore(String url) {
        double score = 0.0;
        try {
            System.out.println("================================================");
            System.out.println("[Analyze Site] " + url);

            WebPage root = new WebPage(url, "root");
            WebTree webTree = new WebTree(root);
            webTree.buildAutomatically();
            webTree.setPostOrderScore(Keywords);

            System.out.println("[WebTree] structure & node scores:");
            webTree.eularPrintTree(); // ★ 印出樹狀結構 + nodeScore
            System.out.println("Root nodeScore = " + webTree.root.nodeScore);
            System.out.println("================================================");

            return webTree.root.nodeScore;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }

}
