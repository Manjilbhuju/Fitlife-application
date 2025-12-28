package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class MainActivity : AppCompatActivity(), MyWorkoutsAdapter.OnWorkoutActionListener {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var workoutsAdapter: MyWorkoutsAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvWorkoutSummary: TextView

    private val addWorkoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val routineName = data?.getStringExtra("ROUTINE_NAME") ?: "New Workout"
            val goal = data?.getStringExtra("GOAL") ?: ""
            val difficulty = data?.getStringExtra("DIFFICULTY") ?: "Beginner"
            val durationWeeks = data?.getIntExtra("DURATION_WEEKS", 4) ?: 4
            val hoursPerDay = data?.getIntExtra("HOURS_PER_DAY", 1) ?: 1
            val equipment = data?.getStringArrayListExtra("EQUIPMENT")?.toMutableList() ?: mutableListOf("No Equipment")
            val injuries = data?.getStringExtra("INJURIES") ?: ""
            val exercises = data?.getParcelableArrayListExtra<Exercise>("EXERCISES") ?: mutableListOf()
            val location = data?.getParcelableExtra<Location>("LINKED_LOCATION")

            val newWorkout = Workout(routineName, difficulty, durationWeeks, hoursPerDay, 0, equipment, goal, injuries, exercises, location)
            WorkoutRepository.activeWorkouts.add(0, newWorkout)
            workoutsAdapter.notifyItemInserted(0)
            findViewById<RecyclerView>(R.id.rv_my_workouts).scrollToPosition(0)
            updateWorkoutSummary()
        }
    }

    private val editWorkoutLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val editedWorkout = result.data?.getParcelableExtra<Workout>("EDITED_WORKOUT")
            if (editedWorkout != null) {
                val index = WorkoutRepository.activeWorkouts.indexOfFirst { it.title == editedWorkout.title } // Assuming title is unique for now
                if (index != -1) {
                    WorkoutRepository.activeWorkouts[index] = editedWorkout
                    workoutsAdapter.notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        toolbar = findViewById(R.id.toolbar)
        tvWorkoutSummary = findViewById(R.id.tv_workout_summary)
        val user = firebaseAuth.currentUser

        if (user != null) {
            toolbar.title = user.displayName ?: "FitLife"
            toolbar.subtitle = "Welcome"
        } else {
            toolbar.title = "FitLife"
            toolbar.subtitle = "Stay Strong 💪"
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_profile_icon -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val rvWorkouts = findViewById<RecyclerView>(R.id.rv_my_workouts)
        workoutsAdapter = MyWorkoutsAdapter(WorkoutRepository.activeWorkouts, this)
        rvWorkouts.adapter = workoutsAdapter
        rvWorkouts.layoutManager = LinearLayoutManager(this)

        updateWorkoutSummary()

        val fabAddWorkout = findViewById<FloatingActionButton>(R.id.fab_add_workout)
        fabAddWorkout.setOnClickListener {
            val intent = Intent(this, AddWorkoutActivity::class.java)
            addWorkoutLauncher.launch(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_equipment -> {
                    startActivity(Intent(this, EquipmentActivity::class.java))
                    true
                }
                R.id.navigation_locations -> {
                    startActivity(Intent(this, LocationsActivity::class.java))
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user name and profile picture on resume
        val user = firebaseAuth.currentUser
        if (user != null) {
            toolbar.title = user.displayName ?: "FitLife"
            val profileIconView = toolbar.menu.findItem(R.id.action_profile_icon)?.actionView
            if (profileIconView != null) {
                val profileIcon = profileIconView.findViewById<ImageView>(R.id.toolbar_profile_icon_view)
                val profileImageFile = File(filesDir, "profile_picture.jpg")
                if (profileImageFile.exists()) {
                    profileIcon.setImageURI(Uri.fromFile(profileImageFile))
                } else {
                    profileIcon.setImageResource(R.drawable.fitlife)
                }
            }
        }
    }

    private fun updateWorkoutSummary(){
        val total = WorkoutRepository.activeWorkouts.size
        val completed = WorkoutRepository.completedWorkouts.size
        tvWorkoutSummary.text = "$total total • $completed completed"
    }

    override fun onWorkoutCompleted(position: Int) {
        val workout = WorkoutRepository.activeWorkouts[position]
        workout.progress = 100
        WorkoutRepository.completedWorkouts.add(workout)
        WorkoutRepository.activeWorkouts.removeAt(position)
        workoutsAdapter.notifyItemRemoved(position)
        updateWorkoutSummary()
    }

    override fun onWorkoutDeleted(position: Int) {
        WorkoutRepository.activeWorkouts.removeAt(position)
        workoutsAdapter.notifyItemRemoved(position)
        updateWorkoutSummary()
    }

    override fun onWorkoutClicked(position: Int) {
        val intent = Intent(this, WorkoutDetailsActivity::class.java)
        intent.putExtra("WORKOUT_DETAILS", WorkoutRepository.activeWorkouts[position])
        startActivity(intent)
    }

    override fun onWorkoutEdit(position: Int) {
        val intent = Intent(this, EditWorkoutActivity::class.java)
        intent.putExtra("WORKOUT_TO_EDIT", WorkoutRepository.activeWorkouts[position])
        editWorkoutLauncher.launch(intent)
    }

    override fun onWorkoutShare(position: Int) {
        val workout = WorkoutRepository.activeWorkouts[position]
        val shareText = buildString {
            append("Check out this workout: ${workout.title}\n\n")
            append("Goal: ${workout.goal}\n")
            append("Difficulty: ${workout.difficulty}\n\n")
            append("Exercises:\n")
            workout.exercises.forEach {
                append("- ${it.name} (${it.sets} sets, ${it.reps} reps)\n")
            }
            append("\nRequired Equipment: ${workout.tags.joinToString()}")
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        startActivity(Intent.createChooser(intent, "Share Workout via"))
    }
}