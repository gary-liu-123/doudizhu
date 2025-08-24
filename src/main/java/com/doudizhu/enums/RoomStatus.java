package com.doudizhu.enums;

/**
 * 房间状态枚举
 */
public enum RoomStatus {
    WAITING("等待玩家"),
    GAMING("游戏中"),
    PLAYING("游戏中"), // 保持兼容性
    FINISHED("已结束");

    private final String description;

    RoomStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}