import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { User, Lock, Shield, Mail, Loader2, CheckCircle2, AlertCircle } from 'lucide-react';

function Profile() {
  const { user, updateUser } = useAuth();
  
  // States
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  // Prepopulate current name
  useEffect(() => {
    if (user) {
      setName(user.name);
    }
  }, [user]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccess('');
    setError('');

    // Check if name is blank
    if (!name.trim()) {
      setError('Profile name cannot be blank.');
      return;
    }

    // Check if password change is attempted
    if (password) {
      if (password.length < 6) {
        setError('Password must be at least 6 characters long.');
        return;
      }
      if (password !== confirmPassword) {
        setError('Passwords do not match.');
        return;
      }
    }

    setLoading(true);

    try {
      const payload = {
        name,
        // Pass password only if user typed it, otherwise pass null so backend @Size doesn't trigger
        password: password.trim() ? password : null
      };

      const response = await api.put('/api/users/profile', payload);
      
      // Update global context state
      updateUser({
        ...user,
        name: response.data.name
      });

      // Clear password fields
      setPassword('');
      setConfirmPassword('');
      setSuccess('Profile updated successfully!');
    } catch (err) {
      console.error('Failed to update profile settings:', err);
      const msg = err.response?.data?.error || 'Profile update failed.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      
      {/* Title */}
      <div>
        <h2 className="text-xl font-bold text-slate-100 tracking-tight flex items-center space-x-2">
          <User size={22} className="text-indigo-400" />
          <span>Profile Settings</span>
        </h2>
        <p className="text-xs text-slate-500 mt-1">Configure your personal workspace preferences</p>
      </div>

      {/* Success Banner */}
      {success && (
        <div className="flex items-center space-x-2 bg-emerald-950/40 border border-emerald-900/50 p-4 rounded-xl text-emerald-400 text-xs">
          <CheckCircle2 size={16} />
          <span>{success}</span>
        </div>
      )}

      {/* Error Banner */}
      {error && (
        <div className="flex items-center space-x-2 bg-rose-950/40 border border-rose-900/50 p-4 rounded-xl text-rose-400 text-xs">
          <AlertCircle size={16} />
          <span>{error}</span>
        </div>
      )}

      {/* Main card grid split */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Left Side: Account Info Card */}
        <div className="md:col-span-1 bg-slate-900/40 border border-slate-900 p-6 rounded-2xl space-y-4">
          <div className="flex flex-col items-center text-center pb-4 border-b border-slate-900">
            <div className="w-16 h-16 rounded-2xl bg-indigo-650/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center text-xl font-black mb-3 shadow-md shadow-indigo-600/5">
              {user?.name ? user.name.split(' ').map(n=>n[0]).slice(0,2).join('').toUpperCase() : '?'}
            </div>
            <h3 className="font-bold text-slate-100 text-sm">{user?.name}</h3>
            <span className="text-[10px] text-slate-500 uppercase font-bold tracking-wider mt-1">{user?.role?.replace('ROLE_', '')}</span>
          </div>

          <div className="space-y-3 pt-2 text-xs">
            <div className="flex flex-col">
              <span className="text-[9px] uppercase font-bold text-slate-500 tracking-wider">Email Address</span>
              <span className="text-slate-350 mt-0.5 break-all">{user?.email}</span>
            </div>
            <div className="flex flex-col">
              <span className="text-[9px] uppercase font-bold text-slate-500 tracking-wider">Workspace Role</span>
              <span className="text-slate-350 mt-0.5">
                {user?.role?.replace('ROLE_', '').replace('_', ' ')}
              </span>
            </div>
          </div>
        </div>

        {/* Right Side: Account Modification Forms */}
        <form onSubmit={handleSubmit} className="md:col-span-2 bg-slate-900/40 border border-slate-900 p-6 rounded-2xl space-y-5">
          
          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
              Full Name
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                <User size={16} />
              </span>
              <input
                type="text"
                required
                disabled={loading}
                className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 placeholder-slate-700 focus:outline-none focus:border-indigo-500 transition"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
              Change Password
            </label>
            <div className="relative">
              <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                <Lock size={16} />
              </span>
              <input
                type="password"
                disabled={loading}
                className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 placeholder-slate-700 focus:outline-none focus:border-indigo-500 transition"
                placeholder="Leave blank to keep current password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          {password && (
            <div className="animate-in fade-in slide-in-from-top-2 duration-150">
              <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">
                Confirm New Password
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center text-slate-500">
                  <Lock size={16} />
                </span>
                <input
                  type="password"
                  required
                  disabled={loading}
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-850 rounded-xl text-sm text-slate-100 placeholder-slate-700 focus:outline-none focus:border-indigo-500 transition"
                  placeholder="Re-enter new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </div>
            </div>
          )}

          <div className="flex items-center justify-end pt-4 border-t border-slate-900/60 mt-6">
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 bg-indigo-650 hover:bg-indigo-600 disabled:bg-indigo-850 rounded-xl text-xs font-bold transition text-white flex items-center space-x-1.5 shadow-md"
            >
              {loading ? (
                <>
                  <Loader2 size={12} className="animate-spin" />
                  <span>Saving...</span>
                </>
              ) : (
                <span>Save Changes</span>
              )}
            </button>
          </div>

        </form>

      </div>

    </div>
  );
}

export default Profile;
