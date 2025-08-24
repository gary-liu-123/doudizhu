import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useGame } from '../../contexts/GameContext';
import { Card } from '../../components/Card/Card';
import { PlayerArea } from '../../components/PlayerArea/PlayerArea';
import { BiddingPanel } from '../../components/BiddingPanel/BiddingPanel';
import PlayControlPanel from '../../components/PlayControlPanel/PlayControlPanel';
import LastPlayArea from '../../components/LastPlayArea/LastPlayArea';
import { playCards, passPlay, analyzeCards } from '../../services/cardPlayService';
import webSocketService from '../../services/websocket';
import './GamePage.css';

export function GamePage() {
  const { roomId } = useParams<{ roomId: string }>();
  const { state, dispatch } = useGame();
  const [isConnected, setIsConnected] = useState(false);

  // 从URL获取玩家姓名
  const urlParams = new URLSearchParams(window.location.search);
  const currentPlayerName = urlParams.get('playerName') || '玩家';

  useEffect(() => {
    // 设置当前玩家信息
    dispatch({ 
      type: 'SET_MY_PLAYER_INFO', 
      payload: { name: currentPlayerName } 
    });
  }, [currentPlayerName, dispatch]);

  useEffect(() => {
    if (!roomId) return;

    // 连接WebSocket
    webSocketService.connect().then(() => {
      setIsConnected(true);
      
      // 订阅房间更新
      webSocketService.subscribe(`/topic/room-${roomId}`, (message) => {
        console.log('收到房间消息:', message);
        handleRoomUpdate(message);
      });
      
      // 获取当前房间信息
      fetchRoomInfo();
    }).catch(console.error);

    return () => {
      webSocketService.disconnect();
    };
  }, [roomId]);

  const fetchRoomInfo = async () => {
    try {
      console.log('获取房间信息:', roomId);
      const response = await fetch(`/api/rooms/${roomId}`);
      const result = await response.json();
      console.log('房间信息响应:', result);
      
      if (result.success) {
        console.log('更新房间状态:', result.data);
        dispatch({ type: 'SET_ROOM', payload: result.data });
        dispatch({ type: 'UPDATE_PLAYERS', payload: result.data.players });
        
        // 如果游戏已开始，更新游戏状态
        if (result.data.gameState) {
          const phase = result.data.gameState.phase;
          let gamePhase: 'waiting' | 'bidding' | 'playing' | 'finished' = 'waiting';
          if (phase === 'BIDDING') gamePhase = 'bidding';
          else if (phase === 'PLAYING') gamePhase = 'playing';
          else if (phase === 'FINISHED') gamePhase = 'finished';
          
          console.log('设置游戏阶段:', gamePhase);
          dispatch({ type: 'SET_GAME_PHASE', payload: gamePhase });
          dispatch({ type: 'SET_CURRENT_PLAYER', payload: result.data.gameState.currentPlayerId?.toString() || '' });
          
          // 设置当前玩家手牌
          const myPlayer = result.data.players.find((p: any) => p.name === currentPlayerName);
          if (myPlayer && myPlayer.handCards) {
            console.log('设置手牌:', myPlayer.handCards.length, '张');
            dispatch({ type: 'SET_HAND_CARDS', payload: myPlayer.handCards });
          }
        }
      } else {
        console.error('获取房间信息失败:', result.message);
      }
    } catch (error) {
      console.error('获取房间信息失败:', error);
    }
  };

  const handleRoomUpdate = (message: any) => {
    console.log('处理房间更新消息:', message.type, message);
    
    switch (message.type) {
      case 'PLAYER_JOINED':
        dispatch({ type: 'SET_ROOM', payload: message.room });
        dispatch({ type: 'UPDATE_PLAYERS', payload: message.room.players });
        break;
        
      case 'GAME_STARTED':
        console.log('游戏开始，更新状态:', message);
        dispatch({ type: 'SET_ROOM', payload: message.room });
        dispatch({ type: 'UPDATE_PLAYERS', payload: message.room.players });
        dispatch({ type: 'SET_GAME_PHASE', payload: 'bidding' });
        dispatch({ type: 'SET_CURRENT_PLAYER', payload: message.currentPlayer?.toString() || '' });
        
        // 找到当前玩家并设置手牌
        const myPlayer = message.room.players.find((p: any) => 
          p.name === currentPlayerName
        );
        console.log('找到当前玩家:', myPlayer);
        if (myPlayer && myPlayer.handCards) {
          dispatch({ type: 'SET_HAND_CARDS', payload: myPlayer.handCards });
        }
        break;

      case 'BIDDING_ACTION':
        console.log('收到叫地主消息:', message);
        dispatch({ 
          type: 'SET_BIDDING_STATE', 
          payload: {
            currentBidScore: message.bidScore || 0,
            currentBidder: message.playerId,
            biddingHistory: [...state.biddingState.biddingHistory, {
              playerId: message.playerId,
              playerName: message.playerName,
              bidScore: message.bidScore,
              timestamp: new Date().toISOString()
            }]
          }
        });
        dispatch({ type: 'SET_CURRENT_PLAYER', payload: message.nextPlayer?.toString() || '' });
        break;

      case 'LANDLORD_DETERMINED':
        console.log('地主确定:', message);
        dispatch({ type: 'SET_LANDLORD', payload: message.landlordId });
        dispatch({ type: 'SET_GAME_PHASE', payload: 'playing' });
        dispatch({ type: 'SET_CURRENT_PLAYER', payload: message.currentPlayer?.toString() || '' });
        
        // 如果当前玩家是地主，更新手牌（包含底牌）
        if (message.landlordId === getCurrentPlayerId()) {
          const updatedPlayer = message.room.players.find((p: any) => p.name === currentPlayerName);
          if (updatedPlayer && updatedPlayer.handCards) {
            dispatch({ type: 'SET_HAND_CARDS', payload: updatedPlayer.handCards });
          }
        }
        break;

      case 'CARD_PLAYED':
        console.log('收到出牌消息:', message);
        dispatch({ type: 'SET_LAST_PLAY', payload: message.play });
        dispatch({ type: 'SET_CURRENT_PLAYER', payload: message.nextPlayer?.toString() || '' });
        dispatch({ type: 'UPDATE_PLAYERS', payload: message.room.players });
        
        // 如果是当前玩家出牌，清空选中的卡牌并更新手牌
        if (message.play && message.play.playerName === currentPlayerName) {
          dispatch({ type: 'SET_SELECTED_CARDS', payload: [] });
          const currentPlayer = message.room.players.find((p: any) => p.name === currentPlayerName);
          if (currentPlayer && currentPlayer.handCards) {
            dispatch({ type: 'SET_HAND_CARDS', payload: currentPlayer.handCards });
          }
        }
        break;

      case 'PLAYER_PASSED':
        console.log('收到跳过出牌消息:', message);
        dispatch({ type: 'SET_CURRENT_PLAYER', payload: message.nextPlayer?.toString() || '' });
        break;

      case 'GAME_END':
        console.log('游戏结束:', message);
        dispatch({ type: 'SET_GAME_PHASE', payload: 'finished' });
        alert(`游戏结束！获胜者：${message.winnerName}`);
        break;
        
      case 'PLAYER_LEFT':
        fetchRoomInfo(); // 重新获取房间信息
        break;
    }
  };

  // 获取当前玩家ID
  const getCurrentPlayerId = () => {
    const currentPlayer = displayPlayers.find(p => p.name === currentPlayerName);
    return currentPlayer ? currentPlayer.userId : '';
  };

  // 叫地主处理
  const handleBid = async (bidScore: number) => {
    if (!roomId) return;
    
    try {
      const response = await fetch('/api/rooms/bid', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          roomId: roomId,
          playerId: getCurrentPlayerId(),
          playerName: currentPlayerName,
          bidScore: bidScore
        }),
      });

      const result = await response.json();
      if (!result.success) {
        console.error('叫地主失败:', result.message);
      }
    } catch (error) {
      console.error('叫地主请求失败:', error);
    }
  };

  // 不叫处理
  const handleBidPass = async () => {
    await handleBid(0);
  };

  const handleCardClick = (cardIndex: number) => {
    const card = state.cards.hand[cardIndex];
    const isSelected = state.selectedCards.some(selectedCard => selectedCard.id === card.id);
    
    if (isSelected) {
      dispatch({
        type: 'SET_SELECTED_CARDS',
        payload: state.selectedCards.filter(c => c.id !== card.id)
      });
    } else {
      dispatch({
        type: 'SET_SELECTED_CARDS',
        payload: [...state.selectedCards, card]
      });
    }
  };

  // 出牌处理
  const handlePlayCards = async () => {
    if (state.selectedCards.length === 0) return;
    if (!roomId) return;
    
    try {
      const cardIds = state.selectedCards.map(card => card.id);
      const result = await playCards({
        roomId,
        playerId: getCurrentPlayerId(),
        playerName: currentPlayerName,
        cardIds
      });
      
      if (!result.success) {
        console.error('出牌失败:', result.message);
        alert('出牌失败: ' + result.message);
      }
    } catch (error) {
      console.error('出牌请求失败:', error);
      alert('出牌请求失败');
    }
  };

  // 跳过出牌处理
  const handlePass = async () => {
    if (!roomId) return;
    
    try {
      const result = await passPlay({
        roomId,
        playerId: getCurrentPlayerId(),
        playerName: currentPlayerName
      });
      
      if (!result.success) {
        console.error('跳过失败:', result.message);
        alert('跳过失败: ' + result.message);
      }
    } catch (error) {
      console.error('跳过请求失败:', error);
      alert('跳过请求失败');
    }
  };

  // 提示功能（暂未实现）
  const handleHint = () => {
    console.log('提示功能暂未实现');
    alert('提示功能暂未实现');
  };
  
  // 分析当前选中的牌型
  const getPlayAnalysis = () => {
    if (state.selectedCards.length === 0) {
      return {
        isValid: false,
        playType: '',
        description: '请选择要出的牌',
        canPlay: false
      };
    }
    
    return analyzeCards(state.selectedCards, state.lastPlay?.cards);
  };

  const playAnalysis = getPlayAnalysis();

  // 处理玩家显示 - 重新安排玩家位置逻辑
  const getPlayersForDisplay = () => {
    if (!state.players || state.players.length === 0) {
      return [];
    }
    
    // 转换后端玩家数据格式
    const allPlayers = state.players.map((player: any) => ({
      userId: player.userId.toString(),
      name: player.name,
      cardCount: player.handCards ? player.handCards.length : 0,
      isLandlord: player.isLandlord || false,
      position: player.position,
      isOnline: player.isOnline || true
    }));

    // 按position排序，确保显示顺序正确
    return allPlayers.sort((a, b) => a.position - b.position);
  };

  const displayPlayers = getPlayersForDisplay();
  
  // 找出当前玩家和其他玩家
  const getCurrentPlayerIndex = () => {
    return displayPlayers.findIndex(p => p.name === currentPlayerName);
  };

  const getOtherPlayers = () => {
    const currentIndex = getCurrentPlayerIndex();
    if (currentIndex === -1) return { topPlayer: null, leftPlayer: null };
    
    // 根据当前玩家位置，安排其他玩家的显示位置
    const otherPlayers = displayPlayers.filter((_, index) => index !== currentIndex);
    
    return {
      topPlayer: otherPlayers[0] || null,
      leftPlayer: otherPlayers[1] || null
    };
  };

  const { topPlayer, leftPlayer } = getOtherPlayers();
  
  return (
    <div className="game-page">
      <div className="game-container">
        {/* 连接状态 */}
        {!isConnected && (
          <div className="connection-status">
            正在连接游戏服务器...
          </div>
        )}

        {/* 房间信息显示 */}
        <div className="room-info">
          <p>房间号: {roomId}</p>
          <p>玩家: {state.myPlayerInfo?.name || currentPlayerName}</p>
          <p>玩家数量: {displayPlayers.length}/3</p>
          <p>房间状态: {state.gamePhase === 'waiting' ? '等待玩家' : 
                          state.gamePhase === 'bidding' ? '叫地主阶段' :
                          state.gamePhase === 'playing' ? '游戏中' : '游戏结束'}</p>
        </div>

        {/* 玩家区域 - 显示其他玩家 */}
        {displayPlayers.length > 1 && (
          <>
            {/* 顶部玩家 */}
            {topPlayer && (
              <PlayerArea 
                player={topPlayer} 
                position="top"
                isCurrentTurn={state.currentPlayer === topPlayer.userId}
              />
            )}
            
            {/* 左侧玩家 */}
            {leftPlayer && (
              <PlayerArea 
                player={leftPlayer} 
                position="left"
                isCurrentTurn={state.currentPlayer === leftPlayer.userId}
              />
            )}
          </>
        )}

        {/* 游戏中央区域 */}
        <div className="game-center">
          <LastPlayArea lastPlay={state.lastPlay} />
        </div>

        {/* 叫地主面板 - 只在叫地主阶段显示 */}
        {state.gamePhase === 'bidding' && (
          <BiddingPanel 
            isMyTurn={state.currentPlayer === getCurrentPlayerId()}
            currentBidScore={state.biddingState.currentBidScore}
            currentBidder={state.biddingState.currentBidder}
            onBid={handleBid}
            onPass={handleBidPass}
          />
        )}

        {/* 出牌控制面板 - 只在游戏中且轮到自己时显示 */}
        {state.gamePhase === 'playing' && (
          <PlayControlPanel 
            isMyTurn={state.currentPlayer === getCurrentPlayerId()}
            selectedCards={state.selectedCards}
            canPlay={playAnalysis.canPlay}
            playType={playAnalysis.description}
            onPlayCards={handlePlayCards}
            onPass={handlePass}
            onCardHint={handleHint}
          />
        )}

        {/* 当前玩家手牌区域 */}
        <div className="hand-cards-area">
          <div className="hand-cards">
            {state.cards.hand.map((card: any, index: number) => (
              <Card
                key={card.id || `${card.suit}-${card.rank}-${index}`}
                card={card}
                isSelected={state.selectedCards.some(selectedCard => selectedCard.id === card.id)}
                onClick={() => handleCardClick(index)}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}