import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { Search, Filter, RefreshCw } from 'lucide-react';

function TaskFilters({ filters, onFilterChange }) {
  const [projects, setProjects] = useState([]);
  const [users, setUsers] = useState([]);

  useEffect(() => {
    // Fetch projects to populate dropdown
    const fetchProjects = async () => {
      try {
        const res = await api.get('/api/projects');
        setProjects(res.data);
      } catch (err) {
        console.error('Failed to load filter projects:', err);
      }
    };

    // Fetch workspace users (members) to populate dropdown
    const fetchUsers = async () => {
      try {
        // Query with large size to get all members from the pageable endpoint
        const res = await api.get('/api/users?size=100');
        setUsers(res.data.content || []);
      } catch (err) {
        console.error('Failed to load filter users:', err);
      }
    };

    fetchProjects();
    fetchUsers();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    onFilterChange({
      ...filters,
      [name]: value
    });
  };

  const handleReset = () => {
    onFilterChange({
      projectId: filters.projectId, // Keep current project scope selected
      keyword: '',
      priority: '',
      assigneeId: ''
    });
  };

  return (
    <div className="bg-slate-900/30 border border-slate-900 p-5 rounded-2xl space-y-4">
      <div className="flex flex-wrap items-center gap-4">
        
        {/* Keyword search bar */}
        <div className="flex-1 min-w-[200px] relative">
          <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
            <Search size={14} />
          </span>
          <input
            type="text"
            name="keyword"
            placeholder="Search by task title or description..."
            className="w-full pl-9 pr-4 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-100 placeholder-slate-700 focus:outline-none focus:border-indigo-500 transition"
            value={filters.keyword}
            onChange={handleChange}
          />
        </div>

        {/* Project Selector dropdown */}
        <div className="w-full sm:w-auto min-w-[150px]">
          <select
            name="projectId"
            className="w-full px-3 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-300 focus:outline-none focus:border-indigo-500 transition cursor-pointer"
            value={filters.projectId}
            onChange={handleChange}
          >
            <option value="">All Projects Scope</option>
            {projects.map((proj) => (
              <option key={proj.id} value={proj.id}>
                {proj.name}
              </option>
            ))}
          </select>
        </div>

        {/* Assignee Filter dropdown */}
        <div className="w-full sm:w-auto min-w-[150px]">
          <select
            name="assigneeId"
            className="w-full px-3 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-300 focus:outline-none focus:border-indigo-500 transition cursor-pointer"
            value={filters.assigneeId}
            onChange={handleChange}
          >
            <option value="">All Assignees</option>
            {users.map((member) => (
              <option key={member.id} value={member.id}>
                {member.name}
              </option>
            ))}
          </select>
        </div>

        {/* Priority Filter dropdown */}
        <div className="w-full sm:w-auto min-w-[120px]">
          <select
            name="priority"
            className="w-full px-3 py-2 bg-slate-950 border border-slate-850 rounded-xl text-xs text-slate-300 focus:outline-none focus:border-indigo-500 transition cursor-pointer"
            value={filters.priority}
            onChange={handleChange}
          >
            <option value="">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>

        {/* Reset Filters button */}
        <button
          onClick={handleReset}
          className="flex items-center justify-center space-x-1.5 px-4 py-2 border border-slate-850 hover:bg-slate-950 rounded-xl text-xs font-bold text-slate-400 hover:text-slate-200 transition active:scale-95"
          title="Reset Filters"
        >
          <RefreshCw size={12} />
          <span className="hidden sm:inline">Reset</span>
        </button>

      </div>
    </div>
  );
}

export default TaskFilters;
