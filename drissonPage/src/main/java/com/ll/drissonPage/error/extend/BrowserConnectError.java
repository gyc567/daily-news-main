package com.ll.drissonPage.error.extend;

import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class BrowserConnectError extends BaseError {
    public BrowserConnectError(String info) {
        super(info);
    }

    public BrowserConnectError() {
        super("浏览器连接失败。");
    }
}
