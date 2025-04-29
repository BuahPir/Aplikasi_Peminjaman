package com.mobile.aplikasipeminjaman

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.time.LocalDate

object PeminjamanService {

    fun pinjamBuku(userId: String, bukuId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val json = """
        {
            "user_id": "$userId",
            "informasi_buku_id": "$bukuId",
            "tanggal_pinjam": "${LocalDate.now()}",
            "status": "dipinjam"
        }
    """.trimIndent()

        val request = Request.Builder()
            .url("https://admin-perpus-main-lfowxm.laravel.cloud/api/peminjaman")
            .post(json.toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PeminjamanService", "Gagal: ${e.message}")
                onError(e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { // ✅ AUTO close response body
                    if (it.isSuccessful) {
                        onSuccess()
                    } else {
                        val errorBody = it.body?.string()
                        Log.e("PeminjamanService", "Server error: $errorBody")
                        onError(errorBody ?: "Error: ${it.code}")
                    }
                }
            }
        })
    }
}