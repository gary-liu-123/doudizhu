package com.doudizhu.dto;

import lombok.Data;

/**
 * 创建房间请求DTO
 */
@Data
public class CreateRoomRequest {
    private String playerName;
    
    public CreateRoomRequest() {}
    
    public CreateRoomRequest(String playerName) {
        this.playerName = playerName;
    }
}