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

    // Form state
    const [showForm, setShowForm] = useState(false);
    const [registrationNumber, setRegistrationNumber] = useState('');
    const [vehicleType, setVehicleType] = useState('TRUCK');
    const [capacityWeight, setCapacityWeight] = useState('2000');
    const [capacityVolume, setCapacityVolume] = useState('1000');
    const [fuelLevel, setFuelLevel] = useState('100');
    const [currentLatitude, setCurrentLatitude] = useState('12.9716');
    const [currentLongitude, setCurrentLongitude] = useState('77.5946');
    const [errorMsg, setErrorMsg] = useState('');

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
    const createVehicleMutation = useMutation({
        mutationFn: async (payload: any) => {
            const res = await fetch(`${apiBase}/vehicles`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}`
                },
                body: JSON.stringify(payload)
            });
            if (!res.ok) {
                const errData = await res.json();
                throw new Error(errData.message || 'Failed to register vehicle');
            }
            return res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['vehicles'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
            setShowForm(false);
            resetForm();
        },
        onError: (err: any) => {
            setErrorMsg(err.message);
        }
    });

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
            queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
        }
    });

    const resetForm = () => {
        setRegistrationNumber('');
        setVehicleType('TRUCK');
        setCapacityWeight('2000');
        setCapacityVolume('1000');
        setFuelLevel('100');
        setCurrentLatitude('12.9716');
        setCurrentLongitude('77.5946');
        setErrorMsg('');
    };

    const handleCreateVehicle = (e: React.FormEvent) => {
        e.preventDefault();
        const payload = {
            registrationNumber,
            vehicleType,
            capacityWeight: parseFloat(capacityWeight),
            capacityVolume: parseFloat(capacityVolume),
            fuelLevel: parseFloat(fuelLevel),
            currentLatitude: parseFloat(currentLatitude),
            currentLongitude: parseFloat(currentLongitude)
        };
        createVehicleMutation.mutate(payload);
    };

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
                <div className="flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight text-white">Relief Transit Fleet</h2>
                        <p className="text-sm text-slate-400">Emergency response vehicles and transport coordinates</p>
                    </div>
                    <button
                        onClick={() => setShowForm(!showForm)}
                        className="bg-rose-600 hover:bg-rose-700 text-white font-semibold px-4 py-2.5 rounded-xl flex items-center gap-2 text-sm cursor-pointer transition-all shadow-lg shadow-rose-950/20"
                    >
                        {showForm ? <AlertCircle className="h-5 w-5" /> : <Truck className="h-5 w-5" />}
                        {showForm ? 'Close Panel' : 'Register Vehicle'}
                    </button>
                </div>

                {/* Form Drawer */}
                {showForm && (
                    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
                        <h3 className="text-md font-bold text-slate-200">Add Fleet Response Vehicle</h3>
                        {errorMsg && (
                            <div className="p-3 bg-rose-950/40 border border-rose-900/50 rounded-xl text-xs text-rose-300">
                                {errorMsg}
                            </div>
                        )}
                        <form onSubmit={handleCreateVehicle} className="grid grid-cols-1 md:grid-cols-3 gap-5">
                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Registration Plate</label>
                                <input
                                    type="text"
                                    value={registrationNumber}
                                    onChange={(e) => setRegistrationNumber(e.target.value)}
                                    placeholder="e.g. KA-01-EQ-5500"
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Vehicle Type</label>
                                <select
                                    value={vehicleType}
                                    onChange={(e) => setVehicleType(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                >
                                    <option value="TRUCK">TRUCK (Heavy Payload)</option>
                                    <option value="VAN">VAN (Medium Transit)</option>
                                    <option value="AMBULANCE">AMBULANCE (Medical Transport)</option>
                                    <option value="BOAT">BOAT (Water Rescue)</option>
                                </select>
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Payload Capacity (kg)</label>
                                <input
                                    type="number"
                                    value={capacityWeight}
                                    onChange={(e) => setCapacityWeight(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Volume Capacity (m³)</label>
                                <input
                                    type="number"
                                    value={capacityVolume}
                                    onChange={(e) => setCapacityVolume(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-xs font-semibold text-slate-500">Fuel Level (%)</label>
                                <input
                                    type="number"
                                    value={fuelLevel}
                                    onChange={(e) => setFuelLevel(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                    required
                                />
                            </div>

                            <div className="space-y-1 grid grid-cols-2 gap-2">
                                <div>
                                    <label className="text-xs font-semibold text-slate-500">Latitude</label>
                                    <input
                                        type="number"
                                        step="any"
                                        value={currentLatitude}
                                        onChange={(e) => setCurrentLatitude(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="text-xs font-semibold text-slate-500">Longitude</label>
                                    <input
                                        type="number"
                                        step="any"
                                        value={currentLongitude}
                                        onChange={(e) => setCurrentLongitude(e.target.value)}
                                        className="w-full bg-slate-950 border border-slate-850 rounded-xl py-2 px-3 text-sm text-slate-300 focus:outline-none"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="md:col-span-3 flex justify-end">
                                <button
                                    type="submit"
                                    disabled={createVehicleMutation.isPending}
                                    className="bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-800 text-white font-semibold px-6 py-2.5 rounded-xl text-sm cursor-pointer transition-all"
                                >
                                    {createVehicleMutation.isPending ? 'Registering...' : 'Save Fleet Vehicle'}
                                </button>
                            </div>
                        </form>
                    </div>
                )}

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
