package com.example.pulsefit_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val fullNameInput =
            findViewById<EditText>(R.id.fullNameInput)

        val emailInput =
            findViewById<EditText>(R.id.emailInput)

        val passwordInput =
            findViewById<EditText>(R.id.passwordInput)

        val confirmPasswordInput =
            findViewById<EditText>(R.id.confirmPasswordInput)

        val weightInput =
            findViewById<EditText>(R.id.weightInput)

        val heightInput =
            findViewById<EditText>(R.id.heightInput)

        val ageInput =
            findViewById<EditText>(R.id.ageInput)

        val goalInput =
            findViewById<EditText>(R.id.goalInput)

        val termsCheckbox =
            findViewById<CheckBox>(R.id.termsCheckbox)

        val registerButton =
            findViewById<Button>(R.id.registerButton)

        val loginLink =
            findViewById<TextView>(R.id.loginLink)

        val googleButton =
            findViewById<Button>(R.id.googleButton)

        googleButton.setOnClickListener {
            GoogleSignInHelper.signIn(this) { success ->
                if (success) {
                    startActivity(Intent(this, Dashboard::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.google_sign_in_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Register new user
        registerButton.setOnClickListener {

            Log.d("PulseFit", "Register button clicked")

            val name =
                fullNameInput.text.toString().trim()

            val email =
                emailInput.text.toString().trim()

            val password =
                passwordInput.text.toString()

            val confirmPassword =
                confirmPasswordInput.text.toString()

            val weight =
                weightInput.text.toString().trim()

            val height =
                heightInput.text.toString().trim()

            val age =
                ageInput.text.toString().trim()

            val goal =
                goalInput.text.toString().trim()


            // Validate required information
            if (
                name.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                confirmPassword.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    getString(R.string.complete_required_fields),
                    Toast.LENGTH_SHORT
                ).show()

            } else if (password != confirmPassword) {

                Toast.makeText(
                    this,
                    getString(R.string.passwords_not_match),
                    Toast.LENGTH_SHORT
                ).show()

            } else if (!termsCheckbox.isChecked) {

                Toast.makeText(
                    this,
                    getString(R.string.accept_terms_toast),
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Log.d(
                    "PulseFit",
                    "Registration validation successful"
                )


                // Create Firebase Authentication account
                auth.createUserWithEmailAndPassword(
                    email,
                    password
                ).addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        val userId =
                            auth.currentUser?.uid

                        if (userId != null) {

                            // Profile information saved in Firestore.
                            // Password is NOT stored in Firestore.
                            val profile = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "weight" to weight,
                                "height" to height,
                                "age" to age,
                                "goal" to goal
                            )


                            db.collection("users")
                                .document(userId)
                                .set(profile)
                                .addOnSuccessListener {

                                    Log.d(
                                        "PulseFit",
                                        "User profile saved successfully"
                                    )

                                    Toast.makeText(
                                        this,
                                        getString(R.string.registration_successful),
                                        Toast.LENGTH_SHORT
                                    ).show()


                                    // Sign out so the user can log in
                                    auth.signOut()

                                    val intent =
                                        Intent(
                                            this,
                                            login::class.java
                                        )

                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { error ->

                                    Log.e(
                                        "PulseFit",
                                        "Profile save failed",
                                        error
                                    )

                                    Toast.makeText(
                                        this,
                                        getString(R.string.profile_not_saved, error.message ?: ""),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        } else {

                            Toast.makeText(
                                this,
                                getString(R.string.unable_create_profile),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {

                        Log.e(
                            "PulseFit",
                            "Registration failed",
                            task.exception
                        )

                        Toast.makeText(
                            this,
                            getString(R.string.registration_failed, task.exception?.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


        // Already have an account
        loginLink.setOnClickListener {

            val intent =
                Intent(
                    this,
                    login::class.java
                )

            startActivity(intent)
        }
    }
}