package com.example.finalyearproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompletedWorkoutsActivity : AppCompatActivity(), MyWorkoutsAdapter.OnWorkoutActionListener {

    private val completedWorkouts = mutableListOf<Workout>()
    private lateinit var completedAdapter: MyWorkoutsAdapter
    private val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    private var selectedWorkoutForSms: Workout? = null
    private var enteredPhoneNumber: String? = null

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
        selectedWorkoutForSms = completedWorkouts[position]
        showPhoneNumberDialog()
    }

    private fun showPhoneNumberDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Phone Number")

        val input = EditText(this)
        input.hint = "Enter phone number"
        builder.setView(input)

        builder.setPositiveButton("Send") { _, _ ->
            val phoneNumber = input.text.toString()
            if (phoneNumber.isNotEmpty()) {
                enteredPhoneNumber = phoneNumber
                sendSms(phoneNumber, selectedWorkoutForSms!!)
            } else {
                Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun sendSms(phoneNumber: String, workout: Workout) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SEND_SMS_PERMISSION_REQUEST_CODE)
        } else {
            try {
                val smsManager = SmsManager.getDefault()
                val message = "Check out this workout: ${workout.title}. Goal: ${workout.goal}. Difficulty: ${workout.difficulty}."
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Toast.makeText(this, "SMS sent successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(selectedWorkoutForSms != null && enteredPhoneNumber != null) {
                    sendSms(enteredPhoneNumber!!, selectedWorkoutForSms!!)
                }
            } else {
                Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}