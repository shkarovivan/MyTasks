package com.shkarov.mytasks.screens

import androidx.annotation.StringRes
import com.shkarov.mytasks.domain.model.Status
import com.shkarov.mytasks.domain.model.Work
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shkarov.mytasks.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shkarov.mytasks.domain.model.DetailTaskUiState
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.Type
import com.shkarov.mytasks.ui.theme.PausedTaskColor
import com.shkarov.mytasks.ui.theme.StartedTaskColor
import com.shkarov.mytasks.ui.theme.StoppedTaskColor
import com.shkarov.mytasks.ui.theme.WaitingTaskColor
import com.shkarov.mytasks.viewmodels.DetailTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTaskScreen(
    taskId: String,
    onBackClick: () -> Unit
) {
    val viewModel: DetailTaskViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.detail_task_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is DetailTaskUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailTaskUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetryClick = { viewModel.loadTask(taskId, force = true) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is DetailTaskUiState.Success -> {
                DetailTaskContent(
                    task = state.task,
                    paddingValues = innerPadding
                )
            }
        }
    }
}

@Composable
private fun DetailTaskContent(
    task: Task,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (task.description.isNotBlank()) {
                    InfoBlock(
                        title = stringResource(R.string.task_description),
                        value = task.description
                    )
                }
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.task_information),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                InfoRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    },
                    label = stringResource(R.string.task_created),
                    value = task.created
                )

                InfoRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null
                        )
                    },
                    label = stringResource(R.string.task_deadline),
                    value = task.deadLine
                )

                InfoRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Task,
                            contentDescription = null
                        )
                    },
                    label = stringResource(R.string.task_status),
                    value = stringResource(task.status.toReadableRes())
                )

                InfoRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Task,
                            contentDescription = null
                        )
                    },
                    label = stringResource(R.string.task_type),
                    value = stringResource(task.type.toReadableRes())
                )

                InfoRow(
                    icon = {
                        Icon(
                            imageVector = if (task.work == Work.WORK) Icons.Default.Work else Icons.Default.Home,
                            contentDescription = null
                        )
                    },
                    label = stringResource(R.string.task_category),
                    value = stringResource(task.work.toReadableRes())
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetryClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun InfoBlock(
    icon: (@Composable () -> Unit)? = null,
    title: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.let { it() }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: (@Composable () -> Unit)? = null,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { it() }

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@StringRes
private fun Status.toReadableRes(): Int = when (this) {
    Status.STARTED -> R.string.status_started
    Status.WAITING -> R.string.status_waiting
    Status.PAUSED -> R.string.status_paused
    Status.STOPPED -> R.string.status_stopped
}

@StringRes
private fun Work.toReadableRes(): Int = when (this) {
    Work.WORK -> R.string.work_work
    Work.HOME -> R.string.work_home
}

@Composable
private fun String.toReadableRes(): Int = when (this) {
    Type.DAILY.value -> R.string.daily_tasks_description
    Type.MEDIUM.value -> R.string.medium_tasks_description
    Type.LARGE.value -> R.string.large_tasks_description
    else -> R.string.daily_tasks_description
}
