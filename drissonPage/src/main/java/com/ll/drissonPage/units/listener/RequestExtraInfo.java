package com.ll.drissonPage.units.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class RequestExtraInfo extends ExtraInfo {
    public String requestId;
    public JSONArray associatedCookies;
    public Map<String, Object> headers;
    public JSONObject connectTiming;
    public Map<String, Object> clientSecurityState;
    public boolean siteHasCookieInOtherPartition;

    public RequestExtraInfo(JSONObject extraInfo) {
        super(extraInfo == null ? new JSONObject() : extraInfo);
        if (extraInfo != null) {
            Object value = extraInfo.get("requestId");
            if (value != null) this.requestId = value.toString();
            value = extraInfo.get("associatedCookies");
            if (value != null) this.associatedCookies = JSON.parseArray(JSON.toJSONString(value.toString()));
            value = extraInfo.get("connectTiming");
            if (value != null) this.connectTiming = JSON.parseObject(JSON.toJSONString(value.toString()));
            value = extraInfo.get("clientSecurityState");
            if (value != null) this.clientSecurityState = JSON.parseObject(JSON.toJSONString(value.toString()));
            value = extraInfo.get("siteHasCookieInOtherPartition");
            if (value != null) this.siteHasCookieInOtherPartition = Boolean.parseBoolean(value.toString());
        }

    }
}
