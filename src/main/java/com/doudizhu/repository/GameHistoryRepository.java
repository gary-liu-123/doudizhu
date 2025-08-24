package com.doudizhu.repository;

import com.doudizhu.entity.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏历史数据访问层
 */
@Repository
public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    
    @Query("SELECT gh FROM GameHistory gh WHERE gh.player1Id = :userId OR gh.player2Id = :userId OR gh.player3Id = :userId ORDER BY gh.createdAt DESC")
    List<GameHistory> findByUserId(@Param("userId") Long userId);
    
    List<GameHistory> findByRoomIdOrderByCreatedAtDesc(String roomId);
}