package com.example.boogle.keywordsearching;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class WordCounter {
	private String urlStr;
    private String content;
    
    public WordCounter(String urlStr){
    	this.urlStr = urlStr;
    }
    
    public String fetchContent() throws IOException {
        HttpURLConnection conn = null;
        BufferedReader br = null;
        StringBuilder retVal = new StringBuilder();
        try {
            URL url = new URL(this.urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false); // ✅ 不處理 redirect
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int status = conn.getResponseCode();

            //若非 200 OK 或為 redirect，直接略過
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
            if (br != null) br.close();
            if (conn != null) conn.disconnect();
        }
        return retVal.toString();
    }

    public int countKeyword(String keyword) throws IOException {
        //若沒成功載入內容，直接返回 0
        if (content == null || content.isEmpty()) return 0;


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
    }

    //抓取所有連結 <a href="...">
    public static List<String> extractLinks(String html) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=[\"']([^\"'#]+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String link = matcher.group(1);
            if (link.startsWith("http")) links.add(link);
        }
        return links;
    }

    // 讓外部可直接取 HTML
    public String getContent() throws IOException {
        if (content == null) content = fetchContent();
        return content;
    }
}
