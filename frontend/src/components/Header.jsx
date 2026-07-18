import React, { useEffect, useState, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Bell, Search, Check, AlertCircle, Loader2 } from 'lucide-react';

function Header() {
  const { user } = useAuth();
  const location = useLocation();

  // Notifications State
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown on clicking outside
  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
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
      
      // Subscribe to user-specific notifications queue
      stompClient.subscribe('/topic/notifications/' + user.email, (message) => {
        const received = JSON.parse(message.body);
        
        // Prepend new notification to the active list
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
    e.stopPropagation(); // Avoid triggering route redirects
    try {
      await api.put(`/api/notifications/${id}/read`);
      
      // Update local state list
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
    <header className="h-16 border-b border-slate-800 bg-slate-900/40 backdrop-blur-md sticky top-0 z-40 flex items-center justify-between px-6">
      
      {/* Page Title */}
      <div>
        <h1 className="text-lg font-bold text-slate-100 tracking-tight">
          {getPageTitle()}
        </h1>
      </div>

      {/* Action Controls */}
      <div className="flex items-center space-x-4">
        
        {/* Mock Search Bar */}
        <div className="relative hidden md:block">
          <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
            <Search size={14} />
          </span>
          <input
            type="text"
            placeholder="Search workspace..."
            className="pl-8 pr-4 py-1.5 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-100 placeholder-slate-600 focus:outline-none focus:border-indigo-500 transition w-48 focus:w-64"
          />
        </div>

        {/* Notifications Dropdown Container */}
        <div className="relative" ref={dropdownRef}>
          <button 
            onClick={() => setIsOpen(!isOpen)}
            className={`relative p-2 rounded-lg transition-all ${
              isOpen ? 'text-indigo-400 bg-slate-800' : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
            }`}
          >
            <Bell size={18} />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-4 h-4 bg-rose-500 text-[9px] font-black text-white rounded-full flex items-center justify-center border border-slate-900">
                {unreadCount}
              </span>
            )}
          </button>

          {/* Dropdown overlay panel */}
          {isOpen && (
            <div className="absolute right-0 mt-3 w-80 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in slide-in-from-top-2 duration-150 z-50">
              
              <div className="px-4 py-3 border-b border-slate-800 flex items-center justify-between bg-slate-950/20">
                <span className="text-xs font-bold text-slate-200">Recent Notifications</span>
                {unreadCount > 0 && (
                  <span className="text-[9px] font-bold bg-rose-550/10 text-rose-400 border border-rose-500/10 px-2 py-0.5 rounded-full">
                    {unreadCount} unread
                  </span>
                )}
              </div>

              {/* Scrollable list */}
              <div className="max-h-[300px] overflow-y-auto divide-y divide-slate-850/40 scrollbar-thin scrollbar-thumb-slate-950">
                {notifications.length > 0 ? (
                  notifications.map((notify) => (
                    <div 
                      key={notify.id}
                      className={`p-4 flex items-start space-x-3 transition-colors ${
                        notify.read ? 'hover:bg-slate-850/40' : 'bg-indigo-950/10 hover:bg-indigo-900/10'
                      }`}
                    >
                      <div className={`p-1.5 rounded-lg shrink-0 mt-0.5 ${
                        notify.read ? 'bg-slate-800 text-slate-500' : 'bg-indigo-500/10 text-indigo-400'
                      }`}>
                        <AlertCircle size={14} />
                      </div>
                      
                      <div className="flex-1 space-y-1">
                        <p className={`text-xs leading-relaxed ${notify.read ? 'text-slate-500' : 'text-slate-350'}`}>
                          {notify.content}
                        </p>
                        <span className="text-[8px] text-slate-500 block">
                          {new Date(notify.createdAt).toLocaleDateString()}
                        </span>
                      </div>

                      {!notify.read && (
                        <button
                          onClick={(e) => handleMarkAsRead(notify.id, e)}
                          title="Mark as Read"
                          className="p-1 hover:bg-slate-800 text-slate-500 hover:text-emerald-500 rounded-lg transition shrink-0 self-center"
                        >
                          <Check size={12} />
                        </button>
                      )}
                    </div>
                  ))
                ) : (
                  <div className="py-10 text-center opacity-30 flex flex-col items-center">
                    <Bell size={24} className="text-slate-500 mb-2" />
                    <span className="text-[10px] font-semibold uppercase tracking-wider text-slate-500">Inbox empty</span>
                  </div>
                )}
              </div>

            </div>
          )}
        </div>

        {/* User Info */}
        <div className="flex items-center space-x-3 border-l border-slate-800 pl-4">
          <div className="flex flex-col text-right hidden sm:flex">
            <span className="text-xs font-semibold text-slate-200">{user?.name}</span>
            <span className="text-[10px] text-slate-500">{user?.email}</span>
          </div>
          <div className="w-8 h-8 rounded-lg bg-indigo-600/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center text-xs font-bold shadow-md">
            {getInitials(user?.name)}
          </div>
        </div>

      </div>

    </header>
  );
}

export default Header;
