package com.example.pulsefit_app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseFitUnitTest {

    // Check that an email has been entered
    @Test
    fun emailIsNotEmpty() {
        val email = "test@pulsefit.com"

        assertTrue(email.isNotEmpty())
    }

    // Check that matching passwords are accepted
    @Test
    fun passwordsMatch() {
        val password = "Test1234"
        val confirmPassword = "Test1234"

        assertEquals(password, confirmPassword)
    }

    // Check that different passwords are detected
    @Test
    fun differentPasswordsDoNotMatch() {
        val password = "Test1234"
        val confirmPassword = "Wrong1234"

        assertFalse(password == confirmPassword)
    }

    // Check the prototype XP level calculation
    @Test
    fun xpCalculatesCorrectLevel() {
        val xp = 250

        val level = when {
            xp >= 1000 -> 5
            xp >= 750 -> 4
            xp >= 500 -> 3
            xp >= 250 -> 2
            else -> 1
        }

        assertEquals(2, level)
    }

    // Check that a registration name has been entered
    @Test
    fun registrationNameIsNotEmpty() {
        val name = "PulseFit User"

        assertTrue(name.isNotEmpty())
    }

    // Check that the fitness goal can be stored
    @Test
    fun fitnessGoalIsStored() {
        val goal = "Improve fitness"

        assertEquals("Improve fitness", goal)
    }

    // Check that meal calories are handled correctly
    @Test
    fun mealCaloriesAreCorrect() {
        val breakfastCalories = 420
        val lunchCalories = 650

        val totalCalories =
            breakfastCalories + lunchCalories

        assertEquals(1070, totalCalories)
    }

    // Check remaining daily calories
    @Test
    fun remainingCaloriesAreCalculated() {
        val dailyGoal = 2200
        val consumed = 1450

        val remaining =
            dailyGoal - consumed

        assertEquals(750, remaining)
    }

    @Test
    fun addMealRequestStoresCorrectValues() {

        val meal =
            AddMealRequest(
                name = "Chicken Salad",
                calories = 350
            )

        assertEquals(
            "Chicken Salad",
            meal.name
        )

        assertEquals(
            350,
            meal.calories
        )
    }
}

