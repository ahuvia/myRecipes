package com.example.myrecipes.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SpoonacularApi {
    @GET("recipes/complexSearch")
    fun searchRecipes(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String
    ): Call<RecipeSearchResponse>
}

data class RecipeSearchResponse(val results: List<Recipe>)
data class Recipe(val title: String, val image: String)
