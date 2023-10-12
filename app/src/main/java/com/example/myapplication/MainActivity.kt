package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import java.io.ByteArrayOutputStream


import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Function to handle the transition to the second activity
    private fun navigateToSecondActivity(response: Response) {
        println("Creating intent")
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("JSON_STRING", response.body()?.string())
        println("Switching to second activity")
        startActivity(intent)
    }

    fun selectPhoto(view: View) {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> cameraLauncher.launch(null)
                options[item] == "Choose from Gallery" -> galleryLauncher.launch("image/*")
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { result ->
            // Handle the result from the camera
            if (result != null) {
                // Convert the bitmap to bytes
                val imageBytes = convertBitmapToBytes(result)

                Log.d("Update", "Calling API with image from camera")
                // Call the function to send image bytes to the API
                callAPI(imageBytes)
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            // Handle the result from the gallery
            if (result != null) {
                val inputStream = contentResolver.openInputStream(result)
                if (inputStream != null) {
                    val imageBytes = inputStream.readBytes()
                    Log.d("Update", "Calling API with image from gallery")
                    // Call the function to send image bytes to the API
                    callAPI(imageBytes)
                } else {
                    // Handle null inputStream
                    Log.d("Update", "inputStream was null, aborting.")
                }
            }
        }

    // Function to convert a Bitmap to bytes
    private fun convertBitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun callAPI(imageBytes: ByteArray) {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "image.jpg",
                RequestBody.create(MediaType.parse("image/*"), imageBytes)
            )
            .build()

        val request = Request.Builder()
            .url("http://192.168.56.1:8001/recognize_books")  // Replace with your API URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("Response code: ${response.code()}")
                    Log.d("Success", "Successfully received JSON from the API")
                    println("Response body: ${response.body()}")
                    navigateToSecondActivity(response)
                } else {
                    println("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Network error: ${e.message}")
            }
        })
    }
}