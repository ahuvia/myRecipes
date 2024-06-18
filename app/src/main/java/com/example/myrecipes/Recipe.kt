package com.example.myrecipes

data class Recipe(
    val name: String = "",
    val description: String = "",
    val ingredients: List<String> = listOf(),
    val instructions: String = "",
    val likes: Int = 0,
    val imageUrl: String? = null
)
