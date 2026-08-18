'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Truck, Navigation, Settings, AlertCircle, CheckCircle } from 'lucide-react';

export default function VehiclesPage() {
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
    const { data: vehicles, isLoading: vLoading } = useQuery({
        queryKey: ['vehicles'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/vehicles`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            return res.json();
        },
        enabled: !!token
    });

    // Mutations
    const updateStatusMutation = useMutation({
        mutationFn: async ({ id, status }: { id: number; status: string }) => {
            const res = await fetch(`${apiBase}/vehicles/${id}/status?status=${status}`, {
                method: 'PATCH',
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            return res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['vehicles'] });
        }
    });

    if (!token || vLoading) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse">Loading Vehicle Fleet...</div>
            </div>
        );
    }

    return (
        <div className="flex bg-slate-950 min-h-screen">
            <Sidebar />

            <main className="flex-1 p-8 space-y-6 overflow-y-auto max-h-screen">
                {/* Header */}
                <div>
                    <h2 className="text-2xl font-bold tracking-tight text-white">Relief Transit Fleet</h2>
                    <p className="text-sm text-slate-400">Emergency response vehicles and transport coordinates</p>
                </div>

                {/* Table list */}
                <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 tracking-wider">
                                <th className="p-4">VEHICLE PLATE</th>
                                <th className="p-4">TYPE</th>
                                <th className="p-4">CAPACITY (WT/VOL)</th>
                                <th className="p-4">COORDINATES (LAT/LON)</th>
                                <th className="p-4">FUEL LEVEL</th>
                                <th className="p-4">STATUS</th>
                                <th className="p-4 text-right">MODIFY STATUS</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800 text-sm text-slate-300">
                            {vehicles?.map((v: any) => (
                                <tr key={v.id} className="hover:bg-slate-850/20">
                                    <td className="p-4 font-semibold text-slate-100 flex items-center gap-2">
                                        <Truck className="h-4 w-4 text-slate-400" />
                                        {v.registrationNumber}
                                    </td>
                                    <td className="p-4 text-xs font-bold tracking-wider">{v.vehicleType}</td>
                                    <td className="p-4 font-mono text-xs">
                                        {v.capacityWeight}kg / {v.capacityVolume}m³
                                    </td>
                                    <td className="p-4 text-xs text-slate-400 font-mono">
                                        {v.currentLatitude.toFixed(4)}, {v.currentLongitude.toFixed(4)}
                                    </td>
                                    <td className="p-4 font-mono text-xs">
                                        <div className="flex items-center gap-2">
                                            <div className="w-16 bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-850">
                                                <div 
                                                    className="bg-emerald-500 h-full rounded-full" 
                                                    style={{ width: `${v.fuelLevel}%` }}
                                                ></div>
                                            </div>
                                            {v.fuelLevel}%
                                        </div>
                                    </td>
                                    <td className="p-4">
                                        <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                                            v.status === 'AVAILABLE' ? 'bg-emerald-500/10 text-emerald-500' :
                                            v.status === 'ASSIGNED' ? 'bg-sky-500/10 text-sky-500' :
                                            v.status === 'IN_TRANSIT' ? 'bg-amber-500/10 text-amber-500' :
                                            'bg-rose-500/10 text-rose-500'
                                        }`}>
                                            {v.status}
                                        </span>
                                    </td>
                                    <td className="p-4 text-right flex justify-end gap-2">
                                        <select
                                            value={v.status}
                                            onChange={(e) => updateStatusMutation.mutate({ id: v.id, status: e.target.value })}
                                            className="bg-slate-950 border border-slate-850 text-xs text-slate-300 rounded-lg py-1 px-2 focus:outline-none"
                                        >
                                            <option value="AVAILABLE">AVAILABLE</option>
                                            <option value="MAINTENANCE">MAINTENANCE</option>
                                            <option value="OUT_OF_SERVICE">OUT_OF_SERVICE</option>
                                        </select>
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
