package com.ll.news.common.wss.quote;

import com.ll.news.common.wss.BaseWssHandler;
import com.ll.news.common.wss.WssContext;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketFrame;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class KeepAliveBaseWssHandler extends BaseWssHandler {
    private static final Logger log = LoggerFactory.getLogger(KeepAliveBaseWssHandler.class);

    public void frameHandler(WebSocket webSocket, WebSocketFrame frame) {
        if (frame.isPing()) {
            onPing(webSocket, frame);
        }
    }

    public void onPing(WebSocket webSocket, WebSocketFrame frame) {
        try {
            Buffer data = frame.binaryData();
            webSocket.writePong(data);
            if (log.isDebugEnabled()) {
                log.debug("receive ping and send pong {}", data.toString());
            }
        } catch (Exception e) {
            // ignore
        }
    }


    @Override
    public void onInit() {

        // 定时发送 ping 保活
        timerKeepAlive();

    }

    public void timerKeepAlive() {
        WssContext wssContext = wssContext();
        Vertx vertx = wssContext.getVertx();
        vertx.setTimer(TimeUnit.SECONDS.toMillis(RandomUtils.nextInt(5, 10)), id -> {
            if (wssContext.connectedStatus()) {
                WebSocket webSocket = wssContext.getWssConnector().getWebSocket();
                webSocket.writePing(Buffer.buffer());
            }
            timerKeepAlive();
        });
    }

}
