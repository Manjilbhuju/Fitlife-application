package com.example.finalyearproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class EquipmentActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var tvEquipmentCount: TextView
    private val strengthEquipment = mutableListOf(
        Equipment("Dumbbells", "Used in 3 workouts", true),
        Equipment("Workout Bench", "Used in 2 workouts"),
        Equipment("Resistance Bands", "Used in 4 workouts", true)
    )
    private val floorExercise = mutableListOf(
        Equipment("Yoga Mat", "Used in 5 workouts", true),
        Equipment("Yoga Block", "Used in 2 workouts"),
        Equipment("Exercise Ball", "Used in 1 workout")
    )
    private val accessories = mutableListOf<Equipment>()
    private val others = mutableListOf<Equipment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipment)

        firebaseAuth = FirebaseAuth.getInstance()
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        tvEquipmentCount = findViewById(R.id.tv_equipment_count)
        val user = firebaseAuth.currentUser

        if (user != null) {
            toolbar.title = user.displayName ?: "FitLife"
            toolbar.subtitle = "Welcome"
        } else {
            toolbar.title = "FitLife"
            toolbar.subtitle = "Stay Strong 💪"
        }

        val rvStrength: RecyclerView = findViewById(R.id.rv_strength_equipment)
        rvStrength.layoutManager = LinearLayoutManager(this)
        val strengthAdapter = EquipmentAdapter(strengthEquipment)
        rvStrength.adapter = strengthAdapter

        val rvFloor: RecyclerView = findViewById(R.id.rv_floor_exercise)
        rvFloor.layoutManager = LinearLayoutManager(this)
        val floorAdapter = EquipmentAdapter(floorExercise)
        rvFloor.adapter = floorAdapter

        val rvAccessories: RecyclerView = findViewById(R.id.rv_accessories)
        rvAccessories.layoutManager = LinearLayoutManager(this)
        val accessoriesAdapter = EquipmentAdapter(accessories)
        rvAccessories.adapter = accessoriesAdapter

        val rvOthers: RecyclerView = findViewById(R.id.rv_others)
        rvOthers.layoutManager = LinearLayoutManager(this)
        val othersAdapter = EquipmentAdapter(others)
        rvOthers.adapter = othersAdapter

        updateEquipmentCount()

        // Swipe to delete callbacks
        setupSwipeToDelete(rvStrength, strengthEquipment, strengthAdapter)
        setupSwipeToDelete(rvFloor, floorExercise, floorAdapter)
        setupSwipeToDelete(rvAccessories, accessories, accessoriesAdapter)
        setupSwipeToDelete(rvOthers, others, othersAdapter)

        val fabAddEquipment = findViewById<FloatingActionButton>(R.id.fab_add_equipment)
        fabAddEquipment.setOnClickListener {
            showAddEquipmentDialog(strengthAdapter, floorAdapter, accessoriesAdapter, othersAdapter, strengthEquipment, floorExercise, accessories, others)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_equipment
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_workouts -> {
                    startActivity(Intent(this, MainActivity::class.java))
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

    private fun updateEquipmentCount() {
        val totalItems = strengthEquipment.size + floorExercise.size + accessories.size + others.size
        tvEquipmentCount.text = "$totalItems items in Inventory"
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView, equipmentList: MutableList<Equipment>, adapter: EquipmentAdapter) {
        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                showDeleteConfirmationDialog(position, equipmentList, adapter, viewHolder)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showDeleteConfirmationDialog(position: Int, list: MutableList<Equipment>, adapter: EquipmentAdapter, viewHolder: RecyclerView.ViewHolder) {
        AlertDialog.Builder(this)
            .setTitle("Delete Equipment")
            .setMessage("Are you sure you want to delete this equipment?")
            .setPositiveButton("Delete") { _, _ ->
                list.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEquipmentCount()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                adapter.notifyItemChanged(viewHolder.adapterPosition) // Re-draw the item to animate it back
                dialog.dismiss()
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(viewHolder.adapterPosition) // Also handle back press or tapping outside
            }
            .create()
            .show()
    }

    private fun showAddEquipmentDialog(
        strengthAdapter: EquipmentAdapter,
        floorAdapter: EquipmentAdapter,
        accessoriesAdapter: EquipmentAdapter,
        othersAdapter: EquipmentAdapter,
        strengthEquipment: MutableList<Equipment>,
        floorExercise: MutableList<Equipment>,
        accessories: MutableList<Equipment>,
        others: MutableList<Equipment>
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_equipment, null)
        val acType = dialogView.findViewById<AutoCompleteTextView>(R.id.actv_equipment_type)
        val etName = dialogView.findViewById<EditText>(R.id.et_equipment_name)

        val equipmentTypes = arrayOf("Strength Equipment", "Floor Exercises", "Accessories", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, equipmentTypes)
        acType.setAdapter(adapter)

        AlertDialog.Builder(this)
            .setTitle("Add New Equipment")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val type = acType.text.toString()
                val name = etName.text.toString()
                if (type.isNotEmpty() && name.isNotEmpty()) {
                    val newEquipment = Equipment(name, "Used in 0 workouts", false)
                    when (type) {
                        "Strength Equipment" -> {
                            strengthEquipment.add(newEquipment)
                            strengthAdapter.notifyItemInserted(strengthEquipment.size - 1)
                        }
                        "Floor Exercises" -> {
                            floorExercise.add(newEquipment)
                            floorAdapter.notifyItemInserted(floorExercise.size - 1)
                        }
                        "Accessories" -> {
                            accessories.add(newEquipment)
                            accessoriesAdapter.notifyItemInserted(accessories.size - 1)
                        }
                        "Others" -> {
                            others.add(newEquipment)
                            othersAdapter.notifyItemInserted(others.size - 1)
                        }
                        else -> {
                            Toast.makeText(this, "Please select a valid type", Toast.LENGTH_LONG).show()
                        }
                    }
                    updateEquipmentCount()
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}