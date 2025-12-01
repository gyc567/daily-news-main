package com.ll.drissonPage.units.listener;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class FailInfo {
    @Getter
    private final DataPacket dataPacket;
    private final JSONObject failInfo;
    public String errorText;
    public boolean canceled;
    public String blockedReason;
    public String corsErrorStatus;

    public FailInfo(DataPacket dataPacket, JSONObject failInfo) {
        this.dataPacket = dataPacket;
        this.failInfo = failInfo;
        if (failInfo != null) {
            Object value = failInfo.get("errorText");
            if (value != null) this.errorText = value.toString();
            value = failInfo.get("canceled");
            if (value != null) this.canceled = Boolean.parseBoolean(value.toString());
            value = failInfo.get("blockedReason");
            if (value != null) this.blockedReason = value.toString();
            value = failInfo.get("corsErrorStatus");
            if (value != null) this.corsErrorStatus = value.toString();
        }
    }

    public Object get(Object item) {
        if (failInfo != null && !failInfo.isEmpty()) return this.failInfo.get(item.toString());
        return null;
    }

}
