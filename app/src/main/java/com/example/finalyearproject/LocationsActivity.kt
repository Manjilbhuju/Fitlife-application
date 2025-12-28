package com.example.finalyearproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import java.io.File

class LocationsActivity : AppCompatActivity() {

    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var toolbar: MaterialToolbar
    private var isSelectableMode = false

    private val mapPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val name = data?.getStringExtra("location_name") ?: "New Location"
            val latitude = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            val newLocation = Location(name, latitude, longitude)
            LocationRepository.savedLocations.add(newLocation)
            locationsAdapter.notifyItemInserted(LocationRepository.savedLocations.size - 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)

        isSelectableMode = intent.getBooleanExtra("isSelectable", false)

        firebaseAuth = FirebaseAuth.getInstance()
        toolbar = findViewById(R.id.toolbar)
        val user = firebaseAuth.currentUser

        if (user != null) {
            toolbar.title = user.displayName ?: "FitLife"
        }
        toolbar.subtitle = if (isSelectableMode) "Select a Location" else "Your saved places"

        val rvLocations = findViewById<RecyclerView>(R.id.rv_locations)
        locationsAdapter = LocationsAdapter(LocationRepository.savedLocations) { location ->
            if (isSelectableMode) {
                val resultIntent = Intent()
                resultIntent.putExtra("SELECTED_LOCATION", location)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                val intent = Intent(this, MapDisplayActivity::class.java)
                intent.putExtra("latitude", location.latitude)
                intent.putExtra("longitude", location.longitude)
                intent.putExtra("name", location.name)
                startActivity(intent)
            }
        }
        rvLocations.adapter = locationsAdapter
        rvLocations.layoutManager = LinearLayoutManager(this)

        val fabAddLocation = findViewById<FloatingActionButton>(R.id.fab_add_location)
        fabAddLocation.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            mapPickerLauncher.launch(intent)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_locations
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_workouts -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.navigation_equipment -> {
                    startActivity(Intent(this, EquipmentActivity::class.java))
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
}