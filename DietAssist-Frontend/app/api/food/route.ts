import { NextResponse } from 'next/server';
import { supabase } from '@/lib/supabase';

// GET: Mendapatkan log makanan user (bisa difilter berdasarkan userId dan tanggal)
export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const userId = searchParams.get('userId');
    const dateStr = searchParams.get('date'); // Format: YYYY-MM-DD

    if (!userId) {
      // Jika tidak ada userId, kembalikan semua data makanan (untuk kebutuhan Dashboard Admin global)
      const { data, error } = await supabase
        .from('food_logs')
        .select(`
          *,
          users_profile (name, email)
        `)
        .order('created_at', { ascending: false });

      if (error) throw error;
      return NextResponse.json({ success: true, logs: data || [] });
    }

    let query = supabase
      .from('food_logs')
      .select('*')
      .eq('user_id', userId)
      .order('created_at', { ascending: false });

    // Jika difilter berdasarkan tanggal spesifik hari ini
    if (dateStr) {
      const startOfDay = `${dateStr}T00:00:00.000Z`;
      const endOfDay = `${dateStr}T23:59:59.999Z`;
      query = query.gte('created_at', startOfDay).lte('created_at', endOfDay);
    }

    const { data, error } = await query;
    if (error) throw error;

    return NextResponse.json({
      success: true,
      logs: data || []
    });
  } catch (error: any) {
    console.error('Gagal mengambil data food logs:', error);
    return NextResponse.json(
      { error: 'Gagal mengambil data food logs', details: error.message },
      { status: 500 }
    );
  }
}

// POST: Membuat log makanan baru dari Android
export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { user_id, food_name, calories, protein, carbs, fats, image_url, image } = body;

    if (!user_id || !food_name) {
      return NextResponse.json(
        { error: 'user_id dan food_name wajib diisi' },
        { status: 400 }
      );
    }

    let uploadedImageUrl = image_url || null;

    if (image) {
      try {
        const buffer = Buffer.from(image, 'base64');
        const fileName = `food_${user_id}_${Date.now()}.jpg`;

        const { data: uploadData, error: uploadError } = await supabase
          .storage
          .from('food-images')
          .upload(fileName, buffer, {
            contentType: 'image/jpeg',
            upsert: true
          });

        if (uploadError) {
          console.error('Gagal upload ke Supabase Storage:', uploadError);
        } else {
          const { data: publicUrlData } = supabase
            .storage
            .from('food-images')
            .getPublicUrl(fileName);

          uploadedImageUrl = publicUrlData.publicUrl;
        }
      } catch (uploadFail) {
        console.error('Error processing base64 image upload:', uploadFail);
      }
    }

    const { data, error } = await supabase
      .from('food_logs')
      .insert({
        user_id,
        food_name,
        calories: parseFloat(calories || 0),
        protein: parseFloat(protein || 0),
        carbs: parseFloat(carbs || 0),
        fats: parseFloat(fats || 0),
        image_url: uploadedImageUrl,
        created_at: new Date().toISOString()
      })
      .select()
      .single();

    if (error) throw error;

    return NextResponse.json({
      success: true,
      message: 'Log makanan berhasil disimpan',
      log: data
    });
  } catch (error: any) {
    console.error('Gagal menyimpan food log:', error);
    return NextResponse.json(
      { error: 'Gagal menyimpan food log', details: error.message },
      { status: 500 }
    );
  }
}
