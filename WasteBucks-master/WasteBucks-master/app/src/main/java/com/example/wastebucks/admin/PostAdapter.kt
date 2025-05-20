package com.example.wastebucks.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wastebucks.R

class PostAdapter(
    private val posts: List<Post>,
    private val onApproveClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postDetails: TextView = view.findViewById(R.id.postDetails)
        val approveButton: Button = view.findViewById(R.id.approveButton)

        fun bind(post: Post) {
            postDetails.text = "${post.id}: ${post.title}"
            approveButton.setOnClickListener {
                onApproveClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size
}
