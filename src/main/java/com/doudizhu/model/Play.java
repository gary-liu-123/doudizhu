package com.doudizhu.model;

import com.doudizhu.enums.PlayType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出牌记录
 */
@Data
public class Play implements Serializable {
    
    private Long playerId;
    private String playerName;
    private List<Card> cards;
    private PlayType type;
    private String timestamp;
    private int rank;  // 主要点数，用于比较大小
    
    public Play() {}
    
    public Play(Long playerId, String playerName, List<Card> cards, PlayType type, int rank) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.cards = cards;
        this.type = type;
        this.rank = rank;
        this.timestamp = LocalDateTime.now().toString();
    }
    
    @JsonIgnore
    public int getCardCount() {
        return cards != null ? cards.size() : 0;
    }
    
    @JsonIgnore
    public boolean isEmpty() {
        return cards == null || cards.isEmpty();
    }
    
    /**
     * 是否可以压过另一手牌
     */
    public boolean canBeat(Play other) {
        if (other == null) return true;
        
        // 王炸最大
        if (this.type == PlayType.ROCKET) {
            return true;
        }
        if (other.type == PlayType.ROCKET) {
            return false;
        }
        
        // 炸弹可以压过非炸弹
        if (this.type == PlayType.BOMB && other.type != PlayType.BOMB) {
            return true;
        }
        if (other.type == PlayType.BOMB && this.type != PlayType.BOMB) {
            return false;
        }
        
        // 相同牌型才能比较
        if (this.type != other.type) {
            return false;
        }
        
        // 牌数必须相同（除了炸弹和王炸）
        if (this.cards.size() != other.cards.size() && 
            !this.type.equals(PlayType.BOMB) && 
            !this.type.equals(PlayType.ROCKET)) {
            return false;
        }
        
        // 比较主要点数
        return this.rank > other.rank;
    }
}