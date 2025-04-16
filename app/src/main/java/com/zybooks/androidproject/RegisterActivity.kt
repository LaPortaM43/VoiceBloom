package com.zybooks.androidproject

// RegisterActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.fullNameEditText)
        addressEditText = findViewById(R.id.addressEditText)
        registerButton = findViewById(R.id.registerButton)

        registerButton.setOnClickListener{
            registerUser()
        }

        // Set up the toolbar as the app's action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the Up button (back button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        // Bottom navigation setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> startActivity(Intent(this, MainActivity::class.java))
            }
            true
        }
    }

    // Handle the back button press (Up button)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // This will go back to the previous activity
        return true
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let {
                    val customer = hashMapOf(
                        "customerID" to user.uid,
                        "customerEmail" to email,
                        "customerName" to name,
                        "customerAddress" to address,
                        "customerPassword" to password
                    )
                    db.collection("customers").document(user.uid)
                        .set(customer)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this, HomepageActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                baseContext, "Failed to save user data: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }
    }
}