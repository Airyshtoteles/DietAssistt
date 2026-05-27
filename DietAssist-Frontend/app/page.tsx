"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { ShieldCheck, Activity, Key, Mail, Lock } from "lucide-react";

export default function AdminLoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    // Simulasi Login Admin sederhana
    setTimeout(() => {
      if (email === "admin@dietassist.com" && password === "admin123") {
        setLoading(false);
        router.push("/dashboard");
      } else {
        setLoading(false);
        setError("Email atau Password Admin salah!");
      }
    }, 1200);
  };

  return (
    <div className="flex min-h-screen items-center justify-center p-4 relative overflow-hidden bg-mesh">
      {/* Decorative Blur Spheres */}
      <div className="absolute top-1/4 left-1/4 w-72 h-72 bg-mint-200 rounded-full mix-blend-multiply filter blur-2xl opacity-60 animate-pulse" />
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-emerald-200 rounded-full mix-blend-multiply filter blur-3xl opacity-40 animate-pulse delay-1000" />

      {/* Login Card */}
      <div className="w-full max-w-md p-8 rounded-3xl shadow-2xl glass relative z-10 border border-white/40 transition-all duration-300 hover:shadow-mint-100/50">
        
        {/* Brand Header */}
        <div className="flex flex-col items-center mb-8">
          <div className="p-4 bg-gradient-to-tr from-mint-500 to-emerald-600 rounded-2xl text-white shadow-lg shadow-mint-500/30 transform transition-transform hover:scale-105 duration-300">
            <Activity className="w-8 h-8" />
          </div>
          <h1 className="mt-4 text-3xl font-extrabold tracking-tight text-slate-800 font-sans">
            Diet<span className="text-mint-600">Assist</span>
          </h1>
          <p className="mt-2 text-sm text-slate-500 font-medium">
            Portal Monitoring & Dashboard Admin
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleLogin} className="space-y-6">
          {error && (
            <div className="p-4 bg-rose-50 border border-rose-100 text-rose-600 text-xs rounded-xl flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 shrink-0 text-rose-500" />
              <span>{error}</span>
            </div>
          )}

          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-wider text-slate-600">
              Email Admin
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Mail className="w-4 h-4" />
              </div>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="admin@dietassist.com"
                className="w-full pl-10 pr-4 py-3 bg-white/70 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-mint-500 focus:border-transparent transition-all placeholder:text-slate-400 text-sm font-medium text-slate-800"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-xs font-bold uppercase tracking-wider text-slate-600">
              Password
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Lock className="w-4 h-4" />
              </div>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-3 bg-white/70 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-mint-500 focus:border-transparent transition-all placeholder:text-slate-400 text-sm font-medium text-slate-800"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 px-4 bg-gradient-to-r from-mint-500 to-emerald-600 text-white font-semibold rounded-xl hover:from-mint-600 hover:to-emerald-700 focus:outline-none focus:ring-2 focus:ring-mint-500 focus:ring-offset-2 transition-all duration-300 shadow-lg shadow-mint-500/20 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <>
                <Key className="w-4 h-4" />
                <span>Masuk ke Dashboard</span>
              </>
            )}
          </button>
        </form>

        <div className="mt-8 text-center text-xs text-slate-400">
          Tip: Gunakan email <code className="bg-slate-100 px-1 py-0.5 rounded font-mono text-slate-600">admin@dietassist.com</code> & pass <code className="bg-slate-100 px-1 py-0.5 rounded font-mono text-slate-600">admin123</code> untuk masuk.
        </div>
      </div>
    </div>
  );
}
