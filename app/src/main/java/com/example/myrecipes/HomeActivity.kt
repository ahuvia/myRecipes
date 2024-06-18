package com.example.myrecipes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val searchEditText = findViewById<EditText>(R.id.editTextSearch)
        val popularRecipesLayout = findViewById<LinearLayout>(R.id.popularRecipesLayout)
        val categoriesLayout = findViewById<LinearLayout>(R.id.categoriesLayout)
        val recipeNameEditText = findViewById<EditText>(R.id.editTextRecipeName)
        val recipeDescriptionEditText = findViewById<EditText>(R.id.editTextRecipeDescription)
        val recipeIngredientsEditText = findViewById<EditText>(R.id.editTextRecipeIngredients)
        val recipeInstructionsEditText = findViewById<EditText>(R.id.editTextRecipeInstructions)
        val recipeImageUrlEditText = findViewById<EditText>(R.id.editTextRecipeImageUrl)
        val saveRecipeButton = findViewById<Button>(R.id.buttonSaveRecipe)

        loadPopularRecipes(popularRecipesLayout)

        saveRecipeButton.setOnClickListener {
            val name = recipeNameEditText.text.toString()
            val description = recipeDescriptionEditText.text.toString()
            val ingredients = recipeIngredientsEditText.text.toString().split(",").map { it.trim() }
            val instructions = recipeInstructionsEditText.text.toString()
            val imageUrl = recipeImageUrlEditText.text.toString()

            if (name.isNotEmpty() && description.isNotEmpty() && ingredients.isNotEmpty() && instructions.isNotEmpty() && imageUrl.isNotEmpty()) {
                val recipe = Recipe(
                    name = name,
                    description = description,
                    ingredients = ingredients,
                    instructions = instructions,
                    imageUrl = imageUrl,
                    likes = 0
                )
                saveRecipe(recipe)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // תצוגת מתכונים פופולריים (דוגמאות)
        val popularRecipes = listOf("Recipe 1", "Recipe 2", "Recipe 3")
        popularRecipes.forEach { recipe ->
            val textView = TextView(this)
            textView.text = recipe
            textView.setOnClickListener {
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe)
                startActivity(intent)
            }
            popularRecipesLayout.addView(textView)
        }

        // תצוגת קטגוריות (דוגמאות)
        val categories = listOf("Desserts", "Main Dishes", "Vegan", "Gluten-Free")
        categories.forEach { category ->
            val textView = TextView(this)
            textView.text = category
            textView.setOnClickListener {
                // פעולה לעבור למסך קטגוריה
            }
            categoriesLayout.addView(textView)
        }
    }

    private fun saveRecipe(recipe: Recipe) {
        db.collection("recipes")
            .add(recipe)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Recipe saved successfully", Toast.LENGTH_SHORT).show()
                loadPopularRecipes(findViewById(R.id.popularRecipesLayout))
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving recipe", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPopularRecipes(layout: LinearLayout) {
        db.collection("recipes")
            .orderBy("likes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                layout.removeAllViews()
                for (document in result) {
                    val recipe = document.toObject(Recipe::class.java)
                    val textView = TextView(this)
                    textView.text = recipe.name
                    textView.setOnClickListener {
                        val intent = Intent(this, RecipeDetailActivity::class.java)
                        intent.putExtra("RECIPE_ID", document.id)
                        startActivity(intent)
                    }
                    layout.addView(textView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show()
            }
    }
}
