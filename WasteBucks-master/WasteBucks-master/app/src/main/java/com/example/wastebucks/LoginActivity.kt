package com.example.wastebucks

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.wastebucks.admin.AdminScreen
import com.example.wastebucks.driver.DriverDashboardActivity
import com.example.wastebucks.driver.PickupRequestModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        ref = database.getReference("Users")

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        val forgotPassword = findViewById<TextView>(R.id.forgot_password)
        forgotPassword.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Forgot Password")
            dialog.setMessage("Enter your email address.")

            val inputField = EditText(this)
            dialog.setView(inputField)

            dialog.setPositiveButton("Send") { _, _ ->
                val email = inputField.text.toString()
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to send reset email!", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            dialog.setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }

            dialog.create().show()
        }

        val email = findViewById<EditText>(R.id.logemail)
        val password = findViewById<EditText>(R.id.log_pwd)
        val loginButton = findViewById<TextView>(R.id.login_btn)

        val signInButton = findViewById<LinearLayout>(R.id.linearLayout3)
        signInButton.setOnClickListener {
            signIn()
        }

        findViewById<TextView>(R.id.login).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            when {
                emailText.isEmpty() -> {
                    email.error = "Please enter an email"
                    email.requestFocus()
                }
                !emailText.contains("@") -> {
                    email.error = "Please enter a valid email"
                    email.requestFocus()
                }
                passwordText.isEmpty() -> {
                    password.error = "Please enter a password"
                    password.requestFocus()
                }
                passwordText.length < 8 -> {
                    password.error = "Password should be at least 8 characters long"
                    password.requestFocus()
                }
                else -> {
                    loginUser(emailText, passwordText)
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        progressDialog.setMessage("Logging In...")
        progressDialog.show()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Login Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        progressDialog.setMessage("Checking User...")
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "User authentication failed", Toast.LENGTH_SHORT).show()
            return
        }

        ref.child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()

                if (!snapshot.exists()) {
                    Toast.makeText(this@LoginActivity, "User not found in database!", Toast.LENGTH_SHORT).show()
                    return
                }

                val userName = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                val userType = snapshot.child("userType").getValue(String::class.java)

                Log.d(TAG, "User Data - Name: $userName, Type: $userType")

                when (userType) {
                    "user" -> {
                        Toast.makeText(this@LoginActivity, "Signed in as $userName", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    "admin" -> {
                        val txt = "Welcome Admin<br/>Signed in as $userName"
                        Toast.makeText(this@LoginActivity, Html.fromHtml(txt), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, com.example.wastebucks.admin.AdminDashboard::class.java))
                        finish()
                    }
                    "driver" -> {
                        Toast.makeText(this@LoginActivity, "Signed in as Driver: $userName", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, DriverDashboardActivity::class.java))
                        finish()
                    }
                    else -> {
                        Toast.makeText(this@LoginActivity, "User type not found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid
                    val timestamp = System.currentTimeMillis().toString()

                    // Check if user exists in the database
                    ref.child(uid ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                // User doesn't exist, so add them to the database
                                val hashMap = hashMapOf(
                                    "uid" to (uid ?: ""),
                                    "email" to (user?.email ?: ""),
                                    "name" to (user?.displayName ?: ""),
                                    "profileImage" to "",
                                    "userType" to "user", // Default user type, can be updated as needed
                                    "points" to "0",
                                    "timestamp" to timestamp
                                )

                                ref.child(uid ?: "").setValue(hashMap)
                                    .addOnSuccessListener {
                                        progressDialog.dismiss()
                                        Toast.makeText(this@LoginActivity, "Login Success...", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        progressDialog.dismiss()
                                        Toast.makeText(this@LoginActivity, "SignIn Failed", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                // User exists, proceed with the normal login
                                progressDialog.dismiss()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            progressDialog.dismiss()
                            Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
