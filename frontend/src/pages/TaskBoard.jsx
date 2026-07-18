import React, { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import TaskFilters from '../components/TaskFilters';
import TaskCard from '../components/TaskCard';
import CreateTaskModal from '../components/CreateTaskModal';
import TaskDetailsModal from '../components/TaskDetailsModal';
import { Loader2, Kanban, AlertCircle, Plus } from 'lucide-react';

const COLUMNS = [
  { id: 'TODO', title: 'To Do', color: 'border-slate-800 bg-slate-900/10' },
  { id: 'IN_PROGRESS', title: 'In Progress', color: 'border-indigo-900/50 bg-indigo-950/5' },
  { id: 'REVIEW', title: 'In Review', color: 'border-purple-900/50 bg-purple-950/5' },
  { id: 'TESTING', title: 'Testing', color: 'border-amber-900/50 bg-amber-950/5' },
  { id: 'DONE', title: 'Completed', color: 'border-emerald-900/50 bg-emerald-950/5' },
  { id: 'BLOCKED', title: 'Blocked', color: 'border-rose-900/50 bg-rose-950/5' }
];

function TaskBoard() {
  const { user } = useAuth();
  const location = useLocation();
  
  // Extract projectId from URL query parameter (e.g. ?projectId=1)
  const getUrlProjectId = () => {
    const params = new URLSearchParams(location.search);
    return params.get('projectId') || '';
  };

  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Modal States
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [selectedTaskId, setSelectedTaskId] = useState(null);

  // Authorization checks
  const isAdminOrPM = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_PROJECT_MANAGER';

  // Shared Filter State
  const [filters, setFilters] = useState({
    projectId: getUrlProjectId(),
    keyword: '',
    priority: '',
    assigneeId: ''
  });

  const fetchTasks = async () => {
    try {
      setLoading(true);
      
      const queryParams = new URLSearchParams();
      if (filters.projectId) queryParams.append('projectId', filters.projectId);
      if (filters.keyword) queryParams.append('keyword', filters.keyword);
      if (filters.priority) queryParams.append('priority', filters.priority);
      if (filters.assigneeId) queryParams.append('assigneeId', filters.assigneeId);
      
      queryParams.append('size', '100'); // Load up to 100 tasks on board
      
      const response = await api.get(`/api/tasks?${queryParams.toString()}`);
      setTasks(response.data.content || []);
    } catch (err) {
      console.error('Failed to load board tasks:', err);
      setError('Failed to fetch tasks for the board.');
    } finally {
      setLoading(false);
    }
  };

  // Sync filters from URL when redirecting from Project cards
  useEffect(() => {
    const urlProjId = getUrlProjectId();
    if (urlProjId) {
      setFilters(prev => ({
        ...prev,
        projectId: urlProjId
      }));
    }
  }, [location.search]);

  useEffect(() => {
    fetchTasks();
  }, [filters]);

  const handleCardClick = (task) => {
    setSelectedTaskId(task.id);
    setIsDetailsOpen(true);
  };

  // Save Task (Create new)
  const handleCreateTask = async (taskData) => {
    try {
      await api.post('/api/tasks', taskData);
      fetchTasks();
      return { success: true };
    } catch (err) {
      const msg = err.response?.data?.error || 'Failed to create task.';
      return { success: false, error: msg };
    }
  };

  return (
    <div className="space-y-6">
      
      {/* Board Header Title & Actions */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-100 tracking-tight flex items-center space-x-2">
            <Kanban size={22} className="text-indigo-400" />
            <span>Tasks Kanban Board</span>
          </h2>
          <p className="text-xs text-slate-500 mt-1">Coordinate and manage project task cards</p>
        </div>

        {isAdminOrPM && (
          <button
            onClick={() => setIsCreateOpen(true)}
            className="flex items-center space-x-1.5 px-4 py-2 bg-indigo-600 hover:bg-indigo-500 transition rounded-xl text-xs font-bold text-white shadow-md shadow-indigo-600/10"
          >
            <Plus size={14} />
            <span>Create Task</span>
          </button>
        )}
      </div>

      {/* Filter Options Panel */}
      <TaskFilters filters={filters} onFilterChange={setFilters} />

      {error && (
        <div className="flex items-center space-x-2 bg-rose-950/40 border border-rose-900/50 p-4 rounded-xl text-rose-400 text-xs">
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Kanban Grid Columns */}
      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 text-slate-400">
          <Loader2 size={32} className="animate-spin text-indigo-500 mb-4" />
          <span className="text-sm font-medium">Updating tasks board...</span>
        </div>
      ) : (
        <div className="flex space-x-6 overflow-x-auto pb-4 scrollbar-thin scrollbar-thumb-slate-800">
          {COLUMNS.map((col) => {
            const columnTasks = tasks.filter(t => t.status === col.id);
            return (
              <div 
                key={col.id} 
                className={`w-80 shrink-0 border rounded-2xl p-4 flex flex-col h-[calc(100vh-255px)] min-h-[450px] ${col.color}`}
              >
                {/* Column Title header */}
                <div className="flex items-center justify-between mb-4 border-b border-slate-900/60 pb-2">
                  <div className="flex items-center space-x-2">
                    <span className="font-bold text-xs text-slate-200">{col.title}</span>
                    <span className="text-[10px] font-bold bg-slate-950/80 px-2 py-0.5 rounded-full text-slate-400">
                      {columnTasks.length}
                    </span>
                  </div>
                </div>

                {/* Task Cards Container list */}
                <div className="flex-1 overflow-y-auto space-y-4 pr-1 scrollbar-thin scrollbar-thumb-slate-950">
                  {columnTasks.length > 0 ? (
                    columnTasks.map((task) => (
                      <TaskCard 
                        key={task.id} 
                        task={task} 
                        onClick={handleCardClick} 
                      />
                    ))
                  ) : (
                    <div className="h-full flex items-center justify-center text-center py-10 opacity-40">
                      <span className="text-[10px] font-medium text-slate-500 uppercase tracking-wider">Empty Column</span>
                    </div>
                  )}
                </div>

              </div>
            );
          })}
        </div>
      )}

      {/* Task Creation Modal */}
      <CreateTaskModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSave={handleCreateTask}
        defaultProjectId={filters.projectId}
      />

      {/* Task Details & Comments Modal */}
      <TaskDetailsModal
        isOpen={isDetailsOpen}
        onClose={() => setIsDetailsOpen(false)}
        taskId={selectedTaskId}
        onTaskUpdated={fetchTasks}
        onTaskDeleted={fetchTasks}
      />

    </div>
  );
}

export default TaskBoard;
