package de.irmo.pumpitup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import de.irmo.pumpitup.ui.CounterScreen
import de.irmo.pumpitup.ui.theme.PumpItUpTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private var audioHelper: AudioHelper? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        audioHelper = AudioHelper()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        enableEdgeToEdge()
        setContent {
            PumpItUpTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                var count by remember { mutableIntStateOf(0) }
                
                val cameraController = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        setImageAnalysisAnalyzer(
                            cameraExecutor,
                            FaceAnalyzer {
                                runOnUiThread {
                                    audioHelper?.playBeep()
                                    count++
                                }
                            }
                        )
                    }
                }
                
                LaunchedEffect(lifecycleOwner) {
                    cameraController.bindToLifecycle(lifecycleOwner)
                }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CounterScreen(
                        count = count,
                        cameraController = cameraController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        audioHelper?.release()
    }
}