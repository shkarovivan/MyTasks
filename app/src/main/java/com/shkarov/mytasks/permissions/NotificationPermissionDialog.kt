package com.shkarov.mytasks.permissions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shkarov.mytasks.R

@Composable
fun NotificationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null
            )
        },
        title = {
            Text(stringResource(id = R.string.permission_notify_text))
        },
        text = {
            Text(stringResource(id = R.string.permission_notify_hint_text))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(id = R.string.open_settings_text))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel_text))
            }
        }
    )
}