import React from 'react';
import { Play } from '../../types/game';
import { Card } from '../Card/Card';
import './LastPlayArea.css';

interface LastPlayAreaProps {
  lastPlay: Play | null;
}

const LastPlayArea: React.FC<LastPlayAreaProps> = ({ lastPlay }) => {
  if (!lastPlay || !lastPlay.cards || lastPlay.cards.length === 0) {
    return (
      <div className="last-play-area">
        <div className="no-play">
          <span className="no-play-text">等待出牌</span>
        </div>
      </div>
    );
  }

  return (
    <div className="last-play-area">
      <div className="play-info">
        <span className="player-name">{lastPlay.playerName}</span>
        <span className="play-type">{getPlayTypeDisplay(lastPlay.type)}</span>
      </div>
      
      <div className="cards-display">
        {lastPlay.cards.map((card, index) => (
          <div key={card.id || index} className="last-play-card">
            <Card 
              card={card}
              isSelected={false}
              onClick={() => {}}
            />
          </div>
        ))}
      </div>
    </div>
  );
};

// 牌型显示映射
const getPlayTypeDisplay = (type: string): string => {
  const typeMap: { [key: string]: string } = {
    'SINGLE': '单张',
    'PAIR': '对子',
    'TRIPLE': '三张',
    'TRIPLE_WITH_SINGLE': '三带一',
    'TRIPLE_WITH_PAIR': '三带二',
    'STRAIGHT': '顺子',
    'PAIR_STRAIGHT': '连对',
    'PLANE': '飞机',
    'PLANE_WITH_SINGLE': '飞机带单',
    'PLANE_WITH_PAIR': '飞机带双',
    'FOUR_WITH_SINGLE': '四带二（单）',
    'FOUR_WITH_PAIR': '四带二（对）',
    'BOMB': '炸弹',
    'ROCKET': '王炸'
  };
  return typeMap[type] || type;
};

export default LastPlayArea;