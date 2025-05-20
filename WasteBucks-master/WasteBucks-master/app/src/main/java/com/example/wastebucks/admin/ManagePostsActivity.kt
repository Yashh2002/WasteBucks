package com.example.wastebucks.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wastebucks.R
import com.google.firebase.firestore.FirebaseFirestore

class ManagePostsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postsAdapter: PostAdapter

    private val db = FirebaseFirestore.getInstance()
    private val postsList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_posts)

        recyclerView = findViewById(R.id.recyclerManagePosts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ Pass onApproveClick function when initializing the adapter
        postsAdapter = PostAdapter(postsList) { post ->
            approvePost(post.id)
        }

        recyclerView.adapter = postsAdapter

        loadPosts()
    }

    private fun loadPosts() {
        db.collection("posts")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                postsList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java).apply {
                        id = document.id
                    }
                    postsList.add(post)
                }
                postsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading posts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approvePost(postId: String) {
        db.collection("posts").document(postId)
            .update("status", "approved")
            .addOnSuccessListener {
                Toast.makeText(this, "Post Approved", Toast.LENGTH_SHORT).show()
                loadPosts()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error approving post", Toast.LENGTH_SHORT).show()
            }
    }
}
