import { NextResponse } from 'next/server';
import { supabase } from '@/lib/supabase';

// PUT: Memperbarui log makanan spesifik berdasarkan ID
export async function PUT(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const id = params.id;
    const body = await request.json();
    const { food_name, calories, protein, carbs, fats } = body;

    const { data, error } = await supabase
      .from('food_logs')
      .update({
        food_name,
        calories: parseFloat(calories || 0),
        protein: parseFloat(protein || 0),
        carbs: parseFloat(carbs || 0),
        fats: parseFloat(fats || 0)
      })
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;

    return NextResponse.json({
      success: true,
      message: 'Log makanan berhasil diperbarui',
      log: data
    });
  } catch (error: any) {
    console.error('Gagal memperbarui food log:', error);
    return NextResponse.json(
      { error: 'Gagal memperbarui food log', details: error.message },
      { status: 500 }
    );
  }
}

// DELETE: Menghapus log makanan berdasarkan ID
export async function DELETE(
  request: Request,
  { params }: { params: { id: string } }
) {
  try {
    const id = params.id;

    const { data, error } = await supabase
      .from('food_logs')
      .delete()
      .eq('id', id)
      .select()
      .single();

    if (error) throw error;

    return NextResponse.json({
      success: true,
      message: 'Log makanan berhasil dihapus',
      log: data
    });
  } catch (error: any) {
    console.error('Gagal menghapus food log:', error);
    return NextResponse.json(
      { error: 'Gagal menghapus food log', details: error.message },
      { status: 500 }
    );
  }
}
