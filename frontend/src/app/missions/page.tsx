'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
    Compass, 
    ArrowRight, 
    Activity, 
    AlertCircle, 
    CheckCircle2, 
    ShieldClose, 
    Truck,
    MapPin
} from 'lucide-react';

export default function MissionsPage() {
    const router = useRouter();
    const queryClient = useQueryClient();
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

    // Queries
    const { data: missions, isLoading: mLoading } = useQuery({
        queryKey: ['missions'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/missions`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            return res.json();
        },
        enabled: !!token
    });

    // Mutations
    const transitionMutation = useMutation({
        mutationFn: async ({ id, action }: { id: number; action: string }) => {
            const res = await fetch(`${apiBase}/missions/${id}/${action}`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            if (!res.ok) throw new Error('Transition failed');
            return res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['missions'] });
        }
    });

    if (!token || mLoading) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse">Loading Mission Operations...</div>
            </div>
        );
    }

    return (
        <div className="flex bg-slate-950 min-h-screen">
            <Sidebar />

            <main className="flex-1 p-8 space-y-6 overflow-y-auto max-h-screen">
                {/* Header */}
                <div>
                    <h2 className="text-2xl font-bold tracking-tight text-white">Active Logistics Missions</h2>
                    <p className="text-sm text-slate-400">Track and dispatch active resource transit routes</p>
                </div>

                {/* Table list */}
                <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 tracking-wider">
                                <th className="p-4">MISSION ID</th>
                                <th className="p-4">REQUEST #</th>
                                <th className="p-4">VEHICLE / DRIVER</th>
                                <th className="p-4">ROUTE NODES count</th>
                                <th className="p-4">STATUS</th>
                                <th className="p-4 text-right">OPERATIONAL ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800 text-sm text-slate-300">
                            {missions?.map((m: any) => (
                                <tr key={m.id} className="hover:bg-slate-850/20">
                                    <td className="p-4 font-semibold text-slate-100 flex items-center gap-2">
                                        <Compass className="h-4 w-4 text-slate-400" />
                                        MSN-{1000 + m.id}
                                    </td>
                                    <td className="p-4 font-mono text-xs">{m.request?.requestNumber}</td>
                                    <td className="p-4">
                                        <div className="flex flex-col">
                                            <span className="text-xs font-medium text-slate-200">{m.vehicle?.registrationNumber}</span>
                                            <span className="text-[10px] text-slate-400">{m.driver?.name}</span>
                                        </div>
                                    </td>
                                    <td className="p-4 text-xs font-mono">
                                        {m.route?.routeNodes ? `${m.route.routeNodes.length} nodes (${m.route.totalDistance.toFixed(1)} km)` : 'Direct (uncalculated)'}
                                    </td>
                                    <td className="p-4">
                                        <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                                            m.status === 'CREATED' ? 'bg-slate-800 text-slate-400' :
                                            m.status === 'DISPATCHED' ? 'bg-indigo-500/10 text-indigo-400' :
                                            m.status === 'IN_TRANSIT' ? 'bg-amber-500/10 text-amber-500' :
                                            m.status === 'BLOCKED' ? 'bg-rose-500/10 text-rose-500 font-extrabold animate-pulse' :
                                            m.status === 'REROUTING' ? 'bg-orange-500/10 text-orange-400' :
                                            m.status === 'DELIVERED' ? 'bg-emerald-500/10 text-emerald-500' :
                                            'bg-rose-500/10 text-rose-500'
                                        }`}>
                                            {m.status}
                                        </span>
                                    </td>
                                    <td className="p-4 text-right flex justify-end gap-1.5 flex-wrap">
                                        {m.status === 'CREATED' && (
                                            <button
                                                onClick={() => transitionMutation.mutate({ id: m.id, action: 'dispatch' })}
                                                className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-2 py-1 rounded cursor-pointer transition-all"
                                            >
                                                Dispatch
                                            </button>
                                        )}
                                        {m.status === 'DISPATCHED' && (
                                            <button
                                                onClick={() => transitionMutation.mutate({ id: m.id, action: 'transit' })}
                                                className="bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold px-2 py-1 rounded cursor-pointer transition-all"
                                            >
                                                Transit
                                            </button>
                                        )}
                                        {m.status === 'IN_TRANSIT' && (
                                            <>
                                                <button
                                                    onClick={() => transitionMutation.mutate({ id: m.id, action: 'block' })}
                                                    className="bg-rose-900/60 hover:bg-rose-800 text-white text-xs font-semibold px-2 py-1 rounded cursor-pointer transition-all"
                                                >
                                                    Block
                                                </button>
                                                <button
                                                    onClick={() => transitionMutation.mutate({ id: m.id, action: 'complete' })}
                                                    className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold px-2 py-1 rounded cursor-pointer transition-all"
                                                >
                                                    Deliver
                                                </button>
                                            </>
                                        )}
                                        {m.status === 'BLOCKED' && (
                                            <button
                                                onClick={() => transitionMutation.mutate({ id: m.id, action: 'reroute' })}
                                                className="bg-orange-600 hover:bg-orange-700 text-white text-xs font-semibold px-2 py-1 rounded cursor-pointer transition-all animate-bounce"
                                            >
                                                Reroute
                                            </button>
                                        )}
                                        {m.status === 'REROUTING' && (
                                            <button
                                                onClick={() => transitionMutation.mutate({ id: m.id, action: 'transit' })}
                                                className="bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold px-2 py-1 rounded cursor-pointer transition-all"
                                            >
                                                Transit
                                            </button>
                                        )}
                                        {m.status !== 'DELIVERED' && m.status !== 'FAILED' && m.status !== 'CANCELLED' && (
                                            <button
                                                onClick={() => transitionMutation.mutate({ id: m.id, action: 'cancel' })}
                                                className="bg-slate-800 hover:bg-slate-700 text-rose-400 text-xs font-semibold px-2 py-1 rounded cursor-pointer transition-all border border-slate-850"
                                            >
                                                Cancel
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
    );
}
