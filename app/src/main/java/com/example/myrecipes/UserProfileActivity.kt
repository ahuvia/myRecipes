package com.example.myrecipes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userRecipesAdapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser

        val profileImageView = findViewById<ImageView>(R.id.imageViewProfile)
        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val saveButton = findViewById<Button>(R.id.buttonSave)
        val userRecipesRecyclerView = findViewById<RecyclerView>(R.id.recyclerViewUserRecipes)

        userRecipesRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecipesAdapter = RecipeAdapter(mutableListOf())
        userRecipesRecyclerView.adapter = userRecipesAdapter

        currentUser?.let { user ->
            emailEditText.setText(user.email)
            loadUserProfile(user.uid)
            loadUserRecipes(user.uid)
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (currentUser != null && name.isNotEmpty()) {
                saveUserProfile(currentUser.uid, name)
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        val profileImageView = findViewById<ImageView>(R.id.imageViewProfile)
                        val nameEditText = findViewById<EditText>(R.id.editTextName)
                        nameEditText.setText(user.name)
                        if (user.profileImageUrl.isNotEmpty()) {
                            Glide.with(this).load(user.profileImageUrl).into(profileImageView)
                        }
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile(userId: String, name: String) {
        db.collection("users").document(userId)
            .update("name", name)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserRecipes(userId: String) {
        db.collection("recipes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val recipes = result.toObjects(Recipe::class.java)
                userRecipesAdapter.updateRecipes(recipes)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading recipes", Toast.LENGTH_SHORT).show()
            }
    }
}
