package com.ll.news.common.wss;

import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;

public abstract class BaseWssHandler implements WssHandler  {

    private WssContext wssContext;

    public WssContext wssContext() {
        return wssContext;
    }

    @Override
    public void wssContext(WssContext wssContext) {
        this.wssContext = wssContext;
    }

    @Override
    public void onInit() {

    }

    @Override
    public void beforeConnect() {

    }

    @Override
    public void onConnect(WebSocket websocket) {

    }

    @Override
    public void onClosed(WebSocket webSocket) {

    }

    @Override
    public void onTextMsg(WebSocket webSocket, String text) {

    }

    @Override
    public void onBinaryMsg(WebSocket webSocket, byte[] bytes) {

    }

    @Override
    public void frameHandler(WebSocket webSocket, WebSocketFrame frame) {

    }
}
