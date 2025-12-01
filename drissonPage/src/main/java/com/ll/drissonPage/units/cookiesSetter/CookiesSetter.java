package com.ll.drissonPage.units.cookiesSetter;

import com.ll.drissonPage.functions.Web;
import com.ll.drissonPage.page.ChromiumBase;
import lombok.AllArgsConstructor;
import okhttp3.Cookie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class CookiesSetter {
    private final ChromiumBase page;

    /**
     * 删除一个cookie
     *
     * @param name cookie的name字段
     */
    public void remove(String name) {
        remove(name, null);
    }

    /**
     * 删除一个cookie
     *
     * @param name cookie的name字段
     * @param url  cookie的url字段，可选
     */
    public void remove(String name, String url) {
        remove(name, url, null);
    }

    /**
     * 删除一个cookie
     *
     * @param name   cookie的name字段
     * @param url    cookie的url字段，可选
     * @param domain cookie的domain字段，可选
     */
    public void remove(String name, String url, String domain) {
        remove(name, url, domain, null);
    }

    /**
     * 删除一个cookie
     *
     * @param name   cookie的name字段
     * @param url    cookie的url字段，可选
     * @param domain cookie的domain字段，可选
     * @param path   cookie的path字段，可选
     */
    public void remove(String name, String url, String domain, String path) {
        if (name == null) throw new NullPointerException("name is not null");
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        if (url != null && !url.isEmpty()) map.put("url", url);
        if (domain != null && !domain.isEmpty()) map.put("domain", domain);
        if (url == null && domain == null) map.put("url", page.url());
        if (path != null && !path.isEmpty()) map.put("path", path);
        this.page.runCdp("Network.deleteCookies", map);
    }

    public void add(Cookie cookies) {
        Web.setBrowserCookies(this.page, List.of(cookies));
    }

    public void add(List<Map<String, Object>> cookies) {
        Web.setBrowserCookies(this.page, cookies);
    }

    public void add(Map<String, Object> cookies) {
        Web.setBrowserCookies(this.page, cookies);
    }

    public void add(Cookie[] cookies) {
        Web.setBrowserCookies(this.page, List.of(cookies));
    }

    public void add(String cookies) {
        Web.setBrowserCookies(this.page, List.of(cookies));
    }

    public void add(String[] cookies) {
        Web.setBrowserCookies(this.page, List.of(cookies));
    }

    /**
     * 清除cookies
     */
    public void clear() {
        this.page.runCdp("Network.clearBrowserCookies");
    }
}
