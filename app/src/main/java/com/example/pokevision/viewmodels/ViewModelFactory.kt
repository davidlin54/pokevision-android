package com.example.pokevision.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokevision.data.PokeVisionRetrofitInstance
import com.example.pokevision.repositories.ImageRepository

class ViewModelFactory : ViewModelProvider.Factory {
    val imageRepository = ImageRepository(PokeVisionRetrofitInstance.apiService)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageViewModel::class.java)) {
            return ImageViewModel(imageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}