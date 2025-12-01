package com.ll.drissonPage.functions;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: liuPengTao
 * @date: 2024-07-01
 */
public class RpaUtil {

    /**
     * 分割字符串
     * @param text
     * @param regex
     * @return
     */
    public static String[] split(String text, String regex) {
        List<String> result = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        int lastMatchEnd = 0;
        while (matcher.find()) {
            if (lastMatchEnd != matcher.start()) {
                result.add(text.substring(lastMatchEnd, matcher.start()));
            }
            result.add(matcher.group());
            lastMatchEnd = matcher.end();
        }

        if (lastMatchEnd < text.length()) {
            result.add(text.substring(lastMatchEnd));
        }

        return result.toArray(new String[0]);
    }
}