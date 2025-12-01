package com.ll.news.common.wss;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class WssConnectOption {

    private int port = 443;

    private String host;

    /**
     * 连接使用的uri
     */
    private String uri;

    /**
     * 原始uri
     */
    private String origUri;

    private Boolean ssl = true;

}
