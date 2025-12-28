package com.example.finalyearproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var ivProfile: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        ivProfile = findViewById(R.id.iv_profile_picture)
        val logoutButton = findViewById<MaterialCardView>(R.id.item_logout)
        val completedButton = findViewById<MaterialCardView>(R.id.item_completed_routine)
        val settingsButton = findViewById<MaterialCardView>(R.id.item_settings)
        val helpButton = findViewById<MaterialCardView>(R.id.item_help)

        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        completedButton.setOnClickListener {
            val intent = Intent(this, CompletedWorkoutsActivity::class.java)
            intent.putParcelableArrayListExtra("COMPLETED_WORKOUTS", ArrayList(WorkoutRepository.completedWorkouts))
            startActivity(intent)
        }

        settingsButton.setOnClickListener { 
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        helpButton.setOnClickListener { 
            startActivity(Intent(this, HelpActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.navigation_profile
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
                R.id.navigation_locations -> {
                    startActivity(Intent(this, LocationsActivity::class.java))
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
            // Update Toolbar
            toolbar.title = user.displayName ?: "FitLife"
            toolbar.subtitle = "Welcome"

            // Update Profile Info
            tvUserName.text = user.displayName
            tvUserEmail.text = user.email

            // Load local profile picture
            val profileImageFile = File(filesDir, "profile_picture.jpg")
            if (profileImageFile.exists()) {
                ivProfile.setImageURI(Uri.fromFile(profileImageFile))
            } else {
                ivProfile.setImageResource(R.drawable.fitlife)
            }
        }
    }
}