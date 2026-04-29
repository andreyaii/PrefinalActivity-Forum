package com.example.prefinalproj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prefinalproj.R
import com.example.prefinalproj.model.Post
import com.example.prefinalproj.model.Reply

/**
 * PostAdapter.kt
 * ---------------
 * The RecyclerView Adapter. This is the "bridge" between your data (List<Post>)
 * and the RecyclerView that displays it on screen.
 *
 * HOW RECYCLERVIEW + ADAPTER WORKS (beginner explanation):
 *
 *   Imagine a conveyor belt of TV screens. You have 100 posts but only
 *   7 can fit on screen at once. RecyclerView creates ~10 "screens" (ViewHolders)
 *   and REUSES them as you scroll. When a row scrolls off the top, that same
 *   ViewHolder gets refilled with new data and appears at the bottom.
 *
 *   The 3 key methods you MUST implement:
 *
 *   1. onCreateViewHolder() — Inflates (creates) a new row view from XML.
 *      Called only a handful of times (just enough to fill the screen + buffer).
 *
 *   2. onBindViewHolder(holder, position) — Fills a ViewHolder with data.
 *      Called every time a row becomes visible. This is where you set text, etc.
 *
 *   3. getItemCount() — Tells RecyclerView how many items exist in total.
 *
 * CONSTRUCTOR PARAMETERS:
 *   posts         — The mutable list of posts (shared with MainActivity)
 *   currentUserId — The logged-in user's ID, to show/hide Delete buttons
 *   onDeletePost  — Lambda called when "Delete Post" is tapped
 *   onReply       — Lambda called when "Reply" is tapped
 *   onDeleteReply — Lambda called when "Delete Reply" is tapped
 *
 * WHY LAMBDAS?
 *   The adapter doesn't know about Retrofit or Activity context — it just displays
 *   data. When an action happens (delete, reply), it "calls back" to MainActivity
 *   which knows how to talk to the API. This keeps the adapter clean and reusable.
 */
class PostAdapter(
    private val posts: MutableList<Post>,
    private val currentUserId: Int,
    private val onDeletePost: (Post) -> Unit,
    private val onReply: (Post) -> Unit,
    private val onDeleteReply: (Reply) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    /**
     * ViewHolder — holds references to the views inside one row.
     *
     * Without ViewHolder, we'd call `itemView.findViewById(...)` every time
     * a row is bound — that's slow because `findViewById` searches the whole
     * view tree. ViewHolder caches the references so we only search once.
     */
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Post views (from item_post.xml)
        val tvUsername: TextView = itemView.findViewById(R.id.tvPostUsername)
        val tvPostText: TextView = itemView.findViewById(R.id.tvPostText)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeletePost)
        val btnReply: Button = itemView.findViewById(R.id.btnReply)
        val repliesContainer: LinearLayout = itemView.findViewById(R.id.repliesContainer)
    }

    /**
     * Step 1: Create a new ViewHolder by inflating item_post.xml.
     * `parent` is the RecyclerView itself. `viewType` is for multi-type lists (not used here).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // LayoutInflater converts an XML file into an actual View object in memory
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    /**
     * Step 2: Bind data from `posts[position]` into the ViewHolder's views.
     * This is called every time a row scrolls into view.
     */
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // --- Fill in the post data ---
        holder.tvUsername.text = post.username
        holder.tvPostText.text = post.postText

        // --- Show "Delete Post" button ONLY if this post belongs to the current user ---
        if (post.userId == currentUserId) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDeletePost(post)  // Tell MainActivity to handle the delete
            }
        } else {
            holder.btnDelete.visibility = View.GONE
        }

        // --- Reply button is visible to everyone ---
        holder.btnReply.setOnClickListener {
            onReply(post)
        }

        // --- Render replies inside the post card ---
        renderReplies(holder.repliesContainer, post)
    }

    /**
     * Dynamically adds reply views inside the post's replies container.
     *
     * We use a LinearLayout as the container and programmatically
     * inflate a simple reply view for each reply in the list.
     *
     * Why not a nested RecyclerView?
     * Nested RecyclerViews are complex. For a small number of replies per post,
     * simple dynamic inflation into a LinearLayout is easier and fast enough.
     */
    private fun renderReplies(container: LinearLayout, post: Post) {
        // Clear existing reply views before re-adding (avoid duplicates on rebind)
        container.removeAllViews()

        val replies = post.replies ?: emptyList()

        if (replies.isEmpty()) {
            container.visibility = View.GONE
            return
        }

        container.visibility = View.VISIBLE

        for (reply in replies) {
            // Inflate the reply item layout for each reply
            val replyView = LayoutInflater.from(container.context)
                .inflate(R.layout.item_reply, container, false)

            // Fill in reply data
            replyView.findViewById<TextView>(R.id.tvReplyUsername).text = reply.username
            replyView.findViewById<TextView>(R.id.tvReplyText).text = reply.replyText

            // Show "Delete Reply" button only for the current user's replies
            val btnDeleteReply = replyView.findViewById<Button>(R.id.btnDeleteReply)
            if (reply.userId == currentUserId) {
                btnDeleteReply.visibility = View.VISIBLE
                btnDeleteReply.setOnClickListener {
                    onDeleteReply(reply)
                }
            } else {
                btnDeleteReply.visibility = View.GONE
            }

            container.addView(replyView)
        }
    }

    /**
     * Step 3: Tell RecyclerView how many items there are.
     * This determines how many times onBindViewHolder will be called.
     */
    override fun getItemCount(): Int = posts.size
}