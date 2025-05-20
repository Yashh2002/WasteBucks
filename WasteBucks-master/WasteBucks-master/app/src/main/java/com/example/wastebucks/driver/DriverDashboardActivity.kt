package com.example.wastebucks.driver

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wastebucks.databinding.ActivityDriverDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.widget.Button
import com.example.wastebucks.R
import com.google.firebase.auth.FirebaseAuth
import com.example.wastebucks.driver.DriverLoginActivity


class DriverDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDriverDashboardBinding
    private lateinit var adapter: DriverPickupAdapter
    private val pickupList = ArrayList<PickupRequestModel>()
    private val firestore = FirebaseFirestore.getInstance()
    private val driverName = "Driver A" // Replace with session data if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DriverPickupAdapter(pickupList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DriverLoginActivity::class.java))
            finish()
        }

        fetchPickupRequests()
    }

    private fun fetchPickupRequests() {
        firestore.collection("orders")
            .whereEqualTo("assignedDriver", driverName)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { documents ->
                pickupList.clear()
                if (!documents.isEmpty) {
                    for (doc in documents) {
                        val pickup = doc.toObject(PickupRequestModel::class.java)
                        pickup.id = doc.id  // ✅ Set document ID correctly
                        pickupList.add(pickup)

                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "No approved pickups assigned.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch pickups", Toast.LENGTH_SHORT).show()
            }
    }
}
