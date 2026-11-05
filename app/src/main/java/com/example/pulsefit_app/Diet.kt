package com.example.pulsefit_app

import android.app.AlertDialog
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import java.util.Calendar
import java.util.concurrent.Executors

// Meals are saved in the local Room database first, so this screen works offline.
// When the phone is online the meals are synced with the PulseFit API (see MealRepository).
class Diet : AppCompatActivity() {

    private val dailyCalorieBudget = 2000

    private val background = Executors.newSingleThreadExecutor()
    private lateinit var repository: MealRepository

    private lateinit var mealList: LinearLayout
    private lateinit var emptyMealsText: TextView
    private lateinit var offlineBanner: TextView
    private lateinit var consumedText: TextView
    private lateinit var remainingText: TextView

    private var connectivityManager: ConnectivityManager? = null

    // Called by Android when the phone connects to or loses the internet
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Back online: send what was saved offline, then show the result
            background.execute {
                if (AppPrefs.get(this@Diet).getBoolean(AppPrefs.KEY_AUTO_SYNC, true)) {
                    repository.sync()
                }
                runOnUiThread { refreshScreen() }
            }
        }

        override fun onLost(network: Network) {
            runOnUiThread { refreshScreen() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_diet)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d("PulseFit", "Diet screen opened")

        repository = MealRepository(this)

        val barcodeButton = findViewById<MaterialButton>(R.id.barcodeButton)
        val addMealButton = findViewById<MaterialButton>(R.id.addMealButton)
        mealList          = findViewById(R.id.mealList)
        emptyMealsText    = findViewById(R.id.emptyMealsText)
        offlineBanner     = findViewById(R.id.offlineBanner)
        consumedText      = findViewById(R.id.consumedText)
        remainingText     = findViewById(R.id.remainingText)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.dietButton

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dietButton      -> true  // already on Diet, do nothing

                R.id.homeButton      -> navigateTo(Dashboard::class.java)
                R.id.mapButton       -> navigateTo(Map::class.java)
                R.id.exerciseButton  -> navigateTo(Exercises::class.java)
                R.id.settingsButton  -> navigateTo(Settings::class.java)
                else                 -> false
            }
        }

        barcodeButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.barcode_soon), Toast.LENGTH_SHORT).show()
        }

        addMealButton.setOnClickListener {
            showAddMealDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        refreshScreen()
        SyncScheduler.requestSync(this)

        // Watch the connection while this screen is open
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    private fun navigateTo(destination: Class<*>): Boolean {
        startActivity(Intent(this, destination))
        overridePendingTransition(0, 0)
        return true
    }

    // Reads today's meals from the local database and redraws the screen
    private fun refreshScreen() {
        offlineBanner.visibility =
            if (NetworkUtils.isInternetAvailable(this)) View.GONE else View.VISIBLE

        background.execute {
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todaysMeals = repository.getMeals().filter { it.createdAt >= startOfToday }

            runOnUiThread { showMeals(todaysMeals) }
        }
    }

    private fun showMeals(meals: List<MealEntity>) {
        mealList.removeAllViews()
        emptyMealsText.visibility = if (meals.isEmpty()) View.VISIBLE else View.GONE

        for (meal in meals) {
            val row = layoutInflater.inflate(R.layout.item_meal, mealList, false)

            row.findViewById<TextView>(R.id.mealName).text = meal.name
            row.findViewById<TextView>(R.id.mealCalories).text =
                getString(R.string.kcal_value, meal.calories)

            // Small "Waiting to sync" label for anything the server does not have yet
            row.findViewById<TextView>(R.id.mealSyncStatus).visibility =
                if (meal.syncState == MealEntity.SYNCED) View.GONE else View.VISIBLE

            row.findViewById<TextView>(R.id.mealDelete).setOnClickListener {
                deleteMeal(meal)
            }

            mealList.addView(row)
        }

        val consumed = meals.sumOf { it.calories }
        consumedText.text = consumed.toString()
        remainingText.text = (dailyCalorieBudget - consumed).coerceAtLeast(0).toString()
    }

    private fun deleteMeal(meal: MealEntity) {
        background.execute {
            repository.deleteMeal(meal)

            runOnUiThread {
                Toast.makeText(this, getString(R.string.meal_deleted), Toast.LENGTH_SHORT).show()
                refreshScreen()
            }
        }
    }

    private fun showAddMealDialog() {
        val mealNameInput = EditText(this).apply { hint = getString(R.string.meal_name_hint) }

        val caloriesInput = EditText(this).apply {
            hint = getString(R.string.calories_hint)
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(mealNameInput)
            addView(caloriesInput)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_meal))
            .setView(dialogLayout)
            .setPositiveButton(getString(R.string.add_button)) { _, _ ->
                val mealName = mealNameInput.text.toString().trim()
                val calories = caloriesInput.text.toString().toIntOrNull()

                if (mealName.isEmpty() || calories == null) {
                    Toast.makeText(this, getString(R.string.enter_valid_meal), Toast.LENGTH_SHORT).show()
                } else {
                    addMeal(mealName, calories)
                }
            }
            .setNegativeButton(getString(R.string.cancel_button), null)
            .show()
    }

    // Always saved locally; it is uploaded straight away if online, or later if offline
    private fun addMeal(name: String, calories: Int) {
        val online = NetworkUtils.isInternetAvailable(this)

        background.execute {
            repository.addMeal(name, calories)

            runOnUiThread {
                Toast.makeText(
                    this,
                    getString(if (online) R.string.meal_added else R.string.meal_saved_offline),
                    Toast.LENGTH_SHORT
                ).show()
                refreshScreen()
            }

            // Online: upload straight away so the "Waiting to sync" label clears
            if (online && AppPrefs.get(this).getBoolean(AppPrefs.KEY_AUTO_SYNC, true)) {
                repository.sync()
                runOnUiThread { refreshScreen() }
            }
        }
    }
}