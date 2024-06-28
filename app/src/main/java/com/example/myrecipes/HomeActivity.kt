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
    private lateinit var categorySpinner: Spinner
    private var allRecipes: MutableList<Recipe> = mutableListOf()
    private var filteredRecipes: MutableList<Recipe> = mutableListOf()
    private var selectedCategory: String = "All"
    private lateinit var buttonConversion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        val buttonLogout: Button = findViewById(R.id.buttonLogout)
        buttonLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        buttonConversion = findViewById(R.id.buttonConversion)
        buttonConversion.setOnClickListener {
            val intent = Intent(this, ConversionActivity::class.java)
            startActivity(intent)
        }

        val searchEditText = findViewById<EditText>(R.id.editTextSearch)
        val categoriesLayout = findViewById<LinearLayout>(R.id.categoriesLayout)
        val addNewRecipeButton = findViewById<Button>(R.id.buttonAddNewRecipe)
        popularRecipesRecyclerView = findViewById(R.id.popularRecipesRecyclerView)
        popularRecipesRecyclerView.layoutManager = LinearLayoutManager(this)
        categorySpinner = findViewById(R.id.categorySpinner)
        val buttonUserProfile: Button = findViewById(R.id.buttonUserProfile)
        buttonUserProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        loadPopularRecipes()
        loadCategories()

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

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = parent?.getItemAtPosition(position) as String
                filterRecipes(searchEditText.text.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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

    private fun loadPopularRecipes() {
        db.collection("recipes")
            .orderBy("likes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                allRecipes = result.map { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipe.id = document.id
                    recipe.name = recipe.name ?: "Unknown"
                    recipe.description = recipe.description ?: ""
                    recipe.ingredients = recipe.ingredients ?: listOf()
                    recipe.instructions = recipe.instructions ?: ""
                    recipe.imageUrl = recipe.imageUrl ?: ""
                    recipe.category = recipe.category ?: "Other"
                    recipe.likes = recipe.likes ?: 0
                    recipe
                }.toMutableList()
                popularRecipesAdapter = RecipeAdapter(allRecipes)
                popularRecipesRecyclerView.adapter = popularRecipesAdapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCategories() {
        // Update the categories to match those in the NewRecipeActivity
        val categories = listOf("All", "בשרי", "חלבי", "פרווה")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun addRecipeToList(recipe: Recipe) {
        allRecipes.add(0, recipe)
        filterRecipes("")
    }

    private fun filterRecipes(query: String) {
        if (!::popularRecipesAdapter.isInitialized) return
        filteredRecipes = allRecipes.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
                    it.name.contains(query, ignoreCase = true)
        }.toMutableList()
        popularRecipesAdapter.updateRecipes(filteredRecipes)
    }
}
