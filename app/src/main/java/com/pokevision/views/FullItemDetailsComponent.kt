package com.pokevision.views

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.marginBottom
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokevision.BuildConfig
import com.pokevision.R
import com.pokevision.models.ImagePrediction
import com.pokevision.models.Item
import com.pokevision.models.ItemDetails
import com.pokevision.models.NetworkResult
import com.pokevision.models.Set
import com.pokevision.viewmodels.ImageViewModel
import com.pokevision.viewmodels.ViewModelFactory
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun FullItemDetailsComponent(item: Item, itemDetails: ItemDetails, set: Set) {
    val imageViewModel : ImageViewModel = viewModel(factory = ViewModelFactory())
    val bitmap = imageViewModel.getImageForItem(item).collectAsState()
    val imageHeight = 200.dp
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AndroidView(
            modifier = Modifier.padding(bottom = 16.dp),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.ADMOB_RESULTS_BANNER_ID
                    loadAd(AdRequest.Builder().build())
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        )
        when (val result = bitmap.value) {
            NetworkResult.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.height(imageHeight).padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
            is NetworkResult.Error -> {
                CircularProgressIndicator(
                    modifier = Modifier.height(imageHeight).padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }
            is NetworkResult.Success -> {
                Image(bitmap = result.data.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.height(imageHeight).aspectRatio(1/1.4f),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Text(text = item.name, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(text = set.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            stringResource(R.string.result_prices, itemDetails.ungradedPrice),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            stringResource(R.string.result_psa_10_prices, itemDetails.psa10Price),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            stringResource(R.string.result_psa_9_prices, itemDetails.psa9Price),
            style = MaterialTheme.typography.labelLarge
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            BottomButton(modifier = Modifier.weight(1f), {
                val intent = Intent(Intent.ACTION_VIEW, item.url.toUri())
                context.startActivity(intent)
            }, stringResource(R.string.open_in_pricecharting))
            BottomButton(modifier = Modifier.weight(1f), {
                val ebayUri = getEbaySearchUri("${item.name} ${set.name}")
                val intent = Intent(Intent.ACTION_VIEW, ebayUri)
                context.startActivity(intent)
            }, stringResource(R.string.open_in_ebay))
        }
    }
}

@Composable
fun BottomButton(modifier: Modifier, onClick: () -> Unit, text: String) {
    return Button(onClick = onClick, modifier = modifier.padding(8.dp)) {
        Text(text)
    }
}

fun getEbaySearchUri(newKeyword: String): Uri {
    val uri = "https://www.ebay.com/sch/i.html?_nkw=pokemon&LH_Sold=1&LH_Complete=1".toUri()
    val newUri = uri.buildUpon()
        .clearQuery()
        .apply {
            // Re-add all query parameters, replacing _nkw value
            for (paramName in uri.queryParameterNames) {
                val value = if (paramName == "_nkw") newKeyword else uri.getQueryParameter(paramName)
                appendQueryParameter(paramName, value)
            }
        }
        .build()
    return newUri
}