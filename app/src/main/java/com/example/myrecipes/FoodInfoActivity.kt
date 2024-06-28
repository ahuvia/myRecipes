package com.example.myrecipes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class FoodInfoActivity : AppCompatActivity() {

    private lateinit var textViewFoodInfo: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_info)

        textViewFoodInfo = findViewById(R.id.textViewFoodInfo)

        fetchFoodInfo()
    }

    private fun fetchFoodInfo() {
        val request = Request.Builder()
            .url("https://api.spoonacular.com/recipes/random?apiKey=d817096f03584175a7d5d01a734f81c8&number=1")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    textViewFoodInfo.text = "Failed to load data"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = JSONObject(responseBody.string())
                    val recipes = json.getJSONArray("recipes")
                    if (recipes.length() > 0) {
                        val recipe = recipes.getJSONObject(0)
                        val title = recipe.getString("title")
                        val summary = recipe.getString("summary")
                        runOnUiThread {
                            textViewFoodInfo.text = "$title\n\n$summary"
                        }
                    }
                }
            }
        })
    }
}
