package com.doudizhu.service;

import com.doudizhu.enums.GamePhase;
import com.doudizhu.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 出牌服务
 */
@Service
public class CardPlayService {
    
    private static final Logger logger = LoggerFactory.getLogger(CardPlayService.class);
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private PlayValidationService playValidationService;
    
    /**
     * 处理出牌请求
     */
    public PlayResult playCards(String roomId, String playerId, String playerName, List<String> cardIds) {
        logger.info("处理出牌请求 - 房间:{}, 玩家:{}, 卡牌数:{}", roomId, playerId, cardIds.size());
        
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        GameState gameState = room.getGameState();
        if (gameState == null || gameState.getPhase() != GamePhase.PLAYING) {
            throw new RuntimeException("当前不是出牌阶段");
        }
        
        // 验证是否轮到该玩家
        Long currentPlayerId = gameState.getCurrentPlayerId();
        Long playerIdLong = Long.parseLong(playerId);
        
        if (!playerIdLong.equals(currentPlayerId)) {
            throw new RuntimeException("还未轮到您出牌");
        }
        
        // 获取玩家
        Player player = room.getPlayers().stream()
            .filter(p -> p.getUserId().equals(playerIdLong))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("找不到玩家"));
        
        // 获取要出的牌
        List<Card> cardsToPlay = getCardsFromIds(player.getHandCards(), cardIds);
        if (cardsToPlay.size() != cardIds.size()) {
            throw new RuntimeException("选择的卡牌无效");
        }
        
        // 验证牌型和是否可以出牌
        Play lastPlay = gameState.getLastPlay();
        if (!playValidationService.canPlayCards(cardsToPlay, lastPlay)) {
            PlayValidationService.PlayAnalysisResult analysis = playValidationService.analyzeCards(cardsToPlay);
            if (!analysis.isValid()) {
                throw new RuntimeException("无效牌型: " + analysis.getDescription());
            } else {
                throw new RuntimeException("无法压过上家的牌");
            }
        }
        
        // 分析牌型
        PlayValidationService.PlayAnalysisResult analysis = playValidationService.analyzeCards(cardsToPlay);
        
        // 创建出牌记录
        Play play = new Play(playerIdLong, playerName, cardsToPlay, analysis.getPlayType(), analysis.getRank());
        
        // 从玩家手牌中移除出的牌
        player.removeCards(cardsToPlay);
        
        // 更新游戏状态
        gameState.addPlay(play);
        gameState.setLastPlay(play);
        
        // 检查是否获胜
        if (player.getHandCards().isEmpty()) {
            return finishGame(room, player);
        }
        
        // 切换到下一个玩家
        Long nextPlayerId = getNextPlayer(room, playerIdLong);
        gameState.setCurrentPlayerId(nextPlayerId);
        
        // 保存房间状态
        roomService.saveRoom(room);
        
        PlayResult result = PlayResult.continueResult(nextPlayerId, play);
        logger.info("出牌处理完成 - 牌型:{}, 下一个玩家:{}", analysis.getPlayType(), nextPlayerId);
        
