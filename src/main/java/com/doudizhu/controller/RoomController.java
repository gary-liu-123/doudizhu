package com.doudizhu.controller;

import com.doudizhu.dto.*;
import com.doudizhu.model.Room;
import com.doudizhu.service.RoomService;
import com.doudizhu.service.BiddingService;
import com.doudizhu.service.CardPlayService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 房间管理控制器
 */
@RestController
@RequestMapping("/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;
    private final BiddingService biddingService;
    private final CardPlayService cardPlayService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomController(RoomService roomService, BiddingService biddingService, 
                         CardPlayService cardPlayService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.biddingService = biddingService;
        this.cardPlayService = cardPlayService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 创建房间
     */
    @PostMapping("/create")
    public ApiResponse<Room> createRoom(@RequestBody CreateRoomRequest request) {
        try {
            // 临时使用随机用户ID，实际项目中应该从JWT或session中获取
            String userId = generateUserId();
            
            Room room = roomService.createRoom(userId, request.getPlayerName());
            
            // 发送房间创建通知
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ROOM_CREATED");
            notification.put("roomId", room.getId());
            notification.put("playerName", request.getPlayerName());
            
            messagingTemplate.convertAndSend("/topic/room-updates", notification);
            
            return ApiResponse.success("房间创建成功", room);
        } catch (Exception e) {
            return ApiResponse.error("创建房间失败: " + e.getMessage());
        }
    }

    /**
     * 加入房间
     */
    @PostMapping("/join")
    public ApiResponse<Room> joinRoom(@RequestBody JoinRoomRequest request) {
        try {
            // 临时使用随机用户ID
            String userId = generateUserId();
            
            Room room = roomService.joinRoom(request.getRoomId(), userId, request.getPlayerName());
            
            // 发送玩家加入通知
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "PLAYER_JOINED");
            notification.put("roomId", room.getId());
            notification.put("playerName", request.getPlayerName());
            notification.put("playerCount", room.getPlayers().size());
            notification.put("room", room);
            
            messagingTemplate.convertAndSend("/topic/room-" + room.getId(), notification);
            
            // 检查是否可以开始游戏
            if (room.canStart()) {
                // 自动开始游戏
                Room gameStartedRoom = roomService.startGame(room.getId());
                
                // 发送游戏开始通知到房间内所有玩家
                Map<String, Object> gameStartNotification = new HashMap<>();
                gameStartNotification.put("type", "GAME_STARTED");
                gameStartNotification.put("roomId", gameStartedRoom.getId());
                gameStartNotification.put("room", gameStartedRoom);
                gameStartNotification.put("currentPlayer", gameStartedRoom.getGameState().getCurrentPlayerId());
                gameStartNotification.put("phase", gameStartedRoom.getGameState().getPhase());
                gameStartNotification.put("playerCount", gameStartedRoom.getPlayers().size());
                
                // 广播给房间内所有玩家
                messagingTemplate.convertAndSend("/topic/room-" + gameStartedRoom.getId(), gameStartNotification);
                
                return ApiResponse.success("加入房间成功，游戏开始!", gameStartedRoom);
            }
            
            return ApiResponse.success("加入房间成功", room);
        } catch (Exception e) {
            return ApiResponse.error("加入房间失败: " + e.getMessage());
        }
    }

    /**
     * 叫地主
     */
    @PostMapping("/bid")
    public ApiResponse<String> bid(@RequestBody BidRequest request) {
        try {
            BiddingService.BiddingResult result = biddingService.processBid(
                request.getRoomId(), 
                request.getPlayerId(), 
                request.getPlayerName(),
                request.getBidScore()
            );
            
            // 根据结果类型发送不同的WebSocket消息
            switch (result.getResultType()) {
                case "CONTINUE":
                    // 继续叫地主，通知下一个玩家
                    sendBiddingMessage(request.getRoomId(), request.getPlayerId(), 
                                     request.getPlayerName(), request.getBidScore(), 
                                     result.getNextPlayerId());
                    break;
                    
                case "LANDLORD_DETERMINED":
                    // 地主确定，通知所有玩家
                    sendLandlordMessage(request.getRoomId(), result.getLandlordId(), 
                                      result.getCurrentPlayerId());
                    break;
                    
                case "RESTART":
                    // 重新开始，通知所有玩家
                    sendRestartMessage(request.getRoomId());
                    break;
            }
            
            return ApiResponse.success("叫地主成功", "OK");
        } catch (Exception e) {
            return ApiResponse.error("叫地主失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间信息
     */
    @GetMapping("/{roomId}")
    public ApiResponse<Room> getRoomInfo(@PathVariable String roomId) {
        try {
            Room room = roomService.getRoomById(roomId);
            if (room == null) {
                return ApiResponse.error("房间不存在");
            }
            return ApiResponse.success(room);
        } catch (Exception e) {
            return ApiResponse.error("获取房间信息失败: " + e.getMessage());
        }
    }

    /**
     * 离开房间
     */
    @PostMapping("/leave")
    public ApiResponse<String> leaveRoom(@RequestParam String userId) {
        try {
            String roomId = roomService.getPlayerRoom(userId);
            roomService.leaveRoom(userId);
            
            if (roomId != null) {
                // 发送玩家离开通知
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "PLAYER_LEFT");
                notification.put("roomId", roomId);
                notification.put("userId", userId);
                
                messagingTemplate.convertAndSend("/topic/room-" + roomId, notification);
            }
            
            return ApiResponse.success("离开房间成功", "OK");
        } catch (Exception e) {
            return ApiResponse.error("离开房间失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送叫地主消息
     */
    private void sendBiddingMessage(String roomId, String playerId, String playerName, 
                                  int bidScore, Long nextPlayerId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "BIDDING_ACTION");
        notification.put("roomId", roomId);
        notification.put("playerId", playerId);
        notification.put("playerName", playerName);
        notification.put("bidScore", bidScore);
        notification.put("nextPlayer", nextPlayerId);
        
        messagingTemplate.convertAndSend("/topic/room-" + roomId, notification);
    }
    
    /**
     * 发送地主确定消息
     */
    private void sendLandlordMessage(String roomId, Long landlordId, Long currentPlayerId) {
        Room room = roomService.getRoomById(roomId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "LANDLORD_DETERMINED");
        notification.put("roomId", roomId);
        notification.put("landlordId", landlordId.toString());
        notification.put("currentPlayer", currentPlayerId);
        notification.put("room", room);
        
        messagingTemplate.convertAndSend("/topic/room-" + roomId, notification);
    }
    
    /**
     * 发送重新开始消息
     */
    private void sendRestartMessage(String roomId) {
        Room room = roomService.getRoomById(roomId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "GAME_RESTARTED");
        notification.put("roomId", roomId);
        notification.put("room", room);
        notification.put("currentPlayer", room.getGameState().getCurrentPlayerId());
        notification.put("phase", room.getGameState().getPhase());
        
        messagingTemplate.convertAndSend("/topic/room-" + roomId, notification);
    }

    /**
     * 临时生成用户ID的方法 - 开发阶段使用
     * 使用时间戳+随机数确保不同标签页有不同ID
     */
    private String generateUserId() {
        // 使用当前时间戳的后6位 + 3位随机数，确保每个请求都有唯一ID
        long timestamp = System.currentTimeMillis() % 1000000; // 6位时间戳
        int random = (int)(Math.random() * 1000); // 3位随机数
        return String.valueOf(timestamp * 1000 + random);
    }

    /**
     * 出牌
     */
    @PostMapping("/play")
    public ApiResponse<String> playCards(@RequestBody PlayCardRequest request) {
        try {
            CardPlayService.PlayResult result = cardPlayService.playCards(
                request.getRoomId(),
                request.getPlayerId(), 
                request.getPlayerName(),
                request.getCardIds()
            );
            
            // 根据结果类型发送不同的WebSocket消息
            switch (result.getResultType()) {
                case "CONTINUE":
                    // 继续出牌，通知下一个玩家
                    sendPlayCardMessage(request.getRoomId(), result.getPlay(), result.getNextPlayerId());
                    break;
                    
                case "GAME_END":
                    // 游戏结束，通知所有玩家
                    sendGameEndMessage(request.getRoomId(), result.getWinnerId(), result.getWinnerName());
                    break;
            }
            
            return ApiResponse.success("出牌成功", "OK");
        } catch (Exception e) {
            return ApiResponse.error("出牌失败: " + e.getMessage());
        }
    }

    /**
     * 不要（跳过出牌）
     */
    @PostMapping("/pass")
    public ApiResponse<String> passPlay(@RequestBody PassPlayRequest request) {
        try {
            CardPlayService.PlayResult result = cardPlayService.passPlay(
                request.getRoomId(),
                request.getPlayerId()
            );
            
            // 发送跳过出牌消息
            sendPassMessage(request.getRoomId(), request.getPlayerId(), 
                          request.getPlayerName(), result.getNextPlayerId());
            
            return ApiResponse.success("跳过成功", "OK");
        } catch (Exception e) {
            return ApiResponse.error("跳过失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送出牌消息
     */
    private void sendPlayCardMessage(String roomId, com.doudizhu.model.Play play, Long nextPlayerId) {
        Room room = roomService.getRoomById(roomId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "CARD_PLAYED");
        notification.put("roomId", roomId);
        notification.put("play", play);
        notification.put("nextPlayer", nextPlayerId);
        notification.put("room", room);
        
        messagingTemplate.convertAndSend("/topic/room-" + roomId, notification);
    }
    
    /**
     * 发送跳过出牌消息
     */
    private void sendPassMessage(String roomId, String playerId, String playerName, Long nextPlayerId) {
        Room room = roomService.getRoomById(roomId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "PLAYER_PASSED");
        notification.put("roomId", roomId);
        notification.put("playerId", playerId);
        notification.put("playerName", playerName);
        notification.put("nextPlayer", nextPlayerId);
        notification.put("room", room);
        
        messagingTemplate.convertAndSend("/topic/room-" + roomId, notification);
    }
    
    /**
     * 发送游戏结束消息
     */
    private void sendGameEndMessage(String roomId, Long winnerId, String winnerName) {
        Room room = roomService.getRoomById(roomId);
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "GAME_END");
        notification.put("roomId", roomId);
        notification.put("winnerId", winnerId);
        notification.put("winnerName", winnerName);
        notification.put("room", room);
        
        messagingTemplate.convertAndSend("/topic/room-" + roomId, notification);
    }
}