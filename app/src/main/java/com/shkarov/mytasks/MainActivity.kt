package com.shkarov.mytasks

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shkarov.mytasks.permissions.NotificationPermissionDialog
import com.shkarov.mytasks.permissions.PermissionHandler
import com.shkarov.mytasks.screens.MainScreen
import com.shkarov.mytasks.ui.theme.MyTasksTheme
import com.shkarov.mytasks.viewmodels.SettingsViewModel
import com.shkarov.mytasks.worker.TaskReminderReceiver
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler

    private val _navigateTo = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _navigateTo.value = intent?.getStringExtra(TaskReminderReceiver.NAVIGATE_TO)

        permissionHandler = PermissionHandler(this)
        permissionHandler.requestNotificationPermission()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val darkThemeEnabled by settingsViewModel.darkThemeEnabled.collectAsState()
            val navigateTo by _navigateTo
            MyTasksTheme(
                darkTheme = darkThemeEnabled
            ) {

                MainScreen(navigateTo = navigateTo)

                if (permissionHandler.showRationale) {
                    NotificationPermissionDialog(
                        onConfirm = { permissionHandler.openAppSettings() },
                        onDismiss = { permissionHandler.dismissRationale() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val route = intent.getStringExtra(TaskReminderReceiver.NAVIGATE_TO_ROUTE)
        Timber.d("MainActivity onNewIntent: navigateTo = $route")
        _navigateTo.value = route
    }

}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyTasksTheme {
        Greeting("Android")
    }
}