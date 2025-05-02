package com.mobile.aplikasipeminjaman

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class register : AppCompatActivity() {

    private lateinit var loginNow: TextView
    private lateinit var regBtn: Button
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var auth: FirebaseAuth

    // Create Supabase client in this activity
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

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        inputEmail = findViewById(R.id.emailInput)
        inputPassword = findViewById(R.id.passwordInput)
        regBtn = findViewById(R.id.regBtn)
        loginNow = findViewById(R.id.loginNow)

        auth = FirebaseAuth.getInstance()

        loginNow.setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }

        regBtn.setOnClickListener {

            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()

                        user?.let {
                            sendUserToSupabase(it)
                        }
                    } else {
                        Toast.makeText(this, "Gagal membuat akun: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun sendUserToSupabase(user: FirebaseUser) {
        // Add debug log
        Log.d("RegisterActivity", "Sending user to Supabase: ${user.uid}, ${user.email}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userData = mapOf(
                    "firebase_uid" to user.uid,
                    "email" to (user.email ?: "")
                )

                // Debug log the data being sent
                Log.d("RegisterActivity", "User data being sent: $userData")

                val response = supabase
                    .from("users")
                    .insert(userData)

                // Debug log the response
                Log.d("RegisterActivity", "Supabase insert response received")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@register, "User berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@register, login::class.java))
                    finish()
                }
            } catch (e: Exception) {
                Log.e("RegisterActivity", "Error sending user to Supabase", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@register, "Gagal kirim ke Supabase: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("Supabase", "Gagal kirim user: ${e.message}")
            }
        }
    }
}