package com.example.myrecipes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class NewRecipeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private lateinit var categorySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_recipe)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val recipeNameEditText = findViewById<EditText>(R.id.editTextRecipeName)
        val recipeDescriptionEditText = findViewById<EditText>(R.id.editTextRecipeDescription)
        val recipeIngredientsEditText = findViewById<EditText>(R.id.editTextRecipeIngredients)
        val recipeInstructionsEditText = findViewById<EditText>(R.id.editTextRecipeInstructions)
        val selectImageButton = findViewById<Button>(R.id.buttonSelectImage)
        val recipeImageView = findViewById<ImageView>(R.id.imageViewRecipeImage)
        val saveRecipeButton = findViewById<Button>(R.id.buttonSaveRecipe)
        categorySpinner = findViewById(R.id.spinnerCategory)

        // הגדרת ה-Spinner עם הקטגוריות
        ArrayAdapter.createFromResource(
            this,
            R.array.recipe_categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        selectImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)
        }

        saveRecipeButton.setOnClickListener {
            val name = recipeNameEditText.text.toString()
            val description = recipeDescriptionEditText.text.toString()
            val ingredients = recipeIngredientsEditText.text.toString().split(",").map { it.trim() }
            val instructions = recipeInstructionsEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()

            if (name.isNotEmpty() && description.isNotEmpty() && ingredients.isNotEmpty() && instructions.isNotEmpty() && selectedImageUri != null) {
                uploadImageAndSaveRecipe(name, description, ingredients, instructions, category)
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            val recipeImageView = findViewById<ImageView>(R.id.imageViewRecipeImage)
            recipeImageView.setImageURI(selectedImageUri)
            recipeImageView.visibility = ImageView.VISIBLE
        }
    }

    private fun uploadImageAndSaveRecipe(name: String, description: String, ingredients: List<String>, instructions: String, category: String) {
        val imageRef = storage.reference.child("images/${UUID.randomUUID()}")
        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val recipe = Recipe(
                            name = name,
                            description = description,
                            ingredients = ingredients,
                            instructions = instructions,
                            imageUrl = downloadUrl.toString(),
                            likes = 0,
                            category = category
                        )
                        saveRecipe(recipe)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveRecipe(recipe: Recipe) {
        val docRef = db.collection("recipes").document()
        recipe.id = docRef.id
        docRef.set(recipe)
            .addOnSuccessListener {
                Toast.makeText(this, "Recipe saved successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent()
                intent.putExtra("NEW_RECIPE_ID", recipe.id)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving recipe", Toast.LENGTH_SHORT).show()
            }
    }
}
