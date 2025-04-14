package com.mobile.aplikasipeminjaman

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class login : AppCompatActivity() {
    private lateinit var registerNow: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        inputEmail = findViewById(R.id.emailInput)
        inputPassword = findViewById(R.id.passwordInput)
        loginBtn = findViewById(R.id.loginBtn)
        auth = FirebaseAuth.getInstance()
        registerNow = findViewById(R.id.registerNow)
        registerNow.setOnClickListener{
            val intent = Intent(this, register::class.java)
            startActivity(intent)
            finish()
        }
        loginBtn.setOnClickListener{
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this@login, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this@login, "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "Login berhasil.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        val user = auth.currentUser
                        updateUI(user)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext,
                            "Akun anda belum terdaftar.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        updateUI(null)
                    }
                }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }
    private fun updateUI(user: FirebaseUser?) {
    }
    private fun reload() {
    }
}