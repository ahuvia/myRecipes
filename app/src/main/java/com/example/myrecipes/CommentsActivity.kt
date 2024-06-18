package com.example.myrecipes

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CommentsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        db = FirebaseFirestore.getInstance()

        val commentsLayout = findViewById<LinearLayout>(R.id.commentsLayout)
        val commentEditText = findViewById<EditText>(R.id.editTextComment)
        val sendCommentButton = findViewById<Button>(R.id.buttonSendComment)

        val recipeId = intent.getStringExtra("RECIPE_ID")

        if (recipeId != null) {
            loadComments(recipeId, commentsLayout)

            sendCommentButton.setOnClickListener {
                val commentText = commentEditText.text.toString()
                if (commentText.isNotEmpty()) {
                    val comment = hashMapOf(
                        "text" to commentText,
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("recipes").document(recipeId).collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show()
                            commentEditText.text.clear()
                            loadComments(recipeId, commentsLayout)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error adding comment", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "No recipe ID found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadComments(recipeId: String, layout: LinearLayout) {
        layout.removeAllViews()  // לנקות את כל התצוגות הקיימות
        db.collection("recipes").document(recipeId).collection("comments")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val commentText = document.getString("text")
                    val textView = TextView(this)
                    textView.text = commentText
                    layout.addView(textView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show()
            }
    }
}
