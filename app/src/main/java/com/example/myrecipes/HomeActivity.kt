package com.example.myrecipes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        val searchEditText = findViewById<EditText>(R.id.editTextSearch)
        val popularRecipesLayout = findViewById<LinearLayout>(R.id.popularRecipesLayout)
        val categoriesLayout = findViewById<LinearLayout>(R.id.categoriesLayout)

        // תצוגת מתכונים פופולריים (דוגמאות)
        val popularRecipes = listOf("Recipe 1", "Recipe 2", "Recipe 3")
        popularRecipes.forEach { recipe ->
            val textView = TextView(this)
            textView.text = recipe
            textView.setOnClickListener {
                // פעולה לעבור למסך פרטי המתכון
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
}
