package com.example.pulsefit_app

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

// Runs in the background (even if the app is closed) as soon as the phone has internet
class SyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        // Nobody logged in = nothing to sync
        if (FirebaseAuth.getInstance().currentUser == null) return Result.success()

        return if (MealRepository(applicationContext).sync()) Result.success() else Result.retry()
    }
}

object SyncScheduler {

    private const val WORK_NAME = "meal_sync"

    // Queue a sync. WorkManager waits for a network connection before running it.
    // force = true is used by the "Sync now" button, which ignores the auto-sync switch.
    fun requestSync(context: Context, force: Boolean = false) {
        val autoSync = AppPrefs.get(context).getBoolean(AppPrefs.KEY_AUTO_SYNC, true)
        if (!autoSync && !force) return

        val request = OneTimeWorkRequest.Builder(SyncWorker::class.java)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}