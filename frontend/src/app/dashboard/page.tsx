'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery } from '@tanstack/react-query';
import { 
    Activity, 
    AlertCircle, 
    Navigation, 
    FlameKindling,
    Percent, 
    TrendingUp,
    ShieldAlert
} from 'lucide-react';
import { 
    ResponsiveContainer, 
    AreaChart, 
    Area, 
    XAxis, 
    YAxis, 
    Tooltip, 
    BarChart, 
    Bar, 
    Cell 
} from 'recharts';

export default function DashboardPage() {
    const router = useRouter();
    const [token, setToken] = useState<string | null>(null);

    useEffect(() => {
        const storedToken = localStorage.getItem('resqflow_token');
        if (!storedToken) {
            router.replace('/login');
        } else {
            setToken(storedToken);
        }
    }, [router]);

    const { data: metrics, isLoading, isError } = useQuery({
        queryKey: ['dashboardMetrics'],
        queryFn: async () => {
            const apiBase = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
            const res = await fetch(`${apiBase}/analytics/dashboard`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            if (!res.ok) throw new Error('Failed to load metrics');
            return res.json();
        },
        enabled: !!token,
        refetchInterval: 5000 // Poll every 5 seconds for live dashboard updates
    });

    if (!token || isLoading) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse">Loading Operations Dashboard...</div>
            </div>
        );
    }

    if (isError) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center p-4">
                <div className="bg-rose-950/40 border border-rose-900/50 rounded-xl p-4 flex gap-2 items-center text-rose-300">
                    <ShieldAlert className="h-5 w-5" />
                    <span>Failed to establish connection to operations server.</span>
                </div>
            </div>
        );
    }

    // Chart mock data representing historical trends
    const timelineData = [
        { name: '08:00', responseTime: 22 },
        { name: '09:00', responseTime: 18 },
        { name: '10:00', responseTime: 19 },
        { name: '11:00', responseTime: 15 },
        { name: '12:00', responseTime: metrics?.avgResponseTimeMinutes || 18.3 }
    ];

    const categoryData = [
        { name: 'Food', count: metrics?.resourceAllocationByCategory?.FOOD || 0, color: '#f43f5e' },
        { name: 'Water', count: metrics?.resourceAllocationByCategory?.WATER || 0, color: '#0ea5e9' },
        { name: 'Medical', count: metrics?.resourceAllocationByCategory?.MEDICAL || 0, color: '#10b981' },
        { name: 'Shelter', count: metrics?.resourceAllocationByCategory?.SHELTER || 0, color: '#f59e0b' }
    ];

    return (
        <div className="flex bg-slate-950 min-h-screen">
            <Sidebar />
            
            <main className="flex-1 p-8 space-y-6 overflow-y-auto max-h-screen">
                
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight text-white">Operations Dashboard</h2>
                        <p className="text-sm text-slate-400">Live feed and analytics of relief logistics</p>
                    </div>
                    <div className="flex items-center gap-2 px-3 py-1.5 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-lg text-xs font-semibold">
                        <span className="h-2 w-2 rounded-full bg-emerald-500 animate-ping"></span>
                        LIVE CONNECTED
                    </div>
                </div>

                {/* Metrics Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
                    
                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
                        <div className="space-y-1">
                            <p className="text-xs font-semibold text-slate-500 tracking-wider">TOTAL REQUESTS</p>
                            <h3 className="text-2xl font-bold text-slate-100">{metrics?.totalRequests}</h3>
                        </div>
                        <div className="bg-slate-800 p-3 rounded-lg text-rose-500">
                            <Activity className="h-6 w-6" />
                        </div>
                    </div>

                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
                        <div className="space-y-1">
                            <p className="text-xs font-semibold text-slate-500 tracking-wider">CRITICAL REQUESTS</p>
                            <h3 className="text-2xl font-bold text-slate-100">{metrics?.criticalRequests}</h3>
                        </div>
                        <div className="bg-slate-800 p-3 rounded-lg text-amber-500">
                            <AlertCircle className="h-6 w-6" />
                        </div>
                    </div>

                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
                        <div className="space-y-1">
                            <p className="text-xs font-semibold text-slate-500 tracking-wider">ACTIVE MISSIONS</p>
                            <h3 className="text-2xl font-bold text-slate-100">{metrics?.activeMissions}</h3>
                        </div>
                        <div className="bg-slate-800 p-3 rounded-lg text-sky-500">
                            <Navigation className="h-6 w-6 animate-bounce" />
                        </div>
                    </div>

                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
                        <div className="space-y-1">
                            <p className="text-xs font-semibold text-slate-500 tracking-wider">FULFILLMENT RATE</p>
                            <h3 className="text-2xl font-bold text-slate-100">{(metrics?.fulfillmentRate * 100).toFixed(1)}%</h3>
                        </div>
                        <div className="bg-slate-800 p-3 rounded-lg text-emerald-500">
                            <Percent className="h-6 w-6" />
                        </div>
                    </div>
                </div>

                {/* Charts Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    
                    {/* Response Time Trend */}
                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-4">
                        <div className="flex items-center justify-between">
                            <h4 className="text-sm font-semibold text-slate-350 flex items-center gap-2">
                                <TrendingUp className="h-4 w-4 text-sky-500" />
                                Average Response Time (Minutes)
                            </h4>
                            <span className="text-xs font-bold text-slate-500">PAST 5 HOURS</span>
                        </div>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <AreaChart data={timelineData}>
                                    <defs>
                                        <linearGradient id="colorTime" x1="0" y1="0" x2="0" y2="1">
                                            <stop offset="5%" stopColor="#0ea5e9" stopOpacity={0.2}/>
                                            <stop offset="95%" stopColor="#0ea5e9" stopOpacity={0}/>
                                        </linearGradient>
                                    </defs>
                                    <XAxis dataKey="name" stroke="#475569" fontSize={11} />
                                    <YAxis stroke="#475569" fontSize={11} />
                                    <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }} />
                                    <Area type="monotone" dataKey="responseTime" stroke="#0ea5e9" strokeWidth={2} fillOpacity={1} fill="url(#colorTime)" />
                                </AreaChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Resources Distributed */}
                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-4">
                        <div className="flex items-center justify-between">
                            <h4 className="text-sm font-semibold text-slate-350 flex items-center gap-2">
                                <FlameKindling className="h-4 w-4 text-rose-500" />
                                Resource Allocation by Category
                            </h4>
                            <span className="text-xs font-bold text-slate-500">BATCH QUANTITIES</span>
                        </div>
                        <div className="h-64">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={categoryData}>
                                    <XAxis dataKey="name" stroke="#475569" fontSize={11} />
                                    <YAxis stroke="#475569" fontSize={11} />
                                    <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }} />
                                    <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                                        {categoryData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={entry.color} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
