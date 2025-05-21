package com.example.pokevision.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.impl.utils.MatrixExt.postRotate
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pokevision.R
import com.example.pokevision.models.ImagePrediction
import com.example.pokevision.models.NetworkResult
import com.example.pokevision.viewmodels.ImageViewModel
import com.example.pokevision.viewmodels.ViewModelFactory

@Composable
fun ResultsContainerComponent(modifier: Modifier = Modifier) {
    val imageViewModel : ImageViewModel = viewModel(factory = ViewModelFactory())
    val snapshotFile = imageViewModel.imageFile.collectAsState()
    val networkResult = imageViewModel.predictions.collectAsState()

    Row(modifier =modifier
        .fillMaxWidth()
        .height(132.dp)
        .background(Color.Black.copy(alpha = 0.6f)), verticalAlignment = Alignment.CenterVertically) {
        if (snapshotFile.value == null) {
            Text(stringResource(R.string.results_start_instruction), modifier = Modifier
                .padding(horizontal = 16.dp), color = Color.White)
        } else {
            Image(bitmap = loadBitmapForcedPortrait(snapshotFile.value!!.absolutePath)!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.height(132.dp).padding(16.dp))
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight(0.8f)
                    .background(Color.Gray)
            )
            when (val result = networkResult.value) {
                NetworkResult.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(48.dp).padding(16.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                    }
                }
                is NetworkResult.Error -> {
                    Button(modifier = Modifier.padding(16.dp), onClick = {imageViewModel.rerunNetworkPrediction()}) {
                        Text(stringResource(R.string.results_try_again), textAlign = TextAlign.Center)
                    }
                }
                is NetworkResult.Success -> {
                    LazyRow {
                        items(result.data) {
                            item -> ImagePredictionComponent(item)
                        }
                    }
                }
            }
        }
    }
}

fun loadBitmapForcedPortrait(filePath: String): Bitmap? {
    val bitmap = BitmapFactory.decodeFile(filePath) ?: return null

    // Check if already portrait (height >= width)
    if (bitmap.height >= bitmap.width) {
        return bitmap
    }

    // Rotate 90 degrees clockwise to make it portrait
    val matrix = Matrix().apply {
        postRotate(90f)
    }

    return Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )
}