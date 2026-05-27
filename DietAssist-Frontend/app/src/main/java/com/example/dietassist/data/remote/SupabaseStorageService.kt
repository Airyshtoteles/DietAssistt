package com.example.dietassist.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object SupabaseStorageService {

    // Konfigurasi Supabase Project (Telah dikonfigurasi otomatis dengan kredensial aktif Anda)
    private const val SUPABASE_URL = "https://bsjsvdfkgtjsdpyuqwhq.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzanN2ZGZrZ3Rqc2RweXVxd2hxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkyNzMxMTQsImV4cCI6MjA5NDg0OTExNH0.jxmF0GJzHE0xoV9nL7-wNUl6igZrHnd7I3Y3nxfwlaA"
    private const val BUCKET_NAME = "food-images"

    private val client = OkHttpClient()

    /**
     * Mengunggah file gambar (byte array) langsung ke Supabase Storage via REST API.
     * Mengembalikan Public URL gambar jika berhasil, atau null jika gagal.
     */
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): String? = withContext(Dispatchers.IO) {
        val uploadUrl = "$SUPABASE_URL/storage/v1/object/$BUCKET_NAME/$fileName"
        
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url(uploadUrl)
            .put(requestBody) // Supabase REST API menggunakan PUT untuk upload file baru
            .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
            .addHeader("apiKey", SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "image/jpeg")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    // Jika sukses mengunggah, kembalikan URL publik dari file tersebut
                    return@withContext "$SUPABASE_URL/storage/v1/object/public/$BUCKET_NAME/$fileName"
                } else {
                    println("Supabase Upload Error: ${response.code} - ${response.body?.string()}")
                    return@withContext null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
