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
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth.currentUser

        val ivProfile = findViewById<CircleImageView>(R.id.iv_edit_profile_picture)
        val tvChangePhoto = findViewById<TextView>(R.id.tv_change_photo)
        val etName = findViewById<EditText>(R.id.et_edit_name)
        val etEmail = findViewById<EditText>(R.id.et_edit_email)
        val btnSave = findViewById<Button>(R.id.btn_save_profile)
        progressBar = findViewById(R.id.profile_progress_bar)

        // Load local profile picture if it exists
        val profileImageFile = File(filesDir, "profile_picture.jpg")
        if (profileImageFile.exists()) {
            ivProfile.setImageURI(Uri.fromFile(profileImageFile))
        } else {
            ivProfile.setImageResource(R.drawable.fitlife)
        }
        
        etName.setText(user?.displayName)
        etEmail.setText(user?.email)

        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedImageUri = result.data?.data
                    ivProfile.setImageURI(selectedImageUri)
                }
        }

        tvChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }


        btnSave.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false
            val newName = etName.text.toString()
            val newEmail = etEmail.text.toString()

            // Save image locally if a new one was selected
            selectedImageUri?.let { saveImageToInternalStorage(it) }

            updateUserProfile(newName, newEmail)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            
            val file = File(filesDir, "profile_picture.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUserProfile(name: String, email: String) {
        val user = firebaseAuth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.email != email) {
                    user.updateEmail(email).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            handleSaveSuccess()
                        } else {
                            handleSaveFailure(emailTask.exception?.message)
                        }
                    }
                } else {
                    handleSaveSuccess()
                }
            } else {
                handleSaveFailure(task.exception?.message)
            }
        }
    }

    private fun handleSaveSuccess() {
        progressBar.visibility = View.GONE
        Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handleSaveFailure(message: String?) {
        progressBar.visibility = View.GONE
        findViewById<Button>(R.id.btn_save_profile).isEnabled = true
        Toast.makeText(this, "Failed to update profile: $message", Toast.LENGTH_LONG).show()
    }
}