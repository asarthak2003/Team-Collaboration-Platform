import React from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Bell, Search, User } from 'lucide-react';

function Header() {
  const { user } = useAuth();
  const location = useLocation();

  // Determine page title based on current routing path
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

  // Get user initials for avatar
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
      
      {/* Dynamic Page Title */}
      <div>
        <h1 className="text-lg font-bold text-slate-100 tracking-tight">
          {getPageTitle()}
        </h1>
      </div>

      {/* Action Controls (Search, Bell Notification, Avatar) */}
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

        {/* Notification Bell Badge Button */}
        <button className="relative p-2 text-slate-400 hover:text-slate-100 hover:bg-slate-800/60 rounded-lg transition-all">
          <Bell size={18} />
          {/* Temporary placeholder red dot for notifications */}
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-rose-500 rounded-full animate-pulse"></span>
        </button>

        {/* User Info / Profile Icon */}
        <div className="flex items-center space-x-3 border-l border-slate-800 pl-4">
          <div className="flex flex-col text-right hidden sm:flex">
            <span className="text-xs font-semibold text-slate-200">{user?.name}</span>
            <span className="text-[10px] text-slate-500">{user?.email}</span>
          </div>
          <div className="w-8 h-8 rounded-lg bg-indigo-600/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center text-xs font-bold shadow-md shadow-indigo-600/5">
            {getInitials(user?.name)}
          </div>
        </div>

      </div>

    </header>
  );
}

export default Header;
