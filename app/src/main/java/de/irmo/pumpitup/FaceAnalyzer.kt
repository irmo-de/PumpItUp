package de.irmo.pumpitup

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val settingsRepository: SettingsRepository,
    private val onPushupCounted: () -> Unit
) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()
    private val detector = FaceDetection.getClient(options)

    private var wasFaceClose = false
    private var lastPushupTime = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            try {
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val face = faces.first()
                            val box = face.boundingBox
                            
                            val faceArea = box.width() * box.height()
                            val imageArea = image.width * image.height
                            
                            val ratio = faceArea.toFloat() / imageArea.toFloat()
                            
                            val upper = settingsRepository.getUpperThreshold()
                            val lower = settingsRepository.getLowerThreshold()
                            
                            Log.d("PushupAnalyzer", "Face Ratio: $ratio, UpperThreshold: $upper, LowerThreshold: $lower, wasFaceClose: $wasFaceClose")
                            
                            // Hysteresis for workout counter
                            // Upper threshold to detect bottom of pushup
                            if (ratio > upper) {
                                if (!wasFaceClose) {
                                    Log.d("PushupAnalyzer", "Crossed upper threshold. Bottom of pushup reached.")
                                }
                                wasFaceClose = true
                            } 
                            // Lower threshold to count completion of pushup
                            else if (ratio < lower) {
                                if (wasFaceClose) {
                                    val currentTime = System.currentTimeMillis()
                                    val debounce = settingsRepository.getDebounceTime()
                                    if (currentTime - lastPushupTime > debounce) {
                                        Log.d("PushupAnalyzer", "Pushup counted!")
                                        onPushupCounted()
                                        lastPushupTime = currentTime
                                    } else {
                                        Log.d("PushupAnalyzer", "Pushup ignored due to debounce (${currentTime - lastPushupTime}ms <= ${debounce}ms)")
                                    }
                                    wasFaceClose = false
                                }
                            }
                        } else {
                            Log.d("PushupAnalyzer", "No face detected in this frame.")
                            // We intentionally do nothing here. If the face gets too close
                            // and the ML Kit detector drops the tracking, ignoring it
                            // here prevents false positives. Wait for it to return and cross lower threshold.
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("PushupAnalyzer", "Face detection failed!", e)
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } catch (e: Exception) {
                Log.e("PushupAnalyzer", "Synchronous error in process", e)
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}
