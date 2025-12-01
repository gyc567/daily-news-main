package com.ll.dataRecorder;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据采集器
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public abstract class BaseRecorder<D> extends OriginalRecorder<D> {
    protected String encoding = "utf-8";
    protected List<Map<Object, Object>> before = new ArrayList<>();
    protected List<Map<Object, Object>> after = new ArrayList<>();
    protected String table;

    public BaseRecorder() {
        this("");
    }

    public BaseRecorder(Path path) {
        this(path, null);
    }

    public BaseRecorder(Integer cacheSize) {
        this("", cacheSize);
    }

    public BaseRecorder(String path) {
        this(path, null);
    }

    public BaseRecorder(Path path, Integer cacheSize) {
        this(path.toAbsolutePath().toString(), cacheSize);
    }

    public BaseRecorder(String path, Integer cacheSize) {
        super(path, cacheSize);
    }

    /**
     * @return 返回用于设置属性的对象
     */
    @Override
    public BaseSetter<?> set() {
        if (super.setter == null) super.setter = new BaseSetter<>(this);
        return (BaseSetter<?>) super.setter;
    }

    /**
     * @return 返回当前before内容
     */
    public List<Map<Object, Object>> before() {
        return this.before;
    }

    /**
     * @return 返回当前before内容
     */
    public List<Map<Object, Object>> after() {
        return this.after;
    }

    /**
     * @return 返回默认表名
     */
    public String table() {
        return this.table;
    }

    /**
     * @return 返回编码格式
     */
    public String encoding() {
        return this.encoding;
    }

}
