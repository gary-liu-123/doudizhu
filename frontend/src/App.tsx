import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { GameProvider } from './contexts/GameContext';
import { HomePage } from './pages/Home/HomePage';
import { GamePage } from './pages/Game/GamePage';
import './App.css';

function App() {
  return (
    <GameProvider>
      <Router>
        <div className="App">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/game/:roomId" element={<GamePage />} />
          </Routes>
        </div>
      </Router>
    </GameProvider>
  );
}

export default App;