import React from 'react';
import { Calendar, User, AlertCircle } from 'lucide-react';

function TaskCard({ task, onClick }) {
  
  // Priority Tag colors
  const getPriorityStyles = (priority) => {
    switch (priority) {
      case 'CRITICAL':
        return 'bg-rose-500/10 text-rose-400 border border-rose-500/20';
      case 'HIGH':
        return 'bg-orange-500/10 text-orange-400 border border-orange-500/20';
      case 'MEDIUM':
        return 'bg-yellow-500/10 text-yellow-400 border border-yellow-500/20';
      default:
        return 'bg-slate-800 text-slate-400 border border-slate-700';
    }
  };

  // Due Date status checks (check if approaching)
  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE';

  const getInitials = (name) => {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).slice(0,2).join('').toUpperCase();
  };

  return (
    <div 
      onClick={() => onClick(task)}
      className="bg-slate-900/60 border border-slate-900 hover:border-slate-800 p-4 rounded-xl shadow-md cursor-pointer transition-all duration-200 hover:-translate-y-0.5"
    >
      <div className="flex flex-col space-y-3">
        
        {/* Priority Badge Indicator */}
        <div className="flex items-center justify-between">
          <span className={`text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${getPriorityStyles(task.priority)}`}>
            {task.priority.toLowerCase()}
          </span>
          {isOverdue && (
            <span className="text-[9px] font-bold text-rose-400 flex items-center space-x-1 uppercase tracking-wider">
              <AlertCircle size={10} />
              <span>Overdue</span>
            </span>
          )}
        </div>

        {/* Task Title & Description Preview */}
        <div>
          <h4 className="text-sm font-bold text-slate-100 line-clamp-1">{task.title}</h4>
          {task.description && (
            <p className="text-xs text-slate-500 line-clamp-2 mt-1 leading-relaxed">
              {task.description}
            </p>
          )}
        </div>

        {/* Card Footer Metadata (Due date and Uploader details) */}
        <div className="flex items-center justify-between pt-3 border-t border-slate-900 text-[10px] text-slate-500">
          <div className="flex items-center space-x-1.5">
            <Calendar size={12} className={isOverdue ? 'text-rose-455' : 'text-slate-500'} />
            <span className={isOverdue ? 'text-rose-400 font-semibold' : ''}>
              {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : 'No due date'}
            </span>
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-slate-400 font-medium truncate max-w-[80px]" title={task.assignedUserName || 'Unassigned'}>
              {task.assignedUserName || 'Unassigned'}
            </span>
            <div className="w-5.5 h-5.5 rounded-md bg-indigo-600/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center text-[9px] font-bold">
              {getInitials(task.assignedUserName)}
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}

export default TaskCard;
