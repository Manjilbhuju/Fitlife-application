package com.example.finalyearproject

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Workout(
    var title: String,
    var difficulty: String,
    var durationWeeks: Int,
    var hoursPerDay: Int,
    var progress: Int,
    var tags: MutableList<String>,
    var goal: String,
    var injuries: String,
    var exercises: MutableList<Exercise> = mutableListOf(),
    var location: Location? = null // New field for linked location
) : Parcelable