package com.ll.drissonPage.functions;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class HttpUrlUtils {
    private HttpUrlUtils() {
    }

    /**
     * 单位毫秒
     */
    public static int connectTimeout = 5000;//毫秒
    /**
     * 单位毫秒
     */
    public static int readTimeout = 5000;//毫秒

    public static String post(String url, Map<String, String> params) throws IOException {
        return post(url, null, params);
    }

    public static String post(String url, Map<String, String> header, Map<String, String> params) throws IOException {
        // 创建 URL 对象
        URL obj = new URL(url);
        // 打开 URL 连接
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        if (header != null) header.forEach(con::setRequestProperty);
        // 设置请求方法为 POST
        con.setRequestMethod("POST");
        con.setConnectTimeout(connectTimeout); // 5 秒钟
        con.setReadTimeout(readTimeout);//5秒
        // 启用输出流，以便写入请求体参数
        con.setDoOutput(true);

        // 构建请求体参数字符串
        if (params != null && !params.isEmpty()) {
            StringBuilder requestBody = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                requestBody.append(entry.getKey());
                requestBody.append("=");
                requestBody.append(entry.getValue());
                requestBody.append("&");
            }
            requestBody.deleteCharAt(requestBody.length() - 1);
            // 将请求体参数写入输出流
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                byte[] postData = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                wr.write(postData);
            }
        }
        // 获取响应码
//        int responseCode = con.getResponseCode();
//        System.out.println("Response Code: " + responseCode);
        StringBuilder response = new StringBuilder();
        // 读取响应内容
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        // 打印响应内容
//        System.out.println("Response Body: " + response.toString());
        return response.toString();
    }

    public static String get(String url) throws IOException {
        return get(url, new HashMap<>());
    }

    public static String get(String url, Map<String, String> header) throws IOException {
        URL obj = new URL(url);
        // 打开 URL 连接
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // 设置请求方法为 GET
        con.setRequestMethod("GET");
        con.setConnectTimeout(connectTimeout); // 5 秒钟
        con.setReadTimeout(readTimeout);//5秒
        if (header != null) header.forEach(con::setRequestProperty);
        // 获取响应码
//        int responseCode = con.getResponseCode();
//        System.out.println("Response Code: " + responseCode);

        StringBuilder response = new StringBuilder();
        // 读取响应内容
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        ;

        // 打印响应内容
//        System.out.println("Response Body: " + response.toString());
        return response.toString();
    }


}
