package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.Executors
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.graphics.BitmapFactory
import okhttp3.ResponseBody

class Settings : AppCompatActivity() {

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                findViewById<android.widget.ImageView>(R.id.profileImage)
                    .setImageURI(uri)

                uploadProfileImage(uri)
            }
        }

    private val background = Executors.newSingleThreadExecutor()

    private lateinit var pendingText: TextView
    private lateinit var lastSyncText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        loadProfileImage()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("PulseFit", "Settings screen opened")

        val chooseProfilePictureButton =
            findViewById<MaterialButton>(R.id.chooseProfilePictureButton)

        chooseProfilePictureButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        val languageSpinner      = findViewById<Spinner>(R.id.languageSpinner)
        val unitsSpinner         = findViewById<Spinner>(R.id.unitsSpinner)
        val syncSwitch           = findViewById<MaterialSwitch>(R.id.syncSwitch)
        val notificationsSwitch  = findViewById<MaterialSwitch>(R.id.notificationsSwitch)
        val testNotification     = findViewById<MaterialButton>(R.id.testNotificationButton)
        val profileDetailsButton = findViewById<MaterialButton>(R.id.profileDetailsButton)
        val manualSyncButton     = findViewById<MaterialButton>(R.id.manualSyncButton)
        val logoutButton         = findViewById<MaterialButton>(R.id.logoutButton)
        val bottomNav            = findViewById<BottomNavigationView>(R.id.bottomNav)
        pendingText              = findViewById(R.id.pendingText)
        lastSyncText             = findViewById(R.id.lastSyncText)

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

        val preferences = AppPrefs.get(this)

        // ---------- Language ----------
        languageSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            LocaleHelper.names
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        languageSpinner.setSelection(LocaleHelper.tags.indexOf(LocaleHelper.currentTag(this)))

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val tag = LocaleHelper.tags[position]

                // Spinner fires once when the screen opens, so only act on a real change
                if (tag != LocaleHelper.currentTag(this@Settings)) {
                    LocaleHelper.apply(this@Settings, tag)
                    PushHelper.createChannel(this@Settings)   // channel name changes language too
                    Log.d("Settings", "Language changed to $tag")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ---------- Units ----------
        val units = arrayOf(getString(R.string.units_metric), getString(R.string.units_imperial))
        unitsSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            units
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        unitsSpinner.setSelection(preferences.getInt(AppPrefs.KEY_UNITS, 0))

        unitsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferences.edit().putInt(AppPrefs.KEY_UNITS, position).apply()
                Log.d("Settings", "Unit preference saved")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ---------- Offline mode & sync ----------
        syncSwitch.isChecked = preferences.getBoolean(AppPrefs.KEY_AUTO_SYNC, true)

        syncSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(AppPrefs.KEY_AUTO_SYNC, isChecked).apply()
            Toast.makeText(
                this,
                if (isChecked) getString(R.string.auto_sync_enabled) else getString(R.string.auto_sync_disabled),
                Toast.LENGTH_SHORT
            ).show()
            Log.d("PulseFit", "Auto-sync preference: $isChecked")

            if (isChecked) SyncScheduler.requestSync(this)
        }

        manualSyncButton.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            manualSyncButton.isEnabled = false

            background.execute {
                val success = MealRepository(this).sync()

                runOnUiThread {
                    manualSyncButton.isEnabled = true
                    Toast.makeText(
                        this,
                        if (success) getString(R.string.sync_complete) else getString(R.string.sync_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    refreshSyncStatus()
                }
            }
        }

        // ---------- Push notifications ----------
        notificationsSwitch.isChecked = preferences.getBoolean(AppPrefs.KEY_PUSH, true)

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            PushHelper.setEnabled(this, isChecked)
            if (isChecked) PushHelper.requestPermissionIfNeeded(this)
        }

        testNotification.setOnClickListener {
            if (!NetworkUtils.isInternetAvailable(this)) {
                Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PushHelper.requestPermissionIfNeeded(this)

            // The API sends a push to this user's phones; it arrives through Firebase Cloud Messaging
            ApiClient.apiService.sendTestNotification().enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    val message = if (response.isSuccessful) R.string.test_notification_requested
                    else R.string.test_notification_failed
                    Toast.makeText(this@Settings, getString(message), Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("PulseFitPush", "Test notification request failed", t)
                    Toast.makeText(this@Settings, getString(R.string.test_notification_failed), Toast.LENGTH_SHORT).show()
                }
            })
        }

        // ---------- Profile ----------
        profileDetailsButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser == null) {
                Toast.makeText(this, getString(R.string.please_log_in_again), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val notProvided = getString(R.string.not_provided)

                        val name   = document.getString("name")  ?: notProvided
                        val email  = document.getString("email") ?: currentUser.email ?: notProvided
                        val weight = document.get("weight")?.toString() ?: notProvided
                        val height = document.get("height")?.toString() ?: notProvided
                        val age    = document.get("age")?.toString()    ?: notProvided
                        val goal   = document.getString("goal")  ?: notProvided

                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.profile_details_title))
                            .setMessage(getString(R.string.profile_details_body, name, email, weight, height, age, goal))
                            .setPositiveButton(getString(R.string.close), null)
                            .show()

                        Log.d("PulseFit", "Profile details loaded")
                    } else {
                        Toast.makeText(this, getString(R.string.profile_not_found), Toast.LENGTH_SHORT).show()
                        Log.w("PulseFit", "User profile document not found")
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, getString(R.string.unable_load_profile), Toast.LENGTH_SHORT).show()
                    Log.e("PulseFit", "Profile details error", exception)
                }
        }

        logoutButton.setOnClickListener {
            // Push notifications must stop going to this phone for the old user
            FirebaseMessaging.getInstance().deleteToken()

            FirebaseAuth.getInstance().signOut()
            Log.d("PulseFit", "User logged out")
            Toast.makeText(this, getString(R.string.logged_out), Toast.LENGTH_SHORT).show()

            startActivity(
                Intent(this, login::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSyncStatus()
    }

    // Shows how many changes are waiting to upload and when we last synced
    private fun refreshSyncStatus() {
        background.execute {
            val pending = MealRepository(this).pendingCount()
            val lastSync = AppPrefs.get(this).getLong(AppPrefs.KEY_LAST_SYNC, 0L)

            runOnUiThread {
                val lastSyncLabel = if (lastSync == 0L) {
                    getString(R.string.never)
                } else {
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(lastSync))
                }

                pendingText.text = getString(R.string.pending_changes, pending)
                lastSyncText.text = getString(R.string.last_synced, lastSyncLabel)
            }
        }
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

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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
                "image", "profile_image", requestBody
            )

            currentUser.getIdToken(false)
                .addOnSuccessListener { tokenResult ->
                    val token = tokenResult.token

                    if (token == null) {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    ApiClient.apiService.uploadProfileImage(
                        "Bearer $token", imagePart
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