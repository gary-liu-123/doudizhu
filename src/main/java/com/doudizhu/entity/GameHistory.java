package com.doudizhu.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 游戏历史实体类
 */
@Data
@Entity
@Table(name = "game_history")
public class GameHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "room_id", nullable = false, length = 32)
    private String roomId;
    
    @Column(name = "player1_id", nullable = false)
    private Long player1Id;
    
    @Column(name = "player2_id", nullable = false)
    private Long player2Id;
    
    @Column(name = "player3_id", nullable = false)
    private Long player3Id;
    
    @Column(name = "landlord_id", nullable = false)
    private Long landlordId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "winner_side", nullable = false)
    private WinnerSide winnerSide;
    
    @Column(columnDefinition = "JSON")
    private String scores;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum WinnerSide {
        LANDLORD, PEASANT
    }
}