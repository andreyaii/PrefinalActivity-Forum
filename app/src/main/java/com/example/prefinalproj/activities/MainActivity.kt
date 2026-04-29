package com.example.prefinalproj.activities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prefinalproj.adapter.PostAdapter
import com.example.prefinalproj.databinding.ActivityMainBinding
import com.example.prefinalproj.model.Post
import com.example.prefinalproj.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * MainActivity.kt
 * ----------------
 * The main posts feed. Shows all forum posts in a scrollable RecyclerView.
 *
 * KEY CONCEPTS:
 *
 * 1. RecyclerView:
 *    An efficient scrollable list. It "recycles" row views as you scroll
 *    instead of keeping ALL rows in memory at once. You need:
 *    - A layout manager (LinearLayoutManager = vertical list)
 *    - An adapter (PostAdapter) that binds data to each row view
 *
 * 2. Pagination:
 *    Instead of loading ALL posts at once (slow), we load page by page.
 *    Page 1 → first batch, Page 2 → second batch, etc.
 *    We detect when the user scrolls to the bottom and load the next page.
 *
 * 3. SharedPreferences (reading):
 *    We read the user_id saved during login to pass it to the adapter
 *    so we can show "Delete" buttons only on the user's own posts.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // The adapter bridges data (List<Post>) and the RecyclerView
    private lateinit var postAdapter: PostAdapter

    // The list of ALL posts loaded so far (across all pages)
    private val postList = mutableListOf<Post>()

    // Pagination state
    private var currentPage = 1
    private var isLoading = false      // Prevents loading two pages at once
    private var hasMorePages = true    // Stops loading when no more data

    // The logged-in user's ID — retrieved from SharedPreferences
    private var currentUserId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Read the logged-in user's ID from SharedPreferences ---
        // "forum_prefs" is the name of our preferences file (same name used in LoginActivity)
        val prefs = getSharedPreferences("forum_prefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getInt("user_id", -1)
        val currentUsername = prefs.getString("username", "User") ?: "User"

        // Update the toolbar title to show who's logged in
        supportActionBar?.title = "Forum — $currentUsername"

        // --- Set up RecyclerView ---
        setupRecyclerView()

        // --- FAB: New Post button ---
        binding.fabNewPost.setOnClickListener {
            startActivity(Intent(this, NewPostActivity::class.java))
        }

        // --- Logout button ---
        binding.btnLogout.setOnClickListener {
            logout()
        }

        // --- Load the first page of posts ---
        loadPosts()
    }

    /**
     * Called when user returns from NewPostActivity.
     * We refresh the feed so the new post appears.
     */
    override fun onResume() {
        super.onResume()
        // Only refresh if there's something already loaded (not on first launch)
        if (postList.isNotEmpty()) {
            refreshFeed()
        }
    }

    /**
     * Sets up the RecyclerView with:
     * - A LinearLayoutManager (vertical, top-to-bottom)
     * - Our PostAdapter
     * - A scroll listener for pagination
     */
    private fun setupRecyclerView() {
        // Create the adapter.
        // We pass `currentUserId` so the adapter can show Delete buttons
        // only on posts/replies that belong to the current user.
        postAdapter = PostAdapter(
            posts = postList,
            currentUserId = currentUserId,
            onDeletePost = { post -> deletePost(post) },
            onReply = { post -> showReplyDialog(post) },
            onDeleteReply = { reply -> deleteReply(reply.id) }
        )

        // LinearLayoutManager arranges items in a vertical list
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = postAdapter

        // --- PAGINATION: detect when user scrolls to the bottom ---
        binding.recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // dy > 0 means scrolling DOWN
                if (dy > 0) {
                    val totalItems = layoutManager.itemCount
                    val lastVisible = layoutManager.findLastVisibleItemPosition()

                    // If we're at the last 2 items and not already loading, load next page
                    if (!isLoading && hasMorePages && lastVisible >= totalItems - 2) {
                        currentPage++
                        loadPosts()
                    }
                }
            }
        })
    }

    /**
     * Loads a page of posts from the API and appends them to the list.
     *
     * How RecyclerView updates work:
     *   - `notifyItemRangeInserted(start, count)` tells the RecyclerView exactly
     *     which items were added — more efficient than `notifyDataSetChanged()`
     *     which redraws everything.
     */
    private fun loadPosts() {
        if (isLoading || !hasMorePages) return
        isLoading = true

        // Show loading indicator (only visible on first load, not on pagination)
        if (currentPage == 1) {
            binding.progressBar.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPosts(currentPage)
                binding.progressBar.visibility = View.GONE
                isLoading = false

                if (response.isSuccessful) {
                    val newPosts = response.body() ?: emptyList()

                    if (newPosts.isEmpty()) {
                        // No more posts — stop paginating
                        hasMorePages = false
                        if (postList.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                        }
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        val startIndex = postList.size
                        postList.addAll(newPosts)
                        // Efficiently notify RecyclerView of the new items
                        postAdapter.notifyItemRangeInserted(startIndex, newPosts.size)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load posts", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                isLoading = false
                Toast.makeText(this@MainActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Clears the list and reloads from page 1 */
    private fun refreshFeed() {
        postList.clear()
        postAdapter.notifyDataSetChanged()
        currentPage = 1
        hasMorePages = true
        isLoading = false
        loadPosts()
    }

    /**
     * Deletes a post via API, then removes it from the local list.
     *
     * We update the local list immediately for a fast UI response,
     * rather than re-fetching the whole page.
     */
    private fun deletePost(post: com.example.prefinalproj.model.Post) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePost(post.id)
                if (response.isSuccessful && response.body()?.success == true) {
                    val index = postList.indexOf(post)
                    if (index != -1) {
                        postList.removeAt(index)
                        postAdapter.notifyItemRemoved(index)
                    }
                    Toast.makeText(this@MainActivity, "Post deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to delete post", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Shows a dialog where the user types their reply.
     *
     * AlertDialog.Builder is the standard Android way to show a pop-up
     * with a text input without creating a new Activity.
     */
    private fun showReplyDialog(post: com.example.prefinalproj.model.Post) {
        // Build a simple input dialog
        val editText = android.widget.EditText(this).apply {
            hint = "Write your reply..."
            setPadding(48, 24, 48, 24)
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Reply to ${post.username}'s post")
            .setView(editText)
            .setPositiveButton("Submit") { _, _ ->
                val replyText = editText.text.toString().trim()
                if (replyText.isNotEmpty()) {
                    submitReply(post.id, replyText)
                } else {
                    Toast.makeText(this, "Reply cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitReply(postId: Int, replyText: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.replyToPost(currentUserId, postId, replyText)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MainActivity, "Reply posted!", Toast.LENGTH_SHORT).show()
                    refreshFeed() // Refresh to show the new reply
                } else {
                    Toast.makeText(this@MainActivity, "Failed to post reply", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteReply(replyId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteReply(replyId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MainActivity, "Reply deleted", Toast.LENGTH_SHORT).show()
                    refreshFeed() // Refresh to remove the deleted reply
                } else {
                    Toast.makeText(this@MainActivity, "Failed to delete reply", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Logs the user out by clearing SharedPreferences and going back to Login.
     */
    private fun logout() {
        // Clear all saved user data
        getSharedPreferences("forum_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Go to Login and clear the back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}