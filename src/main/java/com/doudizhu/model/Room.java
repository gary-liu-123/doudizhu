package com.doudizhu.model;

import com.doudizhu.enums.RoomStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 房间模型类
 */
@Data
public class Room implements Serializable {
    
    private String id;
    private List<Player> players;
    private GameState gameState;
    private RoomStatus status;
    
    private String createdAt;
    private String creatorId;
    
    public Room() {
        this.id = generateRoomId();
        this.players = new ArrayList<>();
        this.status = RoomStatus.WAITING;
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public Room(String creatorId, String creatorName) {
        this();
        this.creatorId = creatorId;
        // 创建房间时，创建者自动加入房间
        Player creator = new Player(Long.parseLong(creatorId), creatorName, 0);
        this.players.add(creator);
    }
    
    private String generateRoomId() {
        // 生成6位数字房间号
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
    
    @JsonIgnore
    public boolean isFull() {
        return players.size() >= 3;
    }
    
    @JsonIgnore
    public boolean isEmpty() {
        return players.isEmpty();
    }
    
    @JsonIgnore
    public boolean canStart() {
        return players.size() == 3 && status == RoomStatus.WAITING;
    }
    
    public Player addPlayer(Long userId, String name) {
        if (isFull()) {
            throw new RuntimeException("房间已满");
        }
        
        // 检查玩家是否已在房间中
        boolean exists = players.stream()
                .anyMatch(p -> p.getUserId().equals(userId));
        if (exists) {
            throw new RuntimeException("玩家已在房间中");
        }
        
        Player newPlayer = new Player(userId, name, players.size());
        players.add(newPlayer);
        
        return newPlayer;
    }
    
    public void removePlayer(Long userId) {
        players.removeIf(p -> p.getUserId().equals(userId));
        
        // 如果房间变空，可以标记为已结束
        if (isEmpty()) {
            status = RoomStatus.FINISHED;
        }
        
        // 重新分配位置
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setPosition(i);
        }
    }
    
    public Player getPlayer(Long userId) {
        return players.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}