package com.doudizhu.service;

import com.doudizhu.enums.CardRank;
import com.doudizhu.enums.CardSuit;
import com.doudizhu.model.Card;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 卡牌服务类
 */
@Service
public class CardService {

    /**
     * 生成一副完整的斗地主牌
     */
    public List<Card> generateDeck() {
        List<Card> deck = new ArrayList<>();
        
        // 生成普通牌 (每种花色13张，共52张)
        for (CardSuit suit : CardSuit.values()) {
            if (suit != CardSuit.JOKER) { // 跳过王的花色
                for (CardRank rank : CardRank.values()) {
                    if (rank != CardRank.SMALL_JOKER && rank != CardRank.BIG_JOKER) {
                        deck.add(new Card(suit, rank));
                    }
                }
            }
        }
        
        // 添加大小王
        deck.add(new Card(CardSuit.JOKER, CardRank.SMALL_JOKER));
        deck.add(new Card(CardSuit.JOKER, CardRank.BIG_JOKER));
        
        return deck;
    }

    /**
     * 洗牌
     */
    public void shuffleDeck(List<Card> deck) {
        Collections.shuffle(deck);
    }

    /**
     * 发牌给玩家
     * 每人17张牌，剩余3张作为地主牌
     */
    public void dealCards(List<Card> deck, List<com.doudizhu.model.Player> players) {
        if (players.size() != 3) {
            throw new RuntimeException("斗地主需要3个玩家");
        }
        
        // 清空所有玩家的手牌
        for (com.doudizhu.model.Player player : players) {
            player.getHandCards().clear();
        }
        
        // 发牌：每人17张
        int cardIndex = 0;
        for (int i = 0; i < 17; i++) {
            for (com.doudizhu.model.Player player : players) {
                player.addCard(deck.get(cardIndex++));
            }
        }
        
        // 剩余3张牌留作地主牌
        // cardIndex 现在应该是51，剩余的3张牌 (51, 52, 53) 作为地主牌
    }

    /**
     * 获取地主牌（最后3张）
     */
    public List<Card> getLandlordCards(List<Card> deck) {
        if (deck.size() < 3) {
            throw new RuntimeException("牌数不足");
        }
        // 返回新的ArrayList而不是SubList，避免序列化问题
        return new ArrayList<>(deck.subList(51, 54));
    }

    /**
     * 对手牌进行排序
     */
    public void sortCards(List<Card> cards) {
        cards.sort((a, b) -> {
            // 先按等级排序，再按花色排序
            int rankCompare = Integer.compare(a.getRank().getValue(), b.getRank().getValue());
            if (rankCompare != 0) {
                return rankCompare;
            }
            return Integer.compare(a.getSuit().ordinal(), b.getSuit().ordinal());
        });
    }
}