package com.example.finalyearproject

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Exercise(
    val name: String,
    val sets: Int,
    val reps: String,
    val instructions: String,
    var imagePath: String? = null // New field for local image path
) : Parcelable