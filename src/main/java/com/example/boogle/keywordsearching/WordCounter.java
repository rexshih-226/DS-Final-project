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

    /**
     * 快取：token -> 出現次數
     * 使用 HashMap：建立後 countKeyword 查詢平均 O(1)
     */
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

    /**
     * 確保 freq（token 統計表）已建立；只建立一次。
     *
     * 演算法/流程：
     * 1) 若 freq 已存在 => 代表已 tokenize 過，直接回傳（cache）
     * 2) 取得純文字 plainText（若尚未建立會先建立）
     * 3) 建立 HashMap 作為 freq
     * 4) 用正規表示式掃描純文字取得 token
     * 5) 每個 token 計數 +1
     *
     * token 定義：
     * - Pattern: [A-Z0-9_]+
     * - 只允許大寫英文字母、數字、底線
     * - 這樣像 "CLASS_LIST" 會當成一個 token，而不是拆成 "CLASS" / "LIST"
     *
     * 時間複雜度：O(N)（Matcher 掃描整段文字一次）
     * 空間複雜度：O(K)（K=不同 token 數量）
     */
    private void ensureFreq() throws IOException {
        // 若已建立過統計表，直接回傳（避免每次 countKeyword 都重掃全文）
        if (freq != null)
            return;

        String text = getPlainText();

        // HashMap：token -> count
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

    /**
     * 回傳某個 keyword 出現次數。
     *
     * 查詢策略：
     * - 第一次查詢：ensureFreq() 會建立 token->count 統計表（O(N)）
     * - 後續查詢：直接 HashMap 查表（平均 O(1)）
     *
     * case-insensitive 作法：
     * - 建表時全文已轉大寫
     * - 查詢 keyword 也轉大寫後查表
     */
    public int countKeyword(String keyword) throws IOException {
        // 防呆：null 或空字串視為 0 次
        if (keyword == null || keyword.isEmpty())
            return 0;

        // 確保 freq 統計表已建立
        ensureFreq();

        // 查詢時同樣轉大寫，與建表格式一致
        String upperKeyword = keyword.toUpperCase();

        // O(1) 查表：不存在回 0
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

            // 只收絕對連結（http/https）
            if (link.startsWith("http://") || link.startsWith("https://"))
                links.add(link);

            // 最多五個連結就停止
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
