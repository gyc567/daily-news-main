package com.ll.drissonPage.page;

import lombok.Getter;
import lombok.Setter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public class Timeout {
    private final ChromiumBase page;
    @Setter
    private Double base = 10.0;
    @Setter
    private Double pageLoad = 30.0;
    @Setter

    private Double script = 30.0;

    public Timeout(ChromiumBase page, Double base, Double pageLoad, Double script) {
        this.page = page;
        if (base != null && base >= 0) this.base = base;
        if (pageLoad != null && pageLoad >= 0) this.pageLoad = pageLoad;
        if (script != null && script >= 0) this.script = script;
    }

    public Timeout(ChromiumBase page, Integer base, Integer pageLoad, Integer script) {
        this.page = page;
        if (base != null && base >= 0) this.base = Double.valueOf(base);
        if (pageLoad != null && pageLoad >= 0) this.pageLoad = Double.valueOf(pageLoad);
        if (script != null && script >= 0) this.script = Double.valueOf(script);
    }

    public Timeout(ChromiumBase page) {
        this(page, -1.0, -1.0, -1.0);
    }

    @Override
    public String toString() {
        return "{base=" + base + ", pageLoad=" + pageLoad + ", script=" + script + '}';
    }

    // 深拷贝方法
    // 深拷贝方法
    public Timeout copy() {
        return new Timeout(this.page, this.base, this.pageLoad, this.script);
    }
}
