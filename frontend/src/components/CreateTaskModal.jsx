import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { X, Loader2, AlertCircle } from 'lucide-react';

function CreateTaskModal({ isOpen, onClose, onSave, defaultProjectId = '' }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [projectId, setProjectId] = useState('');
  const [priority, setPriority] = useState('MEDIUM');
  const [status, setStatus] = useState('TODO');
  const [assignedUserId, setAssignedUserId] = useState('');
  const [dueDate, setDueDate] = useState('');
  
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setTitle('');
      setDescription('');
      setProjectId(defaultProjectId);
      setPriority('MEDIUM');
      setStatus('TODO');
      setAssignedUserId('');
      setDueDate('');
      setError('');
    }
  }, [isOpen, defaultProjectId]);

  useEffect(() => {
    if (!isOpen) return;

    // Load projects and users list
    const loadFormData = async () => {
      try {
        const [projRes, userRes] = await Promise.all([
          api.get('/api/projects'),
          api.get('/api/users?size=100')
        ]);
        setProjects(projRes.data);
        setUsers(userRes.data.content || []);
      } catch (err) {
        console.error('Failed to load form details:', err);
        setError('Failed to load project or team details.');
      }
    };

    loadFormData();
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Task title is required.');
      return;
    }
    if (!projectId) {
      setError('Please select a project.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    const taskData = {
      title,
      description,
      projectId: parseInt(projectId),
      priority,
      status,
      assignedUserId: assignedUserId ? parseInt(assignedUserId) : null,
      dueDate: dueDate || null
    };

    const result = await onSave(taskData);
    setIsSubmitting(false);

    if (result.success) {
      onClose();
    } else {
      setError(result.error);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
      <div className="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150">
        
        {/* Modal Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-900/50">
          <h3 className="font-bold text-slate-100">Create New Task</h3>
          <button 
            onClick={onClose}
            className="p-1 text-slate-400 hover:text-slate-100 hover:bg-slate-800 rounded-lg transition"
          >
            <X size={18} />
          </button>
        </div>

        {/* Modal Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 max-h-[80vh] overflow-y-auto scrollbar-thin scrollbar-thumb-slate-950">
          
          {error && (
            <div className="flex items-center space-x-2 bg-rose-950/40 border border-rose-900/50 p-4 rounded-xl text-rose-400 text-xs">
              <AlertCircle size={16} className="shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
              Task Title
            </label>
            <input
              type="text"
              required
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 placeholder-slate-700 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition"
              placeholder="e.g. Design UI Mockups"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
              Description
            </label>
            <textarea
              rows="3"
              disabled={isSubmitting}
              className="w-full px-4 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 placeholder-slate-700 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition resize-none"
              placeholder="Add key objectives or requirements..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Project Scope
              </label>
              <select
                required
                disabled={isSubmitting}
                className="w-full px-3 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition cursor-pointer"
                value={projectId}
                onChange={(e) => setProjectId(e.target.value)}
              >
                <option value="">Select Project</option>
                {projects.map((proj) => (
                  <option key={proj.id} value={proj.id}>
                    {proj.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Task Assignee
              </label>
              <select
                disabled={isSubmitting}
                className="w-full px-3 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition cursor-pointer"
                value={assignedUserId}
                onChange={(e) => setAssignedUserId(e.target.value)}
              >
                <option value="">Unassigned</option>
                {users.map((member) => (
                  <option key={member.id} value={member.id}>
                    {member.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Priority
              </label>
              <select
                disabled={isSubmitting}
                className="w-full px-3 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition cursor-pointer"
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Initial Status
              </label>
              <select
                disabled={isSubmitting}
                className="w-full px-3 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition cursor-pointer"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="TODO">To Do</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="REVIEW">In Review</option>
                <option value="TESTING">Testing</option>
                <option value="DONE">Completed</option>
                <option value="BLOCKED">Blocked</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Due Date
              </label>
              <input
                type="date"
                disabled={isSubmitting}
                className="w-full px-3 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition cursor-pointer"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
              />
            </div>
          </div>

          {/* Form Footer */}
          <div className="flex items-center justify-end space-x-3 pt-4 border-t border-slate-800/60 mt-6">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="px-4 py-2 bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-xl text-xs font-bold transition text-slate-400 hover:text-slate-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:bg-indigo-800 rounded-xl text-xs font-bold transition text-white flex items-center space-x-1.5 shadow-md shadow-indigo-600/10"
            >
              {isSubmitting ? (
                <>
                  <Loader2 size={12} className="animate-spin" />
                  <span>Creating...</span>
                </>
              ) : (
                <span>Create Task</span>
              )}
            </button>
          </div>

        </form>

      </div>
    </div>
  );
}

export default CreateTaskModal;
