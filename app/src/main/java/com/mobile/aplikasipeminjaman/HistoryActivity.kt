package com.mobile.aplikasipeminjaman

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val supabaseUrl = "https://amcscovnmpcwypzwailr.supabase.co"  // Removed trailing slash
private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFtY3Njb3ZubXBjd3lwendhaWxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUwNzIxOTYsImV4cCI6MjA2MDY0ODE5Nn0._EBHeEOYXXoi9j6i_utzMR8T042nsOes7jv4sMgPG9Q"

private val supabase = createSupabaseClient(
    supabaseUrl = supabaseUrl,
    supabaseKey = anonKey
) {
    install(Postgrest)
    // Add debug log
    Log.d("RegisterActivity", "Initializing Supabase client with URL: $supabaseUrl")
}
class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private val adapter = HistoryAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        recyclerView = findViewById(R.id.recyclerHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchHistory()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun fetchHistory() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabase.from("peminjaman_buku")
                    .select(columns = Columns.list("id, tanggal_pinjam", "tanggal_kembali", "status", "informasi_buku(judul)")) {
                        filter {
                            eq("user_id", uid)
                        }
                    }

                val list = result.decodeList<PeminjamanBuku>()

                withContext(Dispatchers.Main) {
                    adapter.submitList(list)
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity", "Gagal mengambil data: ${e.message}")
            }
        }
    }
}