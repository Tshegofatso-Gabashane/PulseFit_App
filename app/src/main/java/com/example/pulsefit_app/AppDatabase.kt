package com.example.pulsefit_app

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// A meal stored on the phone. syncState says whether the server has it yet.
@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey val id: String,       // random UUID made on the phone, so the server can ignore duplicates
    val ownerId: String,              // Firebase uid, so two accounts on one phone never mix data
    val name: String,
    val calories: Int,
    val createdAt: Long,
    val syncState: Int
) {
    companion object {
        const val SYNCED = 0          // same as the server
        const val PENDING_UPLOAD = 1  // created offline, still has to be sent
        const val PENDING_DELETE = 2  // deleted offline, server still has to be told
    }
}

@Dao
interface MealDao {

    // Everything the user should see (meals waiting to be deleted are hidden)
    @Query("SELECT * FROM meals WHERE ownerId = :ownerId AND syncState != 2 ORDER BY createdAt DESC")
    fun getVisibleMeals(ownerId: String): List<MealEntity>

    @Query("SELECT * FROM meals WHERE ownerId = :ownerId AND syncState = :state")
    fun getByState(ownerId: String, state: Int): List<MealEntity>

    @Query("SELECT id FROM meals WHERE ownerId = :ownerId AND syncState != 0")
    fun getPendingIds(ownerId: String): List<String>

    @Query("SELECT COUNT(*) FROM meals WHERE ownerId = :ownerId AND syncState != 0")
    fun countPending(ownerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(meals: List<MealEntity>)

    @Query("UPDATE meals SET syncState = :state WHERE id = :id")
    fun setSyncState(id: String, state: Int)

    @Query("DELETE FROM meals WHERE id = :id")
    fun deleteById(id: String)
}

@Database(entities = [MealEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulsefit.db"
                ).build().also { instance = it }
            }
    }
}