        return result;
    }
    
    /**
     * 处理跳过出牌（不要）
     */
    public PlayResult passPlay(String roomId, String playerId) {
        logger.info("处理跳过出牌 - 房间:{}, 玩家:{}", roomId, playerId);
        
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        GameState gameState = room.getGameState();
        if (gameState == null || gameState.getPhase() != GamePhase.PLAYING) {
            throw new RuntimeException("当前不是出牌阶段");
        }
        
        // 验证是否轮到该玩家
        Long currentPlayerId = gameState.getCurrentPlayerId();
        Long playerIdLong = Long.parseLong(playerId);
        
        if (!playerIdLong.equals(currentPlayerId)) {
            throw new RuntimeException("还未轮到您出牌");
        }
        
        // 地主不能跳过第一手牌
        if (gameState.getLastPlay() == null && gameState.getLandlordId().equals(playerIdLong)) {
            throw new RuntimeException("地主必须先出牌");
        }
        
        // 获取下一个玩家
        Long nextPlayerId = getNextPlayer(room, playerIdLong);
        
        // 检查是否所有其他玩家都跳过了
        if (gameState.getLastPlay() != null && 
            checkAllOthersPass(room, gameState.getLastPlay().getPlayerId(), nextPlayerId)) {
            
            // 保存原出牌者ID，然后清空lastPlay
            Long originalPlayerId = gameState.getLastPlay().getPlayerId();
            logger.info("所有其他玩家都跳过了，回到原出牌者:{}", originalPlayerId);
            
            // 清空lastPlay，回到原出牌者继续出牌
            gameState.setLastPlay(null);
            gameState.setCurrentPlayerId(originalPlayerId);
        } else {
            // 切换到下一个玩家
            gameState.setCurrentPlayerId(nextPlayerId);
        }
        
        // 保存房间状态
        roomService.saveRoom(room);
        
        return PlayResult.passResult(gameState.getCurrentPlayerId());
    }
    
    /**
     * 根据卡牌ID获取卡牌对象
     */
    private List<Card> getCardsFromIds(List<Card> handCards, List<String> cardIds) {
        return cardIds.stream()
            .map(id -> handCards.stream()
                .filter(card -> card.getId().equals(id))
                .findFirst()
                .orElse(null))
            .filter(card -> card != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取下一个玩家
     */
    private Long getNextPlayer(Room room, Long currentPlayerId) {
        List<Player> players = room.getPlayers();
        
        int currentIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserId().equals(currentPlayerId)) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1) {
            throw new RuntimeException("找不到当前玩家");
        }
        
        int nextIndex = (currentIndex + 1) % players.size();
        return players.get(nextIndex).getUserId();
    }
    
    /**
     * 检查是否所有其他玩家都跳过了
     */
    private boolean checkAllOthersPass(Room room, Long lastPlayerId, Long nextPlayerId) {
        // 简化处理：如果下一个玩家就是出牌的玩家，说明其他人都跳过了
        return lastPlayerId.equals(nextPlayerId);
    }
    
    /**
     * 游戏结束
     */
    private PlayResult finishGame(Room room, Player winner) {
        logger.info("游戏结束 - 获胜者:{}", winner.getName());
        
        GameState gameState = room.getGameState();
        gameState.finishGame();
        
        // 计算积分（后续实现）
        calculateScores(room, winner);
        
        roomService.saveRoom(room);
        
        return PlayResult.gameEndResult(winner.getUserId(), winner.getName());
    }
    
    /**
     * 计算积分（简化版）
     */
    private void calculateScores(Room room, Player winner) {
        // TODO: 实现积分计算逻辑
        // 基础分数、地主身份、炸弹加分等
    }
    
    /**
     * 出牌结果类
     */
    public static class PlayResult {
        private String resultType; // "CONTINUE", "PASS", "GAME_END"
        private Long nextPlayerId;
        private Play play;
        private Long winnerId;
        private String winnerName;
        
        public static PlayResult continueResult(Long nextPlayerId, Play play) {
            PlayResult result = new PlayResult();
            result.resultType = "CONTINUE";
            result.nextPlayerId = nextPlayerId;
            result.play = play;
            return result;
        }
        
        public static PlayResult passResult(Long nextPlayerId) {
            PlayResult result = new PlayResult();
            result.resultType = "PASS";
            result.nextPlayerId = nextPlayerId;
            return result;
        }
        
        public static PlayResult gameEndResult(Long winnerId, String winnerName) {
            PlayResult result = new PlayResult();
            result.resultType = "GAME_END";
            result.winnerId = winnerId;
            result.winnerName = winnerName;
            return result;
        }
        
        // Getters
        public String getResultType() { return resultType; }
        public void setResultType(String resultType) { this.resultType = resultType; }
        public Long getNextPlayerId() { return nextPlayerId; }
        public void setNextPlayerId(Long nextPlayerId) { this.nextPlayerId = nextPlayerId; }
        public Play getPlay() { return play; }
        public void setPlay(Play play) { this.play = play; }
        public Long getWinnerId() { return winnerId; }
        public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }
        public String getWinnerName() { return winnerName; }
        public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
    }
}