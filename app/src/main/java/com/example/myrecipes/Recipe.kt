package com.example.myrecipes

import java.io.Serializable

data class Recipe(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val ingredients: List<String> = listOf(),
    val instructions: String = "",
    val imageUrl: String = "",
    var likes: Int = 0,
)
