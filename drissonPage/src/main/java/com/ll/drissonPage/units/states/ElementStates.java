package com.ll.drissonPage.units.states;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.drissonPage.element.ChromiumElement;
import com.ll.drissonPage.error.extend.CDPError;
import com.ll.drissonPage.error.extend.NoRectError;
import com.ll.drissonPage.functions.Web;
import com.ll.drissonPage.units.Coordinate;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class ElementStates {
    private final ChromiumElement ele;

    /**
     * @return 返回元素是否被选择
     */
    public boolean isSelected() {
        Object o = this.ele.runJs("return this.selected;");

        return o != null && Boolean.parseBoolean(o.toString());
    }

    /**
     * @return 返回元素是否被点击
     */
    public boolean isChecked() {
        Object o = this.ele.runJs("return this.checked;");
        return o != null && Boolean.parseBoolean(o.toString());
    }

    /**
     * @return 返回元素是否显示
     */
    public boolean isDisplayed() {
        return !(this.ele.style("visibility").equals("hidden") || Boolean.parseBoolean(this.ele.runJs("return this.offsetParent === null;").toString()) || this.ele.style("display").equals("none") || Boolean.parseBoolean(this.ele.property("hidden")));
    }

    /**
     * @return 返回元素是否可用
     */
    public boolean isEnabled() {
        Object o = this.ele.runJs("return this.disabled;");
        return o == null || !Boolean.parseBoolean(o.toString());
    }

    /**
     * @return 返回元素是否仍在DOM中
     */
    public boolean isAlive() {
        try {
            JSONObject jsonObject = JSON.parseObject(this.ele.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", this.ele.getBackendId())).toString());
            return jsonObject.getJSONObject("node").getInteger("nodeId") != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return 返回元素是否出现在视口中，以元素click_point为判断
     */
    public boolean isInViewport() {
        Coordinate coordinate = this.ele.rect().clickPoint();
        return coordinate != null && Web.locationInViewport(this.ele.getOwner(), coordinate);
    }

    /**
     * @return 返回元素是否整个都在视口内
     */
    public boolean isWholeInViewport() {
        Coordinate location = this.ele.rect().location();
        Coordinate size = this.ele.rect().size();
        return Web.locationInViewport(this.ele.getOwner(), location) && Web.locationInViewport(this.ele.getOwner(), new Coordinate(location.getX() + size.getX(), location.getY() + size.getX()));
    }

    /**
     * @return 返回元素是否被覆盖，与是否在视口中无关，如被覆盖返回覆盖元素的backend id，否则返回null
     */
    public Integer isCovered() {
        Coordinate coordinate = this.ele.rect().clickPoint();
        try {
            Integer integer = JSON.parseObject(this.ele.getOwner().runCdp("DOM.getNodeForLocation", Map.of("x", coordinate.getX(), "y", coordinate.getY())).toString()).getInteger("backendNodeId");
            if (!Objects.equals(integer, this.ele.getBackendId())) return integer;
            return null;
        } catch (CDPError c) {
            return null;
        }
    }

    /**
     * @return 回元素是否拥有位置和大小，没有返回null，有返回四个角在页面中坐标组成的列表
     */
    public List<Coordinate> hasRect() {
        try {
            return this.ele.rect().corners();
        } catch (NoRectError e) {
            return null;
        }
    }
}
