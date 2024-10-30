package com.example.mediclens

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var cam: ImageView
    lateinit var gallery: ImageView
    private val CAMERACODE=100
    private val  gallery_code=101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cam= findViewById<ImageView>(R.id.camera)
        gallery= findViewById<ImageView>(R.id.gallery)

        cam.setOnClickListener{
            Toast.makeText(this,"Cam clicked", Toast.LENGTH_SHORT).show()
            OpenCamera()
        }
        gallery.setOnClickListener{
            Toast.makeText(this,"gallery clicked", Toast.LENGTH_SHORT).show()
            OpenGallery()
        }
    }
    fun OpenCamera(){
        //Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.CAMERA), CAMERACODE)
        }else{
            //start camera Intent
            val cam_intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cam_intent, CAMERACODE)
        }
    }
    fun OpenGallery(){
        //Check for gallery permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), gallery_code)
            }
            else{
                startGalleryIntent()
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android 6.0 to 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), gallery_code)
            }
            else{
                startGalleryIntent()
            }
        }else{
            //start gallery intent
            startGalleryIntent()
        }
    }

    private fun startGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, gallery_code)
    }
    fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        // Create a temporary file in the cache directory
        val file = File(cacheDir, "captured_image.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return Uri.fromFile(file)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CAMERACODE && resultCode== Activity.RESULT_OK){
//
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val imageUri= saveBitmapToFile(imageBitmap)
            imageUri?.let { SendToOCR(it) } ?: run {
                Toast.makeText(this, "Error: Image URI is null", Toast.LENGTH_SHORT).show()
            }

        }else if (requestCode==gallery_code && resultCode== Activity.RESULT_OK){
            val imageuri: Uri? =data?.data
            imageuri?.let { SendToOCR(it) } ?: run {
                // Handle the error: Uri is null
                Toast.makeText(this, "Error: Image URI is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun SendToOCR(image: Uri){
        Toast.makeText(this,"sending to OCR", Toast.LENGTH_SHORT).show()
        val intent= Intent(this,OCR_Activity::class.java)
        intent.putExtra("imageUri",image)
        startActivity(intent)
    }
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        // Convert the URI to Bitmap
        val contentResolver: ContentResolver = contentResolver
        return try {
            // Open an InputStream from the Uri
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            // Decode the InputStream to a Bitmap
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onRequestPermissionsResult( //***THIS is to handle permissions***
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERACODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, open the camera
                    OpenCamera()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(
                        this,
                        "Camera permission is required to take photos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            gallery_code -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, open the gallery
                    startGalleryIntent()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(
                        this,
                        "Gallery permission is required to access photos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }
}