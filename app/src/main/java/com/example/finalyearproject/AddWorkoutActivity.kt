package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AddWorkoutActivity : AppCompatActivity(), ExerciseAdapter.OnExerciseActionListener {
    private val allEquipment = mutableListOf(
        "Dumbbells", "Workout Bench", "Resistance Bands",
        "Yoga Mat", "Yoga Block", "Exercise Ball"
    )
    private val selectedEquipment = mutableListOf<String>()
    private val exercises = mutableListOf<Exercise>()
    private lateinit var exerciseAdapter: ExerciseAdapter
    private var tempImageUri: Uri? = null
    private var selectedLocation: Location? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            tempImageUri = result.data?.data
        }
    }
    
    private val locationPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedLocation = result.data?.getParcelableExtra("SELECTED_LOCATION")
            findViewById<TextView>(R.id.tv_linked_location).text = selectedLocation?.name ?: "No Location Linked"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_workout)

        // --- Difficulty Level Dropdown ---
        val difficultyLevels = arrayOf("Beginner", "Intermediate", "Advanced")
        val difficultyAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, difficultyLevels)
        val difficultyTextView = findViewById<AutoCompleteTextView>(R.id.actv_difficulty)
        difficultyTextView.setAdapter(difficultyAdapter)

        // --- Equipment Dropdown & Chips ---
        val equipmentTextView = findViewById<AutoCompleteTextView>(R.id.actv_equipment)
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_equipment)
        updateEquipmentAdapter(equipmentTextView)

        equipmentTextView.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            addChipToGroup(selected, chipGroup)
            selectedEquipment.add(selected)
            allEquipment.remove(selected)
            updateEquipmentAdapter(equipmentTextView)
            equipmentTextView.text.clear()
        }

        // --- Exercises List ---
        val rvExercises = findViewById<RecyclerView>(R.id.rv_exercises)
        exerciseAdapter = ExerciseAdapter(exercises, this)
        rvExercises.adapter = exerciseAdapter
        rvExercises.layoutManager = LinearLayoutManager(this)

        val btnAddExercise = findViewById<Button>(R.id.btn_add_exercise)
        btnAddExercise.setOnClickListener {
            showAddExerciseDialog(null, -1)
        }

        // --- Link Location ---
        val btnLinkLocation = findViewById<Button>(R.id.btn_link_location)
        btnLinkLocation.setOnClickListener {
            val intent = Intent(this, LocationsActivity::class.java)
            intent.putExtra("isSelectable", true)
            locationPickerLauncher.launch(intent)
        }

        // --- Create Routine Button ---
        val btnCreateRoutine = findViewById<Button>(R.id.btn_create_routine)
        btnCreateRoutine.setOnClickListener {
            val routineName = findViewById<EditText>(R.id.et_routine_name).text.toString()
            val goal = findViewById<EditText>(R.id.et_goal).text.toString()
            val difficulty = difficultyTextView.text.toString()
            val durationWeeks = findViewById<EditText>(R.id.et_duration_weeks).text.toString().toIntOrNull() ?: 0
            val hoursPerDay = findViewById<EditText>(R.id.et_hours_per_day).text.toString().toIntOrNull() ?: 0
            val injuries = findViewById<EditText>(R.id.et_injuries).text.toString()

            val resultIntent = Intent()
            resultIntent.putExtra("ROUTINE_NAME", routineName)
            resultIntent.putExtra("GOAL", goal)
            resultIntent.putExtra("DIFFICULTY", difficulty)
            resultIntent.putExtra("DURATION_WEEKS", durationWeeks)
            resultIntent.putExtra("HOURS_PER_DAY", hoursPerDay)
            resultIntent.putStringArrayListExtra("EQUIPMENT", ArrayList(selectedEquipment))
            resultIntent.putExtra("INJURIES", injuries)
            resultIntent.putParcelableArrayListExtra("EXERCISES", ArrayList(exercises))
            resultIntent.putExtra("LINKED_LOCATION", selectedLocation)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showAddExerciseDialog(exerciseToEdit: Exercise?, position: Int){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_exercise, null)
        val name = dialogLayout.findViewById<EditText>(R.id.et_exercise_name)
        val sets = dialogLayout.findViewById<EditText>(R.id.et_exercise_sets)
        val reps = dialogLayout.findViewById<EditText>(R.id.et_exercise_reps)
        val instructions = dialogLayout.findViewById<EditText>(R.id.et_exercise_instructions)
        val btnAddImage = dialogLayout.findViewById<Button>(R.id.btn_add_exercise_image)

        exerciseToEdit?.let {
            name.setText(it.name)
            sets.setText(it.sets.toString())
            reps.setText(it.reps)
            instructions.setText(it.instructions)
        }

        btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        with(builder){
            setTitle(if (exerciseToEdit == null) "Add Exercise" else "Edit Exercise")
            setPositiveButton(if (exerciseToEdit == null) "Add" else "Save") { _, _ ->
                var imagePath: String? = exerciseToEdit?.imagePath
                tempImageUri?.let {
                    imagePath = saveImageToInternalStorage(it)
                    tempImageUri = null // Reset for next time
                }

                val exercise = Exercise(
                    name.text.toString(),
                    sets.text.toString().toIntOrNull() ?: 0,
                    reps.text.toString(),
                    instructions.text.toString(),
                    imagePath
                )
                
                if (position == -1) { // Add new
                    exercises.add(exercise)
                    exerciseAdapter.notifyItemInserted(exercises.size - 1)
                } else { // Update existing
                    exercises[position] = exercise
                    exerciseAdapter.notifyItemChanged(position)
                }
            }
            setNegativeButton("Cancel") { _, _ -> tempImageUri = null }
            setView(dialogLayout)
            show()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            
            val file = File(filesDir, "${UUID.randomUUID()}.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addChipToGroup(text: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener { 
            chipGroup.removeView(chip)
            selectedEquipment.remove(text)
            allEquipment.add(text)
            updateEquipmentAdapter(findViewById(R.id.actv_equipment))
        }
        chipGroup.addView(chip)
    }

    private fun updateEquipmentAdapter(autoCompleteTextView: AutoCompleteTextView) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, allEquipment)
        autoCompleteTextView.setAdapter(adapter)
    }

    override fun onExerciseEdit(position: Int) {
        showAddExerciseDialog(exercises[position], position)
    }

    override fun onExerciseDelete(position: Int) {
        exercises.removeAt(position)
        exerciseAdapter.notifyItemRemoved(position)
    }
}