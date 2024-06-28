package com.example.myrecipes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ConversionActivity : AppCompatActivity() {

    private lateinit var editTextIngredientName: EditText
    private lateinit var editTextSourceAmount: EditText
    private lateinit var editTextSourceUnit: EditText
    private lateinit var editTextTargetUnit: EditText
    private lateinit var buttonConvert: Button
    private lateinit var textViewConversionResult: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversion)

        editTextIngredientName = findViewById(R.id.editTextIngredientName)
        editTextSourceAmount = findViewById(R.id.editTextSourceAmount)
        editTextSourceUnit = findViewById(R.id.editTextSourceUnit)
        editTextTargetUnit = findViewById(R.id.editTextTargetUnit)
        buttonConvert = findViewById(R.id.buttonConvert)
        textViewConversionResult = findViewById(R.id.textViewConversionResult)

        buttonConvert.setOnClickListener {
            val ingredientName = editTextIngredientName.text.toString()
            val sourceAmount = editTextSourceAmount.text.toString()
            val sourceUnit = editTextSourceUnit.text.toString()
            val targetUnit = editTextTargetUnit.text.toString()

            if (ingredientName.isNotEmpty() && sourceAmount.isNotEmpty() && sourceUnit.isNotEmpty() && targetUnit.isNotEmpty()) {
                fetchConversion(ingredientName, sourceAmount.toDouble(), sourceUnit, targetUnit)
            } else {
                textViewConversionResult.text = "Please fill all fields"
            }
        }
    }

    private fun fetchConversion(ingredientName: String, sourceAmount: Double, sourceUnit: String, targetUnit: String) {
        val url = "https://api.spoonacular.com/recipes/convert?ingredientName=$ingredientName&sourceAmount=$sourceAmount&sourceUnit=$sourceUnit&targetUnit=$targetUnit&apiKey=d817096f03584175a7d5d01a734f81c8"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    textViewConversionResult.text = "Failed to load data"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val json = JSONObject(responseBody.string())
                    val answer = json.getString("answer")
                    runOnUiThread {
                        textViewConversionResult.text = answer
                    }
                }
            }
        })
    }
}
