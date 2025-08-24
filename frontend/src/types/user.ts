// 用户相关类型
export interface User {
  id: string;
  username: string;
  nickname: string;
  totalScore: number;
  gamesPlayed: number;
  gamesWon: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  nickname: string;
}

export interface AuthResponse {
  user: User;
  token: string;
}