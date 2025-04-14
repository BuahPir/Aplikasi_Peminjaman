package com.mobile.aplikasipeminjaman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class register : AppCompatActivity() {
    private lateinit var loginNow: TextView
    private lateinit var regBtn: Button
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        inputEmail = findViewById(R.id.emailInput)
        inputPassword = findViewById(R.id.passwordInput)
        regBtn = findViewById(R.id.regBtn)
        auth = FirebaseAuth.getInstance()
        loginNow = findViewById(R.id.loginNow);
        loginNow.setOnClickListener{
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish()
        }
        regBtn.setOnClickListener{
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this@register, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this@register, "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(
                            baseContext,
                            "Pembuatan Akun Berhasil.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext,
                            "Pembuatan Akun gagal.",
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