'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Sidebar from '@/components/Sidebar';
import { useQuery } from '@tanstack/react-query';
import dynamic from 'next/dynamic';
import { ShieldAlert } from 'lucide-react';

// Import Leaflet dynamically to disable Server-Side Rendering (SSR)
const MapView = dynamic(() => import('@/components/MapView'), {
    ssr: false,
    loading: () => <div className="w-full h-[550px] bg-slate-900 border border-slate-800 rounded-2xl flex items-center justify-center text-slate-500 animate-pulse">Initializing map grid...</div>
});

export default function MapPage() {
    const router = useRouter();
    const [token, setToken] = useState<string | null>(null);
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
        setMounted(true);
        const storedToken = localStorage.getItem('resqflow_token');
        if (!storedToken) {
            router.replace('/login');
        } else {
            setToken(storedToken);
        }
    }, [router]);

    const apiBase = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

    // Fetch locations data (depots, shelters, zones, roads)
    const { data: locations, isLoading: locsLoading, isError: locsError } = useQuery({
        queryKey: ['locations'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/locations`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            if (!res.ok) throw new Error('Failed to load locations');
            return res.json();
        },
        enabled: !!token
    });

    // Fetch active missions (for route polyline overlays)
    const { data: missions, isLoading: missLoading } = useQuery({
        queryKey: ['missions'],
        queryFn: async () => {
            const res = await fetch(`${apiBase}/missions`, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('resqflow_token')}` }
            });
            if (!res.ok) throw new Error('Failed to load missions');
            return res.json();
        },
        enabled: !!token,
        refetchInterval: 5000 // Poll active vehicle positions every 5s
    });

    if (!token || locsLoading || missLoading) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center">
                <div className="text-slate-400 text-sm animate-pulse">Loading Operations Map...</div>
            </div>
        );
    }

    if (locsError) {
        return (
            <div className="flex h-screen bg-slate-950 items-center justify-center p-4">
                <div className="bg-rose-950/40 border border-rose-900/50 rounded-xl p-4 flex gap-2 items-center text-rose-300">
                    <ShieldAlert className="h-5 w-5" />
                    <span>Failed to retrieve geo coordinates from the server.</span>
                </div>
            </div>
        );
    }

    return (
        <div className="flex bg-slate-950 min-h-screen">
            <Sidebar />

            <main className="flex-1 p-8 space-y-6 flex flex-col h-screen overflow-hidden">
                {/* Header */}
                <div className="flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight text-white">Live Operations Map</h2>
                        <p className="text-sm text-slate-400">Geographical view of depots, shelters, zones, and vehicles in transit</p>
                    </div>
                    {/* Legend */}
                    <div className="flex gap-4 text-xs font-semibold bg-slate-900 border border-slate-800 p-3 rounded-xl flex-wrap items-center">
                        <div className="flex items-center gap-1.5">
                            <span className="h-4 w-4 rounded-full bg-sky-600 flex items-center justify-center text-[9px] text-white font-bold">D</span>
                            <span className="text-slate-400">Depot (D)</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <span className="h-4 w-4 rounded-full bg-emerald-600 flex items-center justify-center text-[9px] text-white font-bold">S</span>
                            <span className="text-slate-400">Shelter (S)</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <span className="h-4 w-4 rounded-full bg-rose-600 flex items-center justify-center text-[9px] text-white font-bold">Z</span>
                            <span className="text-slate-400">Emergency Zone (Z)</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <span className="h-4 w-4 rounded-full bg-slate-700 flex items-center justify-center text-[9px] text-white font-bold">V</span>
                            <span className="text-slate-400">Vehicle (V)</span>
                        </div>
                        <div className="h-4 w-px bg-slate-800 hidden md:block"></div>
                        <div className="flex items-center gap-1.5">
                            <span className="h-0.5 w-6 bg-amber-500 inline-block"></span>
                            <span className="text-slate-400">Active Transit Route</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                            <span className="h-0.5 w-6 border-t-2 border-dashed border-red-500 inline-block"></span>
                            <span className="text-slate-400">Rerouting (Blocked Segment)</span>
                        </div>
                    </div>
                </div>

                {/* Map Container wrapper */}
                <div className="flex-1 w-full bg-slate-900 border border-slate-800 rounded-2xl p-4 shadow-xl min-h-0 flex flex-col">
                    {mounted && (
                        <MapView 
                            depots={locations?.depots || []} 
                            shelters={locations?.shelters || []} 
                            zones={locations?.zones || []} 
                            missions={missions || []} 
                        />
                    )}
                </div>
            </main>
        </div>
    );
}
