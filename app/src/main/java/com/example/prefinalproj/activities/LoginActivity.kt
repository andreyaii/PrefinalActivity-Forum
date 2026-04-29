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
 *
 * KEY CONCEPTS USED HERE:
 *
 * 1. View Binding (ActivityLoginBinding):
 *    Instead of using findViewById() to get views, View Binding auto-generates
 *    a binding class from your XML. So `binding.etUsername` directly refers to
 *    the EditText with id "etUsername" in activity_login.xml.
 *
 * 2. SharedPreferences:
 *    A simple key-value store built into Android. We use it to save the user's
 *    ID after login, so we can retrieve it later in any Activity without
 *    passing it through every Intent manually.
 *
 * 3. Coroutines (lifecycleScope.launch):
 *    API calls MUST NOT run on the main (UI) thread — it would freeze the app.
 *    `lifecycleScope.launch` runs the code on a background thread automatically.
 *    When it's done, you're back on the main thread to update the UI.
 */
class LoginActivity : AppCompatActivity() {

    // View Binding: gives us direct access to all views in activity_login.xml
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding (instead of setContentView(R.layout.activity_login))
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

        // lifecycleScope is tied to this Activity's lifecycle.
        // If the Activity is destroyed (e.g., back button), the coroutine cancels automatically.
        lifecycleScope.launch {
            try {
                // --- MAKE THE API CALL ---
                // `RetrofitClient.apiService` is our shared Retrofit instance
                // `loginUser` is a suspend function that runs on a background thread
                val response = RetrofitClient.apiService.loginUser(username, password)

                // Hide loading indicator
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                // --- CHECK THE RESPONSE ---
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    // Save user ID and username in SharedPreferences for later use
                    saveUserToPrefs(user.id, user.username)

                    Toast.makeText(this@LoginActivity, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    // API returned an error or "false"
                    Toast.makeText(this@LoginActivity, "Invalid username or password", Toast.LENGTH_SHORT).show()
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
     *
     * SharedPreferences works like a persistent dictionary:
     *   - `edit()` opens a write transaction
     *   - `putInt / putString` sets values
     *   - `apply()` saves asynchronously (use `commit()` if you need it to be synchronous)
     *
     * Retrieve later with: prefs.getInt("user_id", -1)
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
        // These flags clear the Activity back stack — pressing Back won't return to Login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}