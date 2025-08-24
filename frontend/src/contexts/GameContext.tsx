import React, { createContext, useContext, useReducer, ReactNode } from 'react';
import { GameState } from '../types/game';
import { Card } from '../types/card';

type GameAction =
  | { type: 'SET_ROOM'; payload: any }
  | { type: 'UPDATE_PLAYERS'; payload: any[] }
  | { type: 'SET_CURRENT_PLAYER'; payload: string }
  | { type: 'SET_MY_PLAYER_INFO'; payload: any }
  | { type: 'SET_GAME_PHASE'; payload: 'waiting' | 'bidding' | 'playing' | 'finished' }
  | { type: 'SET_HAND_CARDS'; payload: Card[] }
  | { type: 'SET_SELECTED_CARDS'; payload: Card[] }
  | { type: 'SET_LANDLORD'; payload: string | null }
  | { type: 'SET_LAST_PLAY'; payload: any }
  | { type: 'SET_BIDDING_STATE'; payload: { currentBidScore: number; currentBidder: string | null; biddingHistory: any[] } };

const initialState: GameState = {
  room: null,
  players: [],
  currentPlayer: '',
  myPlayerInfo: null,
  gamePhase: 'waiting',
  cards: {
    hand: [],
    played: [],
    landlordCards: []
  },
  landlord: null,
  lastPlay: null,
  selectedCards: [],
  biddingState: {
    currentBidScore: 0,
    currentBidder: null,
    biddingHistory: []
  }
};

function gameReducer(state: GameState, action: GameAction): GameState {
  switch (action.type) {
    case 'SET_ROOM':
      return { ...state, room: action.payload };
    case 'UPDATE_PLAYERS':
      return { ...state, players: action.payload };
    case 'SET_CURRENT_PLAYER':
      return { ...state, currentPlayer: action.payload };
    case 'SET_MY_PLAYER_INFO':
      return { ...state, myPlayerInfo: action.payload };
    case 'SET_GAME_PHASE':
      return { ...state, gamePhase: action.payload };
    case 'SET_HAND_CARDS':
      return { ...state, cards: { ...state.cards, hand: action.payload } };
    case 'SET_SELECTED_CARDS':
      return { ...state, selectedCards: action.payload };
    case 'SET_LANDLORD':
      return { ...state, landlord: action.payload };
    case 'SET_LAST_PLAY':
      return { ...state, lastPlay: action.payload };
    case 'SET_BIDDING_STATE':
      return { ...state, biddingState: action.payload };
    default:
      return state;
  }
}

interface GameContextType {
  state: GameState;
  dispatch: React.Dispatch<GameAction>;
}

const GameContext = createContext<GameContextType | undefined>(undefined);

export function GameProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(gameReducer, initialState);

  return (
    <GameContext.Provider value={{ state, dispatch }}>
      {children}
    </GameContext.Provider>
  );
}

export function useGame() {
  const context = useContext(GameContext);
  if (context === undefined) {
    throw new Error('useGame must be used within a GameProvider');
  }
  return context;
}