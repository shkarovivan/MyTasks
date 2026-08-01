package com.shkarov.mytasks.screens

import com.shkarov.mytasks.R

sealed class Screens(
    val route: String,
    val title: Int,
    val icon: Int
) {
    object WorkTasks : Screens(
        route = "character",
        title = R.string.work_tasks,
        icon = R.drawable.ic_baseline_work_24
    )

    object HomeTasks : Screens(
        route = "location",
        title = R.string.home_tasks,
        icon = R.drawable.ic_baseline_home_24
    )
}

sealed class DetailTaskScreen(
    val route: String
) {
    object DetailedTaskScreen : DetailTaskScreen(
        route = "detail_task_screen/{taskId}"
    )
}

sealed class CreateTaskScreen(
    val route: String
) {
    object CreatedTaskScreen : CreateTaskScreen(
        route = "create_task_screen?taskId={taskId}"
    ) {
        const val ARG_TASK_ID = "taskId"

        /** Base route creates a new task; passing a [taskId] opens the editor in edit mode. */
        fun routeFor(taskId: String?): String =
            if (taskId.isNullOrEmpty()) "create_task_screen" else "create_task_screen?taskId=$taskId"
    }

}

sealed class ResponseScreen(
    val route: String
) {
    object SearchScreen : ResponseScreen(
        route = "search_screen"
    )
}
