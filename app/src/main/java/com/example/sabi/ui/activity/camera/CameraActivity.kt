package com.example.sabi.ui.activity.camera

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.example.sabi.R
import com.example.sabi.data.repository.PredictionRepository
import com.example.sabi.data.retrofit.ApiConfig
import com.example.sabi.helper.ImageClassifierHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {

    private lateinit var previewView: PreviewView
    private lateinit var overlay: View
    private var imageCapture: ImageCapture? = null

    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var croppedFile: File? = null
    private val predictionRepository = PredictionRepository(ApiConfig.getApiService())

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = this
        )

        previewView = findViewById(R.id.previewView)
        overlay = findViewById(R.id.overlay)
        val captureButton: ImageButton = findViewById(R.id.captureButton)

        if (isCameraPermissionGranted()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        startCamera()

        captureButton.setOnClickListener {
            captureImage()
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Log.e("CameraActivity", "Camera permission denied")
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraActivity", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        val file = File(getExternalFilesDir(null), "captured_image.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("CameraActivity", "Image saved to: ${file.absolutePath}")
                    cropImage(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraActivity", "Image capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun cropImage(file: File) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)

        if (bitmap == null) {
            Log.e("CameraActivity", "Failed to decode bitmap from file path: ${file.absolutePath}")
            return
        }

        // Periksa orientasi gambar dan lakukan rotasi jika diperlukan
        val rotatedBitmap = rotateBitmapIfNeeded(bitmap, file)

        val previewWidth = previewView.width
        val previewHeight = previewView.height

        val overlayWidth = overlay.width
        val overlayHeight = overlay.height
        val overlayLeft = (previewWidth - overlayWidth) / 2
        val overlayTop = (previewHeight - overlayHeight) / 2

        // Hitung skala antara bitmap dan previewView
        val scaleX = rotatedBitmap.width.toFloat() / previewWidth
        val scaleY = rotatedBitmap.height.toFloat() / previewHeight

        // Hitung koordinat cropping
        val cropLeft = (overlayLeft * scaleX).toInt().coerceIn(0, rotatedBitmap.width - 1)
        val cropTop = (overlayTop * scaleY).toInt().coerceIn(0, rotatedBitmap.height - 1)
        val cropWidth = (overlayWidth * scaleX).toInt().coerceIn(0, rotatedBitmap.width - cropLeft)
        val cropHeight = (overlayHeight * scaleY).toInt().coerceIn(0, rotatedBitmap.height - cropTop)

        // Lakukan cropping
        val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, cropLeft, cropTop, cropWidth, cropHeight)

        croppedFile = File(getExternalFilesDir(null), "cropped_image.jpg") // Simpan file hasil crop
        try {
            FileOutputStream(croppedFile).use { out ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            val originalExif = ExifInterface(file.absolutePath)
            val croppedExif = ExifInterface(croppedFile!!.absolutePath)
            copyExifAttributes(originalExif, croppedExif)
            croppedExif.saveAttributes()

            Log.d("CameraActivity", "Cropped image file size: ${croppedFile!!.length()} bytes")
            Log.d("CameraActivity", "Cropped image saved to: ${croppedFile!!.absolutePath}")

            // Panggil fungsi classifyStaticImage untuk klasifikasi gambar
            imageClassifierHelper.classifyStaticImage(Uri.fromFile(croppedFile))

        } catch (e: IOException) {
            Log.e("CameraActivity", "Failed to save cropped image", e)
        }
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, file: File): Bitmap {
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun copyExifAttributes(source: ExifInterface, target: ExifInterface) {
        val attributes = listOf(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_WHITE_BALANCE
        )
        for (attribute in attributes) {
            source.getAttribute(attribute)?.let { value ->
                target.setAttribute(attribute, value)
            }
        }
    }

    override fun onError(error: String) {
        Log.e("CameraActivity", "Classification error: $error")
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        if (results != null && results.isNotEmpty()) {
            val topResult = results[0].categories.maxByOrNull { it.score }
            if (topResult != null) {
                val label = topResult.label
                val confidence = topResult.score


                croppedFile?.let { file ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val imageRequest = file.asRequestBody("image/*".toMediaType())
                        val multipartBody = MultipartBody.Part.createFormData(
                            "image", file.name, imageRequest
                        )
                        val resultRequest = "result_value".toRequestBody("text/plain".toMediaType())

                        try {
                            val response = withContext(Dispatchers.IO) {
                                predictionRepository.postPrediction(resultRequest, multipartBody)
                            }
                            if (response.isSuccessful) {
                                Log.d("CameraActivity", "Prediction uploaded successfully")
                            }
                        } catch (e: Exception) {
                            Log.e("CameraActivity", "Failed to upload prediction", e)
                        }
                    }
                }

                Log.d("CameraActivity", "Top Label: $label, Confidence: $confidence")

                val intent = Intent(this, ResultCameraActivity::class.java)
                croppedFile?.let {
                    intent.putExtra("image_path", it.absolutePath)
                }
                intent.putExtra("label", label)
                intent.putExtra("confidence", confidence)
                startActivity(intent)

            }
        }
        Log.d("CameraActivity", "Inference time: $inferenceTime ms")
    }
}
