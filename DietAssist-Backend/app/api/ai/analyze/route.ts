import { NextResponse } from 'next/server';
import { analyzeFoodWithGemini } from '@/lib/gemini';

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { textDescription, image, mimeType } = body;

    // Pastikan setidaknya ada teks atau gambar
    if (!textDescription && !image) {
      return NextResponse.json(
        { error: 'Deskripsi teks atau foto makanan wajib disediakan' },
        { status: 400 }
      );
    }

    // Eksekusi pemanggilan Gemini API helper
    const analysis = await analyzeFoodWithGemini(
      textDescription,
      image, // base64 string
      mimeType || 'image/jpeg'
    );

    return NextResponse.json({
      success: true,
      analysis: analysis
    });
  } catch (error: any) {
    console.error('Error saat analisis gizi via Gemini API:', error);
    return NextResponse.json(
      { error: 'Gagal menganalisis makanan dengan Gemini AI', details: error.message },
      { status: 500 }
    );
  }
}
