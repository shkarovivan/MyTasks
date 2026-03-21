package com.shkarov.mytasks.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.shkarov.mytasks.R

@Composable
fun AppDrawerContent(
    notificationsEnabled: Boolean,
    darkThemeEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit
) {
    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_notifications)) },
            supportingContent = { Text(stringResource(R.string.settings_notifications_desc)) },
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsChanged
                )
            }
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_dark_theme)) },
            supportingContent = { Text(stringResource(R.string.settings_dark_theme_desc)) },
            trailingContent = {
                Switch(
                    checked = darkThemeEnabled,
                    onCheckedChange = onDarkThemeChanged
                )
            }
        )
    }
}