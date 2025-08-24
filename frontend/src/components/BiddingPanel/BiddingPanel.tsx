import React, { useState, useEffect } from 'react';
import { useGame } from '../../contexts/GameContext';
import './BiddingPanel.css';

interface BiddingPanelProps {
  isMyTurn: boolean;
  currentBidScore: number;
  currentBidder: string | null;
  onBid: (bidScore: number) => void;
  onPass: () => void;
}

export function BiddingPanel({ 
  isMyTurn, 
  currentBidScore, 
  currentBidder, 
  onBid, 
  onPass 
}: BiddingPanelProps) {
  const { state } = useGame();
  const [timeLeft, setTimeLeft] = useState(30);

  useEffect(() => {
    if (!isMyTurn) {
      setTimeLeft(30);
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          // 时间到自动不叫
          onPass();
          return 30;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [isMyTurn, onPass]);

  const handleBid = (score: number) => {
    if (!isMyTurn || score <= currentBidScore) return;
    onBid(score);
  };

  const getCurrentBidderName = () => {
    if (!currentBidder) return '';
    const bidder = state.players.find((p: any) => p.userId.toString() === currentBidder);
    return bidder ? bidder.name : '';
  };

  return (
    <div className="bidding-panel">
      <div className="bidding-header">
        <h3>叫地主阶段</h3>
        {currentBidScore > 0 && (
          <p className="current-bid">
            当前最高: {getCurrentBidderName()} - {currentBidScore}分
          </p>
        )}
      </div>

      {isMyTurn ? (
        <div className="bidding-actions">
          <div className="countdown-timer">
            <span className="timer-text">{timeLeft}s</span>
          </div>
          
          <div className="bid-buttons">
            <button
              className="bid-button"
              onClick={() => handleBid(1)}
              disabled={currentBidScore >= 1}
            >
              1分
            </button>
            <button
              className="bid-button"
              onClick={() => handleBid(2)}
              disabled={currentBidScore >= 2}
            >
              2分
            </button>
            <button
              className="bid-button"
              onClick={() => handleBid(3)}
              disabled={currentBidScore >= 3}
            >
              3分
            </button>
            <button
              className="pass-button"
              onClick={onPass}
            >
              不叫
            </button>
          </div>
        </div>
      ) : (
        <div className="waiting-for-bid">
          <p>等待其他玩家叫地主...</p>
          <div className="loading-spinner">
            <div className="spinner"></div>
          </div>
        </div>
      )}
    </div>
  );
}