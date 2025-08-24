import React from 'react';
import { Player as PlayerType } from '../types/game';

interface PlayerAreaProps {
  player: PlayerType;
  position: 'top' | 'left' | 'right' | 'bottom';
  isCurrentTurn?: boolean;
  className?: string;
}

export function PlayerArea({ player, position, isCurrentTurn, className }: PlayerAreaProps) {
  const getAvatarUrl = (userId: string) => {
    // 临时使用简单的头像生成
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${userId}`;
  };

  return (
    <div className={`player-area ${position} ${isCurrentTurn ? 'current-turn' : ''} ${className || ''}`}>
      <div className="player-info">
        <div className={`player-avatar ${player.isLandlord ? 'landlord' : ''} ${!player.isOnline ? 'offline' : ''}`}>
          <img src={getAvatarUrl(player.userId)} alt={player.name} />
          {player.isLandlord && <div className="landlord-crown">👑</div>}
        </div>
        <div className="player-details">
          <span className="player-name">{player.name}</span>
          <span className="cards-count">{player.cardCount}张</span>
        </div>
      </div>
      
      {/* 显示其他玩家的手牌（背面） */}
      {position !== 'bottom' && player.cardCount > 0 && (
        <div className="other-player-cards">
          {Array.from({ length: Math.min(player.cardCount, 10) }).map((_, index) => (
            <div key={index} className="card-back" />
          ))}
          {player.cardCount > 10 && (
            <div className="more-cards">+{player.cardCount - 10}</div>
          )}
        </div>
      )}
    </div>
  );
}