package com.zybooks.androidproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomepageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        // Set up the toolbar as the app's action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the Up button (back button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Set item selection listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Home button click
                    val homeIntent = Intent(this, MainActivity::class.java)
                    startActivity(homeIntent)
                    true
                }
                else -> false
            }
        }

        // Set up categories (buttons)
        setupCategories()
    }

    // Handle the back button press (Up button)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // This will go back to the previous activity
        return true
    }

    private fun setupCategories() {
        val categories = listOf("Category 1", "Category 2", "Category 3", "Category 4") // Example categories

        // Find the container where the buttons will go
        val container = findViewById<LinearLayout>(R.id.categoriesContainer)

        // Add buttons dynamically for each category
        for (category in categories) {
            val button = Button(this)
            button.text = category
            button.setOnClickListener {
                // Handle button click for category
                val intent = Intent(this, CategoryActivity::class.java)
                intent.putExtra("CATEGORY_NAME", category)  // Pass category name to next activity
                startActivity(intent)
            }

            // Add the button to the container
            container.addView(button)
        }
    }
}
