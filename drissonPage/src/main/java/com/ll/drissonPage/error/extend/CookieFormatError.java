package com.ll.drissonPage.error.extend;

import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class CookieFormatError extends BaseError {
    public CookieFormatError(String info) {
        super(info);
    }

    public CookieFormatError() {
        super("cookie格式不正确.");
    }
}
