package com.doudizhu.enums;

/**
 * 牌型枚举
 */
public enum PlayType {
    SINGLE("单牌"),
    PAIR("对子"),
    TRIPLE("三张"),
    TRIPLE_WITH_SINGLE("三带一"),
    TRIPLE_WITH_PAIR("三带二"),
    STRAIGHT("顺子"),
    PAIR_STRAIGHT("连对"),
    PLANE("飞机"),
    PLANE_WITH_SINGLE("飞机带单"),
    PLANE_WITH_PAIR("飞机带双"),
    FOUR_WITH_SINGLE("四带二（单）"),
    FOUR_WITH_PAIR("四带二（对）"),
    BOMB("炸弹"),
    ROCKET("火箭");

    private final String description;

    PlayType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}