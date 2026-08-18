'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useMutation } from '@tanstack/react-query';
import { 
    Play, 
    BarChart3, 
    Layers, 
    Activity, 
    Flame,
    Percent,
    Compass
} from 'lucide-react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Legend } from 'recharts';

export default function SimulationPage() {
    const router = useRouter();
    const [token, setToken] = useState<string | null>(null);

    // Form inputs
    const [scenario, setScenario] = useState('FLOOD');
    const [requests, setRequests] = useState('100');
    const [depots, setDepots] = useState('5');
    const [vehicles, setVehicles] = useState('10');
    const [resources, setResources] = useState('150');
    const [zones, setZones] = useState('8');
    const [roadBlockage, setRoadBlockage] = useState('15');
    const [strategy, setStrategy] = useState('HYBRID');

    const [simResult, setSimResult] = useState<any | null>(null);

    useEffect(() => {
        const storedToken = localStorage.getItem('resqflow_token');
        if (!storedToken) {
            router.replace('/login');
        } else {
            setToken(storedToken);
        }
    }, [router]);

    const apiBase = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

    const runMutation = useMutation({
        mutationFn: async (payload: any) => {
            const res = await fetch(`${apiBase}/simulation/run`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}`
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error('Simulation run failed');
            return res.json();
        },
        onSuccess: (data) => {
            setSimResult(data);
        }
    });

    const handleRun = (e: React.FormEvent) => {
        e.preventDefault();
        const payload = {
            scenario,
            requests: parseInt(requests),
            depots: parseInt(depots),
            vehicles: parseInt(vehicles),
            resources: parseInt(resources),
            zones: parseInt(zones),
            blockedRoadPercentage: parseInt(roadBlockage),
            allocationStrategy: strategy
        };
        runMutation.mutate(payload);
    };

    if (!token) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse">Loading Simulation Center...</div>
            </div>
        );
    }

    // Benchmark comparison mock values (Hybrid vs Nearest vs Expiry)
    const benchmarkData = [
        { name: 'Hybrid', criticalRate: 98.2, responseTime: 18.3, resourceWastage: 3.2 },
        { name: 'Nearest', criticalRate: 75.4, responseTime: 12.1, resourceWastage: 24.5 },
        { name: 'Expiry', criticalRate: 81.2, responseTime: 28.5, resourceWastage: 1.8 }
    ];

    return (
        <div className="flex bg-slate-950 min-h-screen">
            <Sidebar />

            <main className="flex-1 p-8 space-y-6 overflow-y-auto max-h-screen">
                {/* Header */}
                <div>
                    <h2 className="text-2xl font-bold tracking-tight text-white">Disaster Simulation Center</h2>
                    <p className="text-sm text-slate-400">Generate scenarios and benchmark allocation strategies</p>
                </div>

                {/* Main Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Control Panel */}
                    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4 lg:col-span-1">
                        <h3 className="text-sm font-bold text-slate-200 flex items-center gap-2">
                            <Activity className="h-5 w-5 text-rose-500" />
                            Configuration Panel
                        </h3>
                        <form onSubmit={handleRun} className="space-y-4">
                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Disaster Scenario</label>
                                <select
                                    value={scenario}
                                    onChange={(e) => setScenario(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                >
                                    <option value="FLOOD">FLOOD (Water Roads Active)</option>
                                    <option value="EARTHQUAKE">EARTHQUAKE (Heavy Road Blockage)</option>
                                    <option value="WILDFIRE">WILDFIRE (Fast Rerouting Needed)</option>
                                </select>
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div className="space-y-1">
                                    <label className="text-xs font-semibold text-slate-500">Requests</label>
                                    <input
                                        type="number"
                                        value={requests}
                                        onChange={(e) => setRequests(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-350 focus:outline-none"
                                    />
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs font-semibold text-slate-500">Depots</label>
                                    <input
                                        type="number"
                                        value={depots}
                                        onChange={(e) => setDepots(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-355 focus:outline-none"
                                    />
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs font-semibold text-slate-500">Vehicles</label>
                                    <input
                                        type="number"
                                        value={vehicles}
                                        onChange={(e) => setVehicles(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-355 focus:outline-none"
                                    />
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs font-semibold text-slate-500">Resources</label>
                                    <input
                                        type="number"
                                        value={resources}
                                        onChange={(e) => setResources(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-355 focus:outline-none"
                                    />
                                </div>
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div className="space-y-1">
                                    <label className="text-xs font-semibold text-slate-500">Zones</label>
                                    <input
                                        type="number"
                                        value={zones}
                                        onChange={(e) => setZones(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-355 focus:outline-none"
                                    />
                                </div>
                                <div className="space-y-1">
                                    <label className="text-xs font-semibold text-slate-500">Road Block %</label>
                                    <input
                                        type="number"
                                        value={roadBlockage}
                                        onChange={(e) => setRoadBlockage(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-355 focus:outline-none"
                                    />
                                </div>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Allocation Strategy</label>
                                <select
                                    value={strategy}
                                    onChange={(e) => setStrategy(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2.5 px-3 text-sm text-slate-300 focus:outline-none"
                                >
                                    <option value="HYBRID">HYBRID (Configured Weights)</option>
                                    <option value="NEAREST">NEAREST (Proximity Sort)</option>
                                    <option value="HIGHEST_PRIORITY">HIGHEST_PRIORITY (Priority Sort)</option>
                                    <option value="EXPIRY_AWARE">EXPIRY_AWARE (Soonest Expiry First)</option>
                                    <option value="FAIR_DISTRIBUTION">FAIR_DISTRIBUTION (Proportional Depots)</option>
                                </select>
                            </div>

                            <button
                                type="submit"
                                disabled={runMutation.isPending}
                                className="w-full bg-rose-600 hover:bg-rose-700 disabled:bg-rose-800 text-white font-semibold py-3 rounded-xl flex items-center justify-center gap-2 cursor-pointer transition-all shadow-lg shadow-rose-950/20"
                            >
                                <Play className="h-5 w-5 fill-white" />
                                {runMutation.isPending ? 'Simulating Engine...' : 'Run Simulation'}
                            </button>
                        </form>
                    </div>

                    {/* Results Panel */}
                    <div className="lg:col-span-2 space-y-6">
                        {/* Simulation Run Result Card */}
                        {simResult && (
                            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
                                <h3 className="text-sm font-bold text-slate-200">Simulation Run Metrics (ID: #{simResult.simulationId})</h3>
                                <div className="grid grid-cols-2 md:grid-cols-3 gap-5">
                                    <div className="p-4 bg-slate-950 rounded-xl border border-slate-850">
                                        <p className="text-[10px] font-semibold text-slate-500 tracking-wider">REQUESTS FULFILLED</p>
                                        <h4 className="text-xl font-bold text-slate-100 mt-1">{simResult.requestsFulfilled} / {simResult.requestsProcessed}</h4>
                                    </div>
                                    <div className="p-4 bg-slate-950 rounded-xl border border-slate-850">
                                        <p className="text-[10px] font-semibold text-slate-500 tracking-wider">CRITICAL RATE</p>
                                        <h4 className="text-xl font-bold text-emerald-400 mt-1">{(simResult.criticalFulfillmentRate * 100).toFixed(1)}%</h4>
                                    </div>
                                    <div className="p-4 bg-slate-950 rounded-xl border border-slate-850">
                                        <p className="text-[10px] font-semibold text-slate-500 tracking-wider">AVG RESPONSE TIME</p>
                                        <h4 className="text-xl font-bold text-slate-100 mt-1">{simResult.averageResponseTimeMinutes.toFixed(1)} mins</h4>
                                    </div>
                                    <div className="p-4 bg-slate-950 rounded-xl border border-slate-850">
                                        <p className="text-[10px] font-semibold text-slate-500 tracking-wider">VEHICLE UTILIZATION</p>
                                        <h4 className="text-xl font-bold text-sky-400 mt-1">{(simResult.vehicleUtilization * 100).toFixed(1)}%</h4>
                                    </div>
                                    <div className="p-4 bg-slate-950 rounded-xl border border-slate-850">
                                        <p className="text-[10px] font-semibold text-slate-500 tracking-wider">RESOURCE WASTAGE</p>
                                        <h4 className="text-xl font-bold text-amber-500 mt-1">{(simResult.resourceWastage * 100).toFixed(1)}%</h4>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Strategy Comparison Chart */}
                        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
                            <h3 className="text-sm font-bold text-slate-200 flex items-center gap-2">
                                <BarChart3 className="h-5 w-5 text-sky-500" />
                                Allocation Strategy Benchmark Comparison
                            </h3>
                            <div className="h-64">
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={benchmarkData}>
                                        <XAxis dataKey="name" stroke="#475569" fontSize={11} />
                                        <YAxis stroke="#475569" fontSize={11} />
                                        <Tooltip contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155' }} />
                                        <Legend wrapperStyle={{ fontSize: 11 }} />
                                        <Bar dataKey="criticalRate" name="Critical Request Fulfill %" fill="#10b981" radius={[4, 4, 0, 0]} />
                                        <Bar dataKey="resourceWastage" name="Resource Wastage %" fill="#f59e0b" radius={[4, 4, 0, 0]} />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
