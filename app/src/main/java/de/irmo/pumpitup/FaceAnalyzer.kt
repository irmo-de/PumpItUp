package de.irmo.pumpitup

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(private val onPushupCounted: () -> Unit) : ImageAnalysis.Analyzer {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .build()
    private val detector = FaceDetection.getClient(options)

    private var isFaceCurrentlyVisible = false
    private var wasFaceClose = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        isFaceCurrentlyVisible = true
                        val face = faces.first()
                        val box = face.boundingBox
                        
                        val faceArea = box.width() * box.height()
                        val imageArea = image.width * image.height
                        
                        // If face covers more than 35% of the frame, consider it close
                        val isClose = (faceArea.toFloat() / imageArea.toFloat()) > 0.35f
                        if (isClose) {
                            wasFaceClose = true
                        }
                    } else {
                        // Face is invisible now
                        if (isFaceCurrentlyVisible && wasFaceClose) {
                            onPushupCounted()
                        }
                        isFaceCurrentlyVisible = false
                        wasFaceClose = false
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
