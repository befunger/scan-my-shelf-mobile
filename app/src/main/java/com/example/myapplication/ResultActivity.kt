package com.example.myapplication

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Retrieve API result
        val jsonString = intent.getStringExtra("JSON_STRING")
        val jsonObject = JSONObject(jsonString)
        val booksArray = jsonObject.getJSONArray("books")

        // Display results
        val bookInfoLayout = findViewById<LinearLayout>(R.id.bookInfoLayout)
        for (i in 0 until booksArray.length()) {
            val bookObject = booksArray.getJSONObject(i)
            val author = bookObject.getString("author")
            val title = bookObject.getString("title")

            val bookTextView = TextView(this)
            bookTextView.text = "Title: $title\nAuthor: $author"
            bookTextView.setPadding(0, 0, 0, 16) // Add some padding between book entries
            bookInfoLayout.addView(bookTextView)
        }
    }
}