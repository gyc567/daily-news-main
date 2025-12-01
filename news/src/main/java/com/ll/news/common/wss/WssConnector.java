package com.ll.news.common.wss;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.impl.VertxImpl;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * WSS 连接
 * 持有一个ws连接
 * 支持自动重连
 *
 * @author cheng.t
 * @date 2024/09/20
 */
public class WssConnector {

    private static final Logger log = LoggerFactory.getLogger(WssConnector.class);

    private static final long delay = 3000L;

    @Getter
    private WssContext context;

    @Setter
    private WebSocketClient webSocketClient;

    @Getter
    protected WebSocket webSocket;

    protected WssHandler wssHandler;

    protected Long timerId;

    public WssConnector(WebSocketClient webSocketClient, WssContext context, WssHandler wssHandler) {
        Vertx vertx = context.getVertx();
        VertxImpl v = (VertxImpl) vertx;
        context.setContext(v.createEventLoopContext());
        context.setWssConnector(this);
        context.setStatus(-1);
        this.context = context;
        this.wssHandler = wssHandler;
        this.webSocketClient = webSocketClient;
        wssHandler.wssContext(context);
        wssHandler.onInit();

        monitor();
    }

    public boolean connectedStatus() {
        return context.connectedStatus();
    }

    public void reconnect() {
        context.changeStatus(0);
    }

    public void connecting() {
        context.changeStatus(1);
    }

    public void connectStatus() {
        context.changeStatus(2);
    }

    public void disableStatus() {
        context.changeStatus(-1);
    }

    public void destroyClient() {
        if (Objects.nonNull(timerId)) {
            context.getVertx().cancelTimer(timerId);
        }
        if (Objects.nonNull(webSocket)) {
            if (!webSocket.isClosed()) {
                webSocket.close();
            }
        }
    }

    protected void handlerAsync(Consumer<Void> handler) {
        context.getContext().executeBlocking(() -> {
            try {
                handler.accept(null);
            } catch (Exception e) {
                log.error("wss handlerAsync error", e);
            }
            return null;
        });
    }

    protected void connect() {
        try {
            wssHandler.beforeConnect();
        } catch (Exception e) {
            log.error("beforeConnect fail, will retry, {}", e.getMessage());
            reconnect();
            return;
        }

        WssConnectOption options = context.getOption();

        int port = options.getPort();
        String host = options.getHost();
        String uri = options.getUri();

        try {
            webSocketClient.connect(port, host, uri, r -> {
                if (r.succeeded()) {
                    connectStatus();
                    log.info("{} wss connect success, link {} ", wssHandler.getClass().getSimpleName(), host + uri);
                    this.webSocket = r.result();

                    handlerAsync(v -> {
                        wssHandler.onConnect(webSocket);
                    });

                    webSocket.frameHandler(frame -> {
                        handlerAsync(v -> {
                            wssHandler.frameHandler(webSocket, frame);
                        });
                    });

                    webSocket.closeHandler(s -> {
                        log.warn("{} close msg {}, reason {}, link {} ", wssHandler.getClass().getSimpleName(), webSocket.closeReason(), webSocket.closeReason(), host + uri);
                        disableStatus();
                        handlerAsync(v -> {
                            wssHandler.onClosed(webSocket);
                        });
                        reconnect();
                    });

                    webSocket.textMessageHandler(text -> {
                        handlerAsync(v -> {
                            try {
                                wssHandler.onTextMsg(webSocket, text);
                            } catch (Exception e) {
                                log.error("wss textMsg handle error {}", text, e);
                            }
                        });
                    });

                    webSocket.binaryMessageHandler(binary -> {
                        handlerAsync(v -> {
                            wssHandler.onBinaryMsg(webSocket, binary.getBytes());
                        });
                    });
                } else {
                    log.warn("{} wss connect failed,will retry, msg {}, link {} ", wssHandler.getClass().getSimpleName(), r.cause().getMessage(), host + uri);
                    reconnect();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    protected long getWsCheckDelay() {
        return delay;
    }

    protected void monitor() {
        context.getVertx().setPeriodic(getWsCheckDelay(), id -> {
            context.getContext().executeBlocking(() -> {
                this.timerId = id;
                // 禁用或连接中或已连接直接跳过
                if (!context.shouldConnect()) {
                    return Future.succeededFuture();
                }
                // 设置为连接中
                connecting();
                if (Objects.isNull(webSocket) || webSocket.isClosed()) {
                    connect();
                } else {
                    webSocket.close().onComplete(v -> {
                        connect();
                    });
                }
                return Future.succeededFuture();
            });
        });
    }

}
