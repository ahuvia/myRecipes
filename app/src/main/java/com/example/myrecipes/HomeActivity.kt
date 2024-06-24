package com.example.myrecipes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var popularRecipesRecyclerView: RecyclerView
    private lateinit var popularRecipesAdapter: RecipeAdapter
    private var allRecipes: MutableList<Recipe> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val searchEditText = findViewById<EditText>(R.id.editTextSearch)
        val categoriesLayout = findViewById<LinearLayout>(R.id.categoriesLayout)
        val addNewRecipeButton = findViewById<Button>(R.id.buttonAddNewRecipe)
        popularRecipesRecyclerView = findViewById(R.id.popularRecipesRecyclerView)
        popularRecipesRecyclerView.layoutManager = LinearLayoutManager(this)

        loadPopularRecipes()
        loadCategories(categoriesLayout)

        addNewRecipeButton.setOnClickListener {
            val intent = Intent(this, NewRecipeActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecipes(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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

    private fun loadPopularRecipes() {
        db.collection("recipes")
            .orderBy("likes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                allRecipes = result.map { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipe.id = document.id
                    recipe
                }.toMutableList()
                popularRecipesAdapter = RecipeAdapter(allRecipes)
                popularRecipesRecyclerView.adapter = popularRecipesAdapter
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
        allRecipes.add(0, recipe)
        popularRecipesAdapter.notifyItemInserted(0)
    }

    private fun filterRecipes(query: String) {
        val filteredRecipes = allRecipes.filter { it.name.contains(query, ignoreCase = true) }
        popularRecipesAdapter.updateRecipes(filteredRecipes)
    }
}
