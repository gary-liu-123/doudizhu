package com.doudizhu.model;

import com.doudizhu.enums.CardRank;
import com.doudizhu.enums.CardSuit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 扑克牌类
 */
@Data
public class Card implements Serializable {
    
    private CardSuit suit;
    private CardRank rank;
    private String id;
    
    public Card() {}
    
    public Card(CardSuit suit, CardRank rank) {
        this.suit = suit;
        this.rank = rank;
        this.id = generateId();
    }
    
    public Card(CardRank rank) {
        // 用于大小王，没有花色
        this.suit = null;
        this.rank = rank;
        this.id = generateId();
    }
    
    private String generateId() {
        if (suit == null) {
            return rank.name();
        }
        return suit.name() + "_" + rank.name();
    }
    
    @JsonIgnore
    public boolean isJoker() {
        return rank.isJoker();
    }
    
    @JsonIgnore
    public boolean isRed() {
        return suit == CardSuit.HEART || suit == CardSuit.DIAMOND;
    }
    
    @JsonIgnore
    public boolean isBlack() {
        return suit == CardSuit.SPADE || suit == CardSuit.CLUB;
    }
    
    @Override
    public String toString() {
        if (isJoker()) {
            return rank.getDisplay();
        }
        return suit.getSymbol() + rank.getDisplay();
    }
}