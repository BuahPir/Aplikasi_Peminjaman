package com.mobile.aplikasipeminjaman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope // Import lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.mobile.aplikasipeminjaman.PeminjamanService.pinjamBuku
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AvailableBooksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bukuAdapter: BukuAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewEmpty: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_available_books) // Buat layout ini

        recyclerView = findViewById(R.id.recyclerViewAvailable)
        progressBar = findViewById(R.id.progressBarAvailable)
        textViewEmpty = findViewById(R.id.textViewEmptyAvailable)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        firebaseAuth = FirebaseAuth.getInstance()

        setupAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = bukuAdapter

        fetchData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets -> // Ganti ID ke container utama layout
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun setupAdapter() {
        bukuAdapter = BukuAdapter(
            onItemClick = { },
            onPinjamBukuClick = {  buku ->
                Log.d("AvailableBooks", "Pinjam clicked: ${buku.judul}")
                pinjamBuku(buku) },
            onKembalikanBukuClick = {
            }
        )
    }
    private fun fetchData() {
        showLoading(true)
        lifecycleScope.launch {
            BookFetchingService.fetchBukuTersedia { listBuku ->
                runOnUiThread {
                    showLoading(false)
                    if (listBuku.isNotEmpty()) {
                        bukuAdapter.submitList(listBuku)
                        recyclerView.visibility = View.VISIBLE
                        emptyStateContainer.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.GONE
                        emptyStateContainer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE // Sembunyikan RV saat loading
        emptyStateContainer.visibility = View.GONE
    }

    private fun pinjamBuku(buku: InformasiBuku) {
        val firebaseUid = firebaseAuth.currentUser?.uid
        Log.d("AvailableBooks", "Attempting to borrow. Firebase UID: $firebaseUid")

        if (firebaseUid == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Peminjaman")
            .setMessage("Apakah Anda yakin ingin meminjam buku \"${buku.judul}\"?")
            .setPositiveButton("Ya") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    PeminjamanService.pinjamBuku(
                        userId = firebaseUid,
                        bukuId = buku.id,
                        onSuccess = {
                            runOnUiThread {
                                Toast.makeText(this@AvailableBooksActivity, "Buku berhasil dipinjam!", Toast.LENGTH_SHORT).show()
                                fetchData()
                            }
                        },
                        onError = { errorMsg ->
                            runOnUiThread {
                                Toast.makeText(this@AvailableBooksActivity, "Gagal pinjam: $errorMsg", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
