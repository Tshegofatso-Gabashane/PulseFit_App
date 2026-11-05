package com.example.pulsefit_app

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Receives push notifications from Firebase Cloud Messaging.
// The API sends "data" messages (a type plus some values) and the app builds the text here,
// so the notification appears in the language the user picked in Settings.
class PulseFitMessagingService : FirebaseMessagingService() {

    // Firebase gives this phone a new token
    override fun onNewToken(token: String) {
        Log.d("PulseFitPush", "New FCM token")
        PushHelper.sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // The user can switch notifications off in Settings
        if (!AppPrefs.get(this).getBoolean(AppPrefs.KEY_PUSH, true)) return

        val text = LocaleHelper.wrap(this)
        val data = message.data
        val name = data["name"] ?: ""
        val calories = data["calories"]?.toIntOrNull() ?: 0
        val total = data["total"]?.toIntOrNull() ?: 0
        val budget = data["budget"]?.toIntOrNull() ?: 0

        val title: String
        val body: String

        when (data["type"]) {
            "meal_synced" -> {
                title = text.getString(R.string.notif_meal_synced_title)
                body = text.getString(R.string.notif_meal_synced_body, name, calories)
            }
            "budget_reached" -> {
                title = text.getString(R.string.notif_budget_title)
                body = text.getString(R.string.notif_budget_body, total, budget)
            }
            "test" -> {
                title = text.getString(R.string.brand_name)
                body = text.getString(R.string.notif_test_body)
            }
            else -> {
                // A message written in the Firebase console: show it as it is
                val notification = message.notification ?: return
                title = notification.title ?: text.getString(R.string.brand_name)
                body = notification.body ?: return
            }
        }

        PushHelper.showNotification(this, title, body)
    }
}

object PushHelper {

    const val CHANNEL_ID = "pulsefit_alerts"

    // Everyone is subscribed to this topic, so announcements can be sent from the Firebase console
    private const val TOPIC = "announcements"

    private const val PERMISSION_REQUEST_CODE = 1001

    // Android 8+ needs a channel before it will show notifications
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val text = LocaleHelper.wrap(context)
        val channel = NotificationChannel(
            CHANNEL_ID,
            text.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = text.getString(R.string.notif_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // Android 13+ asks the user for permission before showing notifications
    fun requestPermissionIfNeeded(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // Call after login: tells the API which phone belongs to this user
    fun registerDevice(context: Context) {
        if (!AppPrefs.get(context).getBoolean(AppPrefs.KEY_PUSH, true)) return

        createChannel(context)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> sendTokenToServer(token) }
            .addOnFailureListener { Log.w("PulseFitPush", "Could not get FCM token", it) }

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        AppPrefs.get(context).edit().putBoolean(AppPrefs.KEY_PUSH, enabled).apply()

        if (enabled) {
            registerDevice(context)
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
        }
    }

    fun sendTokenToServer(token: String) {
        if (FirebaseAuth.getInstance().currentUser == null) return

        ApiClient.apiService.registerDevice(DeviceRequest(token)).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d("PulseFitPush", "Device registered: ${response.code()}")
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.w("PulseFitPush", "Device registration failed", t)
            }
        })
    }

    fun showNotification(context: Context, title: String, body: String) {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        if (needsPermission && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createChannel(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, Dashboard::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pulse)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}