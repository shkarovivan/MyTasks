package com.shkarov.mytasks.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.shkarov.mytasks.R
import com.shkarov.mytasks.settings.NotificationTime
import com.shkarov.mytasks.settings.TimePickerDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.shkarov.mytasks.repository.AiProvider


@Composable
fun AppDrawerContent(
    notificationsEnabled: Boolean,
    darkThemeEnabled: Boolean,
    notificationTime: NotificationTime,
    onNotificationsChanged: (Boolean) -> Unit,
    llmConnectionDirectType: Boolean,
    omLlmTypeChanged: (Boolean) -> Unit,
    llmProvider: String,
    omLlmProviderChanged: (AiProvider) -> Unit,
    llmModel: String,
    omLlmModelChanged: (String) -> Unit,
    providers: List<AiProvider>,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationTimeChanged: (Int, Int) -> Unit,
) {

    var showTimePicker by remember { mutableStateOf(false) }

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

        AnimatedVisibility(
            visible = notificationsEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_notification_time)) },
                supportingContent = {
                    Text(
                        stringResource(
                            R.string.settings_notification_time_desc,
                            notificationTime.formatted()
                        )
                    )
                },
                trailingContent = {
                    TextButton(onClick = { showTimePicker = true }) {
                        Text(
                            text = notificationTime.formatted(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp) // небольшой отступ — вложенная настройка
                    .clickable { showTimePicker = true }
            )
        }


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

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_llm_connection)) },
            supportingContent = { Text(stringResource(R.string.settings_llm_connection_type)) },
            trailingContent = {
                Switch(
                    checked = llmConnectionDirectType,
                    onCheckedChange = omLlmTypeChanged,
                    enabled = false
                )
            }
        )

        AnimatedVisibility(
            visible = llmConnectionDirectType,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { omLlmProviderChanged(provider)}
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = provider.name == llmProvider,
                            onClick = { omLlmProviderChanged(provider)}
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = provider.name)
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = notificationTime.hour,
            initialMinute = notificationTime.minute,
            onConfirm = { hour, minute ->
                onNotificationTimeChanged(hour, minute)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}