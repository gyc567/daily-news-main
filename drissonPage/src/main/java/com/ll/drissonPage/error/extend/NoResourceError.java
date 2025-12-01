package com.ll.drissonPage.error.extend;

import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class NoResourceError extends BaseError {
    public NoResourceError(String info) {
        super(info);
    }

    public NoResourceError() {
        super("该元素无可保存的内容或保存失败。");
    }
}
