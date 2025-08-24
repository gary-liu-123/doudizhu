package com.doudizhu.enums;

/**
 * 扑克牌点数枚举
 */
public enum CardRank {
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K"),
    ACE(14, "A"),
    TWO(15, "2"),
    SMALL_JOKER(16, "小王"),
    BIG_JOKER(17, "大王");

    private final int value;
    private final String display;

    CardRank(int value, String display) {
        this.value = value;
        this.display = display;
    }

    public int getValue() {
        return value;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isJoker() {
        return this == SMALL_JOKER || this == BIG_JOKER;
    }
}