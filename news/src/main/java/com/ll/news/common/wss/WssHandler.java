package com.ll.news.common.wss;

import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;

import javax.annotation.Nullable;

public interface WssHandler {

    WssContext wssContext();

    void wssContext(WssContext wssContext);

    void onInit();

    void beforeConnect();

    void onConnect(WebSocket websocket);

    void onClosed(@Nullable WebSocket webSocket);

    void onTextMsg(WebSocket webSocket, String text);

    void onBinaryMsg(WebSocket webSocket, byte[] bytes);

    void frameHandler(WebSocket webSocket, WebSocketFrame frame);

}
