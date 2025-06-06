package com.pokevision.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import androidx.camera.core.*
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokevision.BuildConfig
import com.pokevision.viewmodels.ImageViewModel
import com.pokevision.viewmodels.ViewModelFactory
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pokevision.viewmodels.AdsViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraPreviewComponent() {
    val adsViewModel : AdsViewModel = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val previewView = remember { PreviewView(context) }
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val capture = ImageCapture.Builder().build()

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                capture
            )

            previewView.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)

                    val action = FocusMeteringAction.Builder(point)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()

                    camera.cameraControl.startFocusAndMetering(action)
                }
                true
            }
        } catch (e: Exception) {
            Log.e("CameraPreview", "Failed to bind camera use cases", e)
        }
    }

    LaunchedEffect(interstitialAd) {
        if (interstitialAd == null) {
            adsViewModel.loadInterstitialAd(context,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay: dim outside rectangle with white border
        CameraOverlay()

        // Capture button floating on preview
        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(vertical = 16.dp)) {
            ResultsContainerComponent(Modifier.padding(vertical = 16.dp))
            CameraIconComponent(previewView, Modifier.padding(16.dp).align(Alignment.CenterHorizontally)) {
                adsViewModel.maybeShowInterstitialAd(context as Activity, interstitialAd) {
                    interstitialAd = null
                }
            }
        }
    }
}

@Composable
fun CameraOverlay(
    borderColor: Color = Color.White,
    borderWidth: Float = 4f,
    dimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    val imageViewModel : ImageViewModel = viewModel(factory = ViewModelFactory())
    // Hold current canvas size in state
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Cache computed rect and path depending on canvasSize
    val (centerRect, dimPath) = remember(canvasSize) {
        if (canvasSize == Size.Zero) {
            // Placeholder before size is known
            Pair(Rect.Zero, Path())
        } else {
            val rect = imageViewModel.getCenterRect(canvasSize.height, canvasSize.width)
            val path = Path().apply {
                addRect(Rect(0f, 0f, canvasSize.width, canvasSize.height), Path.Direction.Clockwise)
                addRect(rect, Path.Direction.CounterClockwise)
                fillType = PathFillType.EvenOdd
            }
            Pair(rect, path)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { newSize ->
                // Update canvas size when layout size changes
                canvasSize = Size(newSize.width.toFloat(), newSize.height.toFloat())
            }
    ) {
        if (canvasSize != Size.Zero) {
            drawPath(dimPath, color = dimColor)
            drawRect(
                color = borderColor,
                topLeft = centerRect.topLeft,
                size = centerRect.size,
                style = Stroke(width = borderWidth)
            )
        }
    }
}



@Composable
fun CameraIconComponent(previewView: PreviewView, modifier: Modifier = Modifier, onClick : () -> Unit) {
    val context = LocalContext.current
    val imageViewModel : ImageViewModel = viewModel(factory = ViewModelFactory())

    Surface(
        shape = CircleShape,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(72.dp)
    ) {
        IconButton(
            onClick = {
                onClick()

                val photoFile = createFile(context)
                val fullBitmap : Bitmap? = previewView.bitmap

                fullBitmap?.let {
                    imageViewModel.cropPreviewImageSnapshot(fullBitmap, photoFile)
                    imageViewModel.onNewImageCaptured(photoFile)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Capture",
                tint = Color.White
            )
        }
    }
}

private fun createFile(context: Context): File {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
    val filename = "JPEG_${sdf.format(Date())}_.jpg"
    val outputDir = context.cacheDir
    return File(outputDir, filename)
}