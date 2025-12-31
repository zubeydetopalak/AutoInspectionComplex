import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import SecretaryDashboard from './components/SecretaryDashboard';
import InspectorDashboard from './components/InspectorDashboard';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/secretary-dashboard" element={<SecretaryDashboard />} />
          <Route path="/inspector-dashboard" element={<InspectorDashboard />} />
          <Route path="/" element={<Navigate to="/login" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
