package com.mobile.aplikasipeminjaman

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc // <-- Import rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable



private val supabaseUrl = "https://amcscovnmpcwypzwailr.supabase.co"  // Removed trailing slash
private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFtY3Njb3ZubXBjd3lwendhaWxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUwNzIxOTYsImV4cCI6MjA2MDY0ODE5Nn0._EBHeEOYXXoi9j6i_utzMR8T042nsOes7jv4sMgPG9Q"

// Initialize Supabase client
private val supabase = createSupabaseClient(
    supabaseUrl = supabaseUrl,
    supabaseKey = anonKey
) {
    install(Postgrest)
    // Add debug log
    Log.d("RegisterActivity", "Initializing Supabase client with URL: $supabaseUrl")
}
object BookFetchingService {


    /**
     * Mengambil daftar buku yang statusnya 'tersedia'.
     * @param onResult Callback dengan hasil List<InformasiBuku> atau list kosong jika error.
     */
    suspend fun fetchBukuTersedia(onResult: (List<InformasiBuku>) -> Unit) {
        Log.d("BookFetchingService", "Memulai fetch buku tersedia...")
        withContext(Dispatchers.IO) { // Jalankan di IO thread
            try {
                val result = supabase.from("informasi_buku").select {
                    filter { eq("status", "tersedia") }
                }.decodeList<InformasiBuku>()
                Log.d("BookFetchingService", "Berhasil fetch ${result.size} buku tersedia.")
                onResult(result)
            } catch (e: Exception) {
                Log.e("BookFetchingService", "Gagal fetch buku tersedia: ${e.message}", e)
                onResult(emptyList())
            }
        }
    }

    @Serializable
    private data class BorrowedBookIdResult(val book_id: String) // Nama kolom harus 'book_id'

    suspend fun fetchBukuDipinjam(userId: String, onResult: (List<InformasiBuku>) -> Unit) {
        Log.d("BookFetchingService", "[RPC] Memulai fetch buku dipinjam untuk userId: $userId")
        if (userId.isBlank()) {
            Log.w("BookFetchingService", "[RPC] User ID kosong, tidak bisa fetch buku dipinjam.")
            onResult(emptyList())
            return
        }

        var borrowedBookIds: List<String> = emptyList()

        withContext(Dispatchers.IO) {
            try {
                Log.d("BookFetchingService", "[RPC] Memanggil fungsi get_borrowed_book_ids_for_user dengan userId: $userId")

                val rpcResult = supabase.postgrest.rpc(
                    function = "get_borrowed_book_ids_for_user",
                    parameters = mapOf("p_user_id" to userId)
                ).decodeList<BorrowedBookIdResult>()

                borrowedBookIds = rpcResult.map { it.book_id }

                Log.d("BookFetchingService", "[RPC] ID Buku yang dipinjam ditemukan via RPC: $borrowedBookIds")

                if (borrowedBookIds.isEmpty()) {
                    Log.w("BookFetchingService", "[RPC] Tidak ada buku yang ditemukan sedang dipinjam oleh user $userId (via RPC).")
                    onResult(emptyList())
                    return@withContext
                }

                Log.d("BookFetchingService", "[RPC] Mencari detail buku di informasi_buku dengan ID: $borrowedBookIds")
                val result = supabase
                    .from("informasi_buku")
                    .select {
                        filter {
                            isIn("id", borrowedBookIds)
                        }
                    }
                    .decodeList<InformasiBuku>()

                Log.d("BookFetchingService", "[RPC] Total buku dipinjam ditemukan: ${result.size}")
                onResult(result)

            } catch (e: Exception) {
                if (e.message?.contains("Could not find the function") == true && e.message?.contains("get_borrowed_book_ids_for_user") == true) {
                    Log.e("BookFetchingService", "[RPC] PASTIKAN FUNGSI 'get_borrowed_book_ids_for_user(text)' SUDAH DIBUAT DAN GRANT EXECUTE DIBERIKAN!", e)
                } else {
                    Log.e("BookFetchingService", "[RPC] Gagal fetch buku dipinjam via RPC: ${e.message}", e)
                }
                onResult(emptyList())
            }
        }
    }
    suspend fun returnBuku(userId: String, bukuId: String, onResult: (Boolean) -> Unit) {
        Log.d("BookFetchingService", "[RPC-Return] Mencoba mengembalikan buku: userId=$userId, bukuId=$bukuId")
        if (userId.isBlank() || bukuId.isBlank()) {
            Log.w("BookFetchingService", "[RPC-Return] User ID atau Buku ID kosong.")
            onResult(false)
            return
        }

        withContext(Dispatchers.IO) {
            var success = false // Default ke false
            try {
                // Panggil fungsi RPC return_book
                Log.d("BookFetchingService", "[RPC-Return] Memanggil fungsi return_book...")
                val response = supabase.postgrest.rpc( // Simpan PostgrestResult
                    function = "return_book", // Nama fungsi di PostgreSQL
                    parameters = mapOf(
                        "p_user_id" to userId, // Nama parameter di fungsi SQL
                        "p_buku_id" to bukuId  // Nama parameter di fungsi SQL
                    )
                ) // <-- Hapus .invoke()

            } catch (e: Exception) {
                // Tangkap error jika fungsi RPC tidak ditemukan atau error lain
                if (e.message?.contains("Could not find the function") == true && e.message?.contains("return_book") == true) {
                    Log.e("BookFetchingService", "[RPC-Return] PASTIKAN FUNGSI 'return_book(text, uuid)' SUDAH DIBUAT DAN GRANT EXECUTE DIBERIKAN!", e)
                } else {
                    // Tangani juga error jaringan atau lainnya
                    // Log error yang sebenarnya terjadi
                    Log.e("BookFetchingService", "[RPC-Return] Gagal mengembalikan buku via RPC: ${e.message}", e) // <-- Log error lebih akurat
                }
                success = false // Pastikan false jika ada error
            } finally {
                onResult(success) // Kirim hasil (true/false) ke callback
            }
        }
    }
}

@Serializable
data class InformasiBuku(
    val id: String,
    val judul: String,
    val penulis: String,
    val cover_url: String,
    val tahun_terbit: Int? = null,
    val penerbit: String? = null,
    val deskripsi: String? = null,
    val file_buku_url: String,
    val status: String
)
