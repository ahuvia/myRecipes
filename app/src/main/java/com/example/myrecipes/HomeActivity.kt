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
import com.google.firebase.storage.FirebaseStorage

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val searchEditText = findViewById<EditText>(R.id.editTextSearch)
        val popularRecipesLayout = findViewById<LinearLayout>(R.id.popularRecipesLayout)
        val categoriesLayout = findViewById<LinearLayout>(R.id.categoriesLayout)
        val addNewRecipeButton = findViewById<Button>(R.id.buttonAddNewRecipe)

        loadPopularRecipes(popularRecipesLayout)
        loadCategories(categoriesLayout)

        addNewRecipeButton.setOnClickListener {
            val intent = Intent(this, NewRecipeActivity::class.java)
            startActivityForResult(intent, 1001)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val newRecipeId = data?.getStringExtra("NEW_RECIPE_ID")
            newRecipeId?.let {
                db.collection("recipes").document(it).get()
                    .addOnSuccessListener { document ->
                        val recipe = document.toObject(Recipe::class.java)
                        recipe?.let { addRecipeToList(it) }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error loading new recipe", Toast.LENGTH_SHORT).show()
                    }
            }
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

    private fun loadCategories(layout: LinearLayout) {
        // שיטת דוגמה לטעינת קטגוריות
        // זהו מקום מתאים להוסיף קוד לטעינת קטגוריות מ- Firestore
    }

    private fun addRecipeToList(recipe: Recipe) {
        val popularRecipesLayout = findViewById<LinearLayout>(R.id.popularRecipesLayout)
        val textView = TextView(this)
        textView.text = recipe.name
        textView.setOnClickListener {
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id) // וודא שה-ID עובר
            startActivity(intent)
        }
        popularRecipesLayout.addView(textView, 0) // להוסיף את המתכון החדש בראש הרשימה
    }

}
