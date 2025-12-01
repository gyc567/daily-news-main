package com.ll.drissonPage.units.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Response {
    private final DataPacket dataPacket;
    private final Map<String, Object> response;
    private final String rawBody;
    private final boolean isBase64Body;
    private Object body;
    private Map<String, Object> headers;

    public String url;
    public int status;
    public String statusText;
    public String mimeType;
    public Map<String, Object> requestHeaders;
    public Boolean connectionReused;
    public int connectionId;
    public String remoteIPAddress;
    public int remotePort;
    public Boolean fromDiskCache;
    public Boolean fromServiceWorker;
    public Boolean encodedDataLength;
    public JSONObject timing;
    public String responseTime;
    public String protocol;
    public String alternateProtocolUsage;
    public String securityState;
    public JSONObject securityDetails;

    public Response(DataPacket dataPacket, JSONObject rawResponse, String rawBody, boolean base64Body) {
        this.dataPacket = dataPacket;
        this.response = rawResponse;
        this.rawBody = rawBody;
        this.isBase64Body = base64Body;
        this.body = null;
        this.headers = null;
        if (rawResponse != null) {
            Object value = rawResponse.get("url");
            if (value != null) this.url = rawResponse.getString("url");
            value = rawResponse.get("status");
            if (value != null) this.status = rawResponse.getInteger("status");
            value = rawResponse.get("statusText");
            if (value != null) this.statusText = rawResponse.getString("statusText");
            value = rawResponse.get("mimeType");
            if (value != null) this.mimeType = rawResponse.getString("mimeType");
            value = rawResponse.get("requestHeaders");
            if (value != null) this.requestHeaders = rawResponse.getJSONObject("requestHeaders");
            value = rawResponse.get("connectionReused");
            if (value != null) this.connectionReused = rawResponse.getBoolean("connectionReused");
            value = rawResponse.get("connectionId");
            if (value != null) this.connectionId = rawResponse.getInteger("connectionId");
            value = rawResponse.get("remoteIPAddress");
            if (value != null) this.remoteIPAddress = rawResponse.getString("remoteIPAddress");
            value = rawResponse.get("remotePort");
            if (value != null) this.remotePort = rawResponse.getInteger("remotePort");
            value = rawResponse.get("fromDiskCache");
            if (value != null) this.fromDiskCache = rawResponse.getBoolean("fromDiskCache");
            value = rawResponse.get("fromServiceWorker");
            if (value != null) this.fromServiceWorker = rawResponse.getBoolean("fromServiceWorker");
            value = rawResponse.get("encodedDataLength");
            if (value != null) this.encodedDataLength = rawResponse.getBoolean("encodedDataLength");
            value = rawResponse.get("timing");
            if (value != null) this.timing = rawResponse.getJSONObject("timing");
            value = rawResponse.get("responseTime");
            if (value != null) this.responseTime = rawResponse.getString("responseTime");
            value = rawResponse.get("protocol");
            if (value != null) this.protocol = rawResponse.getString("protocol");
            value = rawResponse.get("alternateProtocolUsage");
            if (value != null) this.alternateProtocolUsage = rawResponse.getString("alternateProtocolUsage");
            value = rawResponse.get("securityState");
            if (value != null) this.securityState = rawResponse.getString("securityState");
            value = rawResponse.get("securityDetails");
            if (value != null) this.securityDetails = rawResponse.getJSONObject("securityDetails");
        }

    }

    /**
     * @return 以大小写不敏感字符串返回headers数据
     */
    public Map<String, Object> headers() {
        if (this.headers == null)
            this.headers = new CaseInsensitiveMap<>(JSON.parseObject(JSON.toJSONString(this.response.get("headers"))));
        return this.headers;
    }

    /**
     * @return 返回未被处理的body文本
     */
    public String rawBody() {
        return this.rawBody;
    }

    /**
     * @return 返回body内容，如果是json格式，自动进行转换，如果时图片格式，进行base64转换，其它格式直接返回文本
     */
    public Object body() {
        if (body == null) {
            if (this.isBase64Body) {
                byte[] decodedBytes = Base64.getDecoder().decode(this.rawBody);
                this.body = new String(decodedBytes, StandardCharsets.UTF_8);
            } else {
                try {
                    this.body = JSON.parse(this.rawBody);
                } catch (JSONException e) {
                    this.body = this.rawBody;
                }
            }
        }
        return this.body;
    }

    public ResponseExtraInfo extraInfo() {
        return this.dataPacket != null && this.dataPacket.requestExtraInfo != null ? new ResponseExtraInfo(this.dataPacket.requestExtraInfo) : new ResponseExtraInfo(new JSONObject());
    }
}
