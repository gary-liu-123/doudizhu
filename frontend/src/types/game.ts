// 游戏相关类型
import { Card, Play } from './card';

export interface Player {
  userId: string;
  name: string;
  handCards: Card[];
  isLandlord: boolean;
  position: 0 | 1 | 2;
  isOnline: boolean;
  cardCount: number;
}

export interface Room {
  id: string;
  players: Player[];
  status: 'WAITING' | 'PLAYING' | 'FINISHED';
  createdAt: string;
}

export interface GameState {
  room: Room | null;
  players: Player[];
  currentPlayer: string;
  myPlayerInfo: { name: string } | null;
  gamePhase: 'waiting' | 'bidding' | 'playing' | 'finished';
  cards: {
    hand: Card[];
    played: Card[][];
    landlordCards: Card[];
  };
  landlord: string | null;
  lastPlay: Play | null;
  selectedCards: Card[];
  biddingState: {
    currentBidScore: number;
    currentBidder: string | null;
    biddingHistory: BiddingAction[];
  };
}

export interface BiddingAction {
  playerId: string;
  playerName: string;
  bidScore: number; // 0表示不叫
  timestamp: string;
}