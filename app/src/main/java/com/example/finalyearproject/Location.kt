package com.example.finalyearproject

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable