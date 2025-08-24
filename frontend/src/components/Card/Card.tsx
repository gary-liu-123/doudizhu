import React from 'react';
import { Card as CardType } from '../../types/card';

interface CardProps {
  card: CardType;
  isSelected?: boolean;
  isPlayable?: boolean;
  onClick?: () => void;
  className?: string;
}

const suitSymbols = {
  SPADE: '♠',
  HEART: '♥',
  DIAMOND: '♦',
  CLUB: '♣'
};

const rankDisplays = {
  THREE: '3', FOUR: '4', FIVE: '5', SIX: '6', SEVEN: '7', EIGHT: '8',
  NINE: '9', TEN: '10', JACK: 'J', QUEEN: 'Q', KING: 'K', ACE: 'A',
  TWO: '2', SMALL_JOKER: '小王', BIG_JOKER: '大王'
};

export function Card({ card, isSelected, isPlayable = true, onClick, className }: CardProps) {
  const isRed = card.suit === 'HEART' || card.suit === 'DIAMOND';
  const isJoker = card.rank === 'SMALL_JOKER' || card.rank === 'BIG_JOKER';

  return (
    <div
      className={`card ${isSelected ? 'selected' : ''} ${!isPlayable ? 'disabled' : ''} ${className || ''}`}
      onClick={isPlayable ? onClick : undefined}
      data-card={card.id}
    >
      <div className={`card-content ${isRed ? 'red' : 'black'}`}>
        {isJoker ? (
          <div className="joker">
            {rankDisplays[card.rank]}
          </div>
        ) : (
          <>
            <div className="rank">{rankDisplays[card.rank]}</div>
            <div className="suit">{card.suit ? suitSymbols[card.suit] : ''}</div>
          </>
        )}
      </div>
    </div>
  );
}