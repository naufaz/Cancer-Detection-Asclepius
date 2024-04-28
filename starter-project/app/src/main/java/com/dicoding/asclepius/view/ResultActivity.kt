package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import com.dicoding.asclepius.databinding.ActivityResultBinding


@Suppress("DEPRECATION")
class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Mengatur listener untuk tombol kembali pada toolbar
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        binding.apply {
            val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
            val scoreResult = intent.getStringExtra(EXTRA_RESULT_SCORE)
            val index = intent.getBooleanExtra(EXTRA_RESULT_INDEX, false)

            if (scoreResult != null && imageUri != null) {

                val scoreCalculate = scoreResult.toFloat() * 100
                score.text = "$scoreCalculate %"
                resultImage.setImageURI(imageUri)
                if (index) {
                    resultText.text = Html.fromHtml("<b><font color=\"#00FF00\">Cancer - DETECTED</font></b>")
                } else {
                    resultText.text = Html.fromHtml("<b><font color=\"#FF0000\">Non Cancer - DETECTED</font></b>")
                }

//                save.setOnClickListener {
//                    // Implement your save logic here if necessary
//                    finish()
//                }
            }
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
        const val EXTRA_RESULT_SCORE = "extra_result_score"
        const val EXTRA_RESULT_INDEX = "extra_result_index"
    }
}