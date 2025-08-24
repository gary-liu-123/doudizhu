import api from './api';
import { Room } from '../types/game';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: number;
}

export interface CreateRoomRequest {
  playerName: string;
}

export interface JoinRoomRequest {
  roomId: string;
  playerName: string;
}

// 房间相关API
export const roomApi = {
  // 创建房间
  createRoom: async (playerName: string): Promise<ApiResponse<Room>> => {
    return api.post('/rooms/create', { playerName });
  },

  // 加入房间
  joinRoom: async (roomId: string, playerName: string): Promise<ApiResponse<Room>> => {
    return api.post('/rooms/join', { roomId, playerName });
  },

  // 获取房间信息
  getRoomInfo: async (roomId: string): Promise<ApiResponse<Room>> => {
    return api.get(`/rooms/${roomId}`);
  },

  // 离开房间
  leaveRoom: async (userId: string): Promise<ApiResponse<string>> => {
    return api.post('/rooms/leave', null, { params: { userId } });
  },
};

// 基础API
export const baseApi = {
  // 健康检查
  healthCheck: async () => {
    return api.get('/health');
  },
};