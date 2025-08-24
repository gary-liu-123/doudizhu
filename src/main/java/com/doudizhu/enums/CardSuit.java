package com.doudizhu.enums;

/**
 * 扑克牌花色枚举
 */
public enum CardSuit {
    SPADE("♠", "黑桃"),
    HEART("♥", "红桃"),
    DIAMOND("♦", "方块"),
    CLUB("♣", "梅花"),
    JOKER("", "王"); // 大小王使用的花色

    private final String symbol;
    private final String name;

    CardSuit(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }
}