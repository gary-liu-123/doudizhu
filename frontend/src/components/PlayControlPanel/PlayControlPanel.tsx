import React from 'react';
import { Card } from '../../types/card';
import './PlayControlPanel.css';

interface PlayControlPanelProps {
  isMyTurn: boolean;
  selectedCards: Card[];
  canPlay: boolean;
  playType?: string;
  onPlayCards: () => void;
  onPass: () => void;
  onCardHint?: () => void;
}

const PlayControlPanel: React.FC<PlayControlPanelProps> = ({
  isMyTurn,
  selectedCards,
  canPlay,
  playType,
  onPlayCards,
  onPass,
  onCardHint
}) => {
  if (!isMyTurn) {
    return null;
  }

  return (
    <div className="play-control-panel">
      <div className="selected-info">
        <span className="selected-count">
          已选择 {selectedCards.length} 张牌
        </span>
        {playType && (
          <span className="play-type">
            牌型: {playType}
          </span>
        )}
      </div>
      
      <div className="control-buttons">
        <button 
          className="btn-pass"
          onClick={onPass}
        >
          不要
        </button>
        
        {onCardHint && (
          <button 
            className="btn-hint"
            onClick={onCardHint}
          >
            提示
          </button>
        )}
        
        <button 
          className={`btn-play ${canPlay ? 'active' : 'disabled'}`}
          onClick={onPlayCards}
          disabled={!canPlay}
        >
          出牌
        </button>
      </div>
    </div>
  );
};

export default PlayControlPanel;