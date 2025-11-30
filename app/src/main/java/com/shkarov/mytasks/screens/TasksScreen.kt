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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.shkarov.mytasks.R
import com.shkarov.mytasks.data.Status
import com.shkarov.mytasks.data.Task
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
    pages: Array<TaskPages> = TaskPages.values()
) {
    val pagerState = rememberPagerState()
    val viewModel: TaskScreenViewModel = viewModel()

    val load by viewModel.loadProgress.collectAsState()
    val error by viewModel.errorString.collectAsState()
    val dailyTasks by viewModel.dailyTasks.collectAsState()
    val mediumTasks by viewModel.mediumTasks.collectAsState()
    val largeTasks by viewModel.largeTasks.collectAsState()

    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pages[pagerState.currentPage])
    }

        Column(modifier.nestedScroll(rememberNestedScrollInteropConnection())) {
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
                        selectedContentColor = MaterialTheme.colors.onBackground,
                    )
                }
            }

            // Pages
            HorizontalPager(
                pageCount = pages.size,
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



@Composable
fun Toolbar(
    title: String
) {
    Surface {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            backgroundColor = BackGroundMyTasks
        ) {
            Text(
                color = MaterialTheme.colorScheme.onBackground,
                text = title,
                style = MaterialTheme.typography.h6,
                // As title in TopAppBar has extra inset on the left, need to do this: b/158829169
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
        }
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
        backgroundColor =
        when (task.status) {
            Status.STARTED -> {
                if (task.deadLineMs > System.currentTimeMillis()) LatedTaskColor
                else StartedTaskColor
            }
            Status.STOPPED -> StoppedTaskColor
            Status.WAITING -> WaitingTaskColor
            Status.PAUSED -> PausedTaskColor
        }

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
                status = Status.STARTED
            )
        ){}
    }
}