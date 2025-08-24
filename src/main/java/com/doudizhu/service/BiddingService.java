package com.doudizhu.service;

import com.doudizhu.model.Room;
import com.doudizhu.model.Player;
import com.doudizhu.model.GameState;
import com.doudizhu.model.Card;
import com.doudizhu.enums.GamePhase;
import com.doudizhu.enums.RoomStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class BiddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BiddingService.class);
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private CardService cardService;

    /**
     * 处理叫地主请求
     */
    public BiddingResult processBid(String roomId, String playerId, String playerName, int bidScore) {
        logger.info("处理叫地主请求 - 房间:{}, 玩家:{}, 叫分:{}", roomId, playerId, bidScore);
        
        Room room = roomService.getRoomById(roomId);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        
        GameState gameState = room.getGameState();
        if (gameState == null || gameState.getPhase() != GamePhase.BIDDING) {
            throw new RuntimeException("当前不是叫地主阶段");
        }
        
        // 验证是否轮到该玩家 - 修复类型转换问题
        Long currentPlayerId = gameState.getCurrentPlayerId();
        Long playerIdLong = Long.parseLong(playerId);
        
        logger.info("当前应叫地主玩家ID: {}, 请求玩家ID: {}", currentPlayerId, playerIdLong);
        
        if (!playerIdLong.equals(currentPlayerId)) {
            throw new RuntimeException("还未轮到您叫地主，当前应该是玩家 " + currentPlayerId + " 叫地主");
        }
        
        // 验证叫分是否有效
        if (bidScore > 0 && bidScore <= gameState.getCurrentBidScore()) {
            throw new RuntimeException("叫分必须大于当前最高分");
        }
        
        if (bidScore < 0 || bidScore > 3) {
            throw new RuntimeException("叫分范围必须在0-3之间");
        }
        
        // 更新叫地主状态
        BiddingResult result = updateBiddingState(room, playerId, playerName, bidScore);
        
        // 保存房间状态
        roomService.saveRoom(room);
        
        logger.info("叫地主处理完成 - 结果:{}", result.getResultType());
        return result;
    }
    
    /**
     * 更新叫地主状态
     */
    private BiddingResult updateBiddingState(Room room, String playerId, String playerName, int bidScore) {
        GameState gameState = room.getGameState();
        
        // 记录本次叫地主动作
        if (gameState.getBiddingHistory() == null) {
            gameState.setBiddingHistory(new ArrayList<>());
        }
        
        BiddingAction action = new BiddingAction();
        action.setPlayerId(playerId);
        action.setPlayerName(playerName);
        action.setBidScore(bidScore);
        action.setTimestamp(LocalDateTime.now().toString());
        gameState.getBiddingHistory().add(action);
        
        // 更新当前最高叫分
        if (bidScore > gameState.getCurrentBidScore()) {
            gameState.setCurrentBidScore(bidScore);
            gameState.setCurrentBidder(Long.parseLong(playerId));
        }
        
        // 增加叫地主轮次
        gameState.setBiddingRound(gameState.getBiddingRound() + 1);
        
        // 确定下一个玩家
        Long nextPlayerId = getNextBiddingPlayer(room, Long.parseLong(playerId));
        gameState.setCurrentPlayerId(nextPlayerId);
        
        // 判断是否结束叫地主阶段
        return checkBiddingEnd(room, gameState);
    }
    
    /**
     * 获取下一个叫地主的玩家
     */
    private Long getNextBiddingPlayer(Room room, Long currentPlayerId) {
        List<Player> players = room.getPlayers();
        
        // 找到当前玩家的位置
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
        
        // 获取下一个玩家（循环）
        int nextIndex = (currentIndex + 1) % players.size();
        return players.get(nextIndex).getUserId();
    }
    
    /**
     * 检查叫地主是否结束
     */
    private BiddingResult checkBiddingEnd(Room room, GameState gameState) {
        List<BiddingAction> history = gameState.getBiddingHistory();
        
        // 如果有人叫3分，直接成为地主
        if (gameState.getCurrentBidScore() == 3) {
            return finalizeLandlord(room, gameState.getCurrentBidder());
        }
        
        // 检查是否完成一轮叫地主（每人都叫了一次）
        if (history.size() >= 3) {
            // 获取最近3次叫地主记录，看是否所有人都不叫
            List<BiddingAction> recentActions = history.subList(Math.max(0, history.size() - 3), history.size());
            boolean allPass = recentActions.stream().allMatch(action -> action.getBidScore() == 0);
            
            if (allPass && gameState.getCurrentBidScore() == 0) {
                // 所有人都不叫，重新洗牌发牌
                return restartBidding(room);
            } else if (gameState.getCurrentBidScore() > 0) {
                // 有人叫了地主，确定地主
                return finalizeLandlord(room, gameState.getCurrentBidder());
            }
        }
        
        // 继续叫地主 - 修复：使用当前设置的下一个玩家ID
        return BiddingResult.continueResult(gameState.getCurrentPlayerId());
    }
    
    /**
     * 确定地主并分配底牌
     */
    private BiddingResult finalizeLandlord(Room room, Long landlordId) {
        logger.info("确定地主: {}", landlordId);
        
        GameState gameState = room.getGameState();
        gameState.setLandlordId(landlordId);
        gameState.setPhase(GamePhase.PLAYING);
        
        // 找到地主玩家
        Player landlord = room.getPlayers().stream()
                .filter(p -> p.getUserId().equals(landlordId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("找不到地主玩家"));
        
        // 设置地主身份
        landlord.setLandlord(true);
        
        // 分配底牌给地主
        List<Card> landlordCards = gameState.getLandlordCards();
        if (landlordCards != null && !landlordCards.isEmpty()) {
            for (Card card : landlordCards) {
                landlord.addCard(card);
            }
            // 重新排序地主手牌
            cardService.sortCards(landlord.getHandCards());
        }
        
        // 设置当前玩家为地主（地主先出牌）
        gameState.setCurrentPlayerId(landlordId);
        
        // 更新房间状态
        room.setStatus(RoomStatus.GAMING);
        
        return BiddingResult.landlordResult(landlordId, landlordId);
    }
    
    /**
     * 重新开始叫地主（重新洗牌发牌）
     */
    private BiddingResult restartBidding(Room room) {
        logger.info("所有人都不叫，重新洗牌发牌");
        
        // 重新洗牌发牌
        roomService.startGame(room.getId());
        
        return BiddingResult.restartResult();
    }
    
    /**
     * 叫地主结果类
     */
    public static class BiddingResult {
        private String resultType; // "CONTINUE", "LANDLORD_DETERMINED", "RESTART"
        private Long nextPlayerId;
        private Long landlordId;
        private Long currentPlayerId;
        
        public static BiddingResult continueResult(Long nextPlayerId) {
            BiddingResult result = new BiddingResult();
            result.resultType = "CONTINUE";
            result.nextPlayerId = nextPlayerId;
            return result;
        }
        
        public static BiddingResult landlordResult(Long landlordId, Long currentPlayerId) {
            BiddingResult result = new BiddingResult();
            result.resultType = "LANDLORD_DETERMINED";
            result.landlordId = landlordId;
            result.currentPlayerId = currentPlayerId;
            return result;
        }
        
        public static BiddingResult restartResult() {
            BiddingResult result = new BiddingResult();
            result.resultType = "RESTART";
            return result;
        }
        
        // Getters and setters
        public String getResultType() { return resultType; }
        public void setResultType(String resultType) { this.resultType = resultType; }
        public Long getNextPlayerId() { return nextPlayerId; }
        public void setNextPlayerId(Long nextPlayerId) { this.nextPlayerId = nextPlayerId; }
        public Long getLandlordId() { return landlordId; }
        public void setLandlordId(Long landlordId) { this.landlordId = landlordId; }
        public Long getCurrentPlayerId() { return currentPlayerId; }
        public void setCurrentPlayerId(Long currentPlayerId) { this.currentPlayerId = currentPlayerId; }
    }
    
    /**
     * 叫地主动作记录类
     */
    public static class BiddingAction {
        private String playerId;
        private String playerName;
        private int bidScore;
        private String timestamp;
        
        // Getters and setters
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
        public int getBidScore() { return bidScore; }
        public void setBidScore(int bidScore) { this.bidScore = bidScore; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}