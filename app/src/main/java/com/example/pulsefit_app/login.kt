package com.example.pulsefit_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        val forgotPasswordLink = findViewById<TextView>(R.id.forgotPasswordLink)
        val registerLink = findViewById<TextView>(R.id.registerLink)

        loginButton.setOnClickListener {

            Log.d("PulseFit", "Login button clicked")
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {

                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()

            } else {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {

                            Log.d("Login", "User login successful")

                            Toast.makeText(
                                this,
                                "Login successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent =
                                Intent(this, Dashboard::class.java)

                            startActivity(intent)
                            finish()

                        }
                        else {

                            Log.e(
                                "Login",
                                "Login failed",
                                task.exception
                            )

                            Toast.makeText(
                                this,
                                "Login failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        }
            }
        }


        googleSignInButton.setOnClickListener {

            Toast.makeText(this, "Google Sign-In will be connected later", Toast.LENGTH_SHORT).show()
        }

        forgotPasswordLink.setOnClickListener {

            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {

                Toast.makeText(
                    this,
                    "Enter your email address first",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            Log.d(
                                "PulseFit",
                                "Password reset email requested"
                            )

                            Toast.makeText(
                                this,
                                "Password reset email sent",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {

                            Log.e(
                                "PulseFit",
                                "Password reset failed",
                                task.exception
                            )

                            Toast.makeText(
                                this,
                                "Unable to send reset email",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }

        registerLink.setOnClickListener {

            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

    }
}