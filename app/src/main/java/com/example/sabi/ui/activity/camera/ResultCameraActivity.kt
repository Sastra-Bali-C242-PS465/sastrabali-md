package com.example.sabi.ui.activity.camera

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sabi.R
import java.io.IOException

class ResultCameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_camera)

        val resultImageView: ImageView = findViewById(R.id.resultImageView)
        val backButton: Button = findViewById(R.id.backButton)
        val resultTextView: TextView = findViewById(R.id.resultTextView) // Tambahkan TextView untuk hasil klasifikasi


        val imagePath = intent.getStringExtra("image_path")
        val label = intent.getStringExtra("label") ?: "Unknown"
        val confidence = intent.getFloatExtra("confidence", 0f)

        if (imagePath != null) {
            try {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                resultImageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        resultTextView.text = getString(R.string.result_text, label, (confidence * 100).toInt())

        backButton.setOnClickListener {
            finish()
        }
    }
}
