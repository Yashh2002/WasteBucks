package com.example.wastebucks.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wastebucks.R

class PickupRequestAdapter(
    private val requests: MutableList<PickupRequest>,
    private val onAction: (PickupRequest, String) -> Unit
) : RecyclerView.Adapter<PickupRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val address: TextView = view.findViewById(R.id.pickupLocation)
        val status: TextView = view.findViewById(R.id.pickupStatus)
        val btnApprove: Button = view.findViewById(R.id.acceptBtn)
        val btnDecline: Button = view.findViewById(R.id.rejectBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pickup_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val req = requests[position]
        holder.address.text = req.address ?: "Location: ${req.latitude}, ${req.longitude}"
        holder.status.text = req.status ?: ""
        holder.btnApprove.setOnClickListener { onAction(req, "approve") }
        holder.btnDecline.setOnClickListener { onAction(req, "decline") }
    }

    override fun getItemCount(): Int = requests.size

    fun updateList(newRequests: List<PickupRequest>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }
}
