'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery } from '@tanstack/react-query';
import { Layers, Warehouse, Calendar, Activity, AlertCircle } from 'lucide-react';

export default function ResourcesPage() {
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

    // Queries
    const { data: resources, isLoading: resLoading } = useQuery({
        queryKey: ['resources'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/resources`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            return res.json();
        },
        enabled: !!token
    });

    const { data: availability } = useQuery({
        queryKey: ['availability'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/resources/availability`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            return res.json();
        },
        enabled: !!token
    });

    if (!token || resLoading) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse">Loading Supply Resources...</div>
            </div>
        );
    }

    return (
        <div className="flex bg-slate-950 min-h-screen">
            <Sidebar />

            <main className="flex-1 p-8 space-y-6 overflow-y-auto max-h-screen">
                {/* Header */}
                <div>
                    <h2 className="text-2xl font-bold tracking-tight text-white">Resource Warehouses</h2>
                    <p className="text-sm text-slate-400">Inventory stores distributed across depots</p>
                </div>

                {/* Aggregated Availability Grid */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-5">
                    {availability?.map((avail: any, idx: number) => (
                        <div key={idx} className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
                            <div>
                                <p className="text-xs font-semibold text-slate-500 tracking-wider">{avail.resourceType}</p>
                                <h4 className="text-xl font-bold text-slate-100 mt-1">
                                    {avail.totalQuantity.toLocaleString()} <span className="text-xs text-slate-400 font-normal">{avail.unit}</span>
                                </h4>
                            </div>
                            <div className="bg-slate-800 p-2.5 rounded-lg text-rose-500">
                                <Layers className="h-5 w-5" />
                            </div>
                        </div>
                    ))}
                </div>

                {/* Batches Table */}
                <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                    <div className="p-5 border-b border-slate-800">
                        <h3 className="text-sm font-semibold text-slate-200">Active Supply Batches</h3>
                    </div>
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 tracking-wider">
                                <th className="p-4">BATCH NAME</th>
                                <th className="p-4">CATEGORY</th>
                                <th className="p-4">QUANTITY</th>
                                <th className="p-4">DEPOT LOCATION</th>
                                <th className="p-4">EXPIRY DATE</th>
                                <th className="p-4">STATUS</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800 text-sm text-slate-300">
                            {resources?.map((res: any) => (
                                <tr key={res.id} className="hover:bg-slate-850/20">
                                    <td className="p-4 font-semibold text-slate-100 flex items-center gap-2">
                                        <Layers className="h-4 w-4 text-slate-500" />
                                        {res.name}
                                    </td>
                                    <td className="p-4">{res.resourceType}</td>
                                    <td className="p-4 font-mono">
                                        {res.quantity} <span className="text-xs text-slate-500">{res.unit}</span>
                                    </td>
                                    <td className="p-4 flex items-center gap-2">
                                        <Warehouse className="h-4 w-4 text-slate-500" />
                                        {res.depotName}
                                    </td>
                                    <td className="p-4 text-slate-400">
                                        <div className="flex items-center gap-1.5">
                                            <Calendar className="h-4 w-4 text-slate-500" />
                                            {res.expiryDate || 'N/A'}
                                        </div>
                                    </td>
                                    <td className="p-4">
                                        <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                                            res.status === 'AVAILABLE' ? 'bg-emerald-500/10 text-emerald-500' : 'bg-rose-500/10 text-rose-500'
                                        }`}>
                                            {res.status}
                                        </span>
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
