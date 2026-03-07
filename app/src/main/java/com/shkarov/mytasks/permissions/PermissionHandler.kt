package com.shkarov.mytasks.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import timber.log.Timber

class PermissionHandler(private val activity: ComponentActivity) {

    var showRationale by mutableStateOf(false)
        private set

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    init {
        registerLauncher()
    }

    private fun registerLauncher() {
        requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Timber.d("POST_NOTIFICATIONS — разрешение получено")
                showRationale = false
            } else {
                Timber.e("POST_NOTIFICATIONS — разрешение отклонено")
                showRationale = true
            }
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                hasNotificationPermission(activity) -> {
                    Timber.d("POST_NOTIFICATIONS — уже есть")
                }

                activity.shouldShowRequestPermissionRationale(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    Timber.d("POST_NOTIFICATIONS — показываем объяснение")
                    showRationale = true
                }

                else -> {
                    Timber.d("POST_NOTIFICATIONS — запрашиваем")
                    requestPermissionLauncher.launch(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
    }

    fun dismissRationale() {
        showRationale = false
    }

    fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
            it.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(it)
        }
        showRationale = false
    }

    companion object {
        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }
}