'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { 
    Plus, 
    X, 
    FileText, 
    ShieldAlert, 
    Flame, 
    Calendar, 
    CheckCircle,
    UserMinus,
    Coins
} from 'lucide-react';

export default function RequestsPage() {
    const router = useRouter();
    const queryClient = useQueryClient();
    const [token, setToken] = useState<string | null>(null);

    // Form states
    const [showForm, setShowForm] = useState(false);
    const [zoneId, setZoneId] = useState('');
    const [reqType, setReqType] = useState('FOOD');
    const [priority, setPriority] = useState('MEDIUM');
    const [people, setPeople] = useState('100');
    const [durationHours, setDurationHours] = useState('4');
    
    // Requirement details
    const [reqQty, setReqQty] = useState('50');
    const [reqUnit, setReqUnit] = useState('units');

    // Allocation strategy selection modal
    const [selectedRequest, setSelectedRequest] = useState<any | null>(null);
    const [strategy, setStrategy] = useState('HYBRID');
    const [allocationMsg, setAllocationMsg] = useState('');

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
    const { data: requests, isLoading: reqsLoading } = useQuery({
        queryKey: ['requests'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/requests`, {
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
    const createMutation = useMutation({
        mutationFn: async (payload: any) => {
            const res = await fetch(`${apiBase}/requests`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}`
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error('Failed to create request');
            return res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['requests'] });
            setShowForm(false);
            resetForm();
        }
    });

    const cancelMutation = useMutation({
        mutationFn: async (id: number) => {
            const res = await fetch(`${apiBase}/requests/${id}/cancel`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            return res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['requests'] });
        }
    });

    const allocateMutation = useMutation({
        mutationFn: async (payload: { requestId: number; strategy: string }) => {
            const key = crypto.randomUUID(); // Idempotency-Key
            const res = await fetch(`${apiBase}/allocation/allocate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Idempotency-Key': key,
                    'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}`
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.message || 'Allocation failed');
            }
            return res.json();
        },
        onSuccess: (data) => {
            queryClient.invalidateQueries({ queryKey: ['requests'] });
            setAllocationMsg(`Successfully allocated resources. Mission ID: ${data.missionId || 'N/A'}`);
            setTimeout(() => {
                setSelectedRequest(null);
                setAllocationMsg('');
            }, 3000);
        },
        onError: (err: any) => {
            setAllocationMsg(`Error: ${err.message}`);
        }
    });

    const resetForm = () => {
        setZoneId('');
        setReqType('FOOD');
        setPriority('MEDIUM');
        setPeople('100');
        setDurationHours('4');
        setReqQty('50');
        setReqUnit('units');
    };

    const handleCreate = (e: React.FormEvent) => {
        e.preventDefault();
        const dl = new Date();
        dl.setHours(dl.getHours() + parseInt(durationHours));

        const payload = {
            emergencyZoneId: parseInt(zoneId),
            requestType: reqType,
            priority: priority,
            numberOfPeopleAffected: parseInt(people),
            deadline: dl.toISOString(),
            requirements: [
                {
                    resourceType: reqType,
                    quantity: parseFloat(reqQty),
                    unit: reqUnit
                }
            ]
        };

        createMutation.mutate(payload);
    };

    if (!token || reqsLoading) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse">Loading Requests...</div>
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
                        <h2 className="text-2xl font-bold tracking-tight text-white">Emergency Requests</h2>
                        <p className="text-sm text-slate-400">Incoming coordinator requests for assistance</p>
                    </div>
                    <button
                        onClick={() => setShowForm(!showForm)}
                        className="bg-rose-600 hover:bg-rose-700 text-white font-semibold px-4 py-2.5 rounded-xl flex items-center gap-2 text-sm cursor-pointer transition-all shadow-lg shadow-rose-950/20"
                    >
                        {showForm ? <X className="h-5 w-5" /> : <Plus className="h-5 w-5" />}
                        {showForm ? 'Close Panel' : 'New Request'}
                    </button>
                </div>

                {/* Form Drawer */}
                {showForm && (
                    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
                        <h3 className="text-md font-bold text-slate-200">Submit Incident Request</h3>
                        <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-3 gap-5">
                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Emergency Zone</label>
                                <select
                                    value={zoneId}
                                    onChange={(e) => setZoneId(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                >
                                    <option value="">Select Zone</option>
                                    {locationData?.zones?.map((z: any) => (
                                        <option key={z.id} value={z.id}>{z.name}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Request Type</label>
                                <select
                                    value={reqType}
                                    onChange={(e) => setReqType(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                >
                                    <option value="FOOD">FOOD</option>
                                    <option value="WATER">WATER</option>
                                    <option value="MEDICAL">MEDICAL</option>
                                    <option value="SHELTER">SHELTER</option>
                                </select>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Priority Urgency</label>
                                <select
                                    value={priority}
                                    onChange={(e) => setPriority(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                >
                                    <option value="CRITICAL">CRITICAL</option>
                                    <option value="HIGH">HIGH</option>
                                    <option value="MEDIUM">MEDIUM</option>
                                    <option value="LOW">LOW</option>
                                </select>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Affected Population</label>
                                <input
                                    type="number"
                                    value={people}
                                    onChange={(e) => setPeople(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-350 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Hours to Deadline</label>
                                <input
                                    type="number"
                                    value={durationHours}
                                    onChange={(e) => setDurationHours(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-350 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1 grid grid-cols-2 gap-2">
                                <div>
                                    <label className="text-xs font-semibold text-slate-500">Quantity</label>
                                    <input
                                        type="number"
                                        value={reqQty}
                                        onChange={(e) => setReqQty(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-350 focus:outline-none"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="text-xs font-semibold text-slate-500">Unit</label>
                                    <input
                                        type="text"
                                        value={reqUnit}
                                        onChange={(e) => setReqUnit(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-350 focus:outline-none"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="md:col-span-3 flex justify-end">
                                <button
                                    type="submit"
                                    className="bg-emerald-600 hover:bg-emerald-700 text-white font-semibold px-6 py-2.5 rounded-xl text-sm cursor-pointer transition-all"
                                >
                                    Submit Request
                                </button>
                            </div>
                        </form>
                    </div>
                )}

                {/* Strategy Allocation Modal */}
                {selectedRequest && (
                    <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
                        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 w-full max-w-md shadow-2xl space-y-4">
                            <div className="flex justify-between items-center">
                                <h3 className="text-md font-bold text-slate-200">Allocate Request: {selectedRequest.requestNumber}</h3>
                                <button onClick={() => setSelectedRequest(null)} className="text-slate-400 hover:text-slate-200">
                                    <X className="h-5 w-5" />
                                </button>
                            </div>
                            <div className="space-y-4">
                                <div className="space-y-1">
                                    <label className="text-xs font-semibold text-slate-500">Select Allocation Strategy</label>
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

                                {allocationMsg && (
                                    <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-sm text-slate-300 text-center">
                                        {allocationMsg}
                                    </div>
                                )}

                                <button
                                    onClick={() => allocateMutation.mutate({ requestId: selectedRequest.id, strategy })}
                                    className="w-full bg-rose-600 hover:bg-rose-700 py-3 rounded-xl font-semibold text-sm text-white transition-all cursor-pointer shadow-lg"
                                >
                                    {allocateMutation.isPending ? 'Executing Allocation Engine...' : 'Run Strategy & Allocate'}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                {/* Table list */}
                <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-950 border-b border-slate-800 text-xs font-semibold text-slate-400 tracking-wider">
                                <th className="p-4">REQ #</th>
                                <th className="p-4">ZONE</th>
                                <th className="p-4">TYPE</th>
                                <th className="p-4">PRIORITY</th>
                                <th className="p-4">AFFECTED</th>
                                <th className="p-4">STATUS</th>
                                <th className="p-4 text-right">ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800 text-sm text-slate-300">
                            {requests?.map((req: any) => (
                                <tr key={req.id} className="hover:bg-slate-850/20">
                                    <td className="p-4 font-semibold text-slate-100 flex items-center gap-2">
                                        <FileText className="h-4 w-4 text-slate-400" />
                                        {req.requestNumber}
                                    </td>
                                    <td className="p-4">{req.emergencyZoneName}</td>
                                    <td className="p-4">{req.requestType}</td>
                                    <td className="p-4">
                                        <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                                            req.priority === 'CRITICAL' ? 'bg-rose-500/10 text-rose-500' :
                                            req.priority === 'HIGH' ? 'bg-amber-500/10 text-amber-500' :
                                            'bg-sky-500/10 text-sky-500'
                                        }`}>
                                            {req.priority}
                                        </span>
                                    </td>
                                    <td className="p-4">{req.numberOfPeopleAffected}</td>
                                    <td className="p-4">
                                        <span className="text-xs font-semibold text-slate-400">{req.status}</span>
                                    </td>
                                    <td className="p-4 text-right flex justify-end gap-2">
                                        {req.status === 'CREATED' && (
                                            <button
                                                onClick={() => setSelectedRequest(req)}
                                                className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold px-3 py-1.5 rounded-lg cursor-pointer transition-all"
                                            >
                                                Allocate
                                            </button>
                                        )}
                                        {req.status !== 'CANCELLED' && req.status !== 'FULFILLED' && (
                                            <button
                                                onClick={() => cancelMutation.mutate(req.id)}
                                                className="bg-slate-850 hover:bg-slate-800 text-rose-400 hover:text-rose-350 text-xs font-semibold px-3 py-1.5 rounded-lg cursor-pointer transition-all border border-slate-800"
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
