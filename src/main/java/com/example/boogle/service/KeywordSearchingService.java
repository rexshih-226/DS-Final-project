package com.example.boogle.service;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.boogle.model.Keyword;

public class KeywordSearchingService {
    ArrayList<Keyword> fixedKeywords;
    Keyword searchingKeyword;

    public KeywordSearchingService(String keyword) {
        this.searchingKeyword = new Keyword(keyword.toUpperCase(), 10);
        this.fixedKeywords = new ArrayList<Keyword>();
        fixedKeywords.add(new Keyword("JAVA", 5.0));
        fixedKeywords.add(new Keyword("CLASS", 4.0));
        fixedKeywords.add(new Keyword("METHOD", 4.0));
        fixedKeywords.add(new Keyword("INERFACE", 3.0));
        fixedKeywords.add(new Keyword("JAVASCRIPT", -3.0));
    }

    public double calculateScore(String url) {
        double score = 0.0;
        try {
            String content = FetchContent(url);
            int searchingKeywordCount = WordCounter(content, searchingKeyword.name);
            score += searchingKeywordCount * searchingKeyword.weight;

            for (Keyword kw : fixedKeywords) {
                int count = WordCounter(content, kw.name);
                score += count * kw.weight;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return score;
    }

    public int WordCounter(String content, String keyword) {
        try {
            if (content == null || content.isEmpty())
                return 0;

            content = content.toUpperCase();
            keyword = keyword.toUpperCase();

            int retVal = 0;
            int fromIdx = 0;
            int found = -1;

            while ((found = content.indexOf(keyword, fromIdx)) != -1) {
                retVal++;
                fromIdx = found + keyword.length();
            }
            return retVal;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String FetchContent(String urlStr) throws IOException {
        HttpURLConnection conn = null;
        BufferedReader br = null;
        StringBuilder retVal = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false); // ✅ 不處理 redirect
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int status = conn.getResponseCode();

            // 若非 200 OK 或為 redirect，直接略過
            if (status != HttpURLConnection.HTTP_OK || (status >= 300 && status < 400)) {
                System.out.println("略過無法載入或被重導向的網站：" + urlStr);
                return "";
            }

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                retVal.append(line).append("\n");
            }
        } catch (Exception e) {
            System.out.println("載入失敗：" + urlStr + "，略過。");
            return ""; // 忽略任何錯誤
        } finally {
            if (br != null)
                br.close();
            if (conn != null)
                conn.disconnect();
        }
        return retVal.toString();
    }

}
