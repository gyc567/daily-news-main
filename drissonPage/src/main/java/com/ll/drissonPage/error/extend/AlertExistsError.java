package com.ll.drissonPage.error.extend;

import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class AlertExistsError extends BaseError {
    public AlertExistsError(String info) {
        super(info);
    }

    public AlertExistsError() {
        super("存在未处理的提示框");
    }
}
