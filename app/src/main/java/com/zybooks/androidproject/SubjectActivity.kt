package com.zybooks.androidproject

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import java.util.*

class SubjectActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val db = Firebase.firestore
    private lateinit var subjectLayout: LinearLayout
    private var subjectName: String? = null

    companion object {
        private const val TAG = "SubjectActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject)

        // Initialize TTS
        tts = TextToSpeech(this, this)

        // Set up audio manager
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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


        subjectLayout = findViewById(R.id.subjectLayout)
        subjectName = intent.getStringExtra("category_name") ?: run {
            Toast.makeText(this, "No category selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadSubjectCategories(subjectName!!)
    }

    override fun onInit(status: Int) {
        when (status) {
            TextToSpeech.SUCCESS -> {
                val result = tts?.setLanguage(Locale.US)
                when (result) {
                    TextToSpeech.LANG_MISSING_DATA -> {
                        val installIntent = Intent()
                        installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                        startActivity(installIntent)
                    }
                    TextToSpeech.LANG_NOT_SUPPORTED -> {
                        Toast.makeText(this, "Language not supported", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        isTtsReady = true
                        speakText("Ready")
                    }
                }
            }
            else -> {
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadSubjectCategories(subject: String) {
        subjectLayout.removeAllViews()
        supportActionBar?.title = subject

        db.collection("users")
            .document(Firebase.auth.currentUser?.uid ?: "guest")
            .collection("categories")
            .document(subject)
            .collection("subcategories")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(this, "No subcategories found", Toast.LENGTH_SHORT).show()
                }

                result.forEach { document ->
                    val subcategoryName = document.id
                    createSubcategoryButton(subject, subcategoryName)
                }

                addAddButton(subject)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load subcategories", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createSubcategoryButton(subject: String, name: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }

        val button = Button(this).apply {
            text = name
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener {
                speakText(name)
            }
        }

        val removeBtn = Button(this).apply {
            text = "X"
            setOnClickListener {
                removeSubcategory(subject, name)  // Now properly defined below
            }
        }

        layout.addView(button)
        layout.addView(removeBtn)
        subjectLayout.addView(layout)
    }

    private fun removeSubcategory(subject: String, subcategory: String) {
        db.collection("users")
            .document(Firebase.auth.currentUser?.uid ?: "guest")
            .collection("categories")
            .document(subject)
            .collection("subcategories")
            .document(subcategory)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Subcategory removed", Toast.LENGTH_SHORT).show()
                loadSubjectCategories(subject)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove subcategory", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addAddButton(subject: String) {
        val button = Button(this).apply {
            text = "Add Subcategory"
            setOnClickListener { showAddSubcategoryDialog(subject) }
        }
        subjectLayout.addView(button)
    }

    private fun showAddSubcategoryDialog(subject: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Subcategory")

        val input = EditText(this)
        input.hint = "Enter subcategory name"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val subcategoryName = input.text.toString().trim()
            if (subcategoryName.isNotEmpty()) {
                saveSubcategoryToFirebase(subject, subcategoryName)
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun saveSubcategoryToFirebase(subject: String, subcategory: String) {
        val data = hashMapOf("createdAt" to System.currentTimeMillis())
        db.collection("users")
            .document(Firebase.auth.currentUser?.uid ?: "guest")
            .collection("categories")
            .document(subject)
            .collection("subcategories")
            .document(subcategory)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Subcategory added", Toast.LENGTH_SHORT).show()
                createSubcategoryButton(subject, subcategory)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add subcategory", Toast.LENGTH_SHORT).show()
            }
    }

    private fun speakText(text: String) {
        if (isTtsReady && tts != null) {
            tts?.stop()
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}

