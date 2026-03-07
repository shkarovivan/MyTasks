package com.shkarov.mytasks

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shkarov.mytasks.permissions.NotificationPermissionDialog
import com.shkarov.mytasks.permissions.PermissionHandler
import com.shkarov.mytasks.screens.MainScreen
import com.shkarov.mytasks.ui.theme.MyTasksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var permissionHandler: PermissionHandler

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionHandler = PermissionHandler(this)
        permissionHandler.requestNotificationPermission()

        setContent {
            MyTasksTheme {
                MainScreen()
                if (permissionHandler.showRationale) {
                    NotificationPermissionDialog(
                        onConfirm = { permissionHandler.openAppSettings() },
                        onDismiss = { permissionHandler.dismissRationale() }
                    )
                }
            }
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