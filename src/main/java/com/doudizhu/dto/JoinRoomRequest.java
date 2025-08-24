package com.doudizhu.dto;

import lombok.Data;

/**
 * 加入房间请求DTO
 */
@Data
public class JoinRoomRequest {
    private String roomId;
    private String playerName;
    
    public JoinRoomRequest() {}
    
    public JoinRoomRequest(String roomId, String playerName) {
        this.roomId = roomId;
        this.playerName = playerName;
    }
}