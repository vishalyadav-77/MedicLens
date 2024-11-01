package com.example.mediclens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.InputStream
import java.io.ByteArrayOutputStream
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OCR_Activity : AppCompatActivity() {
    private val apikey="3d870a1c9588957"
    private lateinit var extractedTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ocr)

        extractedTextView = findViewById(R.id.textviewresult)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Get the Intent that started this activity
//        val imageUri = intent.getParcelableExtra<Uri>("imageUri")
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri: Uri = Uri.parse(imageUriString)

       imageUri?.let {
           // Convert URI to Bitmap
           val bitmap = uriToBitmap(it)

           if (bitmap != null) {
               val imagePart = convertBitmapToMultipart(bitmap)

               val languagePart = RequestBody.create("text/plain".toMediaTypeOrNull(), "eng") // Create RequestBody for language

               // Send the image part to the API
               val call = RetrofitClient.instance.uploadImage(apikey, imagePart, languagePart)
               call.enqueue(object : Callback<ResponseBody> {
                   override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                       if (response.isSuccessful) {
                           response.body()?.let { responseBody ->
                               val result = responseBody.string()
                               Log.d("OCR Response", result)
                               displayExtractedText(result)
                           } ?: run{
                               Log.e("OCR Error", "Response body is null")
                           }
                       } else {
                           Log.e("OCR Error else{success}", response.errorBody()?.string().orEmpty())
                       }
                   }

                   override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                       Log.e("OCR Error", t.message.orEmpty())
                       Log.e("OCR Error", "Timeout or other error: ${t.message}")
                       extractedTextView.text = "Error: ${t.message}"
                   }
               })
           } else {
               Toast.makeText(this, "Failed to convert image", Toast.LENGTH_SHORT).show()
           }
       } ?: run {
           Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()
       }

    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayExtractedText(response: String) {
        // Parse the JSON response to get extracted text
        // Assuming the OCR.space response contains `ParsedResults` field
        val extractedText = parseOCRResult(response)
        extractedTextView.text = extractedText
    }

    private fun convertBitmapToMultipart(bitmap: Bitmap): MultipartBody.Part { //okhttp
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        return MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
    }
    private fun parseOCRResult(response: String): String {
        val jsonResponse = JSONObject(response)

        // Check if `ParsedResults` field exists
        if (jsonResponse.has("ParsedResults")) {
            val parsedResults: JSONArray = jsonResponse.optJSONArray("ParsedResults") ?: return "No results found."

            val parsedText = StringBuilder()
            for (i in 0 until parsedResults.length()) {
                val result = parsedResults.optJSONObject(i)
                result?.let {
                    // Check if `ParsedText` field exists within each result
                    val text = it.optString("ParsedText", "No text found")
                    parsedText.append(text)
                }
            }
            return if (parsedText.isNotEmpty()) parsedText.toString() else "No text found in the image."
        } else {
            return "ParsedResults field not found in response. The image quality was bad."
        }
    }

}