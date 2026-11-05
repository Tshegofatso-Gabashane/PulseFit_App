package com.example.pulsefit_app

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

// All meal reads/writes go through here. The screens only ever talk to the local
// Room database, so they work offline. sync() then brings the server in line.
// These functions block, so call them from a background thread.
class MealRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dao = AppDatabase.getInstance(appContext).mealDao()

    private fun currentUserId(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun getMeals(): List<MealEntity> = dao.getVisibleMeals(currentUserId())

    fun pendingCount(): Int = dao.countPending(currentUserId())

    // Works with or without internet: saves locally, then asks WorkManager to sync
    fun addMeal(name: String, calories: Int) {
        dao.upsert(
            MealEntity(
                id = UUID.randomUUID().toString(),
                ownerId = currentUserId(),
                name = name,
                calories = calories,
                createdAt = System.currentTimeMillis(),
                syncState = MealEntity.PENDING_UPLOAD
            )
        )
        SyncScheduler.requestSync(appContext)
    }

    fun deleteMeal(meal: MealEntity) {
        if (meal.syncState == MealEntity.PENDING_UPLOAD) {
            // The server never saw it, so just remove it
            dao.deleteById(meal.id)
        } else {
            dao.setSyncState(meal.id, MealEntity.PENDING_DELETE)
        }
        SyncScheduler.requestSync(appContext)
    }

    // Returns true when everything is in sync, false if it should be tried again later
    fun sync(): Boolean = synchronized(SYNC_LOCK) { doSync() }

    private fun doSync(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val api = ApiClient.apiService

        try {
            // 1. Send deletes made offline
            for (meal in dao.getByState(userId, MealEntity.PENDING_DELETE)) {
                val response = api.deleteMeal(meal.id).execute()
                if (!response.isSuccessful) return false
                dao.deleteById(meal.id)
            }

            // 2. Send meals added offline (the server ignores an id it already has)
            for (meal in dao.getByState(userId, MealEntity.PENDING_UPLOAD)) {
                val response = api.addMeal(
                    AddMealRequest(meal.id, meal.name, meal.calories, meal.createdAt)
                ).execute()
                if (!response.isSuccessful) return false
                dao.setSyncState(meal.id, MealEntity.SYNCED)
            }

            // 3. Download the server's list (picks up changes from other devices)
            val response = api.getMeals().execute()
            val serverMeals = response.body()
            if (!response.isSuccessful || serverMeals == null) return false

            val serverIds = serverMeals.map { it.id }.toSet()
            for (meal in dao.getByState(userId, MealEntity.SYNCED)) {
                if (meal.id !in serverIds) dao.deleteById(meal.id)   // deleted on another device
            }

            val stillPending = dao.getPendingIds(userId).toSet()
            dao.upsertAll(
                serverMeals
                    .filter { it.id !in stillPending }
                    .map { MealEntity(it.id, userId, it.name, it.calories, it.createdAt, MealEntity.SYNCED) }
            )

            AppPrefs.get(appContext)
                .edit()
                .putLong(AppPrefs.KEY_LAST_SYNC, System.currentTimeMillis())
                .apply()

            Log.d("PulseFitSync", "Sync finished")
            return true

        } catch (e: Exception) {
            Log.w("PulseFitSync", "Sync failed, will retry", e)
            return false
        }
    }

    companion object {
        private val SYNC_LOCK = Any()
    }
}