package com.ll.drissonPage.units.setter;

import com.ll.drissonPage.element.ChromiumElement;
import com.ll.drissonPage.error.extend.ElementLostError;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class ChromiumElementSetter {
    private final ChromiumElement ele;

    /**
     * 设置元素attribute属性
     *
     * @param attr  属性名
     * @param value 属性值
     */
    public void attr(String attr, String value) {
        try {

            this.ele.getOwner().runCdp("DOM.setAttributeValue", Map.of("nodeId", this.ele.getNodeId(), "name", attr, "value", value));
        }catch (ElementLostError error){
            this.ele._refreshId();
            this.ele.getOwner().runCdp("DOM.setAttributeValue", Map.of("nodeId", this.ele.getNodeId(), "name", attr, "value", value));

        }

    }


    /**
     * 设置元素property属性
     *
     * @param prop  属性名
     * @param value 属性值
     */
    public void prop(String prop, String value) {
        value = value.replace("\"", "\\\"");
        this.ele.runJs("this." + prop + "=\"" + value + "\";");
    }

    /**
     * 设置元素innerHTML
     *
     * @param html html文本
     */
    public void innerHTML(String html) {
        this.prop("innerHTML", html);
    }

    /**
     * 设置元素value值
     *
     * @param value value值
     */
    public void value(String value) {
        this.prop("value", value);
    }

}
