package de.irmo.pumpitup

import android.annotation.SuppressLint
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
            
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces.first()
                        val box = face.boundingBox
                        
                        val faceArea = box.width() * box.height()
                        val imageArea = image.width * image.height
                        
                        val ratio = faceArea.toFloat() / imageArea.toFloat()
                        
                        // Hysteresis for workout counter
                        // Upper threshold to detect bottom of pushup
                        if (ratio > settingsRepository.getUpperThreshold()) {
                            wasFaceClose = true
                        } 
                        // Lower threshold to count completion of pushup
                        else if (ratio < settingsRepository.getLowerThreshold()) {
                            if (wasFaceClose) {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastPushupTime > settingsRepository.getDebounceTime()) {
                                    onPushupCounted()
                                    lastPushupTime = currentTime
                                }
                                wasFaceClose = false
                            }
                        }
                    } else {
                        // We intentionally do nothing here. If the face gets too close
                        // and the ML Kit detector drops the tracking, ignoring it
                        // here prevents false positives. Wait for it to return and cross lower threshold.
                    }
                }
                .addOnFailureListener {
                    // Ignore
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
