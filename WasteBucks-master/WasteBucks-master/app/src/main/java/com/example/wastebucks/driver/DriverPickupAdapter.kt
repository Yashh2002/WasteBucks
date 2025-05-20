package com.example.wastebucks.driver

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.wastebucks.databinding.ItemDriverPickupBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.net.Uri


class DriverPickupAdapter(private val pickups: List<PickupRequestModel>) :
    RecyclerView.Adapter<DriverPickupAdapter.PickupViewHolder>() {

    inner class PickupViewHolder(val binding: ItemDriverPickupBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickupViewHolder {
        val binding = ItemDriverPickupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PickupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PickupViewHolder, position: Int) {
        val pickup = pickups[position]
        holder.binding.tvAddress.text = "Address: ${pickup.address}"
        holder.binding.tvPlasticAmount.text = "Plastic: ${pickup.plasticAmount}"
        holder.binding.tvMetalAmount.text = "Metal: ${pickup.metalAmount}"
        holder.binding.tvCardboardAmount.text = "Cardboard: ${pickup.cardboardAmount}"
        holder.binding.tvLatLong.text = "Location: ${pickup.latitude}, ${pickup.longitude}"
        holder.binding.viewLocationBtn.setOnClickListener {
            val lat = pickup.latitude
            val lon = pickup.longitude
            val context = holder.itemView.context

            val geoUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
            val intent = Intent(Intent.ACTION_VIEW, geoUri)
            context.startActivity(intent)
        }

        holder.binding.completeBtn.setOnClickListener {
            updateStatus(pickup.id, "completed", holder)
        }

        holder.binding.rejectBtn.setOnClickListener {
            updateStatus(pickup.id, "rejected", holder)
        }
    }

    override fun getItemCount(): Int = pickups.size

    private fun updateStatus(pickupId: String, status: String, holder: PickupViewHolder) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("orders").document(pickupId)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Marked as $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Failed to update status", Toast.LENGTH_SHORT).show()
            }
    }
}
