package com.example.myrecipes

import java.io.Serializable

data class Recipe(
    var id: String = "",
    var name: String = "",
    var description: String? = "",
    var ingredients: List<String> = listOf(),
    var instructions: String? = "",
    var imageUrl: String? = "",
    var likes: Int = 0,
    var category: String? = ""
)
