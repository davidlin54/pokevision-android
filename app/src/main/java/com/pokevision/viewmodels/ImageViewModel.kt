package com.pokevision.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevision.models.ImagePrediction
import com.pokevision.models.Item
import com.pokevision.models.NetworkResult
import com.pokevision.repositories.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class ImageViewModel(private val repository: ImageRepository) : ViewModel() {
    private val _imageFile = MutableStateFlow<File?>(null)
    val imageFile : StateFlow<File?> = _imageFile

    private val _predictions = MutableStateFlow<NetworkResult<Array<ImagePrediction>>>(NetworkResult.Loading)
    val predictions : StateFlow<NetworkResult<Array<ImagePrediction>>> = _predictions

    private val imageMap = mutableMapOf<Item, MutableStateFlow<NetworkResult<Bitmap>>>()

    private fun runNetworkPrediction(bytes: ByteArray) {
        _predictions.value = NetworkResult.Loading
        val base64Encoding = Base64.encodeToString(bytes, Base64.NO_WRAP)

        if (!base64Encoding.isNullOrEmpty()) {
            viewModelScope.launch {
                _predictions.value = repository.getImagePredictions(base64Encoding)
            }
        }
    }

    fun onNewImageCaptured(file: File) {
        _imageFile.value = file
        val bytes = file.readBytes()
        runNetworkPrediction(bytes)
    }

    fun rerunNetworkPrediction() {
        runNetworkPrediction(bytes = _imageFile.value?.readBytes()!!)
    }

    fun getImageForItem(item: Item) : StateFlow<NetworkResult<Bitmap>> {
        val savedState = imageMap[item]
        if (savedState != null && savedState.value !is NetworkResult.Error) {
            return savedState
        }

        // purge if > 200 in cache
        if (imageMap.size > 200) {
            imageMap.clear()
        }

        val bitmapState = MutableStateFlow<NetworkResult<Bitmap>>(NetworkResult.Loading)
        viewModelScope.launch {
            bitmapState.value = repository.getImageForItem(item)
        }

        imageMap[item] = bitmapState
        return bitmapState
    }

    fun getCenterRect(canvasHeight: Float, canvasWidth: Float) : Rect {
        val rectWidth = canvasWidth * 0.7f
        val rectHeight = rectWidth * 1.4f

        // Center rectangle coords
        val left = (canvasWidth - rectWidth) / 2f
        val top = canvasHeight / 10f
        val right = left + rectWidth
        val bottom = top + rectHeight

        return Rect(left, top, right, bottom)
    }

    fun cropPreviewImageSnapshot(fullBitmap: Bitmap, photoFile: File) {
        val cropRect =
            getCenterRect(fullBitmap.height.toFloat(), fullBitmap.width.toFloat())

        val croppedBitmap = Bitmap.createBitmap(
            fullBitmap,
            cropRect.left.toInt(),
            cropRect.top.toInt(),
            cropRect.width.toInt(),
            cropRect.height.toInt()
        )

        // 5. Save the cropped bitmap back to a file
        FileOutputStream(photoFile).use { outStream ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
        }
    }
}