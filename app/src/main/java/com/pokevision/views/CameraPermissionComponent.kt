package com.pokevision.views

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.pokevision.R
import com.pokevision.viewmodels.PermissionsViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionComponent(permissionsViewModel: PermissionsViewModel) {
    val permissions =
        listOf(
            Manifest.permission.CAMERA
        )

    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(Unit) {
        multiplePermissionsState.launchMultiplePermissionRequest()
    }

    // use all permissions for now, since we only have 1
    val cameraPermission = multiplePermissionsState.allPermissionsGranted
    permissionsViewModel.updateCameraPermissionGranted(cameraPermission)

    val goToSettings = !multiplePermissionsState.permissions[0].status.isGranted &&
            !multiplePermissionsState.permissions[0].status.shouldShowRationale

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!multiplePermissionsState.allPermissionsGranted) {
            Text(stringResource(R.string.camera_permission_explanation))
            Button(onClick = {
                if (goToSettings) permissionsViewModel.openAppSettings(context)
                else multiplePermissionsState.launchMultiplePermissionRequest()
            }) {
                Text(stringResource(
                    if (goToSettings) R.string.camera_permission_button_go_to_settings
                            else R.string.camera_permission_button))
            }
        }
    }
}