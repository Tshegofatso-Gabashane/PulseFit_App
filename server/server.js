const express = require('express');
const app = express();
const PORT = 3000;

app.use(express.json());

// Hardcoded exercise data
const exercises = [
    { id: 1, name: 'Bench Press',        muscleGroup: 'Chest',     category: 'Strength', difficulty: 6.0 },
    { id: 2, name: 'Squats',             muscleGroup: 'Legs',      category: 'Strength', difficulty: 7.5 },
    { id: 3, name: 'Running',            muscleGroup: 'Full Body', category: 'Cardio',   difficulty: 8.5 },
    { id: 4, name: 'Cycling',            muscleGroup: 'Legs',      category: 'Cardio',   difficulty: 6.8 },
    { id: 5, name: 'Push-ups',           muscleGroup: 'Chest',     category: 'Strength', difficulty: 3.8 },
    { id: 6, name: 'Plank',              muscleGroup: 'Core',      category: 'Strength', difficulty: 3.0 },
    { id: 7, name: 'Jumping Jacks',      muscleGroup: 'Full Body', category: 'Cardio',   difficulty: 5.5 },
    { id: 8, name: 'Deadlift',           muscleGroup: 'Back',      category: 'Strength', difficulty: 8.0 },
];

// Hardcoded meal data
const meals = [
    { id: 1, name: 'Oatmeal + Banana',   calories: 320 },
    { id: 2, name: 'Chicken Salad',      calories: 450 },
    { id: 3, name: 'Grilled Salmon',     calories: 520 },
];

// ---------- ROUTES ----------

// Get all exercises
app.get('/exercises', (req, res) => {
    console.log('GET /exercises');
    res.json(exercises);
});

// Get all meals
app.get('/meals', (req, res) => {
    console.log('GET /meals');
    res.json(meals);
});

// Add a meal (accepts POST)
app.post('/meals', (req, res) => {
    console.log('POST /meals body:', req.body);
    const newMeal = {
        id: meals.length + 1,
        name: req.body.name || 'Unknown',
        calories: req.body.calories || 0,
    };
    meals.push(newMeal);
    res.json(newMeal);
});

// Health check
app.get('/', (req, res) => {
    res.send('PulseFit API is running');
});

// ---------- START ----------
app.listen(PORT, '0.0.0.0', () => {
    console.log(`\n✅ PulseFit API running at http://localhost:${PORT}`);
    console.log(`   Emulator can reach it at http://10.0.2.2:${PORT}\n`);
});