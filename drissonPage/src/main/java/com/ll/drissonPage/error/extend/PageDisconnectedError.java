package com.ll.drissonPage.error.extend;


import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

public class PageDisconnectedError extends BaseError {

    public PageDisconnectedError() {
        super("与页面的连接已断开");
    }

    public PageDisconnectedError(String info) {
        super(info);
    }
}
