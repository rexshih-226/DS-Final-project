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
    private String plainText;

    private java.util.Map<String, Integer> freq;

    public WordCounter(String urlStr) {
        this.urlStr = urlStr;
    }

    private String fetchContent() throws IOException {
        URL url = new URL(this.urlStr);
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        StringBuilder retVal = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            retVal.append(line).append("\n"); // （避免用 String += 造成 O(N^2) 的效能問題）
        }
        return retVal.toString();
    }

    // 取得純文字內容（去除 HTML 標籤、script、style）。
    private String getPlainText() throws IOException {

        if (plainText != null)
            return plainText;

        if (content == null || content.isEmpty()) {
            content = fetchContent();
        }

        if (content == null) {
            plainText = "";
            return plainText;
        }

        String upper = content.toUpperCase();

        upper = upper.replaceAll("(?s)<script.*?</script>", " ");
        upper = upper.replaceAll("(?s)<style.*?</style>", " ");
        upper = upper.replaceAll("(?s)<[^>]+>", " ");
        plainText = upper;
        return plainText;
    }

    // 建一個表然後關鍵字直接用查的，把網頁裡每個字出現的次數都記錄下來，用空間換時間反正網頁不大哈哈
    private void ensureFreq() throws IOException {
        // 若已建立過統計表，直接回傳（避免每次 countKeyword 都重掃全文）
        if (freq != null)
            return;

        String text = getPlainText();

        freq = new java.util.HashMap<>();

        if (text == null || text.isEmpty())
            return;

        // 以 tokenPattern 掃描全文找出 token
        Pattern tokenPattern = Pattern.compile("[A-Z0-9_]+");
        Matcher m = tokenPattern.matcher(text);

        // 每找到一個 token，次數 +1
        // freq.merge(token, 1, Integer::sum) 等價於：
        // freq.put(token, freq.getOrDefault(token, 0) + 1)
        while (m.find()) {
            String token = m.group();
            freq.merge(token, 1, Integer::sum);
        }
    }

    public int countKeyword(String keyword) throws IOException {

        if (keyword == null || keyword.isEmpty())
            return 0;

        // 確保 freq 統計表已建立
        ensureFreq();

        String upperKeyword = keyword.toUpperCase();

        return freq.getOrDefault(upperKeyword, 0);
    }

    public static List<String> extractLinks(String html) {
        List<String> links = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "<a\\s+(?:[^>]*?\\s+)?href=[\"']([^\"'#]+)[\"']",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String link = matcher.group(1);

            if (link.startsWith("http://") || link.startsWith("https://"))
                links.add(link);

            if (links.size() >= 5)
                break;
        }
        return links;
    }

    /**
     * 讓外部直接取得原始 HTML。
     * - 若尚未抓取則抓取一次並快取
     */
    public String getContent() throws IOException {
        if (content == null)
            content = fetchContent();
        return content;
    }
}
