package com.doudizhu.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket消息控制器
 */
@Controller
public class WebSocketController {

    /**
     * 处理加入房间消息
     */
    @MessageMapping("/join-room")
    @SendTo("/topic/room-updates")
    public Map<String, Object> joinRoom(@Payload Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        // 获取会话ID
        String sessionId = headerAccessor.getSessionId();
        
        // 处理加入房间逻辑
        message.put("type", "PLAYER_JOINED");
        message.put("sessionId", sessionId);
        message.put("timestamp", System.currentTimeMillis());
        
        return message;
    }

    /**
     * 处理出牌消息
     */
    @MessageMapping("/play-cards")
    @SendTo("/topic/game-updates")
    public Map<String, Object> playCards(@Payload Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        
        message.put("type", "CARDS_PLAYED");
        message.put("sessionId", sessionId);
        message.put("timestamp", System.currentTimeMillis());
        
        return message;
    }

    /**
     * 处理叫地主消息
     */
    @MessageMapping("/bid-landlord")
    @SendTo("/topic/game-updates")
    public Map<String, Object> bidLandlord(@Payload Map<String, Object> message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        
        message.put("type", "LANDLORD_BID");
        message.put("sessionId", sessionId);
        message.put("timestamp", System.currentTimeMillis());
        
        return message;
    }
}