package com.example.prefinalproj.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prefinalproj.databinding.ActivityRegisterBinding
import com.example.prefinalproj.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * RegisterActivity.kt
 * --------------------
 * Lets a new user create an account.
 * Very similar structure to LoginActivity — study that one first.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Register Button ---
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // --- Input Validation ---
            when {
                username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                username.length < 3 -> {
                    Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.length < 4 -> {
                    Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            performRegister(username, password)
        }

        // --- Back to Login ---
        binding.btnGoToLogin.setOnClickListener {
            finish() // Simply close RegisterActivity — this returns to LoginActivity
        }
    }

    private fun performRegister(username: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.registerUser(username, password)

                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    // Auto-login: save the new user's data and go to the main feed
                    val prefs = getSharedPreferences("forum_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putInt("user_id", user.id)
                        .putString("username", user.username)
                        .apply()

                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created! Welcome, ${user.username}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to MainActivity, clear back stack
                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Registration failed. Username may already be taken.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}