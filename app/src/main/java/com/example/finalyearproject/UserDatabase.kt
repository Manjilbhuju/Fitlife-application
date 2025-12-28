package com.example.finalyearproject

data class User(val email: String, val pass: String)

object UserDatabase {
    val users = mutableListOf(
        User("test@example.com", "password123"),
        User("user@example.com", "qwerty")
    )
}