package com.doudizhu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家类
 */
@Data
public class Player implements Serializable {
    
    private Long userId;
    private String name;
    private List<Card> handCards;
    private boolean isLandlord;
    private int position; // 0, 1, 2
    private boolean isOnline;
    
    public Player() {
        this.handCards = new ArrayList<>();
        this.isLandlord = false;
        this.isOnline = true;
    }
    
    public Player(Long userId, String name, int position) {
        this();
        this.userId = userId;
        this.name = name;
        this.position = position;
    }
    
    @JsonIgnore
    public int getCardCount() {
        return handCards != null ? handCards.size() : 0;
    }
    
    public void addCard(Card card) {
        if (handCards == null) {
            handCards = new ArrayList<>();
        }
        handCards.add(card);
    }
    
    public void removeCard(Card card) {
        if (handCards != null && card != null) {
            handCards.remove(card);
        }
    }
    
    public void removeCards(List<Card> cards) {
        if (handCards != null && cards != null) {
            handCards.removeAll(cards);
        }
    }
    
    @JsonIgnore
    public boolean hasWon() {
        return getCardCount() == 0;
    }
}