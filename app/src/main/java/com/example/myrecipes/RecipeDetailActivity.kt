package com.example.myrecipes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var commentsAdapter: CommentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        db = FirebaseFirestore.getInstance()

        val recipeNameTextView = findViewById<TextView>(R.id.textViewRecipeName)
        val recipeImageView = findViewById<ImageView>(R.id.imageViewRecipe)
        val recipeDescriptionTextView = findViewById<TextView>(R.id.textViewRecipeDescription)
        val ingredientsTextView = findViewById<TextView>(R.id.textViewIngredients)
        val instructionsTextView = findViewById<TextView>(R.id.textViewInstructions)
        val likeButton = findViewById<Button>(R.id.buttonLike)
        val likesCountTextView = findViewById<TextView>(R.id.textViewLikesCount)
        val commentButton = findViewById<Button>(R.id.buttonComment)
        val commentsTitleTextView = findViewById<TextView>(R.id.textViewCommentsTitle)
        val recyclerViewComments = findViewById<RecyclerView>(R.id.recyclerViewComments)

        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter()
        recyclerViewComments.adapter = commentsAdapter

        val recipeId = intent.getStringExtra("RECIPE_ID")

        if (!recipeId.isNullOrEmpty()) {
            db.collection("recipes").document(recipeId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val recipe = document.toObject(Recipe::class.java)
                        if (recipe != null) {
                            recipeNameTextView.text = recipe.name
                            Picasso.get().load(recipe.imageUrl).into(recipeImageView)
                            recipeDescriptionTextView.text = recipe.description
                            ingredientsTextView.text = recipe.ingredients.joinToString("\n")
                            instructionsTextView.text = recipe.instructions
                            likesCountTextView.text = "Likes: ${recipe.likes}"

                            likeButton.setOnClickListener {
                                db.collection("recipes").document(recipeId)
                                    .update("likes", recipe.likes + 1)
                                    .addOnSuccessListener {
                                        recipe.likes += 1
                                        likesCountTextView.text = "Likes: ${recipe.likes}"
                                        Toast.makeText(this, "Liked!", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error liking recipe", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            commentButton.setOnClickListener {
                                val intent = Intent(this, CommentsActivity::class.java)
                                intent.putExtra("RECIPE_ID", recipeId)
                                startActivity(intent)
                            }

                            loadComments(recipeId)
                        } else {
                            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error loading recipe", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No recipe ID found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComments(recipeId: String) {
        db.collection("recipes").document(recipeId).collection("comments")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                val comments = result.toObjects(Comment::class.java)
                commentsAdapter.submitList(comments)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show()
            }
    }
}
