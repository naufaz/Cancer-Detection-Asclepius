package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.text.NumberFormat
import java.util.Date
import com.yalantis.ucrop.UCrop

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            galleryButton.setOnClickListener {
                startGallery()
            }
            analyzeButton.setOnClickListener {
                currentImageUri?.let {
                    analyzeImage(it)
                }
            }
        }
    }



    private fun startGallery() =
        // TODO: Mendapatkan gambar dari Gallery.
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            launchUCrop(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun launchUCrop(uri: Uri) {
        val timestamp = Date().time
        val cachedImage = File(cacheDir, "cropped_image_${timestamp}.jpg")

        val destinationUri = Uri.fromFile(cachedImage)

        val uCrop = UCrop.of(uri, destinationUri).withAspectRatio(1f, 1f)

        uCrop.getIntent(this@MainActivity).apply {
            launcherUCrop.launch(this) // "this" keyword is reference to intent, not activity
        }
    }

    private val launcherUCrop =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val resultUri = UCrop.getOutput(result.data!!)
                if (resultUri != null) {
                    currentImageUri = resultUri
                    showImage()
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val error = UCrop.getError(result.data!!)
                showToast("Error: ${error?.localizedMessage}")
            }
        }

    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.

        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        } ?: showToast("currentImageUri is null")
    }

    private fun analyzeImage(imageUri: Uri) {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.

        CoroutineScope(Dispatchers.Main).launch {
            showProgressAndDisableButtons(true)

            try {
                withContext(Dispatchers.IO) {
                    val imageClassifierHelper = ImageClassifierHelper(context = this@MainActivity,
                        classifierListener = object : ImageClassifierHelper.ClassifierListener {
                            override fun onError(error: String) {
                                showToast("Error: $error")
                            }

                            override fun onResults(
                                results: List<Classifications>?,
                                inferenceTime: Long
                            ) {
                                results?.let { listClassification ->
                                    if (listClassification.isNotEmpty() && listClassification[0].categories.isNotEmpty()) {
                                        val sortedCategories =
                                            listClassification[0].categories.sortedByDescending { it?.score }
                                        moveToResult(sortedCategories)
                                    }
                                }
                            }

                        })

                    imageClassifierHelper.classifyStaticImage(imageUri)
                    withContext(Dispatchers.Main) {
                        showProgressAndDisableButtons(false)
                    }
                }
            } catch (e: Exception) {
                Log.d("MainActivity", e.message.toString())
            }
        }
    }

    private fun showProgressAndDisableButtons(isActive: Boolean) {
        with(binding) {
            progressIndicator.visibility = if (isActive) View.VISIBLE else View.INVISIBLE
            galleryButton.isEnabled = !isActive
            analyzeButton.isEnabled = !isActive
        }
    }

    private fun moveToResult(results: List<Category>) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
        val index = results[0]?.index.toString() == "1"
        val score = results[0]?.score.toString()
        intent.putExtra(ResultActivity.EXTRA_RESULT_INDEX, index)
        intent.putExtra(ResultActivity.EXTRA_RESULT_SCORE, score)
        startActivity(intent)
    }
    private fun formatNumberToPercent(score: Float): String =
        NumberFormat.getPercentInstance().format(score)


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}