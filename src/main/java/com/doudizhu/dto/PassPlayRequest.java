package com.doudizhu.dto;

import lombok.Data;

/**
 * 跳过出牌请求DTO
 */
@Data
public class PassPlayRequest {
    private String roomId;
    private String playerId;
    private String playerName;
}