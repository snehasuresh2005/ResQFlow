'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery } from '@tanstack/react-query';
import { 
    BarChart3, 
    TrendingUp, 
    Activity, 
    ShieldAlert, 
    Clock, 
    Database, 
    ArrowUpRight,
    FileSpreadsheet
} from 'lucide-react';
import { 
    ResponsiveContainer, 
    BarChart, 
    Bar, 
    XAxis, 
    YAxis, 
    Tooltip, 
    LineChart, 
    Line, 
    Cell
} from 'recharts';

export default function AnalyticsPage() {
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

    const apiBase = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

    // 1. Fetch dashboard metrics
    const { data: metrics, isLoading: metricsLoading } = useQuery({
        queryKey: ['analyticsMetrics'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/analytics/dashboard`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            if (!res.ok) throw new Error('Failed to load metrics');
            return res.json();
        },
        enabled: !!token,
        refetchInterval: 5000
    });

    // 2. Fetch live system audits
    const { data: audits, isLoading: auditsLoading } = useQuery({
        queryKey: ['systemAudits'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/analytics/audits`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            if (!res.ok) throw new Error('Failed to load audits');
            return res.json();
        },
        enabled: !!token,
        refetchInterval: 5000
    });

    // 3. Fetch request category distribution count
    const { data: requests } = useQuery({
        queryKey: ['analyticsRequests'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/requests`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            if (!res.ok) throw new Error('Failed to load requests');
            return res.json();
        },
        enabled: !!token
    });

    if (!token || metricsLoading || auditsLoading) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse flex items-center gap-2">
                    <Activity className="h-4 w-4 animate-spin text-sky-500" />
                    <span>Aggregating analytics data streams...</span>
                </div>
            </div>
        );
    }

    // Process requests data into chart distribution
    const requestTypes = requests || [];
    const typeCountMap: Record<string, number> = {};
    requestTypes.forEach((req: any) => {
        const type = req.requestType || 'UNKNOWN';
        typeCountMap[type] = (typeCountMap[type] || 0) + 1;
    });

    const categoryChartData = Object.entries(typeCountMap).map(([name, count]) => ({
        name,
        count,
        fill: name === 'FOOD' ? '#f43f5e' : (name === 'WATER' ? '#0ea5e9' : (name === 'MEDICAL' ? '#10b981' : '#f59e0b'))
    }));

    // Default mock category breakdown fallback if empty
    const categoryData = categoryChartData.length > 0 ? categoryChartData : [
        { name: 'FOOD', count: 4, fill: '#f43f5e' },
        { name: 'WATER', count: 3, fill: '#0ea5e9' },
        { name: 'MEDICAL', count: 2, fill: '#10b981' },
        { name: 'SHELTER', count: 1, fill: '#f59e0b' }
    ];

    // Mock trend line representing allocation efficiency latency
    const responseLatencyTrend = [
        { name: 'Mon', responseTime: 22 },
        { name: 'Tue', responseTime: 18 },
        { name: 'Wed', responseTime: 19 },
        { name: 'Thu', responseTime: 15 },
        { name: 'Fri', responseTime: 18.3 }
    ];

    return (
        <div className="flex bg-slate-950 min-h-screen text-slate-100">
            <Sidebar />
            
            <main className="flex-1 p-8 space-y-6 overflow-y-auto max-h-screen">
                
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight text-white">Operations Analytics</h2>
                        <p className="text-sm text-slate-400">Detailed system operations, resource utilization, and event audit feeds</p>
                    </div>
                </div>

                {/* KPI Overview Cards */}
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                    <div className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl flex flex-col justify-between shadow-lg">
                        <div className="flex justify-between items-start text-slate-400">
                            <span className="text-xs font-semibold uppercase tracking-wider">Total Requests</span>
                            <Database className="h-5 w-5 text-sky-500" />
                        </div>
                        <div className="mt-4">
                            <h3 className="text-3xl font-bold">{metrics?.totalRequests || 0}</h3>
                            <p className="text-xs text-sky-400 mt-1 flex items-center gap-1">
                                <ArrowUpRight className="h-3 w-3" />
                                <span>Active operation lifecycle</span>
                            </p>
                        </div>
                    </div>

                    <div className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl flex flex-col justify-between shadow-lg">
                        <div className="flex justify-between items-start text-slate-400">
                            <span className="text-xs font-semibold uppercase tracking-wider">Critical Requests</span>
                            <ShieldAlert className="h-5 w-5 text-rose-500" />
                        </div>
                        <div className="mt-4">
                            <h3 className="text-3xl font-bold">{metrics?.criticalRequests || 0}</h3>
                            <p className="text-xs text-rose-400 mt-1">High-priority allocations</p>
                        </div>
                    </div>

                    <div className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl flex flex-col justify-between shadow-lg">
                        <div className="flex justify-between items-start text-slate-400">
                            <span className="text-xs font-semibold uppercase tracking-wider">Fulfillment Rate</span>
                            <TrendingUp className="h-5 w-5 text-emerald-500" />
                        </div>
                        <div className="mt-4">
                            <h3 className="text-3xl font-bold">{((metrics?.fulfillmentRate || 0) * 100).toFixed(1)}%</h3>
                            <p className="text-xs text-emerald-400 mt-1">Request resolution ratio</p>
                        </div>
                    </div>

                    <div className="bg-slate-900/60 border border-slate-800 p-6 rounded-2xl flex flex-col justify-between shadow-lg">
                        <div className="flex justify-between items-start text-slate-400">
                            <span className="text-xs font-semibold uppercase tracking-wider">Avg Latency</span>
                            <Clock className="h-5 w-5 text-amber-500" />
                        </div>
                        <div className="mt-4">
                            <h3 className="text-3xl font-bold">{metrics?.avgResponseTimeMinutes || 18.3}m</h3>
                            <p className="text-xs text-amber-400 mt-1">Route & dispatch speed</p>
                        </div>
                    </div>
                </div>

                {/* Charts Grid */}
                <div className="grid gap-6 md:grid-cols-2">
                    
                    {/* Resource distribution chart */}
                    <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col">
                        <h4 className="font-bold text-slate-200 mb-4 flex items-center gap-2">
                            <BarChart3 className="h-4 w-4 text-sky-500" />
                            <span>Request Category Breakdown</span>
                        </h4>
                        <div className="h-64 mt-2">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={categoryData}>
                                    <XAxis dataKey="name" stroke="#64748b" fontSize={11} tickLine={false} />
                                    <YAxis stroke="#64748b" fontSize={11} tickLine={false} />
                                    <Tooltip 
                                        contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px' }}
                                        labelStyle={{ color: '#94a3b8', fontWeight: 'bold' }}
                                    />
                                    <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                                        {categoryData.map((entry, idx) => (
                                            <Cell key={`cell-${idx}`} fill={entry.fill} />
                                        ))}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    {/* Operational performance trend */}
                    <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl shadow-xl flex flex-col">
                        <h4 className="font-bold text-slate-200 mb-4 flex items-center gap-2">
                            <TrendingUp className="h-4 w-4 text-emerald-500" />
                            <span>Average Dispatch Speed Trend</span>
                        </h4>
                        <div className="h-64 mt-2">
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={responseLatencyTrend}>
                                    <XAxis dataKey="name" stroke="#64748b" fontSize={11} tickLine={false} />
                                    <YAxis stroke="#64748b" fontSize={11} tickLine={false} />
                                    <Tooltip 
                                        contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px' }}
                                        labelStyle={{ color: '#94a3b8', fontWeight: 'bold' }}
                                    />
                                    <Line 
                                        type="monotone" 
                                        dataKey="responseTime" 
                                        stroke="#10b981" 
                                        strokeWidth={3}
                                        activeDot={{ r: 6 }} 
                                    />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                </div>

                {/* Audit Logs List */}
                <div className="bg-slate-900/40 border border-slate-800 rounded-2xl shadow-xl p-6">
                    <h4 className="font-bold text-slate-200 mb-4 flex items-center gap-2">
                        <FileSpreadsheet className="h-4 w-4 text-purple-500" />
                        <span>Live Operations Audit Log Feed</span>
                    </h4>
                    
                    <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse text-xs">
                            <thead>
                                <tr className="border-b border-slate-800 text-slate-400 font-semibold uppercase tracking-wider font-sans">
                                    <th className="py-3 px-4">Event ID</th>
                                    <th className="py-3 px-4">Entity Type</th>
                                    <th className="py-3 px-4">Action Event</th>
                                    <th className="py-3 px-4">Entity ID</th>
                                    <th className="py-3 px-4">Timestamp</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-800 text-slate-300 font-sans">
                                {audits && audits.length > 0 ? (
                                    audits.map((a: any) => (
                                        <tr key={a.id} className="hover:bg-slate-900/40 transition">
                                            <td className="py-3 px-4 font-mono text-slate-500">#{a.id}</td>
                                            <td className="py-3 px-4 font-semibold text-sky-400">{a.aggregateType}</td>
                                            <td className="py-3 px-4">
                                                <span className="bg-slate-800 text-slate-200 px-2 py-0.5 rounded font-mono border border-slate-700">
                                                    {a.eventType}
                                                </span>
                                            </td>
                                            <td className="py-3 px-4 font-mono">{a.aggregateId}</td>
                                            <td className="py-3 px-4 text-slate-400">
                                                {new Date(a.createdAt).toLocaleString()}
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="py-8 text-center text-slate-500">
                                            No system logs recorded yet. Dispatch or transit a logistics mission to stream audits.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>

            </main>
        </div>
    );
}
