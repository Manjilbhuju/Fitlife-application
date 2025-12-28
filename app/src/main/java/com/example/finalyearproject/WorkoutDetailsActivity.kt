package com.example.finalyearproject

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class WorkoutDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout) // Reusing layout

        val workout = intent.getParcelableExtra<Workout>("WORKOUT_DETAILS")

        if (workout != null) {
            // Find all the views
            val pageTitle = findViewById<TextView>(R.id.tv_page_title)
            val routineName = findViewById<EditText>(R.id.et_routine_name)
            val goal = findViewById<EditText>(R.id.et_goal)
            val difficulty = findViewById<AutoCompleteTextView>(R.id.actv_difficulty)
            val durationWeeks = findViewById<EditText>(R.id.et_duration_weeks)
            val hoursPerDay = findViewById<EditText>(R.id.et_hours_per_day)
            val equipmentDropdown = findViewById<AutoCompleteTextView>(R.id.actv_equipment)
            val equipmentChipGroup = findViewById<ChipGroup>(R.id.chip_group_equipment)
            val injuries = findViewById<EditText>(R.id.et_injuries)
            val tvLinkedLocation = findViewById<TextView>(R.id.tv_linked_location)
            val btnLinkLocation = findViewById<Button>(R.id.btn_link_location)
            val rvExercises = findViewById<RecyclerView>(R.id.rv_exercises)
            val btnAddExercise = findViewById<Button>(R.id.btn_add_exercise)
            val createButton = findViewById<MaterialButton>(R.id.btn_create_routine)

            // Set the text and values
            pageTitle.text = "Workout Details"
            routineName.setText(workout.title)
            goal.setText(workout.goal)
            difficulty.setText(workout.difficulty, false)
            durationWeeks.setText(workout.durationWeeks.toString())
            hoursPerDay.setText(workout.hoursPerDay.toString())
            injuries.setText(workout.injuries)
            tvLinkedLocation.text = workout.location?.name ?: "No Location Linked"

            // Hide buttons
            btnAddExercise.visibility = View.GONE
            btnLinkLocation.visibility = View.GONE

            // Add equipment tags as non-closable chips
            workout.tags.forEach {
                val chip = Chip(this)
                chip.text = it
                equipmentChipGroup.addView(chip)
            }

            // Display exercises
            rvExercises.adapter = ExerciseAdapter(workout.exercises, object: ExerciseAdapter.OnExerciseActionListener{
                override fun onExerciseEdit(position: Int) {}
                override fun onExerciseDelete(position: Int) {}
            })
            rvExercises.layoutManager = LinearLayoutManager(this)

            // Disable all input fields and set text color
            val disabledColor = Color.parseColor("#1F1F1F")
            routineName.isEnabled = false
            routineName.setTextColor(disabledColor)
            goal.isEnabled = false
            goal.setTextColor(disabledColor)
            difficulty.isEnabled = false
            difficulty.setTextColor(disabledColor)
            durationWeeks.isEnabled = false
            durationWeeks.setTextColor(disabledColor)
            hoursPerDay.isEnabled = false
            hoursPerDay.setTextColor(disabledColor)
            equipmentDropdown.isEnabled = false
            equipmentDropdown.alpha = 0.5f // Make dropdown visually disabled
            injuries.isEnabled = false
            injuries.setTextColor(disabledColor)

            // Update button
            createButton.text = "Close"
            createButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.button_primary_dark)
            createButton.setOnClickListener { finish() }
        }
    }
}