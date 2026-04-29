package com.example.prefinalproj.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prefinalproj.databinding.ActivityNewPostBinding
import com.example.prefinalproj.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * NewPostActivity.kt
 * -------------------
 * A simple screen with a text input where the user types a new post.
 *
 * How user_id gets here:
 *   - The user logged in → LoginActivity saved user_id to SharedPreferences
 *   - Here we just read it from SharedPreferences
 *   - No need to pass it via Intent (though that's also valid)
 */
class NewPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPostBinding
    private var currentUserId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "New Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back arrow

        // Read the saved user ID
        val prefs = getSharedPreferences("forum_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("user_id", -1)

        // --- Submit Post Button ---
        binding.btnSubmitPost.setOnClickListener {
            val postText = binding.etPostContent.text.toString().trim()

            if (postText.isEmpty()) {
                Toast.makeText(this, "Post content cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUserId == -1) {
                Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitPost(postText)
        }
    }

    private fun submitPost(postText: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitPost.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createPost(currentUserId, postText)
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitPost.isEnabled = true

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@NewPostActivity, "Post created!", Toast.LENGTH_SHORT).show()
                    // Return to MainActivity — onResume() there will refresh the feed
                    finish()
                } else {
                    Toast.makeText(this@NewPostActivity, "Failed to create post", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitPost.isEnabled = true
                Toast.makeText(this@NewPostActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Handle the back arrow in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}