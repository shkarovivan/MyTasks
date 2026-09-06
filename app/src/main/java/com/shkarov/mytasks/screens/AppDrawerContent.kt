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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.sp
import com.shkarov.mytasks.domain.provider.ProviderKey
import com.shkarov.mytasks.repository.AiProvider
import timber.log.Timber

private const val BACKEND_FIELD_URL = "url"
private const val BACKEND_FIELD_KEY = "key"


@Composable
fun AppDrawerContent(
    notificationsEnabled: Boolean,
    darkThemeEnabled: Boolean,
    notificationTime: NotificationTime,
    onNotificationsChanged: (Boolean) -> Unit,
    llmConnectionDirectType: Boolean,
    omLlmTypeChanged: (Boolean) -> Unit,
    llmProvider: String,
    onLlmProviderChanged: (AiProvider) -> Unit,
    llmModel: String,
    onLlmModelChanged: (String) -> Unit,
    providers: List<AiProvider>,
    onProviderKeyChanged: (ProviderKey) -> Unit,
    backendUrl: String,
    backendApiKey: String,
    onBackendUrlChanged: (String) -> Unit,
    onBackendApiKeyChanged: (String) -> Unit,
    accountEmail: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onNotificationTimeChanged: (Int, Int) -> Unit,
) {

    var showTimePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogInput by remember { mutableStateOf("") }
    var selectedProviderName by remember { mutableStateOf<String?>(null) }
    // null = provider token dialog, otherwise the backend field being edited
    var backendDialogField by remember { mutableStateOf<String?>(null) }

    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        )

        HorizontalDivider()

        ListItem(
            headlineContent = { Text(stringResource(R.string.settings_account)) },
            supportingContent = {
                Text(
                    accountEmail
                        ?: stringResource(R.string.settings_account_signed_out)
                )
            },
            trailingContent = {
                TextButton(
                    onClick = if (accountEmail != null) onSignOut else onSignIn
                ) {
                    Text(
                        text = stringResource(
                            if (accountEmail != null) {
                                R.string.settings_account_sign_out
                            } else {
                                R.string.settings_account_sign_in
                            }
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
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
                    .padding(start = dimensionResource(R.dimen.padding_main))
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
            supportingContent = {
                Text(
                    stringResource(
                        if (llmConnectionDirectType) {
                            R.string.settings_llm_connection_type
                        } else {
                            R.string.settings_llm_connection_backend
                        }
                    )
                )
            },
            trailingContent = {
                Switch(
                    checked = llmConnectionDirectType,
                    onCheckedChange = omLlmTypeChanged
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
                            .clickable { onLlmProviderChanged(provider) }
                            .padding(
                                horizontal = dimensionResource(R.dimen.padding_main),
                                vertical = dimensionResource(R.dimen.padding_small)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = if (llmProvider.isNotEmpty()) {
                                provider.name == llmProvider
                            } else {
                                provider.name == providers.first().name
                            },
                            onClick = { onLlmProviderChanged(provider) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = provider.name)
                        IconButton(
                            enabled = provider.name == llmProvider,
                            onClick = {
                                selectedProviderName = provider.name
                                dialogInput = ""
                                showDialog = true
                            }
                        ) {
                            Icon(
                                modifier = Modifier
                                    .padding(
                                        horizontal = dimensionResource(R.dimen.padding_small),
                                    ),
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.settings_llm_model_token)
                            )
                        }
                    }
                }

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_llm_model)) },
                    supportingContent = { Text(stringResource(R.string.settings_llm_model_choice)) },
                )

                val currentProvider =
                    providers.firstOrNull { it.name == llmProvider } ?: providers.first()

                Timber.d("currentProvider - ${currentProvider.name}")

                currentProvider.models.forEach { model ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLlmModelChanged(model.path) }
                            .padding(
                                horizontal = dimensionResource(R.dimen.padding_main),
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = if (llmModel.isNotEmpty()
                                && currentProvider.models.map { it.path }.contains(llmModel)
                            ) {
                                model.path == llmModel
                            } else {
                                model.path == providers.first().models.first().path
                            },
                            onClick = { onLlmModelChanged(model.path) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = model.name)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !llmConnectionDirectType,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_backend_url)) },
                    supportingContent = { Text(backendUrl) },
                    modifier = Modifier
                        .padding(start = dimensionResource(R.dimen.padding_main))
                        .clickable {
                            backendDialogField = BACKEND_FIELD_URL
                            selectedProviderName = null
                            dialogInput = ""
                            showDialog = true
                        }
                )

                HorizontalDivider()

                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_backend_key)) },
                    supportingContent = {
                        Text(
                            stringResource(R.string.settings_backend_key_desc)
                                .takeIf { backendApiKey.isBlank() } ?: "•••"
                        )
                    },
                    modifier = Modifier
                        .padding(start = dimensionResource(R.dimen.padding_main))
                        .clickable {
                            backendDialogField = BACKEND_FIELD_KEY
                            selectedProviderName = null
                            dialogInput = ""
                            showDialog = true
                        }
                )
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

    if (showDialog) {
        val dialogTitle = when (backendDialogField) {
            BACKEND_FIELD_URL -> stringResource(R.string.settings_backend_url_dialog_title)
            BACKEND_FIELD_KEY -> stringResource(R.string.settings_backend_key_dialog_title)
            else -> selectedProviderName + stringResource(R.string.settings_llm_model_token_description)
        }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = dialogTitle,
                    fontSize = dimensionResource(R.dimen.main_text_size).value.sp,
                )
            },
            text = {
                TextField(
                    value = dialogInput,
                    onValueChange = { dialogInput = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    when (backendDialogField) {
                        BACKEND_FIELD_URL -> onBackendUrlChanged(dialogInput)
                        BACKEND_FIELD_KEY -> onBackendApiKeyChanged(dialogInput)
                        else -> selectedProviderName?.let {
                            onProviderKeyChanged(ProviderKey(
                                providerName = it,
                                key = dialogInput
                            ))
                        }
                    }
                    showDialog = false
                }) {
                    Text(stringResource(R.string.save_text))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}