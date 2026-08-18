'use client';

import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix Leaflet marker icon asset mapping issue
const fixLeafletIcons = () => {
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
        iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
        iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    });
};

interface MapViewProps {
    depots: any[];
    shelters: any[];
    zones: any[];
    missions: any[];
}

export default function MapView({ depots = [], shelters = [], zones = [], missions = [] }: MapViewProps) {
    
    useEffect(() => {
        fixLeafletIcons();
    }, []);

    // Helper to build div icon for customized marker styles
    const getCustomIcon = (colorClass: string, symbol: string) => {
        return L.divIcon({
            html: `<div class="w-7 h-7 rounded-full ${colorClass} flex items-center justify-center border-2 border-slate-900 text-white font-bold text-xs shadow-lg shadow-black/40">${symbol}</div>`,
            className: 'custom-leaflet-icon',
            iconSize: [28, 28],
            iconAnchor: [14, 14]
        });
    };

    return (
        <MapContainer 
            center={[12.9716, 77.5946]} 
            zoom={12} 
            className="w-full h-full rounded-2xl border border-slate-800"
            style={{ height: '100%', width: '100%' }}
        >
            <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {/* Depots (Blue markers, D) */}
            {depots.map((d) => (
                <Marker 
                    key={`depot-${d.id}`} 
                    position={[d.latitude, d.longitude]} 
                    icon={getCustomIcon('bg-sky-600', 'D')}
                >
                    <Popup>
                        <div className="text-slate-900 font-sans p-1">
                            <h5 className="font-bold text-sm">{d.name}</h5>
                            <p className="text-xs text-slate-500 mt-1">Latitude: {d.latitude.toFixed(4)}</p>
                            <p className="text-xs text-slate-500">Longitude: {d.longitude.toFixed(4)}</p>
                            <p className="text-xs text-slate-500 font-semibold mt-1">Capacity: {d.capacity}m³</p>
                        </div>
                    </Popup>
                </Marker>
            ))}

            {/* Shelters (Emerald markers, S) */}
            {shelters.map((s) => (
                <Marker 
                    key={`shelter-${s.id}`} 
                    position={[s.latitude, s.longitude]} 
                    icon={getCustomIcon('bg-emerald-600', 'S')}
                >
                    <Popup>
                        <div className="text-slate-900 font-sans p-1">
                            <h5 className="font-bold text-sm">{s.name}</h5>
                            <p className="text-xs text-slate-500 mt-1">Occupancy: {s.currentOccupancy} / {s.capacity} persons</p>
                        </div>
                    </Popup>
                </Marker>
            ))}

            {/* Emergency Zones (Red markers, Z) */}
            {zones.map((z) => (
                <Marker 
                    key={`zone-${z.id}`} 
                    position={[z.latitude, z.longitude]} 
                    icon={getCustomIcon(z.severity === 'CRITICAL' ? 'bg-rose-600' : 'bg-amber-600', 'Z')}
                >
                    <Popup>
                        <div className="text-slate-900 font-sans p-1">
                            <h5 className="font-bold text-sm">{z.name}</h5>
                            <span className={`text-[10px] px-1.5 py-0.5 rounded font-bold ${
                                z.severity === 'CRITICAL' ? 'bg-rose-100 text-rose-700' : 'bg-amber-100 text-amber-700'
                            }`}>{z.severity}</span>
                            <p className="text-xs text-slate-600 mt-1.5">Affected Population: {z.populationAffected}</p>
                        </div>
                    </Popup>
                </Marker>
            ))}

            {/* Active Mission Routes (Polylines and Vehicle markers) */}
            {missions.map((m) => {
                if (!m.route || !m.route.routeNodes || m.route.routeNodes.length < 2) return null;

                const positions: [number, number][] = m.route.routeNodes.map((n: any) => {
                    // Node is depot, zone or shelter. Find the matching coordinates.
                    if (n.nodeType === 'DEPOT') {
                        const depot = depots.find(d => d.id == n.nodeId);
                        return depot ? [depot.latitude, depot.longitude] : null;
                    } else if (n.nodeType === 'ZONE') {
                        const zone = zones.find(z => z.id == n.nodeId);
                        return zone ? [zone.latitude, zone.longitude] : null;
                    } else if (n.nodeType === 'SHELTER') {
                        const shelter = shelters.find(s => s.id == n.nodeId);
                        return shelter ? [shelter.latitude, shelter.longitude] : null;
                    }
                    return null;
                }).filter((p: any) => p !== null) as [number, number][];

                if (positions.length < 2) return null;

                const color = m.status === 'BLOCKED' ? '#ef4444' : (m.status === 'IN_TRANSIT' ? '#f59e0b' : '#3b82f6');
                
                return (
                    <React.Fragment key={`mission-route-${m.id}`}>
                        <Polyline 
                            positions={positions} 
                            color={color} 
                            weight={3} 
                            dashArray={m.status === 'REROUTING' ? '5, 5' : undefined} 
                        />
                        {/* Draw Vehicle Current Position if transit */}
                        {m.vehicle && (
                            <Marker 
                                position={[m.vehicle.currentLatitude, m.vehicle.currentLongitude]}
                                icon={getCustomIcon('bg-slate-700', 'V')}
                            >
                                <Popup>
                                    <div className="text-slate-900 font-sans p-1">
                                        <h5 className="font-bold text-sm">Vehicle: {m.vehicle.registrationNumber}</h5>
                                        <p className="text-xs text-slate-500">Status: {m.status}</p>
                                        <p className="text-xs text-slate-500">Driver: {m.driver?.name}</p>
                                        <p className="text-xs text-slate-500">Fuel: {m.vehicle.fuelLevel}%</p>
                                    </div>
                                </Popup>
                            </Marker>
                        )}
                    </React.Fragment>
                );
            })}
        </MapContainer>
    );
}
