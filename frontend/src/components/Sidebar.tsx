'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { 
    LayoutDashboard, 
    FileText, 
    Layers, 
    Truck, 
    Compass, 
    Map as MapIcon, 
    Play, 
    BarChart3, 
    LogOut,
    HeartHandshake
} from 'lucide-react';
import React, { useEffect, useState } from 'react';

export default function Sidebar() {
    const pathname = usePathname();
    const router = useRouter();
    const [userName, setUserName] = useState('');
    const [userRole, setUserRole] = useState('');

    useEffect(() => {
        const name = localStorage.getItem('resqflow_name') || 'Coordinator';
        const role = localStorage.getItem('resqflow_role') || 'COORDINATOR';
        setUserName(name);
        setUserRole(role);
    }, []);

    const handleLogout = () => {
        localStorage.clear();
        router.push('/login');
    };

    const links = [
        { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
        { href: '/requests', label: 'Requests', icon: FileText },
        { href: '/resources', label: 'Resources', icon: Layers },
        { href: '/vehicles', label: 'Vehicles', icon: Truck },
        { href: '/missions', label: 'Missions', icon: Compass },
        { href: '/map', label: 'Operations Map', icon: MapIcon },
        { href: '/simulation', label: 'Simulations', icon: Play },
        { href: '/analytics', label: 'Analytics', icon: BarChart3 }
    ];

    return (
        <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col h-screen sticky top-0">
            {/* Logo */}
            <div className="p-6 border-b border-slate-800 flex items-center gap-3">
                <HeartHandshake className="h-8 w-8 text-rose-500 animate-pulse" />
                <div>
                    <h1 className="text-xl font-bold tracking-wider text-slate-100">RESQFLOW</h1>
                    <p className="text-xs text-rose-400 font-medium">Relief Logistics</p>
                </div>
            </div>

            {/* Links */}
            <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
                {links.map((link) => {
                    const Icon = link.icon;
                    const isActive = pathname === link.href;
                    return (
                        <Link
                            key={link.href}
                            href={link.href}
                            className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all ${
                                isActive 
                                    ? 'bg-rose-650 text-white shadow-md shadow-rose-900/30' 
                                    : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                            }`}
                        >
                            <Icon className="h-5 w-5" />
                            {link.label}
                        </Link>
                    );
                })}
            </nav>

            {/* User Profile & Logout */}
            <div className="p-4 border-t border-slate-800 space-y-3">
                <div className="px-4 py-2 bg-slate-800/50 rounded-lg">
                    <p className="text-sm font-semibold text-slate-200 truncate">{userName}</p>
                    <p className="text-xs text-slate-400 font-medium">{userRole}</p>
                </div>
                <button
                    onClick={handleLogout}
                    className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium text-rose-400 hover:bg-rose-950/30 transition-all cursor-pointer"
                >
                    <LogOut className="h-5 w-5" />
                    Sign Out
                </button>
            </div>
        </aside>
    );
}
