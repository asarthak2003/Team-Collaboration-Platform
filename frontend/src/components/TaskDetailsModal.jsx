import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { X, Loader2, MessageSquare, Trash2, Send, AlertCircle, Calendar, User, ShieldAlert } from 'lucide-react';

function TaskDetailsModal({ isOpen, onClose, taskId, onTaskUpdated, onTaskDeleted }) {
  const { user } = useAuth();
  
  // States
  const [task, setTask] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState('');
  
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [commentSubmitting, setCommentSubmitting] = useState(false);
  const [error, setError] = useState('');

  // Form Fields
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [assignedUserId, setAssignedUserId] = useState('');
  const [dueDate, setDueDate] = useState('');

  const isAdminOrPM = user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_PROJECT_MANAGER';

  // Load Task & Comments details
  const loadData = async () => {
    try {
      setLoading(true);
      setError('');
      const [taskRes, userRes] = await Promise.all([
        api.get(`/api/tasks/${taskId}`),
        api.get('/api/users?size=100')
      ]);
      
      const t = taskRes.data;
      setTask(t);
      setUsers(userRes.data.content || []);

      // Set form parameters
      setTitle(t.title);
      setDescription(t.description || '');
      setStatus(t.status);
      setPriority(t.priority);
      setAssignedUserId(t.assignedUserId ? String(t.assignedUserId) : '');
      setDueDate(t.dueDate ? t.dueDate.split('T')[0] : '');

      // Load comments
      fetchComments();
    } catch (err) {
      console.error('Failed to load task details:', err);
      setError('Failed to retrieve task particulars.');
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async () => {
    try {
      setCommentsLoading(true);
      const res = await api.get(`/api/tasks/${taskId}/comments`);
      setComments(res.data);
    } catch (err) {
      console.error('Failed to load comments:', err);
    } finally {
      setCommentsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen && taskId) {
      loadData();
    }
  }, [isOpen, taskId]);

  if (!isOpen) return null;

  // Authorization Checks
  const isAssignedToCurrentUser = task && task.assignedUserId === user?.id;
  const canModifyAll = isAdminOrPM;
  const canModifyStatus = isAdminOrPM || isAssignedToCurrentUser;

  const handleUpdateTask = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Task title is required.');
      return;
    }

    setSubmitting(true);
    setError('');

    // Construct payload based on role access
    const payload = canModifyAll 
      ? {
          title,
          description,
          status,
          priority,
          assignedUserId: assignedUserId ? parseInt(assignedUserId) : null,
          dueDate: dueDate || null
        }
      : {
          // Member can only modify status
          title: task.title,
          description: task.description,
          status,
          priority: task.priority,
          assignedUserId: task.assignedUserId,
          dueDate: task.dueDate
        };

    try {
      await api.put(`/api/tasks/${taskId}`, payload);
      onTaskUpdated();
      onClose();
    } catch (err) {
      const msg = err.response?.data?.error || 'Failed to update task settings.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteTask = async () => {
    if (!window.confirm('Are you sure you want to delete this task?')) return;
    setSubmitting(true);
    try {
      await api.delete(`/api/tasks/${taskId}`);
      onTaskDeleted();
      onClose();
    } catch (err) {
      setError('Failed to delete task.');
      setSubmitting(false);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    setCommentSubmitting(true);
    try {
      await api.post(`/api/tasks/${taskId}/comments`, { content: newComment });
      setNewComment('');
      fetchComments();
    } catch (err) {
      console.error('Failed to add comment:', err);
      setError('Failed to upload comment.');
    } finally {
      setCommentSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm('Delete this comment?')) return;
    try {
      await api.delete(`/api/comments/${commentId}`);
      fetchComments();
    } catch (err) {
      console.error('Failed to delete comment:', err);
    }
  };

  const getInitials = (name) => {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).slice(0,2).join('').toUpperCase();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm">
      <div className="w-full max-w-4xl bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-150 flex flex-col md:flex-row h-[85vh] max-h-[700px]">
        
        {/* Left Side: Task Configuration Form */}
        <div className="flex-1 p-6 border-b md:border-b-0 md:border-r border-slate-805 flex flex-col overflow-y-auto scrollbar-thin scrollbar-thumb-slate-950">
          
          <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-6">
            <h3 className="font-bold text-slate-100">Task Details</h3>
            <button 
              onClick={onClose}
              className="p-1 md:hidden text-slate-400 hover:text-slate-100 hover:bg-slate-850 rounded-lg transition"
            >
              <X size={18} />
            </button>
          </div>

          {loading ? (
            <div className="flex-1 flex flex-col items-center justify-center py-20 text-slate-500">
              <Loader2 className="animate-spin text-indigo-500 mb-2" size={24} />
              <span className="text-xs">Fetching task particulars...</span>
            </div>
          ) : (
            <form onSubmit={handleUpdateTask} className="space-y-4 flex-1 flex flex-col justify-between">
              
              <div className="space-y-4">
                {error && (
                  <div className="flex items-center space-x-2 bg-rose-950/40 border border-rose-900/50 p-4 rounded-xl text-rose-400 text-xs">
                    <AlertCircle size={16} className="shrink-0" />
                    <span>{error}</span>
                  </div>
                )}

                {/* Title (Disabled for member) */}
                <div>
                  <label className="block text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Task Title
                  </label>
                  <input
                    type="text"
                    required
                    disabled={!canModifyAll || submitting}
                    className="w-full px-4 py-2 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 focus:outline-none focus:border-indigo-500 disabled:opacity-60 transition"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                  />
                </div>

                {/* Description (Disabled for member) */}
                <div>
                  <label className="block text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-2">
                    Description
                  </label>
                  <textarea
                    rows="3"
                    disabled={!canModifyAll || submitting}
                    className="w-full px-4 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-100 focus:outline-none focus:border-indigo-500 disabled:opacity-60 transition resize-none"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                  />
                </div>

                {/* Fields Grid */}
                <div className="grid grid-cols-2 gap-4">
                  
                  {/* Status Selection (Restrict permissions checks) */}
                  <div>
                    <label className="block text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Status
                    </label>
                    <select
                      disabled={!canModifyStatus || submitting}
                      className="w-full px-3 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-100 focus:outline-none focus:border-indigo-500 disabled:opacity-60 cursor-pointer"
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
                    {!canModifyStatus && (
                      <span className="text-[9px] text-rose-400 mt-1 flex items-center space-x-1">
                        <ShieldAlert size={10} />
                        <span>Assigned users only</span>
                      </span>
                    )}
                  </div>

                  {/* Priority Selector (PM/Admin only) */}
                  <div>
                    <label className="block text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Priority
                    </label>
                    <select
                      disabled={!canModifyAll || submitting}
                      className="w-full px-3 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-100 focus:outline-none focus:border-indigo-500 disabled:opacity-60 cursor-pointer"
                      value={priority}
                      onChange={(e) => setPriority(e.target.value)}
                    >
                      <option value="LOW">Low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HIGH">High</option>
                      <option value="CRITICAL">Critical</option>
                    </select>
                  </div>

                  {/* Assignee Selector (PM/Admin only) */}
                  <div>
                    <label className="block text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Assignee
                    </label>
                    <select
                      disabled={!canModifyAll || submitting}
                      className="w-full px-3 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-100 focus:outline-none focus:border-indigo-500 disabled:opacity-60 cursor-pointer"
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

                  {/* Due Date (PM/Admin only) */}
                  <div>
                    <label className="block text-[10px] font-semibold text-slate-400 uppercase tracking-wider mb-2">
                      Due Date
                    </label>
                    <input
                      type="date"
                      disabled={!canModifyAll || submitting}
                      className="w-full px-3 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-100 focus:outline-none focus:border-indigo-500 disabled:opacity-60 cursor-pointer"
                      value={dueDate}
                      onChange={(e) => setDueDate(e.target.value)}
                    />
                  </div>

                </div>
              </div>

              {/* Form Actions Footer Panel */}
              <div className="flex items-center justify-between pt-6 border-t border-slate-800/60 mt-8">
                {canModifyAll ? (
                  <button
                    type="button"
                    onClick={handleDeleteTask}
                    disabled={submitting}
                    className="p-2 bg-slate-950 hover:bg-rose-950 border border-slate-800 rounded-xl text-slate-500 hover:text-rose-500 transition-all active:scale-95"
                    title="Delete Task"
                  >
                    <Trash2 size={16} />
                  </button>
                ) : (
                  <div></div>
                )}
                
                <div className="flex items-center space-x-3">
                  <button
                    type="button"
                    onClick={onClose}
                    disabled={submitting}
                    className="px-4 py-2 bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-xl text-xs font-bold transition text-slate-400 hover:text-slate-200"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="px-5 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:bg-indigo-800 rounded-xl text-xs font-bold transition text-white flex items-center space-x-1.5 shadow-md"
                  >
                    {submitting ? (
                      <>
                        <Loader2 size={12} className="animate-spin" />
                        <span>Updating...</span>
                      </>
                    ) : (
                      <span>Save Changes</span>
                    )}
                  </button>
                </div>
              </div>

            </form>
          )}

        </div>

        {/* Right Side: Chronological Comments Thread */}
        <div className="w-full md:w-[360px] bg-slate-950/20 p-6 flex flex-col h-full overflow-hidden">
          
          <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-4 shrink-0">
            <div className="flex items-center space-x-2 text-slate-300 font-bold text-sm">
              <MessageSquare size={16} className="text-indigo-400" />
              <span>Comments ({comments.length})</span>
            </div>
            <button 
              onClick={onClose}
              className="hidden md:block p-1 text-slate-400 hover:text-slate-100 hover:bg-slate-800 rounded-lg transition"
            >
              <X size={18} />
            </button>
          </div>

          {/* Comments List */}
          <div className="flex-1 overflow-y-auto space-y-4 pr-1 scrollbar-thin scrollbar-thumb-slate-950 my-2">
            {commentsLoading && comments.length === 0 ? (
              <div className="h-full flex items-center justify-center py-20 text-slate-600">
                <Loader2 size={18} className="animate-spin text-slate-500" />
              </div>
            ) : comments.length > 0 ? (
              comments.map((comment) => {
                const isAuthor = comment.createdBy === user?.id;
                const canDeleteComment = isAuthor || isAdminOrPM;
                return (
                  <div key={comment.id} className="bg-slate-900/40 border border-slate-900 p-3 rounded-xl space-y-2 relative group">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-2">
                        <div className="w-5 h-5 rounded-md bg-indigo-600/10 border border-indigo-500/20 text-indigo-455 flex items-center justify-center text-[8px] font-bold">
                          {getInitials(comment.createdByName)}
                        </div>
                        <span className="text-[10px] font-bold text-slate-350">{comment.createdByName}</span>
                      </div>
                      <span className="text-[8px] text-slate-500">{new Date(comment.createdAt).toLocaleDateString()}</span>
                    </div>
                    <p className="text-xs text-slate-400 leading-relaxed pr-6">{comment.content}</p>
                    
                    {canDeleteComment && (
                      <button
                        onClick={() => handleDeleteComment(comment.id)}
                        className="absolute right-2 bottom-2 text-slate-600 hover:text-rose-500 opacity-0 group-hover:opacity-100 transition-opacity p-1"
                        title="Delete Comment"
                      >
                        <Trash2 size={10} />
                      </button>
                    )}
                  </div>
                );
              })
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-center opacity-30 py-20">
                <MessageSquare size={24} className="text-slate-500 mb-2" />
                <span className="text-[10px] font-medium text-slate-500 uppercase tracking-wider">No comments yet</span>
              </div>
            )}
          </div>

          {/* New Comment Input Box */}
          <form onSubmit={handleAddComment} className="mt-4 shrink-0 relative">
            <input
              type="text"
              disabled={commentSubmitting}
              placeholder="Write a comment..."
              className="w-full pl-4 pr-10 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-100 placeholder-slate-700 focus:outline-none focus:border-indigo-500 transition"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
            />
            <button
              type="submit"
              disabled={commentSubmitting || !newComment.trim()}
              className="absolute right-2 top-2 p-1 hover:bg-slate-850 text-indigo-400 hover:text-indigo-300 disabled:opacity-40 transition rounded-lg"
            >
              {commentSubmitting ? <Loader2 size={12} className="animate-spin" /> : <Send size={12} />}
            </button>
          </form>

        </div>

      </div>
    </div>
  );
}

export default TaskDetailsModal;
