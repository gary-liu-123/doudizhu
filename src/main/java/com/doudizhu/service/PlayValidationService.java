package com.doudizhu.service;

import com.doudizhu.enums.CardRank;
import com.doudizhu.enums.PlayType;
import com.doudizhu.model.Card;
import com.doudizhu.model.Play;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 出牌验证服务
 */
@Service
public class PlayValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayValidationService.class);
    
    /**
     * 分析牌型
     */
    public PlayAnalysisResult analyzeCards(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new PlayAnalysisResult(PlayType.SINGLE, false, 0, "请选择要出的牌");
        }
        
        logger.info("分析牌型 - 卡牌数量: {}, 卡牌: {}", cards.size(), 
            cards.stream().map(card -> card.getRank() + "(" + card.getRank().getValue() + ")")
                .collect(Collectors.toList()));
        
        // 按点数分组统计
        Map<Integer, List<Card>> rankGroups = cards.stream()
            .collect(Collectors.groupingBy(card -> card.getRank().getValue()));
        
        // 按点数排序
        List<Integer> ranks = rankGroups.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
        
        logger.info("点数分组: {}, 排序后点数: {}", rankGroups.size(), ranks);
        
        int cardCount = cards.size();
        int groupCount = rankGroups.size();
        
        // 分析牌型
        switch (cardCount) {
            case 1:
                return analyzeSingle(cards);
            case 2:
                return analyzePair(rankGroups, ranks);
            case 3:
                return analyzeTriple(rankGroups, ranks);
            case 4:
                return analyzeFourCards(rankGroups, ranks);
            case 5:
                return analyzeFiveCards(rankGroups, ranks);
            default:
                if (cardCount >= 6) {
                    return analyzeMultipleCards(rankGroups, ranks, cardCount);
                }
                return new PlayAnalysisResult(PlayType.SINGLE, false, 0, "无效牌型");
        }
    }
    
    /**
     * 分析单张
     */
    private PlayAnalysisResult analyzeSingle(List<Card> cards) {
        Card card = cards.get(0);
        return new PlayAnalysisResult(PlayType.SINGLE, true, card.getRank().getValue(), "单张");
    }
    
    /**
     * 分析两张牌
     */
    private PlayAnalysisResult analyzePair(Map<Integer, List<Card>> rankGroups, List<Integer> ranks) {
        if (rankGroups.size() == 1 && rankGroups.values().iterator().next().size() == 2) {
            // 对子
            int rank = ranks.get(0);
            return new PlayAnalysisResult(PlayType.PAIR, true, rank, "对子");
        }
        
        // 检查是否为王炸
        if (ranks.size() == 2 && 
            ranks.contains(CardRank.SMALL_JOKER.getValue()) && 
            ranks.contains(CardRank.BIG_JOKER.getValue())) {
            return new PlayAnalysisResult(PlayType.ROCKET, true, 18, "王炸");
        }
        
        return new PlayAnalysisResult(PlayType.SINGLE, false, 0, "无效牌型");
    }
    
    /**
     * 分析三张牌
     */
    private PlayAnalysisResult analyzeTriple(Map<Integer, List<Card>> rankGroups, List<Integer> ranks) {
        if (rankGroups.size() == 1 && rankGroups.values().iterator().next().size() == 3) {
            // 三张
            int rank = ranks.get(0);
            return new PlayAnalysisResult(PlayType.TRIPLE, true, rank, "三张");
        }
        return new PlayAnalysisResult(PlayType.SINGLE, false, 0, "无效牌型");
    }
    
    /**
     * 分析四张牌
     */
    private PlayAnalysisResult analyzeFourCards(Map<Integer, List<Card>> rankGroups, List<Integer> ranks) {
        Map<Integer, Integer> countMap = rankGroups.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
        
        // 炸弹
        if (rankGroups.size() == 1) {
            int rank = ranks.get(0);
            return new PlayAnalysisResult(PlayType.BOMB, true, rank, "炸弹");
        }
        
        // 三带一
        if (rankGroups.size() == 2) {
            Optional<Integer> tripleRank = countMap.entrySet().stream()
                .filter(e -> e.getValue() == 3)
                .map(Map.Entry::getKey)
                .findFirst();
                
            if (tripleRank.isPresent()) {
                return new PlayAnalysisResult(PlayType.TRIPLE_WITH_SINGLE, true, tripleRank.get(), "三带一");
            }
        }
        
        return new PlayAnalysisResult(PlayType.SINGLE, false, 0, "无效牌型");
    }
    
    /**
     * 分析五张牌
     */
    private PlayAnalysisResult analyzeFiveCards(Map<Integer, List<Card>> rankGroups, List<Integer> ranks) {
        Map<Integer, Integer> countMap = rankGroups.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
        
        // 三带二
        if (rankGroups.size() == 2) {
            Optional<Integer> tripleRank = countMap.entrySet().stream()
                .filter(e -> e.getValue() == 3)
                .map(Map.Entry::getKey)
                .findFirst();
            Optional<Integer> pairRank = countMap.entrySet().stream()
                .filter(e -> e.getValue() == 2)
                .map(Map.Entry::getKey)
                .findFirst();
                
            if (tripleRank.isPresent() && pairRank.isPresent()) {
                return new PlayAnalysisResult(PlayType.TRIPLE_WITH_PAIR, true, tripleRank.get(), "三带二");
            }
        }
        
        // 顺子
        if (rankGroups.size() == 5 && isConsecutive(ranks) && !hasJokers(ranks)) {
            return new PlayAnalysisResult(PlayType.STRAIGHT, true, ranks.get(0), "顺子");
        }
        
        return new PlayAnalysisResult(PlayType.SINGLE, false, 0, "无效牌型");
    }
    
    /**
     * 分析多张牌（6张以上）
     */
    private PlayAnalysisResult analyzeMultipleCards(Map<Integer, List<Card>> rankGroups, List<Integer> ranks, int cardCount) {
        Map<Integer, Integer> countMap = rankGroups.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));
        
        logger.info("分析多张牌 - 卡牌数: {}, 点数分组数: {}, 每个点数的牌数: {}", cardCount, rankGroups.size(), countMap);
        
        // 连对
        if (cardCount % 2 == 0 && cardCount >= 6) {
            boolean allPairs = countMap.values().stream().allMatch(count -> count == 2);
            logger.info("连对检查 - 是否都是对子: {}, 是否连续: {}, 是否有王: {}", 
                allPairs, isConsecutive(ranks), hasJokers(ranks));
            if (allPairs && isConsecutive(ranks) && !hasJokers(ranks)) {
                return new PlayAnalysisResult(PlayType.PAIR_STRAIGHT, true, ranks.get(0), "连对");
            }
        }
        
        // 顺子（6张以上）
        boolean isStraightCandidate = rankGroups.size() == cardCount;
        boolean isConsecutive = isConsecutive(ranks);
        boolean hasJokers = hasJokers(ranks);
        logger.info("顺子检查 - 点数组数等于卡牌数: {}, 连续: {}, 有王: {}", 
            isStraightCandidate, isConsecutive, hasJokers);
        
        if (isStraightCandidate && isConsecutive && !hasJokers) {
            return new PlayAnalysisResult(PlayType.STRAIGHT, true, ranks.get(0), "顺子");
        }
        
        // 飞机（连续的三张）
        if (cardCount % 3 == 0) {
            boolean allTriples = countMap.values().stream().allMatch(count -> count == 3);
            logger.info("飞机检查 - 是否都是三张: {}, 是否连续: {}", allTriples, isConsecutive(ranks));
            if (allTriples && isConsecutive(ranks) && !hasJokers(ranks)) {
                return new PlayAnalysisResult(PlayType.PLANE, true, ranks.get(0), "飞机");
            }
        }
        
        // 飞机带翅膀
        if (cardCount >= 8) {
            List<Integer> tripleRanks = countMap.entrySet().stream()
                .filter(e -> e.getValue() == 3)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
                
            logger.info("飞机带翅膀检查 - 三张的点数: {}", tripleRanks);
                
            if (tripleRanks.size() >= 2 && isConsecutive(tripleRanks) && !hasJokers(tripleRanks)) {
                int remainingCards = cardCount - tripleRanks.size() * 3;
                if (remainingCards == tripleRanks.size()) {
                    return new PlayAnalysisResult(PlayType.PLANE_WITH_SINGLE, true, tripleRanks.get(0), "飞机带单");
                } else if (remainingCards == tripleRanks.size() * 2) {
                    return new PlayAnalysisResult(PlayType.PLANE_WITH_PAIR, true, tripleRanks.get(0), "飞机带对");
                }
            }
        }
        
        logger.info("无法识别牌型 - 返回无效");
        return new PlayAnalysisResult(PlayType.SINGLE, false, 0, "无效牌型：无法识别的" + cardCount + "张牌组合");
    }
    
    /**
     * 检查是否连续
     */
    private boolean isConsecutive(List<Integer> ranks) {
        if (ranks.size() < 2) return false;
        
        for (int i = 1; i < ranks.size(); i++) {
            if (ranks.get(i) - ranks.get(i-1) != 1) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查是否包含王
     */
    private boolean hasJokers(List<Integer> ranks) {
        return ranks.contains(CardRank.SMALL_JOKER.getValue()) || 
               ranks.contains(CardRank.BIG_JOKER.getValue());
    }
    
    /**
     * 验证是否可以出牌
     */
    public boolean canPlayCards(List<Card> cards, Play lastPlay) {
        PlayAnalysisResult analysis = analyzeCards(cards);
        
        if (!analysis.isValid()) {
            return false;
        }
        
        if (lastPlay == null || lastPlay.isEmpty()) {
            return true; // 第一手牌可以任意出
        }
        
        Play currentPlay = new Play(null, null, cards, analysis.getPlayType(), analysis.getRank());
        return currentPlay.canBeat(lastPlay);
    }
    
    /**
     * 牌型分析结果
     */
    public static class PlayAnalysisResult {
        private PlayType playType;
        private boolean valid;
        private int rank;
        private String description;
        
        public PlayAnalysisResult(PlayType playType, boolean valid, int rank, String description) {
            this.playType = playType;
            this.valid = valid;
            this.rank = rank;
            this.description = description;
        }
        
        // Getters
        public PlayType getPlayType() { return playType; }
        public boolean isValid() { return valid; }
        public int getRank() { return rank; }
        public String getDescription() { return description; }
    }
}