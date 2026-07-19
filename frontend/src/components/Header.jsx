import React, { useEffect, useState, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Bell, Search, Check, AlertCircle, Loader2, Sun, Moon, Laptop } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

function Header() {
  const { user } = useAuth();
  const location = useLocation();

  // Notifications State
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Theme State
  const { theme, setTheme } = useTheme();
  const [isThemeOpen, setIsThemeOpen] = useState(false);
  const themeDropdownRef = useRef(null);

  // Close dropdown on clicking outside
  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
      if (themeDropdownRef.current && !themeDropdownRef.current.contains(e.target)) {
        setIsThemeOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  // Fetch initial notifications history
  const loadNotifications = async () => {
    if (!user) return;
    try {
      const [listRes, countRes] = await Promise.all([
        api.get('/api/notifications'),
        api.get('/api/notifications/unread-count')
      ]);
      setNotifications(listRes.data || []);
      setUnreadCount(countRes.data.unreadCount || 0);
    } catch (err) {
      console.error('Failed to load notifications history:', err);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, [user]);

  // WebSocket connection subscribing to /topic/notifications/{email}
  useEffect(() => {
    if (!user?.email) return;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws-chat'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = () => {
      console.log('Connected to WebSocket server');

      stompClient.subscribe('/topic/notifications/' + user.email, (message) => {
        const received = JSON.parse(message.body);
        setNotifications((prev) => [received, ...prev]);
        setUnreadCount((c) => c + 1);
      });
    };

    stompClient.onStompError = (frame) => {
      console.error('STOMP protocol error:', frame.headers['message']);
    };

    stompClient.activate();

    return () => {
      stompClient.deactivate();
      console.log('Disconnected from WebSocket');
    };
  }, [user?.email]);

  const handleMarkAsRead = async (id, e) => {
    e.stopPropagation();
    try {
      await api.put(`/api/notifications/${id}/read`);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((c) => Math.max(0, c - 1));
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  };

  const getPageTitle = () => {
    switch (location.pathname) {
      case '/':
        return 'Workspace Dashboard';
      case '/projects':
        return 'Projects Management';
      case '/tasks':
        return 'Kanban Tasks Board';
      case '/team':
        return 'Team Directory';
      case '/profile':
        return 'Profile Settings';
      default:
        return 'TeamCollab';
    }
  };

  const getInitials = (name) => {
    if (!name) return 'U';
    return name
      .split(' ')
      .map((n) => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  };

  return (
    <header className="h-16 border-b border-theme-border bg-theme-card/45 backdrop-blur-md sticky top-0 z-40 flex items-center justify-between px-6">

      {/* Page Title */}
      <div>
        <h1 className="text-lg font-bold text-theme-text tracking-tight">
          {getPageTitle()}
        </h1>
      </div>

      {/* Action Controls */}
      <div className="flex items-center space-x-4">

        {/* Mock Search Bar */}
        <div className="relative hidden md:block">
          <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-theme-muted">
            <Search size={14} />
          </span>
          <input
            type="text"
            placeholder="Search workspace..."
            className="pl-8 pr-4 py-1.5 bg-theme-bg border border-theme-border rounded-lg text-xs text-theme-text placeholder-theme-muted focus:outline-none focus:border-indigo-500 transition w-48 focus:w-64"
          />
        </div>

        {/* Theme Switcher Dropdown */}
        <div className="relative" ref={themeDropdownRef}>
          <button
            onClick={() => setIsThemeOpen(!isThemeOpen)}
            className={`p-2 rounded-lg transition-all ${isThemeOpen
                ? 'text-indigo-500 bg-theme-bg'
                : 'text-theme-muted hover:text-theme-text hover:bg-theme-bg/60'
              }`}
          >
            {theme === 'light' ? <Sun size={18} /> : theme === 'dark' ? <Moon size={18} /> : <Laptop size={18} />}
          </button>

          {isThemeOpen && (
            <div className="absolute right-0 mt-3 w-40 bg-theme-card border border-theme-border rounded-2xl shadow-2xl overflow-hidden animate-in fade-in slide-in-from-top-2 duration-150 z-50 p-2 space-y-1">
              <button
                type="button"
                onClick={() => { setTheme('light'); setIsThemeOpen(false); }}
                className={`w-full text-left px-3 py-2 text-xs font-semibold rounded-xl flex items-center space-x-2 transition ${theme === 'light' ? 'bg-indigo-650 text-white' : 'text-theme-muted hover:bg-theme-bg hover:text-theme-text'
                  }`}
              >
                <Sun size={14} />
                <span>Light Mode</span>
              </button>
              <button
                type="button"
                onClick={() => { setTheme('dark'); setIsThemeOpen(false); }}
                className={`w-full text-left px-3 py-2 text-xs font-semibold rounded-xl flex items-center space-x-2 transition ${theme === 'dark' ? 'bg-indigo-650 text-white' : 'text-theme-muted hover:bg-theme-bg hover:text-theme-text'
                  }`}
              >
                <Moon size={14} />
                <span>Dark Mode</span>
              </button>
              <button
                type="button"
                onClick={() => { setTheme('system'); setIsThemeOpen(false); }}
                className={`w-full text-left px-3 py-2 text-xs font-semibold rounded-xl flex items-center space-x-2 transition ${theme === 'system' ? 'bg-indigo-650 text-white' : 'text-theme-muted hover:bg-theme-bg hover:text-theme-text'
                  }`}
              >
                <Laptop size={14} />
                <span>System Default</span>
              </button>
            </div>
          )}
        </div>

        {/* Notifications Dropdown Container */}
        <div className="relative" ref={dropdownRef}>
          <button
            onClick={() => setIsOpen(!isOpen)}
            className={`relative p-2 rounded-lg transition-all ${isOpen ? 'text-indigo-500 bg-theme-bg' : 'text-theme-muted hover:text-theme-text hover:bg-theme-bg/60'
              }`}
          >
            <Bell size={18} />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-4 h-4 bg-rose-500 text-[9px] font-black text-white rounded-full flex items-center justify-center border border-theme-border">
                {unreadCount}
              </span>
            )}
          </button>

          {/* Dropdown overlay panel */}
          {isOpen && (
            <div className="absolute right-0 mt-3 w-80 bg-theme-card border border-theme-border rounded-2xl shadow-2xl overflow-hidden animate-in fade-in slide-in-from-top-2 duration-150 z-50">

              <div className="px-4 py-3 border-b border-theme-border flex items-center justify-between bg-theme-bg/40">
                <span className="text-xs font-bold text-theme-text">Recent Notifications</span>
                {unreadCount > 0 && (
                  <span className="text-[9px] font-bold bg-rose-500/10 text-rose-500 border border-rose-500/10 px-2 py-0.5 rounded-full">
                    {unreadCount} unread
                  </span>
                )}
              </div>

              {/* Scrollable list */}
              <div className="max-h-[300px] overflow-y-auto divide-y divide-theme-border scrollbar-thin scrollbar-thumb-theme-bg">
                {notifications.length > 0 ? (
                  notifications.map((notify) => (
                    <div
                      key={notify.id}
                      className={`p-4 flex items-start space-x-3 transition-colors ${notify.read ? 'hover:bg-theme-bg/60' : 'bg-indigo-500/5 hover:bg-indigo-500/10'
                        }`}
                    >
                      <div className={`p-1.5 rounded-lg shrink-0 mt-0.5 ${notify.read ? 'bg-theme-bg text-theme-muted' : 'bg-indigo-550/10 text-indigo-500'
                        }`}>
                        <AlertCircle size={14} />
                      </div>

                      <div className="flex-1 space-y-1">
                        <p className={`text-xs leading-relaxed ${notify.read ? 'text-theme-muted' : 'text-theme-text'}`}>
                          {notify.message}
                        </p>
                        <span className="text-[8px] text-theme-muted block">
                          {new Date(notify.createdAt).toLocaleDateString()}
                        </span>
                      </div>

                      {!notify.read && (
                        <button
                          onClick={(e) => handleMarkAsRead(notify.id, e)}
                          title="Mark as Read"
                          className="p-1 hover:bg-theme-bg text-theme-muted hover:text-emerald-500 rounded-lg transition shrink-0 self-center"
                        >
                          <Check size={12} />
                        </button>
                      )}
                    </div>
                  ))
                ) : (
                  <div className="py-10 text-center opacity-30 flex flex-col items-center">
                    <Bell size={24} className="text-theme-muted mb-2" />
                    <span className="text-[10px] font-semibold uppercase tracking-wider text-theme-muted">Inbox empty</span>
                  </div>
                )}
              </div>

            </div>
          )}
        </div>

        {/* User Info */}
        <div className="flex items-center space-x-3 border-l border-theme-border pl-4">
          <div className="flex flex-col text-right hidden sm:flex">
            <span className="text-xs font-semibold text-theme-text">{user?.name}</span>
            <span className="text-[10px] text-theme-muted">{user?.email}</span>
          </div>
          <div className="w-8 h-8 rounded-lg bg-indigo-600/10 border border-indigo-500/20 text-indigo-500 flex items-center justify-center text-xs font-bold shadow-md">
            {getInitials(user?.name)}
          </div>
        </div>

      </div>

    </header>
  );
}

export default Header;
