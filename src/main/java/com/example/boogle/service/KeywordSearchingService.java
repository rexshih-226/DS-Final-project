package com.example.boogle.service;

import java.util.ArrayList;

import com.example.boogle.keywordsearching.WebPage;
import com.example.boogle.keywordsearching.WebTree;
import com.example.boogle.model.Keyword;

public class KeywordSearchingService {
    private ArrayList<Keyword> Keywords;

    public KeywordSearchingService(String keyword) {
        this.Keywords = new ArrayList<Keyword>();
        Keywords.add(new Keyword("CLASS", 4.0));
        Keywords.add(new Keyword("METHOD", 4.0));
        Keywords.add(new Keyword("INERFACE", 3.0));
        String[] words = keyword.split("\\s+"); // 以空白分隔
        for (String w : words) {
            Keywords.add(new Keyword(w.toUpperCase(), 10.0));
        }
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
