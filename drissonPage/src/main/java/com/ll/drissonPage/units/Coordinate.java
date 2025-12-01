package com.ll.drissonPage.units;

import lombok.Getter;

import java.util.Objects;

/**
 * 坐标
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

@Getter
public class Coordinate {
    /**
     * 横坐标
     */
    private Integer x;
    /**
     * 纵坐标
     */
    private Integer y;

    public Coordinate(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(Integer x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate)) return false;
        Coordinate that = (Coordinate) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    @Override
    public String toString() {
        return "Coordinate{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}