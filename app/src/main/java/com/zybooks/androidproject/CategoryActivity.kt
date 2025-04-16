package com.zybooks.androidproject

// CategoryActivity.kt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class CategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Set up the toolbar as the app's action bar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the Up button (back button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val categoryLayout: LinearLayout = findViewById(R.id.categoryLayout)

        // Example categories
        val categories = listOf("Science", "Math", "History")

        categories.forEach { category ->
            val categoryButton = Button(this)
            categoryButton.text = category
            categoryLayout.addView(categoryButton)
        }
    }

    // Handle the back button press (Up button)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // This will go back to the previous activity
        return true
    }

}
