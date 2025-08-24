package com.doudizhu.dto;

import lombok.Data;

import java.util.List;

/**
 * 出牌请求DTO
 */
@Data
public class PlayCardRequest {
    private String roomId;
    private String playerId;
    private String playerName;
    private List<String> cardIds; // 要出的卡牌ID列表
}