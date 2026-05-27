import { GoogleGenerativeAI, SchemaType } from "@google/generative-ai";

const apiKey = process.env.GEMINI_API_KEY || "AIzaSyD8SmyQtaA9D_9CZ2CP_uXyJlKwC7gtfrs";
const genAI = new GoogleGenerativeAI(apiKey);

export interface AIAnalysisResult {
  food_name: string;
  calories: number;
  protein: number;
  carbs: number;
  fats: number;
  is_nutrition_label: boolean;
  confidence_score: number; // nilai tingkat keyakinan analisis 0.0 - 1.0
}

/**
 * Menganalisis deskripsi makanan (teks) atau gambar (base64) menggunakan Gemini 2.5 Flash API.
 * Mengembalikan objek JSON gizi yang terjamin formatnya berkat responseSchema.
 */
export async function analyzeFoodWithGemini(
  textDescription?: string,
  imageBufferBase64?: string,
  imageMimeType?: string
): Promise<AIAnalysisResult> {
  // Menggunakan model Gemini 2.5 Flash yang sangat cepat dan akurat
  const model = genAI.getGenerativeModel({
    model: "gemini-2.5-flash",
  });

  // Definisikan schema respon JSON untuk menjamin format 100% konsisten tanpa tag markdown
  const responseSchema = {
    type: SchemaType.OBJECT,
    properties: {
      food_name: {
        type: SchemaType.STRING,
        description: "Nama makanan hasil identifikasi visual atau tabel gizi.",
      },
      calories: {
        type: SchemaType.NUMBER,
        description: "Jumlah energi total dalam satuan kilokalori (kkal/kcal).",
      },
      protein: {
        type: SchemaType.NUMBER,
        description: "Kandungan protein dalam satuan gram (g).",
      },
      carbs: {
        type: SchemaType.NUMBER,
        description: "Kandungan karbohidrat dalam satuan gram (g).",
      },
      fats: {
        type: SchemaType.NUMBER,
        description: "Kandungan lemak total dalam satuan gram (g).",
      },
      is_nutrition_label: {
        type: SchemaType.BOOLEAN,
        description: "True jika input gambar merupakan tabel Informasi Nilai Gizi / Nutrition Facts kemasan makanan.",
      },
      confidence_score: {
        type: SchemaType.NUMBER,
        description: "Tingkat akurasi estimasi analisis (0.0 sampai 1.0).",
      },
    },
    required: ["food_name", "calories", "protein", "carbs", "fats", "is_nutrition_label", "confidence_score"],
  };

  const systemInstruction = `
    Anda adalah ahli nutrisi digital profesional dengan kemampuan Computer Vision tingkat lanjut.
    Tugas Anda adalah menganalisis gambar makanan ATAU deskripsi teks makanan yang diberikan oleh pengguna.
    
    ATURAN SPESIFIK PENGENALAN GAMBAR:
    1. Jika gambar yang diunggah berisi tabel "Informasi Nilai Gizi" atau "Nutrition Facts" kemasan makanan:
       - Ekstrak nama produk/kemasan makanan tersebut sebagai 'food_name'.
       - Ekstrak jumlah kalori, protein, karbohidrat, dan lemak sesuai yang tercantum di label. Perhatikan takaran saji (serving size) jika tertera, hitung nilai total gizi untuk satu porsi standar.
       - Set 'is_nutrition_label' menjadi true.
    2. Jika gambar berupa foto makanan/minuman biasa:
       - Lakukan Computer Vision visual untuk menebak nama makanan secara spesifik (misal: "Nasi Uduk dengan Telur Balado").
       - Estimasi kandungan gizi berdasarkan porsi visual standar yang tampak di foto.
       - Set 'is_nutrition_label' menjadi false.
    3. Jika input berupa teks deskripsi makanan (tanpa gambar):
       - Hitung estimasi kalori dan makronutrisi dari nama makanan tersebut.
       - Set 'is_nutrition_label' menjadi false.
       
    Kembalikan hasil analisis hanya dalam struktur JSON terstruktur sesuai responseSchema yang didefinisikan.
  `;

  const parts: any[] = [];

  // Jika ada gambar base64
  if (imageBufferBase64 && imageMimeType) {
    parts.push({
      inlineData: {
        data: imageBufferBase64,
        mimeType: imageMimeType,
      },
    });
  }

  // Masukkan teks prompt
  const userPrompt = textDescription || "MOHON ANALISIS NUTRISI PADA GAMBAR BERIKUT.";
  parts.push({ text: userPrompt });

  const contents = [{ role: 'user', parts }];

  try {
    const response = await model.generateContent({
      contents,
      generationConfig: {
        responseMimeType: "application/json",
        responseSchema: responseSchema,
        temperature: 0.1, // Suhu rendah agar hasil estimasi lebih deterministik dan konsisten
      },
      systemInstruction: systemInstruction,
    });

    const resultText = response.response.text();
    if (!resultText) {
      throw new Error("Respon kosong dari Gemini API");
    }

    return JSON.parse(resultText) as AIAnalysisResult;
  } catch (error) {
    console.error("Gagal melakukan analisis Gemini:", error);
    // Fallback jika API bermasalah atau format tidak sesuai
    return {
      food_name: textDescription || "Analisis Gagal",
      calories: 0,
      protein: 0,
      carbs: 0,
      fats: 0,
      is_nutrition_label: false,
      confidence_score: 0.0,
    };
  }
}
