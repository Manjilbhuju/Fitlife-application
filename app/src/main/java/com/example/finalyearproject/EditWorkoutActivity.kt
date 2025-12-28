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

class EditWorkoutActivity : AppCompatActivity(), ExerciseAdapter.OnExerciseActionListener {

    private var workout: Workout? = null
    private val allEquipment = mutableListOf(
        "Dumbbells", "Workout Bench", "Resistance Bands",
        "Yoga Mat", "Yoga Block", "Exercise Ball"
    )
    private val selectedEquipment = mutableListOf<String>()
    private lateinit var exerciseAdapter: ExerciseAdapter
    private var tempImageUri: Uri? = null
    private var tempImageView: ImageView? = null
    private var selectedLocation: Location? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
            if (result.resultCode == Activity.RESULT_OK) {
                tempImageUri = result.data?.data
                tempImageView?.setImageURI(tempImageUri)
                tempImageView?.visibility = View.VISIBLE
            }
    }

    private val locationPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedLocation = result.data?.getParcelableExtra("SELECTED_LOCATION")
            findViewById<TextView>(R.id.tv_linked_location).text = selectedLocation?.name ?: "No Location Linked"
            workout?.location = selectedLocation
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_workout)

        workout = intent.getParcelableExtra("WORKOUT_TO_EDIT")

        if (workout == null) {
            finish() // Should not happen
            return
        }

        val etRoutineName = findViewById<EditText>(R.id.et_routine_name)
        val etGoal = findViewById<EditText>(R.id.et_goal)
        val actvDifficulty = findViewById<AutoCompleteTextView>(R.id.actv_difficulty)
        val etDurationWeeks = findViewById<EditText>(R.id.et_duration_weeks)
        val etHoursPerDay = findViewById<EditText>(R.id.et_hours_per_day)
        val actvEquipment = findViewById<AutoCompleteTextView>(R.id.actv_equipment)
        val chipGroupEquipment = findViewById<ChipGroup>(R.id.chip_group_equipment)
        val etInjuries = findViewById<EditText>(R.id.et_injuries)
        val tvLinkedLocation = findViewById<TextView>(R.id.tv_linked_location)
        val btnLinkLocation = findViewById<Button>(R.id.btn_link_location)
        val rvExercises = findViewById<RecyclerView>(R.id.rv_exercises)
        val btnAddExercise = findViewById<Button>(R.id.btn_add_exercise)
        val btnSaveChanges = findViewById<Button>(R.id.btn_save_workout)

        // Populate fields
        selectedLocation = workout!!.location
        etRoutineName.setText(workout!!.title)
        etGoal.setText(workout!!.goal)
        actvDifficulty.setText(workout!!.difficulty, false)
        etDurationWeeks.setText(workout!!.durationWeeks.toString())
        etHoursPerDay.setText(workout!!.hoursPerDay.toString())
        etInjuries.setText(workout!!.injuries)
        tvLinkedLocation.text = workout!!.location?.name ?: "No Location Linked"

        // Populate equipment
        workout!!.tags.forEach { tag ->
            if(!selectedEquipment.contains(tag)) {
                selectedEquipment.add(tag)
                allEquipment.remove(tag)
                addChipToGroup(tag, chipGroupEquipment)
            }
        }
        updateEquipmentAdapter(actvEquipment)

        // Populate exercises
        exerciseAdapter = ExerciseAdapter(workout!!.exercises, this)
        rvExercises.adapter = exerciseAdapter
        rvExercises.layoutManager = LinearLayoutManager(this)

        // Listeners
        btnAddExercise.setOnClickListener { showAddExerciseDialog(null, -1) }
        actvEquipment.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            addChipToGroup(selected, chipGroupEquipment)
            selectedEquipment.add(selected)
            allEquipment.remove(selected)
            updateEquipmentAdapter(actvEquipment)
            actvEquipment.text.clear()
        }
        btnLinkLocation.setOnClickListener {
            val intent = Intent(this, LocationsActivity::class.java)
            intent.putExtra("isSelectable", true)
            locationPickerLauncher.launch(intent)
        }

        btnSaveChanges.setOnClickListener {
            workout!!.title = etRoutineName.text.toString()
            workout!!.goal = etGoal.text.toString()
            workout!!.difficulty = actvDifficulty.text.toString()
            workout!!.durationWeeks = etDurationWeeks.text.toString().toIntOrNull() ?: 0
            workout!!.hoursPerDay = etHoursPerDay.text.toString().toIntOrNull() ?: 0
            workout!!.injuries = etInjuries.text.toString()
            workout!!.tags = selectedEquipment

            val resultIntent = Intent()
            resultIntent.putExtra("EDITED_WORKOUT", workout)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showAddExerciseDialog(exerciseToEdit: Exercise?, position: Int) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_exercise, null)
        val name = dialogLayout.findViewById<EditText>(R.id.et_exercise_name)
        val sets = dialogLayout.findViewById<EditText>(R.id.et_exercise_sets)
        val reps = dialogLayout.findViewById<EditText>(R.id.et_exercise_reps)
        val instructions = dialogLayout.findViewById<EditText>(R.id.et_exercise_instructions)
        val btnAddImage = dialogLayout.findViewById<Button>(R.id.btn_add_exercise_image)
        val ivPreview = dialogLayout.findViewById<ImageView>(R.id.iv_exercise_image_preview)

        exerciseToEdit?.let {
            name.setText(it.name)
            sets.setText(it.sets.toString())
            reps.setText(it.reps)
            instructions.setText(it.instructions)
            it.imagePath?.let {
                ivPreview.setImageURI(Uri.fromFile(File(it)))
                ivPreview.visibility = View.VISIBLE
            }
        }

        btnAddImage.setOnClickListener {
            tempImageView = ivPreview // Store a reference to the dialog's image view
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        with(builder) {
            setTitle(if (exerciseToEdit == null) "Add Exercise" else "Edit Exercise")
            setPositiveButton(if (exerciseToEdit == null) "Add" else "Save") { _, _ ->
                var imagePath: String? = exerciseToEdit?.imagePath
                tempImageUri?.let {
                    imagePath = saveImageToInternalStorage(it)
                }
                val exercise = Exercise(
                    name.text.toString(),
                    sets.text.toString().toIntOrNull() ?: 0,
                    reps.text.toString(),
                    instructions.text.toString(),
                    imagePath
                )

                if (position == -1) { // Add new
                    workout?.exercises?.add(exercise)
                    exerciseAdapter.notifyItemInserted(workout?.exercises?.size?.minus(1) ?: 0)
                } else { // Update existing
                    workout?.exercises?.set(position, exercise)
                    exerciseAdapter.notifyItemChanged(position)
                }
                cleanupDialog() // Clear temporary references
            }
            setNegativeButton("Cancel") { _, _ -> cleanupDialog() }
            setOnDismissListener { cleanupDialog() }
            setView(dialogLayout)
            show()
        }
    }
    
    private fun cleanupDialog(){
        tempImageUri = null
        tempImageView = null
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
        showAddExerciseDialog(workout?.exercises?.get(position), position)
    }

    override fun onExerciseDelete(position: Int) {
        workout?.exercises?.removeAt(position)
        exerciseAdapter.notifyItemRemoved(position)
    }
}