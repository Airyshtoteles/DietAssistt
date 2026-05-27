"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { 
  Activity, Users, Flame, RefreshCw, LogOut, Trash2, Search, Sparkles, PlusCircle, AlertCircle, Droplets
} from "lucide-react";
import { supabase } from "@/lib/supabase";

interface FoodLog {
  id: string;
  user_id: string;
  food_name: string;
  calories: number;
  protein: number;
  carbs: number;
  fats: number;
  image_url: string | null;
  created_at: string;
  users_profile?: {
    name: string;
    email: string;
  };
}

export default function AdminDashboardPage() {
  const router = useRouter();
  const [logs, setLogs] = useState<FoodLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Fungsi mengambil log makanan global
  const fetchGlobalLogs = async () => {
    try {
      setLoading(true);
      const res = await fetch("/api/food");
      if (res.ok) {
        const data = await res.json();
        setLogs(data.logs || []);
      }
    } catch (error) {
      console.error("Gagal memuat log makanan admin:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchGlobalLogs();

    // SINKRONISASI REAL-TIME MENGGUNAKAN SUPABASE REALTIME CHANNEL
    // Mendengarkan insert/delete pada tabel food_logs dan mentrigger refetch secara instan.
    const channel = supabase
      .channel("schema-db-changes")
      .on(
        "postgres_changes",
        {
          event: "*",
          schema: "public",
          table: "food_logs",
        },
        (payload) => {
          console.log("Perubahan real-time terdeteksi:", payload);
          showToast(`Pembaruan database terdeteksi! Mengambil data terbaru...`);
          fetchGlobalLogs();
        }
      )
      .subscribe();

    return () => {
      supabase.removeChannel(channel);
    };
  }, []);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(null);
    }, 4000);
  };

  // Menghapus log makanan
  const handleDeleteLog = async (id: string) => {
    if (!confirm("Apakah Anda yakin ingin menghapus log makanan ini?")) return;
    
    setDeletingId(id);
    try {
      const res = await fetch(`/api/food/${id}`, {
        method: "DELETE",
      });
      if (res.ok) {
        showToast("Log makanan berhasil dihapus dari sistem!");
        // State terupdate otomatis dari subscription Postgres atau filter manual
        setLogs((prev) => prev.filter((log) => log.id !== id));
      } else {
        alert("Gagal menghapus log makanan");
      }
    } catch (error) {
      console.error("Gagal menghapus:", error);
    } finally {
      setDeletingId(null);
    }
  };

  // Logout
  const handleLogout = () => {
    router.push("/");
  };

  // Filter logs berdasarkan input pencarian
  const filteredLogs = logs.filter((log) => {
    const term = searchQuery.toLowerCase();
    const food = log.food_name.toLowerCase();
    const user = log.users_profile?.name?.toLowerCase() || "";
    const email = log.users_profile?.email?.toLowerCase() || "";
    return food.includes(term) || user.includes(term) || email.includes(term);
  });

  // Kalkulasi metrik global
  const totalUsers = new Set(logs.map((l) => l.user_id)).size;
  const totalCalories = logs.reduce((sum, item) => sum + (item.calories || 0), 0);
  const totalMacros = logs.reduce(
    (acc, item) => {
      acc.protein += item.protein || 0;
      acc.carbs += item.carbs || 0;
      acc.fats += item.fats || 0;
      return acc;
    },
    { protein: 0, carbs: 0, fats: 0 }
  );

  return (
    <div className="min-h-screen bg-slate-50">
      
      {/* Top Navbar */}
      <nav className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-slate-100 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-gradient-to-tr from-mint-500 to-emerald-600 rounded-xl text-white">
            <Activity className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-800 tracking-tight">
              Diet<span className="text-mint-600">Assist</span> Portal
            </h1>
            <p className="text-xs text-slate-400 font-medium">Monitoring Real-Time</p>
          </div>
        </div>

        {/* Real-time Indicator & Actions */}
        <div className="flex items-center gap-4">
          <span className="hidden sm:inline-flex items-center gap-2 px-3 py-1.5 bg-emerald-50 text-emerald-700 text-xs font-semibold rounded-full border border-emerald-100">
            <span className="w-2.5 h-2.5 bg-emerald-500 rounded-full animate-ping" />
            <span>Koneksi Real-Time Aktif</span>
          </span>

          <button 
            onClick={fetchGlobalLogs}
            className="p-2 text-slate-500 hover:text-mint-600 hover:bg-slate-50 rounded-xl transition-all"
            title="Refresh Manual"
          >
            <RefreshCw className={`w-5 h-5 ${loading ? "animate-spin" : ""}`} />
          </button>

          <button
            onClick={handleLogout}
            className="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-rose-600 hover:bg-rose-50 rounded-xl transition-all"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden sm:inline">Keluar</span>
          </button>
        </div>
      </nav>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto px-6 py-8 space-y-8">
        
        {/* Floating Toast Notification */}
        {toastMessage && (
          <div className="fixed bottom-6 right-6 z-50 flex items-center gap-2.5 px-4 py-3 bg-slate-900 text-white rounded-xl shadow-xl animate-bounce text-sm max-w-sm">
            <Sparkles className="w-4 h-4 text-mint-400 shrink-0" />
            <span>{toastMessage}</span>
          </div>
        )}

        {/* Hero Section */}
        <div className="bg-gradient-to-r from-slate-900 to-emerald-950 text-white rounded-3xl p-8 relative overflow-hidden shadow-xl shadow-emerald-950/20">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_70%_120%,rgba(16,185,129,0.2),transparent)]" />
          <div className="relative z-10 space-y-2">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 bg-white/10 rounded-full text-xs font-semibold backdrop-blur-md text-mint-300">
              <Sparkles className="w-3.5 h-3.5" />
              Tugas Akhir Mahasiswa
            </div>
            <h2 className="text-3xl font-extrabold tracking-tight">Analisis Konsumsi & Dashboard Monitoring</h2>
            <p className="text-slate-300 max-w-xl text-sm leading-relaxed">
              Selamat datang di pusat pemantauan gizi terpadu DietAssist. Pantau log makanan, asupan air harian, serta hasil ekstraksi AI Gemini 2.5 Flash dari seluruh akun mahasiswa/pengguna.
            </p>
          </div>
        </div>

        {/* Global Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          
          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-5">
            <div className="p-4 bg-blue-50 text-blue-600 rounded-2xl">
              <Users className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Total User</p>
              <h3 className="text-2xl font-extrabold text-slate-800 mt-1">{totalUsers}</h3>
              <p className="text-[10px] text-slate-400 mt-0.5">Akun terdaftar unik</p>
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-5">
            <div className="p-4 bg-emerald-50 text-emerald-600 rounded-2xl">
              <Flame className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Energi Tercatat</p>
              <h3 className="text-2xl font-extrabold text-slate-800 mt-1">
                {totalCalories.toLocaleString("id-ID")} <span className="text-xs font-semibold text-slate-500">kkal</span>
              </h3>
              <p className="text-[10px] text-slate-400 mt-0.5">Akumulasi seluruh log</p>
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-5">
            <div className="p-4 bg-amber-50 text-amber-600 rounded-2xl">
              <Sparkles className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Total Entri Makanan</p>
              <h3 className="text-2xl font-extrabold text-slate-800 mt-1">{logs.length}</h3>
              <p className="text-[10px] text-slate-400 mt-0.5">Analisis Gemini berhasil</p>
            </div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex items-center gap-5">
            <div className="p-4 bg-sky-50 text-sky-600 rounded-2xl">
              <Droplets className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-bold text-slate-400 uppercase tracking-wider">Rasio Makronutrisi</p>
              <div className="text-xs font-bold text-slate-700 mt-1 space-y-0.5">
                <div>P: {Math.round(totalMacros.protein)}g</div>
                <div>K: {Math.round(totalMacros.carbs)}g | L: {Math.round(totalMacros.fats)}g</div>
              </div>
            </div>
          </div>

        </div>

        {/* CRUD log section */}
        <div className="bg-white rounded-3xl border border-slate-100 shadow-sm overflow-hidden">
          
          {/* Header & Search */}
          <div className="p-6 border-b border-slate-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <h3 className="text-lg font-bold text-slate-800">Daftar Aktivitas Log Makanan User</h3>
              <p className="text-xs text-slate-400 font-medium">Lacak riwayat diet dan intervensi CRUD</p>
            </div>

            <div className="relative max-w-sm w-full">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-400">
                <Search className="w-4 h-4" />
              </div>
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Cari makanan atau nama pengguna..."
                className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-mint-500 focus:border-transparent transition-all placeholder:text-slate-400 text-sm font-medium text-slate-800"
              />
            </div>
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            {loading ? (
              <div className="p-12 text-center text-slate-400 flex flex-col items-center justify-center gap-3">
                <div className="w-8 h-8 border-4 border-mint-500 border-t-transparent rounded-full animate-spin" />
                <p className="text-xs font-semibold">Memuat log database real-time...</p>
              </div>
            ) : filteredLogs.length === 0 ? (
              <div className="p-12 text-center text-slate-400 flex flex-col items-center justify-center gap-2">
                <AlertCircle className="w-8 h-8 text-slate-300" />
                <p className="text-sm font-medium">Tidak ada data log makanan yang sesuai.</p>
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-slate-50/50 text-[10px] uppercase font-bold text-slate-500 tracking-wider border-b border-slate-100">
                    <th className="px-6 py-4">Foto</th>
                    <th className="px-6 py-4">Pengguna</th>
                    <th className="px-6 py-4">Nama Makanan</th>
                    <th className="px-6 py-4 text-center">Kalori</th>
                    <th className="px-6 py-4 text-center">P / K / L (g)</th>
                    <th className="px-6 py-4">Tanggal Dicatat</th>
                    <th className="px-6 py-4 text-right">Aksi</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-slate-700 text-sm font-medium">
                  {filteredLogs.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-50/40 transition-colors">
                      <td className="px-6 py-4">
                        {log.image_url ? (
                          <img
                            src={log.image_url}
                            alt={log.food_name}
                            className="w-12 h-12 object-cover rounded-xl shadow-inner border border-slate-100 hover:scale-110 transition-transform duration-200"
                            onError={(e) => {
                              (e.target as HTMLElement).style.display = 'none';
                            }}
                          />
                        ) : (
                          <div className="w-12 h-12 bg-mint-50 border border-mint-100 rounded-xl flex items-center justify-center text-mint-600 font-bold text-[10px]">
                            TEXT
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <div className="font-semibold text-slate-800">
                          {log.users_profile?.name || "User Anonim"}
                        </div>
                        <div className="text-xs text-slate-400">
                          {log.users_profile?.email || log.user_id.slice(0, 8)}
                        </div>
                      </td>
                      <td className="px-6 py-4 font-semibold text-slate-800">{log.food_name}</td>
                      <td className="px-6 py-4 text-center font-extrabold text-emerald-600">
                        {log.calories} kkal
                      </td>
                      <td className="px-6 py-4 text-center text-xs">
                        <div className="flex items-center justify-center gap-1">
                          <span className="px-1.5 py-0.5 bg-rose-50 text-rose-600 rounded">{log.protein}g</span>
                          <span className="px-1.5 py-0.5 bg-amber-50 text-amber-600 rounded">{log.carbs}g</span>
                          <span className="px-1.5 py-0.5 bg-blue-50 text-blue-600 rounded">{log.fats}g</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-xs text-slate-500">
                        {new Date(log.created_at).toLocaleString("id-ID", {
                          day: "numeric",
                          month: "short",
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button
                          disabled={deletingId === log.id}
                          onClick={() => handleDeleteLog(log.id)}
                          className="p-2 text-rose-500 hover:bg-rose-50 rounded-xl transition-all inline-flex items-center justify-center disabled:opacity-50"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div className="p-4 bg-slate-50 border-t border-slate-100 text-center text-xs text-slate-400">
            Total logs termonitor: <span className="font-bold">{filteredLogs.length}</span> entri.
          </div>
        </div>

      </main>
    </div>
  );
}
