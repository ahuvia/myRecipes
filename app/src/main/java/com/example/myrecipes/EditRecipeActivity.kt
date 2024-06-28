package com.example.myrecipes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recipeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recipeNameEditText = findViewById<EditText>(R.id.editTextRecipeName)
        val recipeDescriptionEditText = findViewById<EditText>(R.id.editTextRecipeDescription)
        val ingredientsEditText = findViewById<EditText>(R.id.editTextIngredients)
        val instructionsEditText = findViewById<EditText>(R.id.editTextInstructions)
        val saveButton = findViewById<Button>(R.id.buttonSave)

        recipeId = intent.getStringExtra("RECIPE_ID") ?: ""

        if (recipeId.isNotEmpty()) {
            db.collection("recipes").document(recipeId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val recipe = document.toObject(Recipe::class.java)
                        if (recipe != null) {
                            recipeNameEditText.setText(recipe.name)
                            recipeDescriptionEditText.setText(recipe.description)
                            ingredientsEditText.setText(recipe.ingredients.joinToString("\n"))
                            instructionsEditText.setText(recipe.instructions)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error loading recipe", Toast.LENGTH_SHORT).show()
                }
        }

        saveButton.setOnClickListener {
            val updatedRecipe = Recipe(
                id = recipeId,
                userId = auth.currentUser?.uid ?: "",
                name = recipeNameEditText.text.toString(),
                description = recipeDescriptionEditText.text.toString(),
                ingredients = ingredientsEditText.text.toString().split("\n"),
                instructions = instructionsEditText.text.toString(),
                imageUrl = "", // התמונה נשארת ללא שינוי
                category = "", // הקטגוריה נשארת ללא שינוי
                likes = 0 // הלייקים נשארים ללא שינוי
            )

            db.collection("recipes").document(recipeId).set(updatedRecipe)
                .addOnSuccessListener {
                    Toast.makeText(this, "Recipe updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error updating recipe", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
