import { NextResponse } from 'next/server';
import { GoogleGenerativeAI } from '@google/generative-ai';

const apiKey = process.env.GEMINI_API_KEY || "";
const genAI = new GoogleGenerativeAI(apiKey);

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const { messages } = body; // messages format: [{ role: 'user' | 'model', content: string }]

    if (!messages || !Array.isArray(messages)) {
      return NextResponse.json(
        { error: 'Format pesan tidak valid atau kosong' },
        { status: 400 }
      );
    }

    // Menggunakan model Gemini 2.5 Flash yang cepat dan responsif untuk teks
    const model = genAI.getGenerativeModel({
      model: 'gemini-2.5-flash',
    });

    const systemInstruction = `
      Anda adalah DietAssistAi, konsultan kesehatan, diet, dan nutrisi digital profesional.
      Tugas Anda adalah membantu pengguna menjawab pertanyaan mereka dengan ramah, informatif, dan berbasis sains.
      Fokus bahasan Anda meliputi:
      1. Pola makan/diet sehat (misal: diet keto, kalori defisit, diet diabetes, dll).
      2. Informasi gizi bahan makanan, porsi, dan rekomendasi menu.
      3. Tips hidrasi (kebutuhan minum air harian).
      4. Tips olahraga ringan dan kebiasaan sehat lainnya.
      
      Aturan Penting:
      - Jawab dengan bahasa Indonesia yang santun, bersahabat, dan mudah dimengerti.
      - Gunakan format teks terstruktur dengan poin-poin tebal (bold) jika memberikan daftar agar mudah dibaca.
      - Ingatkan pengguna secara halus untuk berkonsultasi dengan dokter atau ahli gizi medis jika pertanyaan berkaitan dengan kondisi penyakit kronis yang kritis.
      - Singkat, padat, dan langsung menjawab inti pertanyaan pengguna.
    `;

    // Bentuk riwayat percakapan untuk Gemini SDK
    // Gemini mengharapkan format: contents: [{ role: 'user' | 'model', parts: [{ text: string }] }]
    const contents = messages.map((msg: any) => {
      // Petakan role: 'assistant' -> 'model', 'user' -> 'user'
      const role = msg.role === 'assistant' || msg.role === 'model' ? 'model' : 'user';
      return {
        role: role,
        parts: [{ text: msg.content || msg.text || '' }]
      };
    });

    const response = await model.generateContent({
      contents: contents,
      systemInstruction: systemInstruction,
      generationConfig: {
        temperature: 0.7,
        maxOutputTokens: 800,
      }
    });

    const replyText = response.response.text();

    return NextResponse.json({
      success: true,
      reply: replyText || 'Maaf, saya tidak dapat memproses jawaban saat ini.'
    });
  } catch (error: any) {
    console.error('Error saat berkomunikasi dengan DietAssistAi:', error);
    return NextResponse.json(
      { error: 'Gagal mendapatkan respon dari DietAssistAi', details: error.message },
      { status: 500 }
    );
  }
}
