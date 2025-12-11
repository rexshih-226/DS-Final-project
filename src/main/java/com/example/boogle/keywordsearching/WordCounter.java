package com.example.boogle.keywordsearching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordCounter {
    private String urlStr;
    private String content;

    public WordCounter(String urlStr) {
        this.urlStr = urlStr;
    }

    private String fetchContent() throws IOException{
		URL url = new URL(this.urlStr);
		URLConnection conn = url.openConnection();
		InputStream in = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
	
		String retVal = "";
	
		String line = null;
		
		while ((line = br.readLine()) != null){
		    retVal = retVal + line + "\n";
		}
	
		return retVal;
    }

    public int countKeyword(String keyword) throws IOException {
        // 若沒成功載入內容，直接返回 0
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
    }

    // 抓取所有連結 <a href="...">
    public static List<String> extractLinks(String html) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=[\"']([^\"'#]+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String link = matcher.group(1);
            if(link.startsWith("http://") || link.startsWith("https://"))
                links.add(link);
            if(links.size() >= 5) break; // 最多五個連結
        }
        return links;
    }

    // 讓外部可直接取 HTML
    public String getContent() throws IOException {
        if (content == null)
            content = fetchContent();
        return content;
    }
}
