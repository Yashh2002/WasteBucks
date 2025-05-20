package com.example.wastebucks.admin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wastebucks.LoginActivity
import com.example.wastebucks.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.wastebucks.admin.PickupRequestAdapter


class AdminDashboard : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var pickupRequestAdapter: PickupRequestAdapter
    private val pickupRequests = mutableListOf<PickupRequest>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerPickupRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val logoutButton = findViewById<Button>(R.id.btnLogout)
        val managePostsButton = findViewById<Button>(R.id.btnPosts)

        // ✅ Logout Button Click Listener
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // ✅ Manage Posts Button Click Listener
        managePostsButton.setOnClickListener {
            Toast.makeText(this, "Manage Posts Clicked!", Toast.LENGTH_SHORT).show()
            // Add intent to navigate to the post management screen if needed.
        }

        pickupRequestAdapter = PickupRequestAdapter(pickupRequests) { request, action ->
            when (action) {
                "approve" -> approvePickupRequest(request.id)
                "decline" -> declinePickupRequest(request.id)
            }
        }
        recyclerView.adapter = pickupRequestAdapter

        loadPickupRequests()
    }

    private fun loadPickupRequests() {
        db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("AdminDashboard", "Found ${documents.size()} pickup requests")
                val requestsList = mutableListOf<PickupRequest>()

                for (document in documents) {
                    val request = document.toObject(PickupRequest::class.java).apply {
                        id = document.id
                    }

                    Log.d("AdminDashboard", "Request ID: $ | Status: ${request.status} | Address: ${request.address}")

                    // ✅ Show only pending requests
                    if (request.status == "pending") {
                        requestsList.add(request)
                        Log.d("AdminDashboard", "Added to UI list: ")
                    }
                }

                Log.d("AdminDashboard", "Final list count: ${requestsList.size}")
                pickupRequestAdapter.updateList(requestsList)
            }
            .addOnFailureListener { exception ->
                Log.e("AdminDashboard", "Error loading pickup requests", exception)
                Toast.makeText(this, "Error loading pickup requests", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approvePickupRequest(requestId: String) {
        db.collection("orders").document(requestId)
            .update("status", "approved")
            .addOnSuccessListener {
                Toast.makeText(this, "Pickup request approved", Toast.LENGTH_SHORT).show()
                assignDriver(requestId)
                loadPickupRequests()  // Reload list to remove request
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error approving pickup request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun assignDriver(requestId: String) {
        val driverList = listOf("Driver A", "Driver B", "Driver C")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Assign Driver")
        builder.setItems(driverList.toTypedArray()) { _, which ->
            val selectedDriver = driverList[which]

            db.collection("orders").document(requestId)
                .update("assignedDriver", selectedDriver)
                .addOnSuccessListener {
                    Toast.makeText(this, "Driver $selectedDriver assigned!", Toast.LENGTH_SHORT).show()
                    loadPickupRequests() // Refresh after driver assignment
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error assigning driver", Toast.LENGTH_SHORT).show()
                }
        }
        builder.show()
    }

    private fun declinePickupRequest(requestId: String) {
        db.collection("orders").document(requestId)
            .update("status", "declined")
            .addOnSuccessListener {
                Toast.makeText(this, "Pickup request declined", Toast.LENGTH_SHORT).show()
                notifyUser(requestId)
                loadPickupRequests()  // Reload list to remove request
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error declining pickup request", Toast.LENGTH_SHORT).show()
            }
    }

    private fun notifyUser(requestId: String) {
        db.collection("orders").document(requestId)
            .get()
            .addOnSuccessListener { document ->
                val userId = document.getString("userId") ?: return@addOnSuccessListener

                val notification = mapOf(
                    "message" to "Your pickup request has been declined.",
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("users").document(userId)
                    .collection("notifications")
                    .add(notification)
                    .addOnSuccessListener {
                        Toast.makeText(this, "User notified of decline", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error notifying user", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}
