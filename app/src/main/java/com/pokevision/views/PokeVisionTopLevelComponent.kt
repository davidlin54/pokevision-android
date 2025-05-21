package com.pokevision.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.pokevision.viewmodels.PermissionsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PokeVisionTopLevelComponent() {
    val permissionsViewModel : PermissionsViewModel = viewModel()

    val isCameraPermissionGranted = permissionsViewModel
        .cameraPermissionGranted.collectAsState()

    if (isCameraPermissionGranted.value) {
        CameraPreviewComponent()
    } else {
        CameraPermissionComponent(permissionsViewModel)
    }
}