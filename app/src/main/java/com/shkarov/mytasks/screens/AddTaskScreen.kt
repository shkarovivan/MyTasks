package com.shkarov.mytasks.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.Status
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.Type
import com.shkarov.mytasks.domain.model.Work
import com.shkarov.mytasks.ui.theme.MyTasksTheme
import com.shkarov.mytasks.viewmodels.AddTaskViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.saveable.rememberSaveable
import com.shkarov.mytasks.utils.toEpochMillis
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun AddTaskScreen(
    isWorkTask: Boolean = true,
    onBackClick: () -> Unit
) {
    var titleValue by remember { mutableStateOf("") }
    var chosenData by remember { mutableStateOf<String?>("") }
    val onChangeData: (String) -> Unit = { chosenData = it }
    var descriptionValue by remember { mutableStateOf("") }

    val viewModel: AddTaskViewModel = hiltViewModel()
    val selectedTypeValue = remember { mutableStateOf("") }
    val isSelectedTypeItem: (String) -> Boolean = { selectedTypeValue.value == it }
    val onChangeTypeState: (String) -> Unit = { selectedTypeValue.value = it }

    val selectedDeadlineValue = remember { mutableStateOf("") }
    val isSelectedDeadlineItem: (String) -> Boolean = { selectedDeadlineValue.value == it }
    val onChangeDeadlineState: (String) -> Unit = { selectedDeadlineValue.value = it }

    val dailyTasksLabel = stringResource(id = R.string.daily_tasks)
    val mediumTasksLabel = stringResource(id = R.string.medium_tasks)
    val largeTasksLabel = stringResource(id = R.string.large_tasks)

    var chosenDateText by rememberSaveable { mutableStateOf("") }
    var chosenDateMs by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val chooseDateItem = stringResource(id = R.string.deadline_choose_date)

    val typeItems = listOf(
        stringResource(id = R.string.daily_tasks),
        stringResource(id = R.string.medium_tasks),
        stringResource(id = R.string.large_tasks)
    )

    val deadlineItems = mutableListOf(
        stringResource(id = R.string.deadline_1),
        stringResource(id = R.string.deadline_2),
        stringResource(id = R.string.deadline_3),
        stringResource(id = R.string.deadline_4),
        stringResource(id = R.string.deadline_5),
        stringResource(id = R.string.deadline_6),
        stringResource(id = R.string.deadline_7),
        stringResource(id = R.string.deadline_choose_date)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = dimensionResource(id = R.dimen.bottom_height))
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(all = dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(id = R.string.task_title),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_main),
                        top = dimensionResource(id = R.dimen.padding_main)
                    )
            )

            TextInput(
                value = titleValue,
                onValueChange = {
                    titleValue = it
                },
                minLines = 2
            )
            GrayDivider()
            Text(
                text = stringResource(id = R.string.task_description_text),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_main),
                        top = dimensionResource(id = R.dimen.padding_main)
                    )
            )

            TextInput(
                value = descriptionValue,
                onValueChange = {
                    descriptionValue = it
                },
                minLines = 5
            )
            GrayDivider()

            Text(
                text = stringResource(id = R.string.task_type_text),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_main),
                        top = dimensionResource(id = R.dimen.padding_main),
                        bottom = dimensionResource(id = R.dimen.padding_small),
                    )
            )

            typeItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(
                            selected = isSelectedTypeItem(item),
                            onClick = {
                                onChangeData("sdvsdgv")
                                onChangeTypeState(item)
                            },
                            role = Role.RadioButton
                        )
                        .padding(
                            vertical = dimensionResource(id = R.dimen.padding_smallest),
                            horizontal = dimensionResource(id = R.dimen.padding_main)
                        )
                        .fillMaxWidth()
                ) {
                    RadioButton(
                        selected = isSelectedTypeItem(item),
                        onClick = null
                    )
                    Text(
                        text = item,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(start = dimensionResource(id = R.dimen.padding_main))
                    )
                }
            }

            GrayDivider()

            Text(
                text = stringResource(id = R.string.task_deadline_text),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_main),
                        top = dimensionResource(id = R.dimen.padding_main),
                        bottom = dimensionResource(id = R.dimen.padding_small),
                    )
            )

            deadlineItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .selectable(
                            selected = isSelectedDeadlineItem(item),
                            onClick = {
                                if (item == chooseDateItem) {
                                    onChangeDeadlineState(item)     // оставляем выбранным радио "выбрать дату"
                                    showDatePicker = true
                                } else {
                                    onChangeDeadlineState(item)
                                }
                            },
                            role = Role.RadioButton
                        )
                        .padding(
                            vertical = dimensionResource(id = R.dimen.padding_smallest),
                            horizontal = dimensionResource(id = R.dimen.padding_main)
                        )
                        .fillMaxWidth()
                ) {
                    RadioButton(
                        selected = isSelectedDeadlineItem(item),
                        onClick = null
                    )
                    Text(
                        text = item,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(start = dimensionResource(id = R.dimen.padding_main))
                    )
                }
            }

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

            if (chosenDateText.isNotBlank()) {
                Row {
                    Text(
                        text = stringResource(id = R.string.choosen_data_text),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.padding_main),
                            top = dimensionResource(id = R.dimen.padding_main)
                        )
                    )

                    Text(
                        text = chosenDateText,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            start = dimensionResource(id = R.dimen.padding_main),
                            top = dimensionResource(id = R.dimen.padding_main)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_main)))
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .padding(
                            vertical = dimensionResource(id = R.dimen.padding_smallest),
                            horizontal = dimensionResource(id = R.dimen.padding_main)
                        )
                        .fillMaxWidth(),
                    onClick = {
                        // 1. Проверяем, что поля заполнены
                        if (titleValue.isBlank()) {
                            Timber.w("Заголовок задачи не может быть пустым")
                            return@Button
                        }
                        val zone = ZoneId.systemDefault()
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                        val deadlineTextToSave =
                            if (selectedDeadlineValue.value == chooseDateItem && chosenDateMs != null) {

                                val localDate = Instant.ofEpochMilli(chosenDateMs!!)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()

                                localDate
                                    .atStartOfDay(zone)
                                    .format(formatter)

                            } else {
                                selectedDeadlineValue.value
                            }

                        val task = Task(
                            id = System.currentTimeMillis().toString(),
                            created = SimpleDateFormat(
                                "dd.MM.yyyy",
                                Locale.getDefault()
                            ).format(Date()),
                            title = titleValue,
                            description = descriptionValue.takeIf { !descriptionValue.isEmpty() } ?: titleValue,
                            type = when (selectedTypeValue.value) {
                                dailyTasksLabel -> Type.DAILY.value
                                mediumTasksLabel -> Type.MEDIUM.value
                                largeTasksLabel -> Type.LARGE.value
                                else -> "daily" // дефолт
                            },
                            deadLine = deadlineTextToSave,
                            deadLineMs = deadlineTextToSave.toEpochMillis(),
                            status = Status.STARTED,
                            work = if (isWorkTask) Work.WORK else Work.HOME
                        )

                        viewModel.addTask(task)
                        onBackClick()
                    })
                {
                    Text(
                        text = stringResource(id = R.string.save_text),
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

        }

    }
}


