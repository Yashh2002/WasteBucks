package com.example.wastebucks.admin

data class PickupRequest(
    var id: String = "",
    val address: String = "",
    val assignedDriver: String? = null,
    val cardboardAmount: Int = 0,
    val plasticAmount: Int = 0,
    val metalAmount: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "pending"  // ✅ Non-null with default
)

