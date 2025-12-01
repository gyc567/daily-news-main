package com.ll.news.bitcoin;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum EntityTypeEnum {
    // 0:unknown,1:ETFs,2:Countries,3:Public Companies,4:Private Companies,5:BTC Mining Companies,6:Defi
    UNKNOWN(0, "unknown", "未知", "未知"),
    ETFS(1, "ETFs", "etfs", "etfs"),
    COUNTRIES(2, "Countries", "countries", "countries"),
    PUBLIC_COMPANIES(3, "Public Companies", "public", "public"),
    PRIVATE_COMPANIES(4, "Private Companies", "private", "private"),
    BTC_MINING_COMPANIES(5, "BTC Mining Companies", "miners", "Bitcoin Mining Companies that Own Bitcoin"),
    DEFI(6, "Defi", "defi", "defi"),
    ;

    private Integer type;
    private String name;
    private String mark;
    private String title;


    public static final Map<String, EntityTypeEnum> enumMap = new HashMap<>();

    static {
        for (EntityTypeEnum entityTypeEnum : EntityTypeEnum.values()) {
            enumMap.put(entityTypeEnum.getName(), entityTypeEnum);
        }
    }


    EntityTypeEnum(Integer type, String name, String mark, String title) {
        this.type = type;
        this.name = name;
        this.mark = mark;
        this.title = title;
    }

    public static List<EntityTypeEnum> getEntityTypesWithoutUnknown() {
        return Arrays.stream(EntityTypeEnum.values())
                .filter(entityTypeEnum -> !entityTypeEnum.equals(EntityTypeEnum.UNKNOWN))
                .collect(Collectors.toList());
    }

    public static EntityTypeEnum getByType(Integer type) {
        for (EntityTypeEnum entityTypeEnum : EntityTypeEnum.values()) {
            if (entityTypeEnum.getType().equals(type)) {
                return entityTypeEnum;
            }
        }
        return null;
    }

    public static EntityTypeEnum getByName(String name) {
        for (EntityTypeEnum entityTypeEnum : EntityTypeEnum.values()) {
            if (entityTypeEnum.name().equals(name)) {
                return entityTypeEnum;
            }
        }
        return null;
    }

    public static Integer getTypeByName(String name) {
        for (EntityTypeEnum entityTypeEnum : EntityTypeEnum.values()) {
            if (entityTypeEnum.getName().equals(name)) {
                return entityTypeEnum.getType();
            }
        }
        return null;
    }
}
