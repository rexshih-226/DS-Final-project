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

/**
 * WordCounter:
 * 1) 從指定 URL 抓取 HTML (content 快取)
 * 2) 將 HTML 轉成純文字：去 script/style、去 tag，並轉大寫 (plainText 快取)
 * 3) 將純文字 tokenize 成 token->count 的統計表 (freq 快取)
 * 4) countKeyword() 可用 O(1) (平均) 查表取得某個關鍵字出現次數
 *
 * 設計重點：
 * - Lazy loading：只有在需要時才抓取/處理
 * - Cache：每個昂貴步驟只做一次，避免每次查詢都掃整段文字
 * - Case-insensitive：統一轉成大寫後再統計與查詢
 */
public class WordCounter {

    /** 目標網址 */
    private String urlStr;

    /** 快取：原始 HTML 內容（只抓一次） */
    private String content;

    /** 快取：處理後的純文字（去 tag + 去 script/style + 轉大寫，只做一次） */
    private String plainText;

    /**
     * 快取：token -> 出現次數
     * 使用 HashMap：建立後 countKeyword 查詢平均 O(1)
     */
    private java.util.Map<String, Integer> freq;

    public WordCounter(String urlStr) {
        this.urlStr = urlStr;
    }

    /**
     * 從 urlStr 抓取網頁內容（HTML）。
     *
     * 演算法/流程：
     * - 開啟 URLConnection
     * - 取得 InputStream
     * - 用 BufferedReader 一行行讀入
     * - 用 StringBuilder 累加（避免用 String += 造成 O(N^2) 的效能問題）
     *
     * 時間複雜度：O(N)（N=HTML 字元數）
     * 空間複雜度：O(N)
     *
     * 注意：
     * - 原始版本未關閉資源，理想做法是 try-with-resources 以確保關閉 stream。
     * - 這裡保留原本行為，只加註解說明。
     */
    private String fetchContent() throws IOException {
        URL url = new URL(this.urlStr);
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        // 用 StringBuilder 取代一直字串相加，避免大量拷貝造成效能差
        StringBuilder retVal = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            retVal.append(line).append("\n");
        }
        return retVal.toString();
    }

    /**
     * 取得「純文字版本」的內容（只計算一次並快取）。
     *
     * 核心目的：
     * - 讓後續 tokenize 與 countKeyword 不必處理 HTML 結構
     * - 先把大小寫統一（轉大寫），達到不分大小寫的統計/查詢
     *
     * 步驟：
     * 1) 如果 plainText 已算過 => 直接回傳（cache）
     * 2) 若尚未抓 HTML => fetchContent()
     * 3) content 轉大寫
     * 4) 先移除 <script>...</script> 與 <style>...</style>（避免 JS/CSS 被當文字計數）
     * 5) 再移除所有 HTML tag：<...>
     *
     * 正規表示式說明：
     * - (?s) 啟用 DOTALL：讓 '.' 可以匹配換行，才能跨行移除 script/style/tag
     * - "<script.*?</script>"：非貪婪匹配到最近的 </script>
     * - "<[^>]+>"：匹配任何 tag（簡化版 HTML parser）
     *
     * 時間複雜度：大致 O(N)（每個 replaceAll 會掃過整串字元）
     */
    private String getPlainText() throws IOException {
        // 若已經算過純文字，直接回傳（避免重複做正則替換）
        if (plainText != null)
            return plainText;

        // lazy load：如果尚未抓取 HTML 或 content 為空，先抓取
        if (content == null || content.isEmpty()) {
            content = fetchContent();
        }

        // 若抓不到內容，避免 NPE，純文字視為空
        if (content == null) {
            plainText = "";
            return plainText;
        }

        // 統一轉大寫：之後 token 與 keyword 都用大寫，達到 case-insensitive
        String upper = content.toUpperCase();

        // 先砍掉 <script>...</script> 區塊（避免 JS 影響文字統計）
        upper = upper.replaceAll("(?s)<script.*?</script>", " ");

        // 再砍掉 <style>...</style> 區塊（避免 CSS 影響文字統計）
        upper = upper.replaceAll("(?s)<style.*?</style>", " ");

        // 最後砍掉所有 HTML tag（僅保留 tag 外面的文字）
        upper = upper.replaceAll("(?s)<[^>]+>", " ");

        // 快取結果，之後不用再做清理
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

    /**
     * 從 HTML 中抓取 <a href="..."> 的連結，最多回傳 5 個。
     *
     * 正規表示式：
     * - <a\\s+ : <a 後面至少一個空白
     * - (?:[^>]*?\\s+)? : 中間可能有其他屬性（非貪婪），直到 href 前
     * - href=["']([^\"'#]+)["'] : 抓 href 的值，並排除含 # 的片段（#fragment）
     *
     * 過濾策略：
     * - 只收集以 http:// 或 https:// 開頭的「絕對網址」
     * - 遇到相對路徑（/xxx）不處理
     * - 找到 5 個就停止，提高效率
     *
     * 時間複雜度：O(N)（掃描 HTML 一次；但找到 5 個後提前停止）
     */
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
