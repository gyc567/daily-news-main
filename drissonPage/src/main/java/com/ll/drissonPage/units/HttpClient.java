package com.ll.drissonPage.units;

import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.internal.http2.Header;

import java.util.Collection;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
@Getter
public class HttpClient {
    private OkHttpClient client;
    private Collection<? extends Header> headers;


}
