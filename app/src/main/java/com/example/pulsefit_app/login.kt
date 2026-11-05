package com.example.pulsefit_app

import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
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

                Toast.makeText(this, getString(R.string.enter_email_password), Toast.LENGTH_SHORT).show()

            } else {

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {

                            Log.d("Login", "User login successful")

                            Toast.makeText(
                                this,
                                getString(R.string.login_successful),
                                Toast.LENGTH_SHORT
                            ).show()

                            goToDashboard()

                        }
                        else {

                            Log.e(
                                "Login",
                                "Login failed",
                                task.exception
                            )

                            Toast.makeText(
                                this,
                                getString(R.string.login_failed, task.exception?.message ?: ""),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        }


        // Single sign-on with a Google account
        googleSignInButton.setOnClickListener {
            GoogleSignInHelper.signIn(this) { success ->
                if (success) {
                    goToDashboard()
                } else {
                    Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_LONG).show()
                }
            }
        }

        forgotPasswordLink.setOnClickListener {

            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {

                Toast.makeText(
                    this,
                    getString(R.string.enter_email_first),
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
                                getString(R.string.reset_email_sent),
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
                                getString(R.string.reset_email_failed),
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




    private fun goToDashboard() {
        startActivity(Intent(this, Dashboard::class.java))
        finish()
    }
}