package com.example.pulsefit_app

import android.os.CancellationSignal
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

// Google single sign-on, shared by the Login and Register screens.
// onFinished(true) = the user is now logged in to Firebase, onFinished(false) = it failed.
object GoogleSignInHelper {

    private const val TAG = "GoogleSignIn"

    fun signIn(activity: AppCompatActivity, onFinished: (Boolean) -> Unit) {

        // "Sign in with Google" button flow: always shows the account picker,
        // and lets the user add a Google account if the phone has none
        val option = GetSignInWithGoogleOption
            .Builder(activity.getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        CredentialManager.create(activity).getCredentialAsync(
            activity,
            request,
            CancellationSignal(),
            ContextCompat.getMainExecutor(activity),
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {

                override fun onResult(result: GetCredentialResponse) {
                    firebaseLogin(result.credential, onFinished)
                }

                override fun onError(e: GetCredentialException) {
                    // The type tells us why: NoCredential, Cancellation, "28444" = SHA-1 problem, ...
                    Log.e(TAG, "Credential Manager failed: ${e.type} - ${e.message}", e)
                    onFinished(false)
                }
            }
        )
    }

    private fun firebaseLogin(credential: Credential, onFinished: (Boolean) -> Unit) {

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Log.w(TAG, "Unexpected credential type: ${credential.type}")
            onFinished(false)
            return
        }

        val googleToken = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleToken.idToken, null)

        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Firebase login with Google successful")
                    createProfileIfMissing(onFinished)
                } else {
                    Log.e(TAG, "Firebase rejected the Google login", task.exception)
                    onFinished(false)
                }
            }
    }

    // First-time Google users have no profile yet, so make an empty one (Settings reads it)
    private fun createProfileIfMissing(onFinished: (Boolean) -> Unit) {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onFinished(false)
            return
        }

        val profileRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

        profileRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    profileRef.set(
                        hashMapOf(
                            "name" to (user.displayName ?: ""),
                            "email" to (user.email ?: ""),
                            "weight" to "",
                            "height" to "",
                            "age" to "",
                            "goal" to ""
                        )
                    )
                }
                onFinished(true)
            }
            .addOnFailureListener {
                // Not a reason to block the login
                onFinished(true)
            }
    }
}