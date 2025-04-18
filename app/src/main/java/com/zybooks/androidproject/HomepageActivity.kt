package com.zybooks.androidproject

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class HomepageActivity : AppCompatActivity() {

    private lateinit var categoriesContainer: LinearLayout
    private val db = Firebase.firestore
    private val userId = Firebase.auth.currentUser?.uid ?: "guest"
    private val categoryNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // supportActionBar?.setDisplayHomeAsUpEnabled(true)


        categoriesContainer = findViewById(R.id.categoriesContainer)
        loadUserCategories()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserCategories() {
        categoriesContainer.removeAllViews()
        categoryNames.clear()

        db.collection("users").document(userId).collection("categories")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val categoryName = document.id
                    categoryNames.add(categoryName)

                    val categoryLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }

                    val categoryButton = Button(this).apply {
                        text = categoryName
                        setOnClickListener {
                            val intent = Intent(this@HomepageActivity, SubjectActivity::class.java)
                            intent.putExtra("category_name", categoryName)
                            startActivity(intent)
                        }
                    }

                    val removeButton = Button(this).apply {
                        text = "X"
                        setOnClickListener {
                            removeCategory(categoryName)
                        }
                    }

                    categoryLayout.addView(categoryButton)
                    categoryLayout.addView(removeButton)

                    categoriesContainer.addView(categoryLayout)
                }

                addUtilityButtons()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
                addUtilityButtons()
            }
    }

    private fun addUtilityButtons() {
        val addCategoryButton = Button(this).apply {
            text = "Add Category"
            setOnClickListener {
                showAddCategoryDialog()
            }
        }
        categoriesContainer.addView(addCategoryButton)

        Firebase.auth.currentUser?.let {
            val logoutButton = Button(this).apply {
                text = "Logout"
                setOnClickListener {
                    Firebase.auth.signOut()
                    Toast.makeText(this@HomepageActivity, "Logged out", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@HomepageActivity, MainActivity::class.java))
                    finish()
                }
            }
            categoriesContainer.addView(logoutButton)
        }
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Category")

        val input = EditText(this)
        input.hint = "Enter category name"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val categoryName = input.text.toString().trim()
            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (categoryNames.contains(categoryName)) {
                Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
            } else {
                saveCategoryToFirebase(categoryName)
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun saveCategoryToFirebase(category: String) {
        val data = hashMapOf("createdAt" to System.currentTimeMillis())
        db.collection("users").document(userId)
            .collection("categories").document(category)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                loadUserCategories()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeCategory(category: String) {
        val categoryRef = db.collection("users").document(userId).collection("categories").document(category)
        val subcategoriesRef = categoryRef.collection("subcategories")

        // First delete all subcategories
        subcategoriesRef.get().addOnSuccessListener { subcategories ->
            val batch = db.batch()
            for (doc in subcategories) {
                batch.delete(doc.reference)
            }

            batch.commit().addOnSuccessListener {
                // Then delete the category itself
                categoryRef.delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category and its subcategories removed", Toast.LENGTH_SHORT).show()
                        loadUserCategories()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to remove category", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to delete subcategories", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch subcategories", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
