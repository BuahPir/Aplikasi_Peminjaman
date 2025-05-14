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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class BorrowedBooksActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bukuAdapter: BukuAdapter // Gunakan adapter yang sama
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewEmpty: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentBooksCountTextView: TextView

    private val TAG = "BorrowedBooksActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_borrowed_books) // Buat layout ini

        Log.d(TAG, "onCreate: Activity Dibuat")
        recyclerView = findViewById(R.id.recyclerViewBorrowed)
        progressBar = findViewById(R.id.progressBarBorrowed)
        textViewEmpty = findViewById(R.id.textViewEmptyBorrowed)
        currentBooksCountTextView = findViewById(R.id.currentBooksCount)
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
            onItemClick = { buku ->
                Log.d(TAG, "onItemClick: Item diklik - Judul: ${buku.judul}, ID: ${buku.id}")
                 val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("buku_id", buku.id)
                 }
                 startActivity(intent)
            },
            onPinjamBukuClick = {  },
            onKembalikanBukuClick = { buku ->
                Log.d(
                    TAG,
                    "onKembalikanBukuClick: Tombol Kembalikan diklik untuk buku: ${buku.judul}"
                )
                // Tampilkan dialog konfirmasi sebelum mengembalikan
                showReturnConfirmationDialog(buku)
            }
        )
    }
    private fun fetchData() {
        Log.d(TAG, "fetchData: Memulai pengambilan data buku dipinjam")
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e("BorrowedBooks", "Pengguna tidak login!")
            textViewEmpty.text = "Anda harus login untuk melihat buku pinjaman."
            textViewEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            return
        }

        showLoading(true)
        val userId = currentUser.uid
        lifecycleScope.launch {
            BookFetchingService.fetchBukuDipinjam(userId) { listBuku ->
                runOnUiThread {
                    showLoading(false)
                    if (listBuku.isNotEmpty()) {
                        Log.d(TAG, "fetchData Callback: listBuku TIDAK kosong. Mengirim ke adapter.")
                        bukuAdapter.submitList(listBuku)
                        recyclerView.visibility = View.VISIBLE
                        textViewEmpty.visibility = View.GONE
                        currentBooksCountTextView.text = listBuku.size.toString()

                    } else {
                        Log.w(TAG, "fetchData Callback: listBuku KOSONG.")
                        recyclerView.visibility = View.GONE
                        textViewEmpty.visibility = View.VISIBLE
                        // Cek log sebelumnya untuk tahu kenapa list kosong (tidak ada pinjaman vs error fetch)
                        textViewEmpty.text = "Anda belum meminjam buku apapun."
                        currentBooksCountTextView.text = "0"
                        Log.w("BorrowedBooks", "Daftar buku dipinjam kosong atau gagal diambil.")
                    }
                }
            }
        }
    }

    private fun showReturnConfirmationDialog(buku: InformasiBuku) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pengembalian")
            .setMessage("Apakah Anda yakin ingin mengembalikan buku \"${buku.judul}\"?")
            .setPositiveButton("Ya") { _, _ ->
                // Jika user klik "Ya", panggil fungsi proses pengembalian
                prosesPengembalianBuku(buku)
            }
            .setNegativeButton("Batal", null) // Tombol "Batal" tidak melakukan apa-apa
            .show()
    }
    /**
     * Memproses pengembalian buku dengan memanggil BookFetchingService.
     * @param buku Objek InformasiBuku yang akan dikembalikan.
     */
    private fun prosesPengembalianBuku(buku: InformasiBuku) {
        val currentUser = firebaseAuth.currentUser
        // Pastikan user masih login saat proses ini berjalan
        if (currentUser == null) {
            Toast.makeText(this, "Gagal mendapatkan info pengguna. Silakan login ulang.", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid

        Log.d(TAG, "prosesPengembalianBuku: Memanggil BookFetchingService.returnBuku untuk userId: $userId, bukuId: ${buku.id}")
        // Anda bisa menampilkan loading indicator lain di sini jika mau

        lifecycleScope.launch {
            // Panggil fungsi service untuk mengembalikan buku
            BookFetchingService.returnBuku(userId, buku.id) {
                // Update UI di Main Thread setelah proses selesai tambah
                runOnUiThread {
                    fetchData()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        textViewEmpty.visibility = View.GONE
    }
}
