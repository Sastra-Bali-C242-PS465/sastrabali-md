package com.example.sabi.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import com.example.sabi.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions
import org.tensorflow.lite.task.core.BaseOptions

class ImageClassifierHelper(
    private var threshold: Float = 0.1f,
    private var maxResults: Int = 1,
    private val modelName: String = "aksara_classification.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        val bitmap = getBitmapFromUri(imageUri)

        val processedImage = preprocessImage(bitmap, 192, 192)

        val inferenceTime = System.currentTimeMillis()
        val results = runInference(processedImage)
        val timeElapsed = System.currentTimeMillis() - inferenceTime

        classifierListener?.onResults(results, timeElapsed)
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IllegalArgumentException("Could not decode bitmap from Uri.")
    }

    private fun preprocessImage(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): TensorImage {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        val grayscaleBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        for (x in 0 until targetWidth) {
            for (y in 0 until targetHeight) {
                val pixel = resizedBitmap.getPixel(x, y)
                val gray = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114).toInt()
                val rgbPixel = Color.rgb(gray, gray, gray)
                grayscaleBitmap.setPixel(x, y, rgbPixel)
            }
        }

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(grayscaleBitmap)
        return tensorImage
    }

    private fun runInference(processedImage: TensorImage): List<Classifications>? {
        return try {
            imageClassifier?.classify(processedImage)
        } catch (e: Exception) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
            null
        }
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>?, inferenceTime: Long)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}
