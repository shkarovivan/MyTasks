package com.shkarov.mytasks.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.Status
import com.shkarov.mytasks.domain.model.Task
import com.shkarov.mytasks.domain.model.Work
import com.shkarov.mytasks.ui.theme.*
import com.shkarov.mytasks.viewmodels.TaskScreenViewModel
import kotlinx.coroutines.launch

enum class TaskPages(
    @StringRes val titleResId: Int,
    @DrawableRes val drawableResId: Int
) {
    DAILY_TASKS(R.string.daily_tasks, R.drawable.ic_baseline_work_24),
    MEDIUM_TASKS(R.string.medium_tasks, R.drawable.ic_baseline_work_24),
    LARGE_TASKS(R.string.large_tasks, R.drawable.ic_baseline_work_24)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(
    navController: NavHostController,
    isWorkTasks: Boolean,
    onPageChange: (TaskPages) -> Unit,
    modifier: Modifier = Modifier,
    pages: Array<TaskPages> = TaskPages.entries.toTypedArray()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val viewModel: TaskScreenViewModel = hiltViewModel()

    val load by viewModel.loadProgress.collectAsState()
    val error by viewModel.errorString.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val mediumTasks by viewModel.mediumTasks.collectAsState()
    val largeTasks by viewModel.largeTasks.collectAsState()

    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pages[pagerState.currentPage])
    }

    Column(modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())) {
            val coroutineScope = rememberCoroutineScope()

            val title = if (isWorkTasks) stringResource(id = R.string.work_tasks_title)
            else stringResource(id = R.string.home_tasks_title)
            Toolbar(title = title)

            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage
            ) {
                pages.forEachIndexed { index, page ->
                    val title = stringResource(id = page.titleResId)
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = title) },
                        icon = {
                            Icon(
                                painter = painterResource(id = page.drawableResId),
                                contentDescription = title
                            )
                        },
                        unselectedContentColor = Color.DarkGray,
                        selectedContentColor = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            // Pages
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top
            ) { index ->
                when (pages[index]) {
                    TaskPages.DAILY_TASKS -> {
                        TasksList(navController = navController, tasks = dailyTasks)
                    }
                    TaskPages.MEDIUM_TASKS -> {
                        TasksList(navController = navController, tasks = mediumTasks)
                    }
                    TaskPages.LARGE_TASKS -> {
                        TasksList(navController = navController, tasks = largeTasks)
                    }
                }
            }
        }
    }



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    title: String
) {
    Surface {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = { 
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
    }
}

@Composable
fun TasksList(
    navController: NavHostController,
    tasks: List<Task>
) {
    LazyColumn(
        modifier = Modifier
            .padding(
                start = dimensionResource(id = R.dimen.padding_small).value.dp,
                end = dimensionResource(id = R.dimen.padding_small).value.dp,
                top = dimensionResource(id =R.dimen.padding_small).value.dp,
                bottom = dimensionResource(id = R.dimen.bottom_height).value.dp
            )
    ) {
        items(tasks) { itemTask ->
            TaskView(
                task = itemTask
            ) {
                navController.navigate("detail_task_screen/${itemTask.id}") {
                    popUpTo(Screens.WorkTasks.route)
                }
            }
            Divider(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp),
                color = Color.DarkGray,
                thickness = 1.dp
            )
        }
    }
}

@Composable
fun TaskView(
    task: Task,
    navigateToDetailTask: ()-> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = dimensionResource(id = R.dimen.padding_smallest),
                horizontal = dimensionResource(id = R.dimen.padding_small)
            )
            .clip(
                RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius))
            )
            .clickable { navigateToDetailTask() },
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                Status.STARTED -> {
                    if (task.deadLineMs > System.currentTimeMillis()) LatedTaskColor
                    else StartedTaskColor
                }
                Status.STOPPED -> StoppedTaskColor
                Status.WAITING -> WaitingTaskColor
                Status.PAUSED -> PausedTaskColor
            }
        )
    )
    {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically

            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = dimensionResource(id = R.dimen.padding_small)),
                    text = task.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensionResource(id = R.dimen.main_text_size).value.sp,
                    textAlign = TextAlign.Start
                )

                Image(
                    modifier = Modifier
                        .padding(horizontal = dimensionResource(id = R.dimen.padding_small)),
                    painter = painterResource(
                        id =
                        when (task.status) {
                            Status.STARTED -> R.drawable.ic_baseline_play_arrow_24
                            Status.PAUSED -> R.drawable.ic_baseline_pause_24
                            Status.STOPPED -> R.drawable.ic_baseline_stop_24
                            Status.WAITING -> R.drawable.ic_baseline_timer_24
                        }
                    ),
                    contentDescription = null,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = task.deadLine,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensionResource(id = R.dimen.main_text_size).value.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}


@Preview
@Composable
private fun TasksScreenPreview() {
    MyTasksTheme {
        //TasksScreen(navController,true, {})
    }
}

@Preview
@Composable
private fun TaskViewPreview() {
    MyTasksTheme {
        TaskView(
            task = Task(
                id = "1",
                created = "21.04.2023",
                title = "Проверить работу",
                description = "Проверить работу TaskView в превью приложения и попарвить при необходимости",
                type = "daily",
                deadLine = "21.04.2023",
                deadLineMs = 1,
                status = Status.STARTED,
                work = Work.WORK
            )
        ){}
    }
}