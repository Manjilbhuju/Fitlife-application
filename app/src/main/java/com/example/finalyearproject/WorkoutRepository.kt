package com.example.finalyearproject

object WorkoutRepository {
    val activeWorkouts = mutableListOf<Workout>()
    val completedWorkouts = mutableListOf<Workout>()

    init {
        // Initial dummy data
        if (activeWorkouts.isEmpty() && completedWorkouts.isEmpty()) {
            val yogaExercises = mutableListOf(
                Exercise("Downward-Facing Dog", 1, "5 breaths", "Start on all fours. Lift your hips up and back to form an inverted V."),
                Exercise("Warrior II", 1, "5 breaths per side", "Stand with feet wide apart. Turn your right foot out 90 degrees. Bend your right knee.")
            )
            activeWorkouts.add(Workout("Morning Yoga Flow", "Beginner", 4, 1, 75, mutableListOf("Yoga Mat", "Block"), "Flexibility", "", yogaExercises))

            val hiitExercises = mutableListOf(
                Exercise("Jumping Jacks", 3, "30 seconds", "Start with feet together and arms at your sides. Jump your feet out and raise your arms."),
                Exercise("High Knees", 3, "30 seconds", "Run in place, bringing your knees up to your chest.")
            )
            activeWorkouts.add(Workout("HIIT Cardio Blast", "Intermediate", 6, 1, 40, mutableListOf("No Equipment"), "Fat Loss", "", hiitExercises))
        }
    }
}