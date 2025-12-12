package com.example.boogle.keywordsearching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private String fetchContent() throws IOException {
        URL url = new URL(this.urlStr);
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String retVal = "";

        String line = null;

        while ((line = br.readLine()) != null) {
            retVal = retVal + line + "\n";
        }

        return retVal;
    }

    private String getPlainText() throws IOException {
        if (content == null || content.isEmpty()) {
            content = fetchContent();
        }
        String upper = content.toUpperCase();

        // 先砍掉 <script> 和 <style> 內容
        upper = upper.replaceAll("(?s)<script.*?</script>", " ");
        upper = upper.replaceAll("(?s)<style.*?</style>", " ");

        // 再砍掉所有 HTML tag
        upper = upper.replaceAll("(?s)<[^>]+>", " ");

        return upper;
    }

    public int countKeyword(String keyword) throws IOException {
        String text = getPlainText();
        if (text == null || text.isEmpty())
            return 0;

        String upperKeyword = keyword.toUpperCase();

        Pattern p = Pattern.compile("\\b" + Pattern.quote(upperKeyword) + "\\b");
        Matcher m = p.matcher(text);

        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    // 抓取所有連結 <a href="...">
    public static List<String> extractLinks(String html) {
        List<String> links = new ArrayList<>();
        Pattern pattern = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=[\"']([^\"'#]+)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String link = matcher.group(1);
            if (link.startsWith("http://") || link.startsWith("https://"))
                links.add(link);
            if (links.size() >= 5)
                break; // 最多五個連結
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
