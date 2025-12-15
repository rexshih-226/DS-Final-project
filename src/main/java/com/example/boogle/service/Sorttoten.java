package com.example.boogle.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.example.boogle.model.Keyword;

public class Sorttoten {
    private ArrayList<Keyword> Keywords;


    public Sorttoten(String keyword) {
        this.Keywords = new ArrayList<Keyword>();
        loadKeywordsFromFile("keywords.txt");
        String[] words = keyword.split("\\s+"); // 以空白分隔
        for (String w : words) {
            Keywords.add(new Keyword(w.toUpperCase(), 10.0));
        }
    }

    public double calculateScore(String title, String snippet) throws IOException{
        double score = 0.0;
        String content = title + " " + snippet;
        for (Keyword k : Keywords) {
            try {
                int number = countKeyword(content, k.name);

                System.out.printf("[KeywordCount] snippet=%s, keyword=%s, count=%d%n",
                        content, k.name, number);

                score += number * k.weight;
            } catch (IOException e) {
                System.err.println("Count keyword failed. snippet=" + title
                        + ", keyword=" + k.name + ", msg=" + e.getMessage());
            }
        }
        return score;
    }

    public int countKeyword(String snippet, String keyword) throws IOException{
		
		snippet = snippet.toUpperCase();
	
		int retVal = 0;
		int fromIdx = 0;
		int found = -1;
	
		while ((found = snippet.indexOf(keyword, fromIdx)) != -1){
		    retVal++;
		    fromIdx = found + keyword.length();
		}
	
		return retVal;
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
}
