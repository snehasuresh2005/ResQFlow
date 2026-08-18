'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function RootPage() {
    const router = useRouter();

    useEffect(() => {
        const token = localStorage.getItem('resqflow_token');
        if (token) {
            router.replace('/dashboard');
        } else {
            router.replace('/login');
        }
    }, [router]);

    return (
        <div className="flex h-screen bg-slate-950 items-center justify-center">
            <div className="text-slate-400 text-sm animate-pulse">Initializing ResQFlow Control Center...</div>
        </div>
    );
}
