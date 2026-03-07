package com.shkarov.mytasks.permissions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

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
            Text("Разрешение на уведомления")
        },
        text = {
            Text(
                "Без разрешения на уведомления вы не будете " +
                        "получать ежедневные напоминания о задачах. " +
                        "Откройте настройки и включите уведомления."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Открыть настройки")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}