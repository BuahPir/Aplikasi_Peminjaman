package com.mobile.aplikasipeminjaman

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
class DetailActivity : BaseActivity() {
    private lateinit var buttonBacaPdf: Button
    private lateinit var textJudul: TextView
    private lateinit var textInfo: TextView
    private lateinit var textDeskripsi: TextView
    private lateinit var imageCover: ImageView
    private lateinit var shimmerCover: ShimmerFrameLayout
    private lateinit var shimmerTitle: ShimmerFrameLayout
    private lateinit var shimmerInfo: ShimmerFrameLayout
    private lateinit var shimmerDeskripsi: ShimmerFrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)
        buttonBacaPdf = findViewById(R.id.buttonBacaPdf)
        textJudul = findViewById(R.id.textJudul)
        textInfo = findViewById(R.id.textInfo)
        textDeskripsi = findViewById(R.id.textDeskripsi)
        imageCover = findViewById(R.id.imageCover)

        shimmerCover = findViewById(R.id.shimmerCover)
        shimmerTitle = findViewById(R.id.shimmerTitle)
        shimmerInfo = findViewById(R.id.shimmerInfo)
        shimmerDeskripsi = findViewById(R.id.shimmerDeskripsi)

        val bukuId = intent.getStringExtra("buku_id") ?: return
        fetchDetailBuku(bukuId)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun fetchDetailBuku(id: String) {
        shimmerCover.startShimmer()
        shimmerTitle.startShimmer()
        shimmerInfo.startShimmer()
        shimmerDeskripsi.startShimmer()
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
            try {
                // Fetch data from Supabase, menggunakan `eq` untuk filter query

                val result = supabase
                    .from("informasi_buku")
                    .select {
                        filter {
                            eq("id", id)
                        }
                    }
                    .decodeList<InformasiBuku>()

                runOnUiThread {
                    shimmerCover.stopShimmer()
                    shimmerCover.visibility = View.GONE
                    shimmerTitle.stopShimmer()
                    shimmerTitle.visibility = View.GONE
                    shimmerInfo.stopShimmer()
                    shimmerInfo.visibility = View.GONE
                    shimmerDeskripsi.stopShimmer()
                    shimmerDeskripsi.visibility = View.GONE

                    if (result.isNotEmpty()) {
                        val buku = result[0]

                        textJudul.text = buku.judul
                        textInfo.text = "oleh ${buku.penulis} • ${buku.tahun_terbit ?: "-"}"
                        textDeskripsi.text = buku.deskripsi ?: "Konten tidak tersedia."

                        Glide.with(this@DetailActivity)
                            .load(buku.cover_url)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(imageCover)

                        imageCover.visibility = View.VISIBLE
                        textJudul.visibility = View.VISIBLE
                        textInfo.visibility = View.VISIBLE
                        textDeskripsi.visibility = View.VISIBLE
                        buttonBacaPdf.visibility = View.VISIBLE

                        buttonBacaPdf.setOnClickListener {
                            AlertDialog.Builder(this@DetailActivity)
                                .setTitle("Baca Buku")
                                .setMessage("Apakah kamu ingin membuka file PDF buku ini?")
                                .setPositiveButton("Ya") { _, _ ->
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(
                                        Uri.parse(buku.file_buku_url),
                                        "application/pdf"
                                    )
                                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                                    try {
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(
                                            this@DetailActivity,
                                            "Tidak ada aplikasi untuk membuka PDF.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                                .setNegativeButton("Batal", null)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            this@DetailActivity,
                            "Buku tidak ditemukan",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "Gagal fetch detail buku: ${e.localizedMessage}")
                runOnUiThread {
                    Toast.makeText(this@DetailActivity, "Gagal memuat data buku", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}