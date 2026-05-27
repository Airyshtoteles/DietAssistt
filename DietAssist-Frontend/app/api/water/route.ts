import { NextResponse } from 'next/server';
import { supabase } from '@/lib/supabase';

// GET: Mendapatkan log air minum pengguna
export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const userId = searchParams.get('userId');
    const dateStr = searchParams.get('date'); // Format: YYYY-MM-DD

    if (!userId) {
      return NextResponse.json(
        { error: 'userId wajib disediakan' },
        { status: 400 }
      );
    }

    let query = supabase
      .from('water_logs')
      .select('*')
      .eq('user_id', userId)
      .order('created_at', { ascending: false });

    if (dateStr) {
      const startOfDay = `${dateStr}T00:00:00.000Z`;
      const endOfDay = `${dateStr}T23:59:59.999Z`;
      query = query.gte('created_at', startOfDay).lte('created_at', endOfDay);
    }

    const { data, error } = await query;
    if (error) throw error;

    // Hitung total air hari ini
    const totalMl = (data || []).reduce((sum, item) => sum + (item.amount_ml || 0), 0);

    return NextResponse.json({
      success: true,
      logs: data || [],
      total_ml: totalMl
    });
  } catch (error: any) {
    console.error('Gagal mengambil log air:', error);
    return NextResponse.json(
      { error: 'Gagal mengambil log air', details: error.message },
      { status: 500 }
    );
  }
}

// POST: Mencatat konsumsi air minum baru
export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { user_id, amount_ml } = body;

    if (!user_id || !amount_ml) {
      return NextResponse.json(
        { error: 'user_id dan amount_ml wajib diisi' },
        { status: 400 }
      );
    }

    const { data, error } = await supabase
      .from('water_logs')
      .insert({
        user_id,
        amount_ml: parseInt(amount_ml),
        created_at: new Date().toISOString()
      })
      .select()
      .single();

    if (error) throw error;

    return NextResponse.json({
      success: true,
      message: 'Log air minum berhasil disimpan',
      log: data
    });
  } catch (error: any) {
    console.error('Gagal menyimpan log air:', error);
    return NextResponse.json(
      { error: 'Gagal menyimpan log air', details: error.message },
      { status: 500 }
    );
  }
}
