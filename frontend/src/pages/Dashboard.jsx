import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { 
  FolderKanban, 
  CheckSquare, 
  TrendingUp, 
  Activity, 
  ShieldAlert,
  Loader2,
  ListTodo
} from 'lucide-react';

function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Check if current user is Admin or PM
  const isAdminOrPM = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_PROJECT_MANAGER';

  useEffect(() => {
    if (!isAdminOrPM) {
      setLoading(false);
      return;
    }

    const fetchStats = async () => {
      try {
        const response = await api.get('/api/dashboard/stats');
        setStats(response.data);
      } catch (err) {
        console.error('Error fetching dashboard stats:', err);
        setError('Failed to load dashboard workspace metrics.');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [user, isAdminOrPM]);

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-20 text-slate-400">
        <Loader2 size={32} className="animate-spin text-indigo-500 mb-4" />
        <span className="text-sm font-medium">Gathering workspace statistics...</span>
      </div>
    );
  }

  // MEMBER VIEW (Landing / Work Dashboard)
  if (!isAdminOrPM) {
    return (
      <div className="space-y-6">
        {/* Welcome Hero Banner */}
        <div className="bg-gradient-to-r from-indigo-900/40 via-slate-900/60 to-slate-900 border border-slate-800 p-8 rounded-2xl relative overflow-hidden shadow-lg">
          <div className="relative z-10">
            <h2 className="text-2xl font-black text-white mb-2">Welcome back, {user?.name}!</h2>
            <p className="text-slate-400 text-sm max-w-lg leading-relaxed">
              Have a productive day! Check your tasks board to update statuses, join project discussion chats, or view your notifications for recent mentions.
            </p>
          </div>
          {/* Subtle light bubble */}
          <div className="absolute -top-16 -right-16 w-48 h-48 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none"></div>
        </div>

        {/* Action Widgets */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-slate-900/40 border border-slate-900 p-6 rounded-2xl flex flex-col justify-between hover:border-slate-850 transition">
            <div>
              <div className="text-indigo-400 mb-4 bg-indigo-500/10 p-2.5 rounded-lg w-fit"><ListTodo size={24} /></div>
              <h3 className="font-bold text-slate-100 text-base mb-1">My Assigned Tasks</h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                Review, filter, and modify tasks assigned to you. Move task status indicators through columns on the Kanban board.
              </p>
            </div>
            <a href="/tasks" className="mt-6 text-xs text-indigo-400 hover:text-indigo-300 font-semibold transition w-fit">
              Go to Tasks Board &rarr;
            </a>
          </div>

          <div className="bg-slate-900/40 border border-slate-900 p-6 rounded-2xl flex flex-col justify-between hover:border-slate-850 transition">
            <div>
              <div className="text-violet-400 mb-4 bg-violet-500/10 p-2.5 rounded-lg w-fit"><FolderKanban size={24} /></div>
              <h3 className="font-bold text-slate-100 text-base mb-1">Shared Projects</h3>
              <p className="text-xs text-slate-500 leading-relaxed">
                Check active projects workspace list, track audit activity logs, and chat live in project discussion rooms.
              </p>
            </div>
            <a href="/projects" className="mt-6 text-xs text-violet-400 hover:text-violet-300 font-semibold transition w-fit">
              View Projects Directory &rarr;
            </a>
          </div>
        </div>
      </div>
    );
  }

  // ADMIN & PM VIEW (Metrics Dashboard)
  return (
    <div className="space-y-6">
      
      {/* Page Title Notice */}
      {error && (
        <div className="flex items-center space-x-2 bg-rose-950/40 border border-rose-900/50 p-4 rounded-xl text-rose-400 text-xs">
          <ShieldAlert size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Main stats counters */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        
        {/* Total Projects Card */}
        <div className="bg-slate-900/40 border border-slate-900 p-6 rounded-2xl flex items-center justify-between">
          <div>
            <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Total Projects</span>
            <h3 className="text-2xl font-black text-white mt-1">{stats?.totalProjects || 0}</h3>
          </div>
          <div className="bg-indigo-600/10 text-indigo-400 p-3 rounded-xl border border-indigo-500/10">
            <FolderKanban size={20} />
          </div>
        </div>

        {/* Total Tasks Card */}
        <div className="bg-slate-900/40 border border-slate-900 p-6 rounded-2xl flex items-center justify-between">
          <div>
            <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Total Tasks</span>
            <h3 className="text-2xl font-black text-white mt-1">{stats?.totalTasks || 0}</h3>
          </div>
          <div className="bg-violet-600/10 text-violet-400 p-3 rounded-xl border border-violet-500/10">
            <CheckSquare size={20} />
          </div>
        </div>

        {/* Task Completion Rate Card */}
        <div className="bg-slate-900/40 border border-slate-900 p-6 rounded-2xl flex items-center justify-between">
          <div>
            <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">Task Completion</span>
            <h3 className="text-2xl font-black text-white mt-1">
              {stats?.taskCompletionRate ? `${stats.taskCompletionRate.toFixed(1)}%` : '0%'}
            </h3>
          </div>
          <div className="bg-emerald-600/10 text-emerald-400 p-3 rounded-xl border border-emerald-500/10">
            <TrendingUp size={20} />
          </div>
        </div>

        {/* System Activity status */}
        <div className="bg-slate-900/40 border border-slate-900 p-6 rounded-2xl flex items-center justify-between">
          <div>
            <span className="text-[10px] uppercase font-bold text-slate-500 tracking-wider">System Status</span>
            <h3 className="text-sm font-extrabold text-emerald-400 mt-2 flex items-center space-x-1.5">
              <span className="w-2 h-2 bg-emerald-500 rounded-full animate-ping"></span>
              <span>Fully Operational</span>
            </h3>
          </div>
          <div className="bg-slate-950 text-slate-500 p-3 rounded-xl border border-slate-800">
            <Activity size={20} />
          </div>
        </div>

      </div>

      {/* Grid layouts for distribution graphs */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* Task status distribution */}
        <div className="bg-slate-900/30 border border-slate-900 p-6 rounded-2xl">
          <h3 className="font-bold text-sm text-slate-200 mb-4 border-b border-slate-900 pb-3">Tasks By Status</h3>
          {stats?.taskByStatus && Object.keys(stats.taskByStatus).length > 0 ? (
            <div className="space-y-4">
              {Object.entries(stats.taskByStatus).map(([status, count]) => {
                const percentage = stats.totalTasks > 0 ? (count / stats.totalTasks) * 100 : 0;
                return (
                  <div key={status} className="space-y-1.5">
                    <div className="flex items-center justify-between text-xs font-semibold">
                      <span className="text-slate-400 capitalize">{status.toLowerCase().replace('_', ' ')}</span>
                      <span className="text-slate-200">{count} tasks ({percentage.toFixed(0)}%)</span>
                    </div>
                    <div className="h-2 bg-slate-950 rounded-full overflow-hidden">
                      <div 
                        className="h-full bg-indigo-500 rounded-full transition-all duration-500" 
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <span className="text-xs text-slate-600 block text-center py-6">No tasks logged in database yet.</span>
          )}
        </div>

        {/* Task priority distribution */}
        <div className="bg-slate-900/30 border border-slate-900 p-6 rounded-2xl">
          <h3 className="font-bold text-sm text-slate-200 mb-4 border-b border-slate-900 pb-3">Tasks By Priority</h3>
          {stats?.taskByPriority && Object.keys(stats.taskByPriority).length > 0 ? (
            <div className="space-y-4">
              {Object.entries(stats.taskByPriority).map(([priority, count]) => {
                const percentage = stats.totalTasks > 0 ? (count / stats.totalTasks) * 100 : 0;
                
                // Priority specific colors
                const getPriorityColor = (p) => {
                  switch (p) {
                    case 'CRITICAL': return 'bg-rose-500';
                    case 'HIGH': return 'bg-orange-500';
                    case 'MEDIUM': return 'bg-yellow-500';
                    default: return 'bg-slate-500';
                  }
                };

                return (
                  <div key={priority} className="space-y-1.5">
                    <div className="flex items-center justify-between text-xs font-semibold">
                      <span className="text-slate-400 capitalize">{priority.toLowerCase()}</span>
                      <span className="text-slate-200">{count} tasks ({percentage.toFixed(0)}%)</span>
                    </div>
                    <div className="h-2 bg-slate-950 rounded-full overflow-hidden">
                      <div 
                        className={`h-full rounded-full transition-all duration-500 ${getPriorityColor(priority)}`} 
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <span className="text-xs text-slate-600 block text-center py-6">No tasks logged in database yet.</span>
          )}
        </div>

      </div>

    </div>
  );
}

export default Dashboard;
