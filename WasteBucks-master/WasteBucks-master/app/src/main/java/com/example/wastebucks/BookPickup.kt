package com.example.wastebucks

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.travijuu.numberpicker.library.NumberPicker

class BookPickup : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dialog: Dialog
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_book_pickup, container, false)

        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val plasticAmountPicker: NumberPicker = view.findViewById(R.id.numberPickerPlastic)
        val cardboardAmountPicker: NumberPicker = view.findViewById(R.id.numberPickerCardboard)
        val metalAmountPicker: NumberPicker = view.findViewById(R.id.numberPickerMetal)
        val address: EditText = view.findViewById(R.id.editTextAddress)
        val bookButton: Button = view.findViewById(R.id.buttonBookPickup)

        plasticAmountPicker.setMax(10)
        plasticAmountPicker.setMin(0)
        plasticAmountPicker.setUnit(1)
        plasticAmountPicker.setValue(0)

        cardboardAmountPicker.setMax(10)
        cardboardAmountPicker.setMin(0)
        cardboardAmountPicker.setUnit(1)
        cardboardAmountPicker.setValue(0)

        metalAmountPicker.setMax(10)
        metalAmountPicker.setMin(0)
        metalAmountPicker.setUnit(1)
        metalAmountPicker.setValue(0)

        // Request Location Permission if not granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }

        bookButton.setOnClickListener {
            val order = hashMapOf(
                "plasticAmount" to plasticAmountPicker.getValue(),
                "cardboardAmount" to cardboardAmountPicker.getValue(),
                "metalAmount" to metalAmountPicker.getValue(),
                "address" to address.text.toString(),
                "timestamp" to System.currentTimeMillis(),
                "status" to "pending",
                "userId" to FirebaseAuth.getInstance().currentUser?.uid,
                "latitude" to latitude,
                "longitude" to longitude
            )

            db.collection("orders")
                .add(order)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(context, "Pickup successfully booked!", Toast.LENGTH_LONG).show()
                    showSuccessDialog()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
        return view
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessDialog() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.custom_dialog_layout)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

        val okayButton: Button = dialog.findViewById(R.id.btn_okay)
        okayButton.setOnClickListener {
            dialog.dismiss()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Home())
                .addToBackStack(null)
                .commit()
        }
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getCurrentLocation()
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
