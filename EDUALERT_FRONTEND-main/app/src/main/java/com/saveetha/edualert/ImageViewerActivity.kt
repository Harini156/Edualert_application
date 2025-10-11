package com.saveetha.edualert

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val imageView = findViewById<ImageView>(R.id.imageView)
        val imageUrl = intent.getStringExtra("IMAGE_URL")

        if (imageUrl.isNullOrEmpty()) {
            Toast.makeText(this, "No image URL provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load image with Glide
        Glide.with(this)
            .load(imageUrl)
            .into(imageView)
    }
}
