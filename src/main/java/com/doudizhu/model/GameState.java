package com.doudizhu.model;

import com.doudizhu.enums.GamePhase;
import com.doudizhu.service.BiddingService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏状态类
 */
@Data
public class GameState {
    
    private GamePhase phase;
    private Long currentPlayerId;
    private List<Card> landlordCards;
    private Long landlordId;
    private Play lastPlay;
    private List<Play> playHistory;
    private Map<Long, Integer> scores;
    private int biddingRound; // 叫地主轮次
    
    // 叫地主相关字段
    private int currentBidScore; // 当前最高叫分
    private Long currentBidder; // 当前最高叫分者
    private List<BiddingService.BiddingAction> biddingHistory; // 叫地主历史
    
    public GameState() {
        this.phase = GamePhase.WAITING;
        this.landlordCards = new ArrayList<>();
        this.playHistory = new ArrayList<>();
        this.scores = new HashMap<>();
        this.biddingRound = 0;
        this.currentBidScore = 0;
        this.biddingHistory = new ArrayList<>();
    }
    
    @JsonIgnore
    public boolean isGameStarted() {
        return phase != GamePhase.WAITING;
    }
    
    @JsonIgnore
    public boolean isGameFinished() {
        return phase == GamePhase.FINISHED;
    }
    
    public void startBidding(Long firstPlayerId) {
        this.phase = GamePhase.BIDDING;
        this.currentPlayerId = firstPlayerId;
        this.biddingRound = 1;
        this.currentBidScore = 0;
        this.currentBidder = null;
        this.biddingHistory.clear();
    }
    
    public void startPlaying() {
        this.phase = GamePhase.PLAYING;
        this.currentPlayerId = landlordId; // 地主先出牌
    }
    
    public void finishGame() {
        this.phase = GamePhase.FINISHED;
    }
    
    public void addPlay(Play play) {
        this.playHistory.add(play);
        this.lastPlay = play;
    }
}