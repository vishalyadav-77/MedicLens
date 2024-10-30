package com.example.mediclens

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class Test : AppCompatActivity() {
    lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView=findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(null)

        val myimageuri: Uri? = intent.getParcelableExtra<Uri>("imageUri")

        Toast.makeText(this,"test acitivtiy", Toast.LENGTH_SHORT).show()
        //LOAD IMAGE
        if (myimageuri != null){
            //load image
            Glide.with(this)
                .load(myimageuri)
                .into(imageView)
        }else{
            Toast.makeText(this,"image not found", Toast.LENGTH_SHORT).show()
        }
    }
}