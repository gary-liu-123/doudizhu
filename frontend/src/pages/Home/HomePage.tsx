import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { roomApi } from '../../services/roomService';
import websocketService from '../../services/websocket';
import './HomePage.css';

export function HomePage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [playerName, setPlayerName] = useState('');
  const [showNameInput, setShowNameInput] = useState(false);
  const [actionType, setActionType] = useState<'create' | 'join' | 'quick' | null>(null);
  const [roomIdInput, setRoomIdInput] = useState('');

  const handleCreateRoom = async () => {
    if (!playerName.trim()) {
      setActionType('create');
      setShowNameInput(true);
      return;
    }

    setLoading(true);
    try {
      // 连接WebSocket
      await websocketService.connect();
      
      // 创建房间
      const response = await roomApi.createRoom(playerName);
      
      if (response.success) {
        console.log('房间创建成功:', response.data);
        // 跳转到游戏页面，传递玩家信息
        navigate(`/game/${response.data.id}?playerName=${encodeURIComponent(playerName)}`);
      } else {
        alert(response.message);
      }
    } catch (error: any) {
      console.error('创建房间失败:', error);
      alert('创建房间失败: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinRoom = async () => {
    if (!playerName.trim() || !roomIdInput.trim()) {
      setActionType('join');
      setShowNameInput(true);
      return;
    }

    setLoading(true);
    try {
      // 连接WebSocket
      await websocketService.connect();
      
      // 加入房间
      const response = await roomApi.joinRoom(roomIdInput, playerName);
      
      if (response.success) {
        console.log('加入房间成功:', response.data);
        // 跳转到游戏页面，传递玩家信息
        navigate(`/game/${response.data.id}?playerName=${encodeURIComponent(playerName)}`);
      } else {
        alert(response.message);
      }
    } catch (error: any) {
      console.error('加入房间失败:', error);
      alert('加入房间失败: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickMatch = () => {
    // 快速匹配功能待实现
    alert('快速匹配功能即将上线！');
  };

  const handleConfirmAction = () => {
    if (!playerName.trim()) {
      alert('请输入玩家昵称');
      return;
    }

    if (actionType === 'join' && !roomIdInput.trim()) {
      alert('请输入房间号');
      return;
    }

    setShowNameInput(false);
    
    if (actionType === 'create') {
      handleCreateRoom();
    } else if (actionType === 'join') {
      handleJoinRoom();
    }
  };

  const handleCancelAction = () => {
    setShowNameInput(false);
    setActionType(null);
    setPlayerName('');
    setRoomIdInput('');
  };

  return (
    <div className="home-page">
      <div className="home-container">
        <header className="home-header">
          <h1 className="game-title">斗地主在线游戏</h1>
          <p className="game-subtitle">经典三人对战，随时随地开局</p>
        </header>

        <main className="home-main">
          {!showNameInput ? (
            <div className="action-buttons">
              <button 
                className="btn btn-primary" 
                onClick={handleCreateRoom}
                disabled={loading}
              >
                {loading && actionType === 'create' ? '创建中...' : '创建房间'}
              </button>
              <button 
                className="btn btn-secondary" 
                onClick={() => {
                  setActionType('join');
                  setShowNameInput(true);
                }}
                disabled={loading}
              >
                加入房间
              </button>
              <button 
                className="btn btn-accent" 
                onClick={handleQuickMatch}
                disabled={loading}
              >
                快速匹配
              </button>
            </div>
          ) : (
            <div className="name-input-form">
              <h3>
                {actionType === 'create' ? '创建房间' : 
                 actionType === 'join' ? '加入房间' : '快速匹配'}
              </h3>
              
              <div className="form-group">
                <input
                  type="text"
                  placeholder="请输入玩家昵称"
                  value={playerName}
                  onChange={(e) => setPlayerName(e.target.value)}
                  className="form-input"
                  maxLength={20}
                />
              </div>

              {actionType === 'join' && (
                <div className="form-group">
                  <input
                    type="text"
                    placeholder="请输入房间号"
                    value={roomIdInput}
                    onChange={(e) => setRoomIdInput(e.target.value)}
                    className="form-input"
                    maxLength={6}
                  />
                </div>
              )}

              <div className="form-actions">
                <button 
                  className="btn btn-secondary" 
                  onClick={handleCancelAction}
                  disabled={loading}
                >
                  取消
                </button>
                <button 
                  className="btn btn-primary" 
                  onClick={handleConfirmAction}
                  disabled={loading}
                >
                  {loading ? '处理中...' : '确认'}
                </button>
              </div>
            </div>
          )}

          <div className="user-stats">
            <div className="stat-item">
              <span className="stat-label">总积分</span>
              <span className="stat-value">0</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">游戏场次</span>
              <span className="stat-value">0</span>
            </div>
            <div className="stat-item">
              <span className="stat-label">胜率</span>
              <span className="stat-value">0%</span>
            </div>
          </div>
        </main>

        <footer className="home-footer">
          <p>&copy; 2024 斗地主在线游戏</p>
        </footer>
      </div>
    </div>
  );
}