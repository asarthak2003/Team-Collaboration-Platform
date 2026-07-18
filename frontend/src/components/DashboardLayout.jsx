import React from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

function DashboardLayout() {
  return (
    <div className="flex h-screen bg-slate-950 overflow-hidden text-slate-100">
      
      {/* 1. Left Side Navigation Panel */}
      <Sidebar />

      {/* 2. Right Side Content Segment */}
      <div className="flex-1 flex flex-col overflow-hidden">
        
        {/* Top Header Navbar */}
        <Header />

        {/* Dynamic Nested Sub-Page Content Container */}
        <main className="flex-1 overflow-y-auto p-6 bg-slate-950/40">
          <div className="max-w-6xl mx-auto">
            {/* The Outlet is where subpages (Dashboard, Projects, Tasks) are dynamically loaded */}
            <Outlet />
          </div>
        </main>

      </div>

    </div>
  );
}

export default DashboardLayout;
