// App.js
import React, { useState, useEffect } from 'react';
import './App.css';

function UserNameModal({ onSubmit, onClose }) {
  const [username, setUsername] = useState('');

  const handleSubmit = () => {
    onSubmit(username);
    onClose();
  };

  return (
    <div className="modal">
      <input
        type="text"
        placeholder="Enter your name"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
      />
      <button onClick={handleSubmit}>Submit</button>
      <button onClick={onClose}>Close</button>
    </div>
  );
}

function App() {
  const [grid, setGrid] = useState(Array(20).fill().map(() => Array(20).fill(' ')));
  const [snakeLength, setSnakeLength] = useState(0);
  const [gameStarted, setGameStarted] = useState(false);
  const [gameOver, setGameOver] = useState(false);
  const [isBackendReady, setIsBackendReady] = useState(false);
  const [leaderboard, setLeaderboard] = useState([]);
  const [showLeaderboard, setShowLeaderboard] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [studentInfo, setStudentInfo] = useState({ name: '', id: '' });

  const fetchStudentInfo = () => {
    fetch("http://localhost:8080/student-info")
      .then(response => response.json())
      .then(data => {
        setStudentInfo({ name: data.name, id: data.id });
      })
      .catch(error => console.error('Error:', error));
  };

  const fetchGameData = () => {
    fetch("http://localhost:8080/game-data")
      .then(response => response.json())
      .then(data => {
        setGrid(data.grid);
        setSnakeLength(data.snakeLength);
        if (data.gameOver && !gameOver) {
          setGameOver(true);
          setGameStarted(false);
        }
      })
      .catch(error => console.error('Error:', error));
  };

  const handleGameOver = () => {
    setShowModal(true); 
  };

  const handleModalSubmit = (username) => {
    submitScore(username); // 모달에서 제출 시 실행할 함수
  };

  const submitScore = (playerName) => {    
    fetch("http://localhost:8080/submit-score", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: playerName, score: snakeLength })
    }).then(() => {
    }).catch(error => console.error('Error:', error));
  };


  const fetchLeaderboard = () => {
    fetch("http://localhost:8080/leaderboard")
        .then(response => response.json())
        .then(data => {
            setLeaderboard(data);
            if (!showLeaderboard) {
              setShowLeaderboard(true);
            }
            else {
              setShowLeaderboard(false);
            }
        })
        .catch(error => console.error('Error fetching leaderboard:', error));
  };


  const startNewGame = () => {
    setGrid(Array(20).fill().map(() => Array(20).fill(' ')));
    setSnakeLength(0);
    fetch("http://localhost:8080/new-game", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
    }).then(() => {
        setGameStarted(true);
        setGameOver(false);
        setShowLeaderboard(false);
    }).catch(error => console.error('Error:', error));
  };


  const handleKeyPress = (event) => {
    if (event.key === 'Escape') {
      fetch("http://localhost:8080/game-over", {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
      }).then(() => {
          setGameOver(true);
          setGameStarted(false);
      }).catch(error => console.error('Error:', error));
      return;
    }
    let direction = '';
    switch (event.key) {
      case 'ArrowUp': direction = 'UP'; break;
      case 'ArrowDown': direction = 'DOWN'; break;
      case 'ArrowLeft': direction = 'LEFT'; break;
      case 'ArrowRight': direction = 'RIGHT'; break;
      default: return;
    }

    fetch("http://localhost:8080/set-direction", {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ direction })
    });
  };

  useEffect(() => {
    fetchStudentInfo(); // 컴포넌트가 마운트될 때 학생 정보를 가져옴
  }, []);

  useEffect(() => {
    if (gameStarted) {
      window.addEventListener("keydown", handleKeyPress);
      const interval = setInterval(fetchGameData, 60); // every 60ms

      return () => {
        window.removeEventListener("keydown", handleKeyPress);
        clearInterval(interval);
      };
    }
  }, [gameStarted]);

  useEffect(() => {
    if (gameOver) {
      handleGameOver();
    }
  }, [gameOver]);
  
  useEffect(() => {
    fetch("http://localhost:8080/health-check")
        .then(response => {
            if (response.ok) {
                setIsBackendReady(true);
            }
        })
        .catch(error => {
            console.error('Error checking backend status:', error);
            // Optionally set a timeout to retry
        });
  }, []);

  if (!gameStarted) {
    return (
      <div className="App">
        <header className="App-header">
          {gameOver ? <h1>Game Over</h1> : <h1>Welcome to Snake Game</h1>}
          <div className="student-info left-aligned">
          <p>Student Name: {studentInfo.name} </p>
          <p>Student ID: {studentInfo.id}</p>
          </div>

          <div className="button-container">
            <button onClick={startNewGame} disabled={!isBackendReady}>
              {gameOver ? 'Start New Game' : isBackendReady ? 'Start Game' : 'Loading Game...'}
            </button>
            <button onClick={fetchLeaderboard} disabled={!isBackendReady}>
              {showLeaderboard ? 'Close Leaderboard' : isBackendReady ? 'View Leaderboard' : 'Loading Leaderboard...'}
            </button>
          </div>
          {showLeaderboard && (
              <div className="leaderboard">
                  <h2>Leaderboard</h2>
                  <table>
                      <thead>
                          <tr>
                              <th>Rank</th>
                              <th>Name</th>
                              <th>Score</th>
                          </tr>
                      </thead>
                      <tbody>
                          {leaderboard.map((row, index) => (
                              <tr key={index}>
                                  <td>{index + 1}</td>
                                  <td>{row.name}</td>
                                  <td>{row.score}</td>
                              </tr>
                          ))}
                      </tbody>  
                  </table>
              </div>
          )}
          {showModal && (
            <UserNameModal
              onSubmit={handleModalSubmit}
              onClose={() => setShowModal(false)}
            />
          )}
        </header>
      </div>
    );
  }

  return (
    <div className="App">
      <div className="student-info left-aligned">
          <p>Student Name: {studentInfo.name} </p>
          <p>Student ID: {studentInfo.id}</p>
      </div>
      <header className="App-header">
        <p>Snake Length: {snakeLength}</p>
        <div className="game-grid">
          {grid.map((row, rowIndex) => (
            <div key={rowIndex} className="grid-row">
              {row.map((cell, cellIndex) => (
                <span key={cellIndex} className={`grid-cell ${
                  cell === '*' ? 'snake' : cell === '@' ? 'apple' : ''
                }`}>{cell === '*' ? '' : cell === '@' ? '' : cell}</span> 
              ))}
            </div>
          ))}
        </div>
      </header>
    </div>
  );
}

export default App;
