package com.example.wastebucks.driver

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wastebucks.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class DriverLoginActivity : AppCompatActivity() {
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        // Initialize views
        emailField = findViewById(R.id.driverEmail)
        passwordField = findViewById(R.id.driverPassword)
        loginButton = findViewById(R.id.driverLoginButton)
        progressBar = findViewById(R.id.driverLoginProgress)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().getReference("Users")

        loginButton.setOnClickListener { loginDriver() }
    }

    private fun loginDriver() {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        // Validate input fields
        if (TextUtils.isEmpty(email)) {
            emailField.error = "Email is required"
            return
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.error = "Password is required"
            return
        }

        progressBar.visibility = View.VISIBLE
        loginButton.isEnabled = false

        // Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                loginButton.isEnabled = true

                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    currentUser?.let {
                        checkUserType(it.uid)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Login Failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun checkUserType(uid: String) {
        usersRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userType = snapshot.child("userType").getValue(String::class.java)

                if ("driver".equals(userType, ignoreCase = true)) {
                    Toast.makeText(this@DriverLoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@DriverLoginActivity, DriverDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@DriverLoginActivity, "Access denied: Not a driver", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DriverLoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
