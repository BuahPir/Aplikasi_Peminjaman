package com.mobile.aplikasipeminjaman

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity : AppCompatActivity() {
    private val timeoutMillis: Long = 5 * 60 * 1000 // 5 menit
    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable { autoLogout() }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimeout()
    }

    override fun onResume() {
        super.onResume()
        resetTimeout()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeoutRunnable)
    }

    private fun resetTimeout() {
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, timeoutMillis)
    }

    private fun autoLogout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Anda telah otomatis logout karena tidak aktif.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, login::class.java))
        finishAffinity()
    }
}