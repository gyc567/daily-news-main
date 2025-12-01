package com.ll.drissonPage.error.extend;

import com.ll.drissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class GetDocumentError extends BaseError {
    public GetDocumentError(String info) {
        super(info);
    }

    public GetDocumentError() {
        super("获取文档失败。");
    }
}
