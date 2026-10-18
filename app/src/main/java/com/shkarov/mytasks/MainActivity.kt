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
import androidx.lifecycle.lifecycleScope
import com.shkarov.mytasks.permissions.NotificationPermissionDialog
import com.shkarov.mytasks.permissions.PermissionHandler
import com.shkarov.mytasks.repository.AuthRepository
import com.shkarov.mytasks.screens.MainScreen
import com.shkarov.mytasks.sync.SyncManager
import com.shkarov.mytasks.ui.theme.MyTasksTheme
import com.shkarov.mytasks.viewmodels.SettingsViewModel
import com.shkarov.mytasks.worker.TaskReminderReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var syncManager: SyncManager

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

    // Activity context is required for a reliable silent token fetch; warm the
    // cache here and run the startup sync round right after.
    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            authRepository.prefetchToken(this@MainActivity)
            syncManager.syncNow("start")
        }
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