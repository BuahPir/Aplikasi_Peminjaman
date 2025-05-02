package com.mobile.aplikasipeminjaman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth // Import KTX
import com.google.firebase.ktx.Firebase // Import KTX
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

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
class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var buttonSignout: Button
    private lateinit var buttonAvailableBooks: Button
    private lateinit var buttonBorrowedBooks: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Pastikan Anda menggunakan layout XML yang sesuai (contoh: activity_main_nav_example.xml)
        setContentView(R.layout.activity_main) // Ganti dengan layout Anda

        firebaseAuth = FirebaseAuth.getInstance()
        buttonSignout = findViewById(R.id.buttonSignOut) // Pastikan ID ada di XML
        buttonAvailableBooks = findViewById(R.id.buttonAvailableBooks) // Pastikan ID ada di XML
        buttonBorrowedBooks = findViewById(R.id.buttonBorrowedBooks) // Pastikan ID ada di XML

        // Cek jika user belum login, kembali ke layar login
        if (firebaseAuth.currentUser == null) {
            Log.w("MainActivity", "Pengguna tidak login, kembali ke LoginActivity.")
            val intent = Intent(this, login::class.java) // Asumsi nama activity login adalah login
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Tutup MainActivity
            return // Hentikan onCreate lebih lanjut
        }

        // Tombol Logout
        buttonSignout.setOnClickListener {
            Firebase.auth.signOut() // Logout Firebase
            // Tidak perlu logout Supabase karena tidak digunakan
            Log.d("Auth", "Firebase sign out successful")
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Tombol ke Buku Tersedia
        buttonAvailableBooks.setOnClickListener {
            val intent = Intent(this, AvailableBooksActivity::class.java)
            startActivity(intent)
        }

        // Tombol ke Buku Dipinjam
        buttonBorrowedBooks.setOnClickListener {
            val intent = Intent(this, BorrowedBooksActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets -> // Ganti ID ke container utama Anda
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
