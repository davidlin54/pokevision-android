package com.pokevision.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PermissionsViewModel : ViewModel() {
    private val _cameraPermissionGranted = MutableStateFlow(false)
    val cameraPermissionGranted : StateFlow<Boolean> = _cameraPermissionGranted

    fun updateCameraPermissionGranted(granted: Boolean) {
        _cameraPermissionGranted.value = granted
    }

}