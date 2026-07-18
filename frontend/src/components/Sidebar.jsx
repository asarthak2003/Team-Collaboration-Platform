import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Layers, 
  LayoutDashboard, 
  FolderKanban, 
  CheckSquare, 
  Users, 
  User, 
  LogOut 
} from 'lucide-react';

function Sidebar() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const menuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Projects', path: '/projects', icon: FolderKanban },
    { name: 'Tasks', path: '/tasks', icon: CheckSquare },
    { name: 'Team', path: '/team', icon: Users },
    { name: 'Profile', path: '/profile', icon: User },
  ];

  return (
    <aside className="w-64 bg-theme-card border-r border-theme-border flex flex-col justify-between h-screen sticky top-0">
      
      {/* Brand Logo Header */}
      <div>
        <div className="p-6 border-b border-theme-border flex items-center space-x-3">
          <div className="bg-indigo-600 p-2 rounded-lg text-white">
            <Layers size={20} />
          </div>
          <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-violet-400 to-indigo-300 bg-clip-text text-transparent">
            TeamCollab
          </span>
        </div>

        {/* Navigation Links */}
        <nav className="p-4 space-y-1.5">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.name}
                to={item.path}
                className={`flex items-center space-x-3 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                  isActive
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/10'
                    : 'text-theme-muted hover:bg-theme-bg hover:text-theme-text'
                }`}
              >
                <Icon size={18} />
                <span>{item.name}</span>
              </Link>
            );
          })}
        </nav>
      </div>

      {/* User Session profile / Logout Footer */}
      <div className="p-4 border-t border-theme-border bg-theme-bg/40">
        <div className="flex items-center justify-between">
          <div className="flex flex-col truncate pr-2">
            <span className="text-sm font-bold text-theme-text truncate">{user?.name}</span>
            <span className="text-[10px] text-theme-muted uppercase font-semibold tracking-wider truncate">
              {user?.role?.replace('ROLE_', '')}
            </span>
          </div>
          <button
            onClick={logout}
            title="Sign Out"
            className="p-2.5 bg-theme-bg hover:bg-rose-500/10 border border-theme-border text-theme-muted hover:text-rose-500 rounded-xl transition-all active:scale-95"
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>

    </aside>
  );
}

export default Sidebar;
