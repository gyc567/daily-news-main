package com.ll.drissonPage.error.extend;

import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

public class StorageError extends BaseError {
    public StorageError(String info) {
        super(info);
    }

    public StorageError() {
        super("无法操作当前存储数据。");
    }
}
