package com.example.myrecipes

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipeAdapter(
    private var recipes: List<Recipe>
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImageView: ImageView = view.findViewById(R.id.recipeImageView)
        val recipeNameTextView: TextView = view.findViewById(R.id.recipeNameTextView)
        val recipeLikesTextView: TextView = view.findViewById(R.id.recipeLikesTextView)
        val likesIcon: ImageView = view.findViewById(R.id.likesIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeNameTextView.text = recipe.name
        holder.recipeLikesTextView.text = recipe.likes.toString()
        Glide.with(holder.itemView.context).load(recipe.imageUrl).into(holder.recipeImageView)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        fun updateLikeIcon(isLiked: Boolean) {
            val likeIconRes = if (isLiked) R.drawable.ic_liked else R.drawable.ic_like
            holder.likesIcon.setImageResource(likeIconRes)
        }

        // Check if the user has already liked this recipe
        db.collection("users").document(currentUser!!.uid).collection("likedRecipes").document(recipe.id)
            .get()
            .addOnSuccessListener { document ->
                val isLiked = document.exists()
                updateLikeIcon(isLiked)
            }

        holder.likesIcon.setOnClickListener {
            val recipeRef = db.collection("recipes").document(recipe.id)
            val userLikeRef = db.collection("users").document(currentUser.uid).collection("likedRecipes").document(recipe.id)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(recipeRef)
                val currentLikes = snapshot.getLong("likes") ?: 0

                if (transaction.get(userLikeRef).exists()) {
                    // User already liked this recipe, remove the like
                    transaction.update(recipeRef, "likes", currentLikes - 1)
                    transaction.delete(userLikeRef)
                    -1
                } else {
                    // User hasn't liked this recipe yet, add a like
                    transaction.update(recipeRef, "likes", currentLikes + 1)
                    transaction.set(userLikeRef, mapOf("liked" to true))
                    1
                }
            }.addOnSuccessListener { change ->
                recipe.likes += change
                holder.recipeLikesTextView.text = recipe.likes.toString()
                updateLikeIcon(change > 0)
            }.addOnFailureListener { e ->
                Toast.makeText(holder.itemView.context, "Error updating likes", Toast.LENGTH_SHORT).show()
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    fun addRecipe(recipe: Recipe) {
        (recipes as MutableList).add(0, recipe)
        notifyItemInserted(0)
    }
}
