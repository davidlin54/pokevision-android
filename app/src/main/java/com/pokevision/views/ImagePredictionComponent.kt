package com.pokevision.views

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.play.core.review.ReviewManagerFactory
import com.pokevision.R
import com.pokevision.models.ImagePrediction
import com.pokevision.models.NetworkResult
import com.pokevision.viewmodels.ImageViewModel
import com.pokevision.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.pokevision.viewmodels.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePredictionComponent(imagePrediction: ImagePrediction) {
    val imageViewModel : ImageViewModel = viewModel(factory = ViewModelFactory())
    val reviewViewModel : ReviewViewModel = viewModel()

    val item = imagePrediction.item
    val itemDetails = imagePrediction.itemDetails
    val set = imagePrediction.set
    val prediction = imagePrediction.prediction

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet = remember { mutableStateOf(false) }
    val activity = LocalContext.current as Activity

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet.value = false

                reviewViewModel.maybeLaunchReviewRequest(activity)
            },
            sheetState = sheetState
        ) {
            FullItemDetailsComponent(item, itemDetails, set)
        }
    }

    val bitmap = imageViewModel.getImageForItem(item).collectAsState()

    Row(modifier = Modifier.padding(16.dp)
        .background(Color.LightGray)
        .fillMaxHeight().clickable{
            showSheet.value = true
            scope.launch{ sheetState.show() }
        }) {
        when (val result = bitmap.value) {
            NetworkResult.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.height(48.dp).padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
            is NetworkResult.Error -> {
                CircularProgressIndicator(
                    modifier = Modifier.height(48.dp).padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
            is NetworkResult.Success -> {
                Image(
                    bitmap = result.data.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.height(132.dp).aspectRatio(1 / 1.4f),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Column(modifier = Modifier.fillMaxHeight().width(150.dp).padding(start = 8.dp), verticalArrangement = Arrangement.Center) {
            Text(text = item.name, maxLines = 2, style = MaterialTheme.typography.titleSmall)
            Text(text = set.name, maxLines = 1, style = MaterialTheme.typography.bodySmall, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(R.string.result_prediction_confidence, (prediction * 100f)),
                color = if (prediction < 0.5) Color.Red else Color(0xFF006400),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                stringResource(R.string.result_prices, itemDetails.ungradedPrice),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}