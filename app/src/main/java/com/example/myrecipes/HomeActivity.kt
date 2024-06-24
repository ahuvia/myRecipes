package com.example.myrecipes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
                val recipes = result.map { document ->
                    val recipe = document.toObject(Recipe::class.java)
                    recipe.id = document.id
                    recipe
                }
                popularRecipesAdapter = RecipeAdapter(recipes)
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
        popularRecipesAdapter.addRecipe(recipe)
    }
}

class RecipeAdapter(private val recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.recipeImageView)
        val textView: TextView = view.findViewById(R.id.recipeNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.textView.text = recipe.name
        Glide.with(holder.itemView.context).load(recipe.imageUrl).into(holder.imageView)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = recipes.size

    fun addRecipe(recipe: Recipe) {
        (recipes as MutableList).add(0, recipe)
        notifyItemInserted(0)
    }
}
