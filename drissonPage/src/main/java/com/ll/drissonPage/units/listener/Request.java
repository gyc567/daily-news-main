package com.ll.drissonPage.units.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Request {
    protected String url;
    protected CaseInsensitiveMap<String, Object> headers;
    protected String method;
    public String urlFragment;
    public Object hasPostData;
    public String mixedContentType;
    public String initialPriority;

    public String referrerPolicy;
    public Object isLinkPreload;
    public Object trustTokenParams;
    public Boolean isSameSite;
    private final Map<String, Object> request;
    private final DataPacket dataPacket;
    private final String rawPostData;
    private String postData;

    public Request(DataPacket dataPacket, JSONObject rawRequest, String postData) {
        this.dataPacket = dataPacket;
        if (rawRequest != null) {
            Object value = rawRequest.get("url");
            if (value != null) this.url = value.toString();
            value = rawRequest.get("method");
            if (value != null) this.method = value.toString();
            value = rawRequest.get("urlFragment");
            if (value != null) this.urlFragment = value.toString();
            value = rawRequest.get("hasPostData");
            if (value != null) this.hasPostData = value;
            value = rawRequest.get("mixedContentType");
            if (value != null) this.mixedContentType = value.toString();
            value = rawRequest.get("initialPriority");
            if (value != null) this.initialPriority = value.toString();
            value = rawRequest.get("referrerPolicy");
            if (value != null) this.referrerPolicy = value.toString();
            value = rawRequest.get("isLinkPreload");
            if (value != null) this.isLinkPreload = value;
            value = rawRequest.get("trustTokenParams");
            if (value != null) this.trustTokenParams = value;
            value = rawRequest.get("isSameSite");
            if (value != null)
                this.isSameSite = Boolean.parseBoolean(rawRequest.getOrDefault("isSameSite", "").toString());
        }
        this.request = rawRequest;
        this.rawPostData = postData;
        this.postData = null;
    }

    /**
     * @return 以大小写不敏感字符串返回headers数据
     */
    public Map<String, Object> headers() {
        if (this.headers == null)
            this.headers = new CaseInsensitiveMap<>(JSON.parseObject(JSON.toJSONString(this.request.get("headers"))));
        return this.headers;
    }

    /**
     * @return 返回postData数据 如果是其他类型则会格式化成string
     */
    public String postData() {
        if (this.postData == null) {
            Object postData;
            if (this.rawPostData != null) {
                postData = this.rawPostData;
            } else if (this.request.get("postData") != null) {
                postData = this.request.get("postData");
            } else {
                postData = false;
            }
            try {
                this.postData = JSON.parse(postData.toString()).toString();
            } catch (JSONException e) {
                this.postData = postData.toString();
            }
        }
        return this.postData;
    }


    public Map<String, Object> cookies() {
        Map<String, Object> map = new HashMap<>();
        for (Object associatedCookie : this.extraInfo().associatedCookies) {
            JSONObject jsonObject = JSON.parseObject(associatedCookie.toString());
            if (jsonObject.get("blockedReasons") != null)
                map.putAll(JSON.parseObject(jsonObject.getString("cookie")));
        }

        return map;
    }

    /**
     *
     * @return 返回额外数据
     */

    public RequestExtraInfo extraInfo() {
        return new RequestExtraInfo(this.dataPacket.requestExtraInfo == null ? new JSONObject() : this.dataPacket.requestExtraInfo);
    }
}
