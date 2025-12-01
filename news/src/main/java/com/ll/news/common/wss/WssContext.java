package com.ll.news.common.wss;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import lombok.Data;

@Data
public class WssContext {

    private Vertx vertx;

    private Context context;

    private WssConnector wssConnector;

    private WssConnectOption option;



    // -1:禁用, 0:断开，1:连接中， 2：已连上
    private int status = -1;

    public WssContext(WssConnectOption option, Vertx vertx) {
        this.option = option;
        this.vertx = vertx;
    }

    public boolean connectedStatus() {
        return status == 2;
    }

    public void changeStatus(int status) {
        context.runOnContext(s -> {
            this.status = status;
        });
    }

    public boolean shouldConnect() {
        if (status == 0) {
            return true;
        }
        return false;
    }

}
