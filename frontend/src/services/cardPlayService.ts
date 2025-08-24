import { Card } from '../types/card';

const API_BASE_URL = '/api';

export interface PlayCardRequest {
  roomId: string;
  playerId: string;
  playerName: string;
  cardIds: string[];
}

export interface PassPlayRequest {
  roomId: string;
  playerId: string;
  playerName: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

/**
 * 出牌API
 */
export const playCards = async (request: PlayCardRequest): Promise<ApiResponse<string>> => {
  const response = await fetch(`${API_BASE_URL}/rooms/play`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  
  return response.json();
};

/**
 * 跳过出牌API
 */
export const passPlay = async (request: PassPlayRequest): Promise<ApiResponse<string>> => {
  const response = await fetch(`${API_BASE_URL}/rooms/pass`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  });
  
  return response.json();
};

/**
 * 牌型验证和分析（客户端预验证）
 */
export interface PlayAnalysis {
  isValid: boolean;
  playType: string;
  description: string;
  canPlay: boolean;
}

export const analyzeCards = (selectedCards: Card[], lastPlayCards?: Card[]): PlayAnalysis => {
  if (selectedCards.length === 0) {
    return {
      isValid: false,
      playType: 'NONE',
      description: '请选择要出的牌',
      canPlay: false
    };
  }

  // 简化的牌型识别，后端会进行完整验证
  const analysis = getCardTypeAnalysis(selectedCards);
  
  // 如果没有上家牌，任意有效牌型都可以出
  if (!lastPlayCards || lastPlayCards.length === 0) {
    return {
      ...analysis,
      canPlay: analysis.isValid
    };
  }
  
  // 简单的大小比较（具体逻辑由后端处理）
  const canBeat = canBeatLastPlay(selectedCards, lastPlayCards);
  
  return {
    ...analysis,
    canPlay: analysis.isValid && canBeat
  };
};

/**
 * 简化的牌型分析
 */
const getCardTypeAnalysis = (cards: Card[]): Omit<PlayAnalysis, 'canPlay'> => {
  const count = cards.length;
  
  if (count === 1) {
    return {
      isValid: true,
      playType: 'SINGLE',
      description: '单张'
    };
  }
  
  if (count === 2) {
    // 检查是否为对子或王炸
    if (isSameRank(cards)) {
      return {
        isValid: true,
        playType: 'PAIR',
        description: '对子'
      };
    }
    
    if (isRocket(cards)) {
      return {
        isValid: true,
        playType: 'ROCKET',
        description: '王炸'
      };
    }
    
    return {
      isValid: false,
      playType: 'INVALID',
      description: '无效牌型'
    };
  }
  
  if (count === 3) {
    if (isSameRank(cards)) {
      return {
        isValid: true,
        playType: 'TRIPLE',
        description: '三张'
      };
    }
    
    return {
      isValid: false,
      playType: 'INVALID',
      description: '无效牌型'
    };
  }
  
  if (count === 4) {
    // 检查炸弹或三带一
    if (isBomb(cards)) {
      return {
        isValid: true,
        playType: 'BOMB',
        description: '炸弹'
      };
    }
    
    if (isTripleWithOne(cards)) {
      return {
        isValid: true,
        playType: 'TRIPLE_WITH_SINGLE',
        description: '三带一'
      };
    }
    
    return {
      isValid: false,
      playType: 'INVALID',
      description: '无效牌型'
    };
  }
  
  if (count === 5) {
    if (isTripleWithPair(cards)) {
      return {
        isValid: true,
        playType: 'TRIPLE_WITH_PAIR',
        description: '三带二'
      };
    }
    
    if (isStraight(cards)) {
      return {
        isValid: true,
        playType: 'STRAIGHT',
        description: '顺子'
      };
    }
    
    return {
      isValid: false,
      playType: 'INVALID',
      description: '无效牌型'
    };
  }
  
  // 更多牌数的情况，简化处理
  if (count >= 6) {
    if (isStraight(cards)) {
      return {
        isValid: true,
        playType: 'STRAIGHT',
        description: '顺子'
      };
    }
    
    if (count % 2 === 0 && isConsecutivePairs(cards)) {
      return {
        isValid: true,
        playType: 'PAIR_STRAIGHT',
        description: '连对'
      };
    }
  }
  
  return {
    isValid: false,
    playType: 'INVALID',
    description: '无效牌型'
  };
};

// 辅助函数
const isSameRank = (cards: Card[]): boolean => {
  return cards.every(card => card.rank === cards[0].rank);
};

const isRocket = (cards: Card[]): boolean => {
  return cards.length === 2 && 
         cards.some(card => card.rank === 'SMALL_JOKER') &&
         cards.some(card => card.rank === 'BIG_JOKER');
};

const isBomb = (cards: Card[]): boolean => {
  return cards.length === 4 && isSameRank(cards);
};

const isTripleWithOne = (cards: Card[]): boolean => {
  const rankCounts = getRankCounts(cards);
  const counts = Object.values(rankCounts);
  return counts.includes(3) && counts.includes(1);
};

const isTripleWithPair = (cards: Card[]): boolean => {
  const rankCounts = getRankCounts(cards);
  const counts = Object.values(rankCounts);
  return counts.includes(3) && counts.includes(2);
};

const isStraight = (cards: Card[]): boolean => {
  if (cards.length < 5) return false;
  
  const ranks = cards.map(card => getRankValue(card.rank)).sort((a, b) => a - b);
  
  // 检查是否连续
  for (let i = 1; i < ranks.length; i++) {
    if (ranks[i] - ranks[i - 1] !== 1) {
      return false;
    }
  }
  
  // 顺子不能包含2和王
  return !ranks.some(rank => rank >= 15);
};

const isConsecutivePairs = (cards: Card[]): boolean => {
  const rankCounts = getRankCounts(cards);
  const ranks = Object.keys(rankCounts).map(Number).sort((a, b) => a - b);
  
  // 检查每个点数都是2张
  if (!Object.values(rankCounts).every(count => count === 2)) {
    return false;
  }
  
  // 检查点数是否连续
  for (let i = 1; i < ranks.length; i++) {
    if (ranks[i] - ranks[i - 1] !== 1) {
      return false;
    }
  }
  
  // 连对不能包含2和王
  return !ranks.some(rank => rank >= 15);
};

const getRankCounts = (cards: Card[]): { [rank: number]: number } => {
  const counts: { [rank: number]: number } = {};
  
  cards.forEach(card => {
    const rankValue = getRankValue(card.rank);
    counts[rankValue] = (counts[rankValue] || 0) + 1;
  });
  
  return counts;
};

const getRankValue = (rank: string): number => {
  const rankValues: { [key: string]: number } = {
    'THREE': 3, 'FOUR': 4, 'FIVE': 5, 'SIX': 6, 'SEVEN': 7,
    'EIGHT': 8, 'NINE': 9, 'TEN': 10, 'JACK': 11, 'QUEEN': 12,
    'KING': 13, 'ACE': 14, 'TWO': 15, 'SMALL_JOKER': 16, 'BIG_JOKER': 17
  };
  
  return rankValues[rank] || 0;
};

const canBeatLastPlay = (currentCards: Card[], lastCards: Card[]): boolean => {
  // 简化的比较逻辑，实际由后端验证
  // 王炸最大
  if (isRocket(currentCards)) return true;
  
  // 炸弹可以压非炸弹
  if (isBomb(currentCards) && !isBomb(lastCards)) return true;
  
  // 同类型牌比较大小（简化）
  if (currentCards.length === lastCards.length) {
    const currentMax = Math.max(...currentCards.map(c => getRankValue(c.rank)));
    const lastMax = Math.max(...lastCards.map(c => getRankValue(c.rank)));
    return currentMax > lastMax;
  }
  
  return false;
};