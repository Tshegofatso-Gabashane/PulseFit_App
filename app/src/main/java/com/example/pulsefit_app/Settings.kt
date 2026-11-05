package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.graphics.BitmapFactory
import okhttp3.ResponseBody
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.firebase.firestore.FirebaseFirestore

class Settings : AppCompatActivity() {
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                findViewById<android.widget.ImageView>(R.id.profileImage)
                    .setImageURI(uri)

                uploadProfileImage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("PulseFit", "Settings screen opened")

        val languageSpinner      = findViewById<Spinner>(R.id.languageSpinner)
        val unitsSpinner         = findViewById<Spinner>(R.id.unitsSpinner)
        val syncSwitch           = findViewById<MaterialSwitch>(R.id.syncSwitch)
        val profileDetailsButton = findViewById<MaterialButton>(R.id.profileDetailsButton)
        val profileImage = findViewById<android.widget.ImageView>(R.id.profileImage)
        val chooseProfilePictureButton = findViewById<MaterialButton>(R.id.chooseProfilePictureButton)
        val manualSyncButton     = findViewById<MaterialButton>(R.id.manualSyncButton)
        val logoutButton         = findViewById<MaterialButton>(R.id.logoutButton)
        val bottomNav            = findViewById<BottomNavigationView>(R.id.bottomNav)



        bottomNav.selectedItemId = R.id.settingsButton

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.settingsButton -> true   // already here

                R.id.homeButton     -> navigateTo(Dashboard::class.java)
                R.id.mapButton      -> navigateTo(Map::class.java)
                R.id.exerciseButton -> navigateTo(Exercises::class.java)
                R.id.dietButton     -> navigateTo(Diet::class.java)
                else -> false
            }
        }

        chooseProfilePictureButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        val preferences = getSharedPreferences("PulseFitSettings", MODE_PRIVATE)

        val languages = arrayOf("English", "isiZulu", "Sesotho")
        languageSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val units = arrayOf("Metric (kg/km)", "Imperial (lb/mi)")
        unitsSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            units
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        languageSpinner.setSelection(preferences.getInt("language_position", 0))
        unitsSpinner.setSelection(preferences.getInt("units_position", 0))
        syncSwitch.isChecked = preferences.getBoolean("sync_enabled", false)

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                preferences.edit()
                    .putInt("language_position", position)
                    .apply()

                val languageCodes = arrayOf("en", "zu", "st")
                val selectedLanguage = languageCodes[position]

                val currentLanguage =
                    AppCompatDelegate.getApplicationLocales().toLanguageTags()

                if (currentLanguage != selectedLanguage) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(selectedLanguage)
                    )
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        unitsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferences.edit().putInt("units_position", position).apply()
                Log.d("Settings", "Unit preference saved")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        syncSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean("sync_enabled", isChecked).apply()
            Toast.makeText(
                this,
                if (isChecked) "Cloud sync enabled" else "Cloud sync disabled",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("PulseFit", "Sync preference: $isChecked")
        }

        profileDetailsButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name   = document.getString("name")  ?: "Not provided"
                        val email  = document.getString("email") ?: currentUser.email ?: "Not provided"
                        val weight = document.get("weight")?.toString() ?: "Not provided"
                        val height = document.get("height")?.toString() ?: "Not provided"
                        val age    = document.get("age")?.toString()    ?: "Not provided"
                        val goal   = document.getString("goal")  ?: "Not provided"

                        val profileMessage = """
                            Name: $name
                            Email: $email
                            Weight: $weight
                            Height: $height
                            Age: $age
                            Goal: $goal
                        """.trimIndent()

                        AlertDialog.Builder(this)
                            .setTitle("Profile Details")
                            .setMessage(profileMessage)
                            .setPositiveButton("CLOSE", null)
                            .show()

                        Log.d("PulseFit", "Profile details loaded")
                    } else {
                        Toast.makeText(this, "Profile details not found", Toast.LENGTH_SHORT).show()
                        Log.w("PulseFit", "User profile document not found")
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Unable to load profile details", Toast.LENGTH_SHORT).show()
                    Log.e("PulseFit", "Profile details error", exception)
                }
        }

        manualSyncButton.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance()
                .collection("exercises")
                .get()
                .addOnSuccessListener { snapshot ->
                    Toast.makeText(this, "Cloud connection successful", Toast.LENGTH_SHORT).show()
                    Log.d("PulseFit", "Cloud check successful: ${snapshot.size()} exercises found")
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Cloud connection failed", Toast.LENGTH_SHORT).show()
                    Log.e("PulseFit", "Cloud check failed", exception)
                }
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Log.d("PulseFit", "User logged out")
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            startActivity(
                Intent(this, login::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }

        loadProfileImage()

    }

    private fun loadProfileImage() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) return

        currentUser.getIdToken(false)
            .addOnSuccessListener { tokenResult ->

                val token = tokenResult.token ?: return@addOnSuccessListener

                ApiClient.apiService.getProfileImage("Bearer $token")
                    .enqueue(object : Callback<ResponseBody> {

                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                val imageBytes = response.body()?.bytes()

                                if (imageBytes != null) {
                                    val bitmap = BitmapFactory.decodeByteArray(
                                        imageBytes, 0, imageBytes.size
                                    )

                                    findViewById<android.widget.ImageView>(
                                        R.id.profileImage
                                    ).setImageBitmap(bitmap)
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<ResponseBody>,
                            t: Throwable
                        ) {
                            Log.e("ProfileImage", "Image loading failed", t)
                        }
                    })
            }
            .addOnFailureListener {
                Log.e("ProfileImage", "Could not verify account", it)
            }
    }

    private fun uploadProfileImage(uri: Uri) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Please sign in first.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

            val imageBytes = contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            }

            if (imageBytes == null) {
                Toast.makeText(this, "Unable to read image.", Toast.LENGTH_SHORT).show()
                return
            }

            val requestBody = imageBytes.toRequestBody(mimeType.toMediaType())

            val imagePart = MultipartBody.Part.createFormData(
                "image",
                "profile_image",
                requestBody
            )

            currentUser.getIdToken(false)
                .addOnSuccessListener { tokenResult ->

                    val token = tokenResult.token

                    if (token == null) {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    ApiClient.apiService.uploadProfileImage(
                        "Bearer $token",
                        imagePart
                    ).enqueue(object : Callback<ImageUploadResponse> {

                        override fun onResponse(
                            call: Call<ImageUploadResponse>,
                            response: Response<ImageUploadResponse>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@Settings,
                                    "Profile image uploaded successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@Settings,
                                    "Upload failed: ${response.code()}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(
                            call: Call<ImageUploadResponse>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                this@Settings,
                                "Upload error: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Could not verify your account.", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Image error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateTo(destination: Class<*>): Boolean {
        startActivity(Intent(this, destination))
        overridePendingTransition(0, 0)
        return true
    }
}