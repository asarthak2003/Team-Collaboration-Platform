import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';
import Register from './pages/Register';

// Protected Route wrapper component
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 text-indigo-400 flex items-center justify-center font-bold">
        Loading...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

function AppContent() {
  const { user } = useAuth();
  
  return (
    <Routes>
      {/* Auth routes (redirect logged-in users back to root home) */}
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <Register />} />
      
      {/* Protected Root Dashboard */}
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <div className="min-h-screen bg-slate-950 text-white flex flex-col items-center justify-center space-y-4">
              <h1 className="text-4xl font-extrabold text-indigo-400 animate-pulse">
                Welcome back, {user?.name}!
              </h1>
              <p className="text-slate-400 text-sm">
                You are successfully logged in as <span className="text-indigo-400 font-semibold">{user?.role}</span>
              </p>
              <button 
                onClick={() => window.location.reload(localStorage.clear())}
                className="mt-4 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 rounded-lg text-xs font-bold transition"
              >
                Sign Out / Reset Session
              </button>
            </div>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppContent />
      </Router>
    </AuthProvider>
  );
}

export default App;
