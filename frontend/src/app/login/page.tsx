'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { HeartHandshake, ShieldAlert, KeyRound, Mail, ArrowRight } from 'lucide-react';

export default function LoginPage() {
    const router = useRouter();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const apiBase = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
            const res = await fetch(`${apiBase}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });

            if (!res.ok) {
                throw new Error('Authentication failed. Please verify your credentials.');
            }

            const data = await res.json();
            localStorage.setItem('resqflow_token', data.token);
            localStorage.setItem('resqflow_id', data.id.toString());
            localStorage.setItem('resqflow_name', data.name);
            localStorage.setItem('resqflow_email', data.email);
            localStorage.setItem('resqflow_role', data.role);

            router.push('/dashboard');
        } catch (err: any) {
            setError(err.message || 'Server connection error.');
        } finally {
            setLoading(false);
        }
    };

    const fillCredentials = (role: 'admin' | 'coordinator' | 'driver' | 'volunteer' | 'viewer') => {
        if (role === 'admin') {
            setEmail('admin@resqflow.com');
        } else if (role === 'coordinator') {
            setEmail('coordinator@resqflow.com');
        } else if (role === 'driver') {
            setEmail('driver@resqflow.com');
        } else if (role === 'volunteer') {
            setEmail('volunteer@resqflow.com');
        } else {
            setEmail('viewer@resqflow.com');
        }
        setPassword('password');
    };

    return (
        <div className="flex min-h-screen bg-slate-950 items-center justify-center p-4">
            <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden p-8 space-y-6">
                
                {/* Header */}
                <div className="flex flex-col items-center text-center space-y-2">
                    <div className="bg-rose-500/10 p-3 rounded-full border border-rose-500/20">
                        <HeartHandshake className="h-10 w-10 text-rose-500 animate-pulse" />
                    </div>
                    <h1 className="text-2xl font-bold tracking-tight text-white">RESQFLOW Portal</h1>
                    <p className="text-sm text-slate-400">Emergency Operations Control Center</p>
                </div>

                {/* Error Banner */}
                {error && (
                    <div className="bg-rose-950/40 border border-rose-900/50 rounded-xl p-3 flex gap-2 items-center text-sm text-rose-300">
                        <ShieldAlert className="h-5 w-5 shrink-0" />
                        <span>{error}</span>
                    </div>
                )}

                {/* Form */}
                <form onSubmit={handleLogin} className="space-y-4">
                    <div className="space-y-1">
                        <label className="text-xs font-semibold text-slate-300">Operations Email</label>
                        <div className="relative">
                            <Mail className="absolute left-3 top-3 h-5 w-5 text-slate-500" />
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="name@resqflow.com"
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-rose-500"
                                required
                            />
                        </div>
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-semibold text-slate-300">Password</label>
                        <div className="relative">
                            <KeyRound className="absolute left-3 top-3 h-5 w-5 text-slate-500" />
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 pl-10 pr-4 text-sm text-slate-200 placeholder-slate-600 focus:outline-none focus:border-rose-500"
                                required
                            />
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-rose-600 hover:bg-rose-700 disabled:bg-rose-800 py-3 rounded-xl font-semibold text-sm text-white flex items-center justify-center gap-2 cursor-pointer transition-all shadow-lg shadow-rose-950/20"
                    >
                        {loading ? 'Authenticating...' : 'Enter Operations'}
                        <ArrowRight className="h-5 w-5" />
                    </button>
                </form>

                {/* Fast Seeding Helpers */}
                <div className="pt-4 border-t border-slate-800 space-y-3">
                    <p className="text-xs font-semibold text-center text-slate-500">Demo Fast Access (Password: password)</p>
                    <div className="grid grid-cols-3 gap-2">
                        <button
                            onClick={() => fillCredentials('admin')}
                            type="button"
                            className="bg-slate-800/40 hover:bg-slate-800 border border-slate-800 rounded-xl py-2 text-xs font-medium text-slate-300 transition-all cursor-pointer"
                        >
                            ADMIN
                        </button>
                        <button
                            onClick={() => fillCredentials('coordinator')}
                            type="button"
                            className="bg-slate-800/40 hover:bg-slate-800 border border-slate-800 rounded-xl py-2 text-xs font-medium text-slate-300 transition-all cursor-pointer"
                        >
                            COORD
                        </button>
                        <button
                            onClick={() => fillCredentials('driver')}
                            type="button"
                            className="bg-slate-800/40 hover:bg-slate-800 border border-slate-800 rounded-xl py-2 text-xs font-medium text-slate-300 transition-all cursor-pointer"
                        >
                            DRIVER
                        </button>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                        <button
                            onClick={() => fillCredentials('volunteer')}
                            type="button"
                            className="bg-slate-800/40 hover:bg-slate-800 border border-slate-800 rounded-xl py-2 text-xs font-medium text-slate-300 transition-all cursor-pointer"
                        >
                            VOLUNTEER
                        </button>
                        <button
                            onClick={() => fillCredentials('viewer')}
                            type="button"
                            className="bg-slate-800/40 hover:bg-slate-800 border border-slate-800 rounded-xl py-2 text-xs font-medium text-slate-300 transition-all cursor-pointer"
                        >
                            VIEWER
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
