package com.ll.drissonPage.units.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ResponseExtraInfo extends ExtraInfo {
    public String requestId;
    public JSONArray blockedCookies;
    public String resourceIPAddressSpace;
    public int statusCode;
    public String cookiePartitionKey;
    public boolean cookiePartitionKeyOpaque;

    public ResponseExtraInfo(JSONObject extraInfo) {
        super(extraInfo);
        if (extraInfo != null) {
            Object value = extraInfo.get("requestId");
            if (value != null) this.requestId = extraInfo.getString("requestId");
            value = extraInfo.get("blockedCookies");
            if (value != null) this.blockedCookies = extraInfo.getJSONArray("blockedCookies");
            value = extraInfo.get("resourceIPAddressSpace");
            if (value != null) this.resourceIPAddressSpace = extraInfo.getString("resourceIPAddressSpace");
            value = extraInfo.get("statusCode");
            if (value != null) this.statusCode = extraInfo.getInteger("statusCode");
            value = extraInfo.get("cookiePartitionKey");
            if (value != null) this.cookiePartitionKey = extraInfo.getString("cookiePartitionKey");
            value = extraInfo.get("requestId");
            if (value != null) this.requestId = extraInfo.getString("requestId");
            value = extraInfo.get("cookiePartitionKeyOpaque");
            if (value != null) this.cookiePartitionKeyOpaque = extraInfo.getBoolean("cookiePartitionKeyOpaque");

        }
    }
}
