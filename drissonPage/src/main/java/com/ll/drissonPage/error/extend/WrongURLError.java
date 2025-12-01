package com.ll.drissonPage.error.extend;

import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class WrongURLError extends BaseError {


    public WrongURLError() {
        super("无效的url。");
    }

    public WrongURLError(String info) {
        super(info);
    }
}
