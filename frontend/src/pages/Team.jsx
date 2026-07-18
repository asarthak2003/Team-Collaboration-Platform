import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { 
  Users, 
  UserCheck, 
  Shield, 
  ArrowLeft, 
  ArrowRight, 
  Loader2, 
  AlertCircle,
  Mail
} from 'lucide-react';

function Team() {
  const { user: currentUser } = useAuth();
  
  // Paginated User States
  const [members, setMembers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Admin selected roles mapping (userId -> selectedRole)
  const [pendingRoles, setPendingRoles] = useState({});
  const [updatingId, setUpdatingId] = useState(null);

  const isAdmin = currentUser?.role === 'ROLE_ADMIN';

  const fetchMembers = async () => {
    try {
      setLoading(true);
      setError('');
      // Fetch members with page size 6
      const response = await api.get(`/api/users?page=${page}&size=6`);
      setMembers(response.data.content || []);
      setTotalPages(response.data.totalPages || 1);
    } catch (err) {
      console.error('Failed to load team directory:', err);
      setError('Failed to fetch workspace team directory.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMembers();
  }, [page]);

  const handleRoleChangeSelect = (userId, roleName) => {
    setPendingRoles(prev => ({
      ...prev,
      [userId]: roleName
    }));
  };

  const handleUpdateRole = async (userId) => {
    const newRole = pendingRoles[userId];
    if (!newRole) return;

    setUpdatingId(userId);
    setError('');

    try {
      // Endpoint: PUT /api/users/{id}/role?role=ROLE_XXXX
      await api.put(`/api/users/${userId}/role?role=${newRole}`);
      
      // Update local context/roles state
      setPendingRoles(prev => {
        const copy = { ...prev };
        delete copy[userId];
        return copy;
      });
      
      fetchMembers();
    } catch (err) {
      const msg = err.response?.data?.error || 'Failed to modify member role.';
      setError(msg);
    } finally {
      setUpdatingId(null);
    }
  };

  const getInitials = (name) => {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  };

  return (
    <div className="space-y-6">
      
      {/* Title */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-100 tracking-tight flex items-center space-x-2">
            <Users size={22} className="text-indigo-400" />
            <span>Team Directory</span>
          </h2>
          <p className="text-xs text-slate-500 mt-1">Review team members and coordinate workspace access roles</p>
        </div>
      </div>

      {error && (
        <div className="flex items-center space-x-2 bg-rose-950/40 border border-rose-900/50 p-4 rounded-xl text-rose-400 text-xs">
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="flex flex-col items-center justify-center py-20 text-slate-400">
          <Loader2 size={32} className="animate-spin text-indigo-500 mb-4" />
          <span className="text-sm font-medium">Gathering team directory...</span>
        </div>
      ) : (
        <>
          {/* Members Grid Layout */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {members.map((member) => {
              const currentMemberRole = member.role;
              const selectedRole = pendingRoles[member.id] || currentMemberRole;
              const isSelf = member.id === currentUser?.id;
              const isBusy = updatingId === member.id;

              return (
                <div 
                  key={member.id}
                  className="bg-slate-900/40 border border-slate-900 p-6 rounded-2xl flex flex-col justify-between hover:border-slate-800 transition"
                >
                  <div>
                    {/* Header info */}
                    <div className="flex items-center space-x-3.5 mb-4">
                      <div className="w-10 h-10 rounded-xl bg-indigo-650/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center text-sm font-bold shadow-md">
                        {getInitials(member.name)}
                      </div>
                      <div>
                        <h3 className="font-bold text-slate-100 text-sm truncate max-w-[150px]" title={member.name}>
                          {member.name} {isSelf && <span className="text-[10px] text-indigo-400 font-bold">(You)</span>}
                        </h3>
                        <div className="flex items-center space-x-1 text-[10px] text-slate-500 mt-0.5">
                          <Mail size={10} />
                          <span className="truncate max-w-[150px]">{member.email}</span>
                        </div>
                      </div>
                    </div>

                    {/* Role Display */}
                    <div className="flex items-center space-x-2 bg-slate-950 p-3 rounded-xl border border-slate-850 my-4">
                      <Shield size={14} className="text-indigo-400 shrink-0" />
                      <div className="flex flex-col">
                        <span className="text-[9px] uppercase font-bold text-slate-500 tracking-wider">Current Role</span>
                        <span className="text-xs font-semibold text-slate-350">
                          {currentMemberRole.replace('ROLE_', '').replace('_', ' ')}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Administrative actions Panel */}
                  {isAdmin && !isSelf && (
                    <div className="border-t border-slate-900 pt-4 mt-4 space-y-3">
                      <label className="block text-[10px] font-bold text-slate-500 uppercase tracking-wider">
                        Modify Role
                      </label>
                      <div className="flex items-center space-x-2">
                        <select
                          disabled={isBusy}
                          className="flex-1 px-3 py-1.5 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-300 focus:outline-none focus:border-indigo-500 transition cursor-pointer"
                          value={selectedRole}
                          onChange={(e) => handleRoleChangeSelect(member.id, e.target.value)}
                        >
                          <option value="ROLE_MEMBER">Workspace Member</option>
                          <option value="ROLE_PROJECT_MANAGER">Project Manager</option>
                          <option value="ROLE_ADMIN">Administrator</option>
                        </select>
                        
                        {selectedRole !== currentMemberRole && (
                          <button
                            onClick={() => handleUpdateRole(member.id)}
                            disabled={isBusy}
                            className="px-3 py-1.5 bg-indigo-650 hover:bg-indigo-600 disabled:bg-indigo-800 transition rounded-xl text-xs font-bold text-white shadow-md flex items-center space-x-1"
                          >
                            {isBusy ? (
                              <Loader2 size={12} className="animate-spin" />
                            ) : (
                              <UserCheck size={12} />
                            )}
                            <span className="hidden sm:inline">Save</span>
                          </button>
                        )}
                      </div>
                    </div>
                  )}

                </div>
              );
            })}
          </div>

          {/* Pagination Navigation panel */}
          {totalPages > 1 && (
            <div className="flex items-center justify-center space-x-4 pt-8">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="p-2 border border-slate-850 hover:bg-slate-950/60 rounded-xl text-slate-400 hover:text-slate-200 transition disabled:opacity-40"
              >
                <ArrowLeft size={16} />
              </button>
              <span className="text-xs text-slate-500 font-medium">
                Page <span className="text-slate-300 font-bold">{page + 1}</span> of <span className="text-slate-300 font-bold">{totalPages}</span>
              </span>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                className="p-2 border border-slate-850 hover:bg-slate-950/60 rounded-xl text-slate-400 hover:text-slate-200 transition disabled:opacity-40"
              >
                <ArrowRight size={16} />
              </button>
            </div>
          )}
        </>
      )}

    </div>
  );
}

export default Team;
