package com.shkarov.mytasks.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.Status
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.Type
import com.shkarov.mytasks.domain.model.Work
import com.shkarov.mytasks.ui.theme.MyTasksTheme
import com.shkarov.mytasks.utils.toEpochMillis
import com.shkarov.mytasks.viewmodels.AddTaskViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    taskId: String? = null,
    isWorkTask: Boolean = true,
    onBackClick: () -> Unit
) {
    val isEditing = taskId != null
    val viewModel: AddTaskViewModel = hiltViewModel()
    val taskToEdit by viewModel.taskToEdit.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        if (!taskId.isNullOrEmpty()) viewModel.loadTaskForEdit(taskId)
    }

    var titleValue by remember { mutableStateOf("") }
    var descriptionValue by remember { mutableStateOf("") }

    val selectedTypeValue = remember { mutableStateOf("") }
    val selectedDeadlineValue = remember { mutableStateOf("") }

    val dailyTasksLabel = stringResource(R.string.daily_tasks)
    val mediumTasksLabel = stringResource(R.string.medium_tasks)
    val largeTasksLabel = stringResource(R.string.large_tasks)

    var chosenDateText by rememberSaveable { mutableStateOf("") }
    var chosenDateMs by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val chooseDateItem = stringResource(R.string.deadline_choose_date)

    val typeItems = listOf(dailyTasksLabel, mediumTasksLabel, largeTasksLabel)

    val deadlineItems = listOf(
        stringResource(R.string.deadline_1),
        stringResource(R.string.deadline_2),
        stringResource(R.string.deadline_3),
        stringResource(R.string.deadline_4),
        stringResource(R.string.deadline_5),
        stringResource(R.string.deadline_6),
        stringResource(R.string.deadline_7),
        chooseDateItem
    )

    // Pre-fill the form from the task being edited (runs once it finishes loading).
    LaunchedEffect(taskToEdit) {
        val task = taskToEdit ?: return@LaunchedEffect
        titleValue = task.title
        descriptionValue = task.description
        selectedTypeValue.value = when (task.type) {
            Type.MEDIUM.value -> mediumTasksLabel
            Type.LARGE.value -> largeTasksLabel
            else -> dailyTasksLabel
        }
        val isQuickDeadline = task.deadLine in deadlineItems && task.deadLine != chooseDateItem
        when {
            // A previously picked concrete date (or any non-quick value with a timestamp).
            !isQuickDeadline && task.deadLineMs > 0L -> {
                selectedDeadlineValue.value = chooseDateItem
                chosenDateMs = task.deadLineMs
                chosenDateText = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    .format(Date(task.deadLineMs))
            }
            // One of the "1..7 days" quick options.
            isQuickDeadline -> {
                selectedDeadlineValue.value = task.deadLine
            }
            else -> {
                selectedDeadlineValue.value = chooseDateItem
                chosenDateText = task.deadLine
            }
        }
    }

    val onSave: () -> Unit = {
        if (titleValue.isBlank()) {
            Timber.w("Заголовок задачи не может быть пустым")
        } else {
            val zone = ZoneId.systemDefault()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            // Quick-pick options ("1..7 days") are stored as N days from now;
            // "Choose date" stores the picked date. Both produce a real deadLineMs.
            val quickDays = deadlineItems.indexOf(selectedDeadlineValue.value) + 1
            val isQuickPick = quickDays in 1..7

            val deadlineTextToSave: String
            val deadlineMsToSave: Long
            if (selectedDeadlineValue.value == chooseDateItem && chosenDateMs != null) {
                val localDate = Instant.ofEpochMilli(chosenDateMs!!)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                deadlineTextToSave = localDate.atStartOfDay(zone).format(formatter)
                deadlineMsToSave = deadlineTextToSave.toEpochMillis()
            } else if (isQuickPick) {
                val ms = System.currentTimeMillis() + quickDays * 24L * 60 * 60 * 1000
                deadlineMsToSave = ms
                deadlineTextToSave = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    .format(Date(ms))
            } else {
                deadlineTextToSave = selectedDeadlineValue.value
                deadlineMsToSave = deadlineTextToSave.toEpochMillis()
            }

            val existing = taskToEdit
            val task = Task(
                id = existing?.id ?: System.currentTimeMillis().toString(),
                created = existing?.created ?: SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
                ).format(Date()),
                title = titleValue,
                description = descriptionValue.takeIf { it.isNotEmpty() } ?: titleValue,
                type = when (selectedTypeValue.value) {
                    dailyTasksLabel -> Type.DAILY.value
                    mediumTasksLabel -> Type.MEDIUM.value
                    largeTasksLabel -> Type.LARGE.value
                    else -> existing?.type ?: Type.DAILY.value // дефолт
                },
                deadLine = deadlineTextToSave,
                deadLineMs = deadlineMsToSave,
                status = existing?.status ?: Status.STARTED,
                work = existing?.work ?: if (isWorkTask) Work.WORK else Work.HOME
            )

            viewModel.addTask(task)
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (isEditing) R.string.edit_task_title else R.string.new_task_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save_text),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card: title + description (editable)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EditableField(
                        label = stringResource(R.string.task_title),
                        value = titleValue,
                        onValueChange = { titleValue = it },
                        minLines = 1
                    )
                    EditableField(
                        label = stringResource(R.string.task_description),
                        value = descriptionValue,
                        onValueChange = { descriptionValue = it },
                        minLines = 4
                    )
                }
            }

            // Card: information (type + deadline choices)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.task_information),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = stringResource(R.string.task_type_text),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    typeItems.forEach { item ->
                        SelectableOptionRow(
                            label = item,
                            selected = selectedTypeValue.value == item,
                            onClick = { selectedTypeValue.value = item }
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.task_deadline_text),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    deadlineItems.forEach { item ->
                        SelectableOptionRow(
                            label = item.trim(),
                            selected = selectedDeadlineValue.value == item,
                            onClick = {
                                if (item == chooseDateItem) {
                                    selectedDeadlineValue.value = item
                                    showDatePicker = true
                                } else {
                                    selectedDeadlineValue.value = item
                                }
                            }
                        )
                    }

                    if (chosenDateText.isNotBlank()) {
                        InfoValueRow(
                            label = stringResource(R.string.choosen_data_text),
                            value = chosenDateText
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val ms = datePickerState.selectedDateMillis
                                if (ms != null) {
                                    chosenDateMs = ms
                                    chosenDateText = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                        .format(Date(ms))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text(text = stringResource(id = android.R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(text = stringResource(id = android.R.string.cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        // Same surfaceVariant block as the details screen, but editable:
        // the pencil icon + text cursor make clear this is an input, not read-only.
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = if (minLines > 1) Alignment.Top else Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(18.dp)
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    minLines = minLines,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectableOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (selected) 1f else 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = androidx.compose.ui.semantics.Role.RadioButton
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@Composable
private fun InfoValueRow(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Preview
@Composable
fun previewAddTaskScreen() {
    MyTasksTheme {
        AddTaskScreen {

        }
    }
}
