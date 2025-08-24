package com.doudizhu.service;

import com.doudizhu.enums.GamePhase;
import com.doudizhu.enums.RoomStatus;
import com.doudizhu.model.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 房间管理服务
 */
@Service
public class RoomService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CardService cardService;
    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String PLAYER_ROOM_PREFIX = "player:";
    
    public RoomService(RedisTemplate<String, Object> redisTemplate, CardService cardService) {
        this.redisTemplate = redisTemplate;
        this.cardService = cardService;
    }
    
    /**
     * 创建房间
     */
    public Room createRoom(String creatorId, String creatorName) {
        // 检查创建者是否已在其他房间
        String existingRoomId = getPlayerRoom(creatorId);
        if (existingRoomId != null) {
            throw new RuntimeException("您已在房间 " + existingRoomId + " 中");
        }
        
        Room room = new Room(creatorId, creatorName);
        
        // 保存房间信息到Redis
        String roomKey = ROOM_KEY_PREFIX + room.getId();
        redisTemplate.opsForValue().set(roomKey, room, 24, TimeUnit.HOURS);
        
        // 保存玩家房间映射
        String playerKey = PLAYER_ROOM_PREFIX + creatorId;
        redisTemplate.opsForValue().set(playerKey, room.getId(), 24, TimeUnit.HOURS);
        
        return room;
    }
    
    /**
     * 加入房间
     */
    public Room joinRoom(String roomId, String userId, String userName) {
        // 检查玩家是否已在其他房间
        String existingRoomId = getPlayerRoom(userId);
        if (existingRoomId != null && !existingRoomId.equals(roomId)) {
            throw new RuntimeException("您已在房间 " + existingRoomId + " 中");
        }
        
        Room room = getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        if (room.isFull()) {
            throw new RuntimeException("房间已满");
        }
        
        // 添加玩家到房间
        Player newPlayer = room.addPlayer(Long.parseLong(userId), userName);
        
        // 更新房间信息
        updateRoom(room);
        
        // 保存玩家房间映射
        String playerKey = PLAYER_ROOM_PREFIX + userId;
        redisTemplate.opsForValue().set(playerKey, roomId, 24, TimeUnit.HOURS);
        
        return room;
    }
    
    /**
     * 离开房间
     */
    public void leaveRoom(String userId) {
        String roomId = getPlayerRoom(userId);
        if (roomId == null) {
            return;
        }
        
        Room room = getRoomById(roomId);
        if (room != null) {
            room.removePlayer(Long.parseLong(userId));
            
            if (room.isEmpty()) {
                // 删除空房间
                deleteRoom(roomId);
            } else {
                // 更新房间信息
                updateRoom(room);
            }
        }
        
        // 删除玩家房间映射
        String playerKey = PLAYER_ROOM_PREFIX + userId;
        redisTemplate.delete(playerKey);
    }
    
    /**
     * 获取房间信息
     */
    public Room getRoomById(String roomId) {
        String roomKey = ROOM_KEY_PREFIX + roomId;
        return (Room) redisTemplate.opsForValue().get(roomKey);
    }
    
    /**
     * 获取玩家所在房间ID
     */
    public String getPlayerRoom(String userId) {
        String playerKey = PLAYER_ROOM_PREFIX + userId;
        return (String) redisTemplate.opsForValue().get(playerKey);
    }
    
    /**
     * 更新房间信息
     */
    public void updateRoom(Room room) {
        String roomKey = ROOM_KEY_PREFIX + room.getId();
        redisTemplate.opsForValue().set(roomKey, room, 24, TimeUnit.HOURS);
    }
    
    /**
     * 保存房间信息 (updateRoom的别名)
     */
    public void saveRoom(Room room) {
        updateRoom(room);
    }
    
    /**
     * 删除房间
     */
    public void deleteRoom(String roomId) {
        String roomKey = ROOM_KEY_PREFIX + roomId;
        redisTemplate.delete(roomKey);
    }
    
    /**
     * 检查房间是否可以开始游戏
     */
    public boolean canStartGame(String roomId) {
        Room room = getRoomById(roomId);
        return room != null && room.canStart();
    }
    
    /**
     * 开始游戏 - 发牌并选择第一个玩家
     */
    public Room startGame(String roomId) {
        Room room = getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        if (!room.canStart()) {
            throw new RuntimeException("房间无法开始游戏");
        }
        
        // 初始化游戏状态
        GameState gameState = new GameState();
        room.setGameState(gameState);
        
        // 生成并洗牌
        List<Card> deck = cardService.generateDeck();
        cardService.shuffleDeck(deck);
        
        // 发牌
        cardService.dealCards(deck, room.getPlayers());
        
        // 获取地主牌
        List<Card> landlordCards = cardService.getLandlordCards(deck);
        gameState.setLandlordCards(landlordCards);
        
        // 为每个玩家的手牌排序
        for (Player player : room.getPlayers()) {
            cardService.sortCards(player.getHandCards());
        }
        
        // 随机选择第一个玩家开始叫地主
        Random random = new Random();
        int firstPlayerIndex = random.nextInt(room.getPlayers().size());
        Player firstPlayer = room.getPlayers().get(firstPlayerIndex);
        
        // 开始叫地主阶段
        gameState.startBidding(firstPlayer.getUserId());
        
        // 更新房间状态
        room.setStatus(RoomStatus.GAMING);
        updateRoom(room);
        
        return room;
    }
}