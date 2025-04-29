package com.mobile.aplikasipeminjaman

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseAuthService {

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://amcscovnmpcwypzwailr.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFtY3Njb3ZubXBjd3lwendhaWxyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDUwNzIxOTYsImV4cCI6MjA2MDY0ODE5Nn0._EBHeEOYXXoi9j6i_utzMR8T042nsOes7jv4sMgPG9Q"
    ) {
        install(Postgrest)
    }

    suspend fun validateUser(firebaseUid: String): Boolean {
        return try {
            val result = supabase
                .from("users")
                .select {
                    filter {
                        eq("firebase_uid", firebaseUid)
                    }
                    single()
                }
                .decodeSingle<UserData>()

            result.id.isNotEmpty() // Kalau user ditemukan, berarti valid
        } catch (e: Exception) {
            Log.e("SupabaseAuthService", "Gagal validasi user: ${e.message}")
            false
        }
    }
    suspend fun getUserUuid(firebaseUid: String): String? {
        Log.d("SupabaseAuthService", "Mencari user dengan Firebase UID: '$firebaseUid'")
        return try {
            val results = supabase
                .from("users")
                .select {
                    filter {
                        eq("firebase_uid", firebaseUid)
                    }
                }
                .decodeList<UserData>()  // Use decodeList instead of decodeSingle

            Log.d("SupabaseAuthService", "Hasil query: ${results.size} data ditemukan")
            if (results.isNotEmpty()) {
                Log.d("SupabaseAuthService", "User ditemukan dengan UUID: ${results.first().id}")
                results.first().id
            } else {
                Log.d("SupabaseAuthService", "Tidak ada user dengan Firebase UID: $firebaseUid")
                // Debug: list all users to check what's in database

                try {
                    val allUsers = supabase.from("users").select().decodeList<UserData>()
                    Log.d("SupabaseAuthService", "Total users in DB: ${allUsers.size}")
                    for (user in allUsers) {
                        Log.d("SupabaseAuthService", "DB User: ${user.id}, Firebase UID: ${user.firebase_uid}")
                    }
                } catch (e: Exception) {
                    Log.e("SupabaseAuthService", "Error listing users: ${e.message}")
                }
                null

            }
        } catch (e: Exception) {
            Log.e("SupabaseAuthService", "Gagal ambil UUID: ${e.message}", e)
            null
        }
    }
}

@kotlinx.serialization.Serializable
data class UserData(
    val id: String,
    val firebase_uid: String,
    val email: String
)