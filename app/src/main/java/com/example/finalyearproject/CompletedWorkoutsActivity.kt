package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompletedWorkoutsActivity : AppCompatActivity(), MyWorkoutsAdapter.OnWorkoutActionListener {

    private val completedWorkouts = mutableListOf<Workout>()
    private lateinit var completedAdapter: MyWorkoutsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_workouts)

        val passedWorkouts = intent.getParcelableArrayListExtra<Workout>("COMPLETED_WORKOUTS")
        if (passedWorkouts != null) {
            completedWorkouts.addAll(passedWorkouts)
        }

        val rvCompleted: RecyclerView = findViewById(R.id.rv_completed_workouts)
        rvCompleted.layoutManager = LinearLayoutManager(this)
        completedAdapter = MyWorkoutsAdapter(completedWorkouts, this)
        rvCompleted.adapter = completedAdapter
    }

    override fun onWorkoutCompleted(position: Int) {
        // Workout is already completed, do nothing.
    }

    override fun onWorkoutDeleted(position: Int) {
        completedWorkouts.removeAt(position)
        completedAdapter.notifyItemRemoved(position)
    }

    override fun onWorkoutClicked(position: Int) {
        val intent = Intent(this, WorkoutDetailsActivity::class.java)
        intent.putExtra("WORKOUT_DETAILS", completedWorkouts[position])
        startActivity(intent)
    }

    override fun onWorkoutEdit(position: Int) {
        // Not editable from this screen, do nothing.
    }

    override fun onWorkoutShare(position: Int) {
        // Not shareable from this screen, do nothing
    }
}