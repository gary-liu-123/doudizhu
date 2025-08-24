import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface WebSocketMessage {
  type: string;
  payload: any;
  timestamp: number;
}

class WebSocketService {
  private client: Client | null = null;
  private isConnected = false;

  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.isConnected) {
        resolve();
        return;
      }

      this.client = new Client({
        webSocketFactory: () => new SockJS('/api/ws'),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      this.client.onConnect = () => {
        this.isConnected = true;
        console.log('WebSocket connected');
        resolve();
      };

      this.client.onStompError = (error) => {
        console.error('WebSocket error:', error);
        this.isConnected = false;
        reject(error);
      };

      this.client.onDisconnect = () => {
        this.isConnected = false;
        console.log('WebSocket disconnected');
      };

      this.client.activate();
    });
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }

  // 发送消息
  send(destination: string, message: any) {
    if (this.client && this.isConnected) {
      this.client.publish({
        destination,
        body: JSON.stringify(message),
      });
    }
  }

  // 订阅主题
  subscribe(destination: string, callback: (message: any) => void) {
    if (this.client && this.isConnected) {
      return this.client.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body);
          callback(data);
        } catch (error) {
          console.error('Error parsing message:', error);
        }
      });
    }
    return null;
  }

  isConnectionReady(): boolean {
    return this.isConnected;
  }
}

export default new WebSocketService();