package com.doudizhu.dto;

import lombok.Data;

/**
 * 叫地主请求DTO
 */
@Data
public class BidRequest {
    private String roomId;
    private String playerId;
    private String playerName;
    private int bidScore; // 0表示不叫，1-3表示叫分
}