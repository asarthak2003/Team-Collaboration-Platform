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
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between h-screen sticky top-0">
      
      {/* Brand Logo Header */}
      <div>
        <div className="p-6 border-b border-slate-800 flex items-center space-x-3">
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
                    : 'text-slate-400 hover:bg-slate-800/60 hover:text-slate-200'
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
      <div className="p-4 border-t border-slate-800 bg-slate-900/40">
        <div className="flex items-center justify-between">
          <div className="flex flex-col truncate pr-2">
            <span className="text-sm font-bold text-slate-200 truncate">{user?.name}</span>
            <span className="text-[10px] text-slate-500 uppercase font-semibold tracking-wider truncate">
              {user?.role?.replace('ROLE_', '')}
            </span>
          </div>
          <button
            onClick={logout}
            title="Sign Out"
            className="p-2.5 bg-slate-950 hover:bg-rose-950 text-slate-400 hover:text-rose-455 rounded-xl transition-all active:scale-95"
          >
            <LogOut size={16} />
          </button>
        </div>
      </div>

    </aside>
  );
}

export default Sidebar;
