package com.doudizhu.enums;

/**
 * 游戏阶段枚举
 */
public enum GamePhase {
    WAITING("等待玩家"),
    BIDDING("叫地主阶段"),
    PLAYING("游戏中"),
    FINISHED("游戏结束");

    private final String description;

    GamePhase(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}