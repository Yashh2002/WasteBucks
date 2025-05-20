package com.example.wastebucks.driver

data class PickupRequestModel(
    var id: String = "",            // Firestore document ID
    val address: String = "",
    val assignedDriver: String = "",
    val cardboardAmount: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val metalAmount: Int = 0,
    val plasticAmount: Int = 0,
    var status: String = "",
    val timestamp: Long = 0L,
    val userId: String = ""
)