@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int
) {
    BasicTextField(
        modifier = Modifier
            .padding(
                vertical = dimensionResource(id = R.dimen.padding_main)
            )
            .fillMaxWidth()
            .testTag("searchTextFieldTag"),

        singleLine = false,
        minLines = minLines,
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        textStyle = TextStyle(
            fontSize = dimensionResource(id = R.dimen.main_text_size).value.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        ),
        enabled = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_main),
                        end = dimensionResource(id = R.dimen.padding_main)
                    )
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(
                            size = dimensionResource(id = R.dimen.corner_radius)
                        )
                    )
                    .border(
                        width = dimensionResource(id = R.dimen.border_width),
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.corner_radius))
                    )
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_main),
                        vertical = dimensionResource(id = R.dimen.padding_small)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Favorite icon",
                    // tint = Color.DarkGray
                )
                Box(
                    modifier = Modifier
                        .padding(start = dimensionResource(id = R.dimen.padding_small))
                ) {
                    innerTextField()
                }
            }
        }
    )
}

@Composable
fun GrayDivider() {
    Divider(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp),
        color = Color.DarkGray,
        thickness = 2.dp
    )
}

@Preview
@Composable
fun previewAddTaskScreen() {
    MyTasksTheme {
        AddTaskScreen {

        }
    }
}