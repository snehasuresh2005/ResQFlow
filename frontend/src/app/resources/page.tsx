'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
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

    const queryClient = useQueryClient();

    // Form state for creating new resource batch
    const [showForm, setShowForm] = useState(false);
    const [name, setName] = useState('');
    const [resourceType, setResourceType] = useState('FOOD');
    const [quantity, setQuantity] = useState('500');
    const [unit, setUnit] = useState('units');
    const [depotId, setDepotId] = useState('');
    const [daysToExpiry, setDaysToExpiry] = useState('30');
    const [errorMsg, setErrorMsg] = useState('');

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

    const { data: locationData } = useQuery({
        queryKey: ['locations'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/locations`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            return res.json();
        },
        enabled: !!token
    });

    // Mutations
    const createResourceMutation = useMutation({
        mutationFn: async (payload: any) => {
            const res = await fetch(`${apiBase}/resources`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}`
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.message || 'Failed to create resource batch');
            }
            return res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['resources'] });
            queryClient.invalidateQueries({ queryKey: ['availability'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
            setShowForm(false);
            resetForm();
        },
        onError: (err: any) => {
            setErrorMsg(err.message);
        }
    });

    const resetForm = () => {
        setName('');
        setResourceType('FOOD');
        setQuantity('500');
        setUnit('units');
        setDepotId('');
        setDaysToExpiry('30');
        setErrorMsg('');
    };

    const handleCreateResource = (e: React.FormEvent) => {
        e.preventDefault();
        if (!depotId) {
            alert('Please select a target Depot location.');
            return;
        }

        const exp = new Date();
        exp.setDate(exp.getDate() + parseInt(daysToExpiry));
        const formattedExp = exp.toISOString().split('T')[0];

        const payload = {
            name,
            resourceType,
            quantity: parseFloat(quantity),
            unit,
            depotId: parseInt(depotId),
            expiryDate: formattedExp,
            priority: 1,
            weightPerUnit: 1.0,
            volumePerUnit: 0.5
        };

        createResourceMutation.mutate(payload);
    };

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
                <div className="flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight text-white">Resource Warehouses</h2>
                        <p className="text-sm text-slate-400">Inventory stores distributed across depots</p>
                    </div>
                    <button
                        onClick={() => setShowForm(!showForm)}
                        className="bg-rose-600 hover:bg-rose-700 text-white font-semibold px-4 py-2.5 rounded-xl flex items-center gap-2 text-sm cursor-pointer transition-all shadow-lg shadow-rose-950/20"
                    >
                        {showForm ? <AlertCircle className="h-5 w-5" /> : <Warehouse className="h-5 w-5" />}
                        {showForm ? 'Close Panel' : 'New Supply Batch'}
                    </button>
                </div>

                {/* Form Drawer */}
                {showForm && (
                    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
                        <h3 className="text-md font-bold text-slate-200">Register Supply Stock Batch</h3>
                        {errorMsg && (
                            <div className="p-3 bg-rose-950/40 border border-rose-900/50 rounded-xl text-xs text-rose-300">
                                {errorMsg}
                            </div>
                        )}
                        <form onSubmit={handleCreateResource} className="grid grid-cols-1 md:grid-cols-3 gap-5">
                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Batch Name</label>
                                <input
                                    type="text"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="e.g. High-energy Food Ration Pack"
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Resource Category</label>
                                <select
                                    value={resourceType}
                                    onChange={(e) => setResourceType(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                >
                                    <option value="FOOD">FOOD</option>
                                    <option value="WATER">WATER</option>
                                    <option value="MEDICAL">MEDICAL</option>
                                    <option value="SHELTER">SHELTER</option>
                                    <option value="EQUIPMENT">EQUIPMENT</option>
                                </select>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Depot Warehouse</label>
                                <select
                                    value={depotId}
                                    onChange={(e) => setDepotId(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                >
                                    <option value="">Select Depot</option>
                                    {locationData?.depots?.map((d: any) => (
                                        <option key={d.id} value={d.id}>{d.name}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Quantity</label>
                                <input
                                    type="number"
                                    value={quantity}
                                    onChange={(e) => setQuantity(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Measurement Unit</label>
                                <input
                                    type="text"
                                    value={unit}
                                    onChange={(e) => setUnit(e.target.value)}
                                    placeholder="units, litres, kits..."
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Days to Expiry</label>
                                <input
                                    type="number"
                                    value={daysToExpiry}
                                    onChange={(e) => setDaysToExpiry(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="md:col-span-3 flex justify-end">
                                <button
                                    type="submit"
                                    disabled={createResourceMutation.isPending}
                                    className="bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-800 text-white font-semibold px-6 py-2.5 rounded-xl text-sm cursor-pointer transition-all"
                                >
                                    {createResourceMutation.isPending ? 'Registering Stock...' : 'Save Resource Stock'}
                                </button>
                            </div>
                        </form>
                    </div>
                )}

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
