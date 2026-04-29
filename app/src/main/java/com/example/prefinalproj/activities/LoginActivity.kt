package com.example.prefinalproj.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prefinalproj.databinding.ActivityLoginBinding
import com.example.prefinalproj.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * LoginActivity.kt
 * -----------------
 * The first screen the user sees. Handles login logic.
 */
class LoginActivity : AppCompatActivity() {

    // View Binding: gives us direct access to all views in activity_login.xml
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Auto-login: if user is already logged in, skip to MainActivity ---
        val prefs = getSharedPreferences("forum_prefs", Context.MODE_PRIVATE)
        val savedUserId = prefs.getInt("user_id", -1)
        if (savedUserId != -1) {
            // User was previously logged in — go straight to the posts feed
            goToMain()
            return
        }

        // --- Button: Login ---
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Basic validation before making an API call
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the login function
            performLogin(username, password)
        }

        // --- Button: Go to Register screen ---
        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Performs the actual login API call.
     * This runs inside a coroutine to avoid blocking the UI thread.
     */
    private fun performLogin(username: String, password: String) {
        // Show a loading indicator while the request is in progress
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false  // Prevent double-tapping

        lifecycleScope.launch {
            try {
                // --- MAKE THE API CALL ---
                val response = RetrofitClient.apiService.loginUser(username, password)

                // Hide loading indicator
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                // --- CHECK THE RESPONSE ---
                if (response.isSuccessful && response.body() != null) {
                    // 1. Grab the wrapper object (LoginResponse)
                    val loginData = response.body()!!

                    // 2. Check if success is true AND the user object exists
                    if (loginData.success && loginData.user != null) {

                        // 3. Extract the actual user data
                        val user = loginData.user

                        // Save user ID and username in SharedPreferences for later use
                        saveUserToPrefs(user.id, user.username)

                        Toast.makeText(this@LoginActivity, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()
                        goToMain()

                    } else {
                        // The server returned a 200 OK, but success was false (wrong password)
                        Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // The server returned an error code (like 404 or 500)
                    Toast.makeText(this@LoginActivity, "Server error. Try again.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // Network error (no internet, server down, etc.)
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Saves the logged-in user's data to SharedPreferences.
     */
    private fun saveUserToPrefs(userId: Int, username: String) {
        val prefs = getSharedPreferences("forum_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("user_id", userId)
            .putString("username", username)
            .apply()
    }

    /** Navigates to MainActivity and clears the back stack so user can't go back to Login */
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}