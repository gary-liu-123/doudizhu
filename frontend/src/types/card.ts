// 卡牌相关类型
export interface Card {
  suit: 'SPADE' | 'HEART' | 'DIAMOND' | 'CLUB' | null;
  rank: 'THREE' | 'FOUR' | 'FIVE' | 'SIX' | 'SEVEN' | 'EIGHT' | 'NINE' | 'TEN' | 'JACK' | 'QUEEN' | 'KING' | 'ACE' | 'TWO' | 'SMALL_JOKER' | 'BIG_JOKER';
  id: string;
}

export interface Play {
  cards: Card[];
  type: PlayType;
  rank: number;
  playerId: string;
  playerName?: string;
  timestamp?: string;
}

export type PlayType = 
  | 'SINGLE' 
  | 'PAIR' 
  | 'TRIPLE' 
  | 'TRIPLE_WITH_SINGLE'
  | 'TRIPLE_WITH_PAIR'
  | 'STRAIGHT' 
  | 'PAIR_STRAIGHT' 
  | 'PLANE' 
  | 'PLANE_WITH_SINGLE'
  | 'PLANE_WITH_PAIR'
  | 'FOUR_WITH_SINGLE'
  | 'FOUR_WITH_PAIR'
  | 'BOMB' 
  | 'ROCKET';