package com.shkarov.mytasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.screens.*
import com.shkarov.mytasks.worker.TaskReminderReceiver

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    onFABVisibilityChanged: (Boolean) -> Unit,
    onGraphReady: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screens.WorkTasks.route) {
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(true)
                onGraphReady()
            }
            TasksScreen(navController, true, {})
        }

        composable(route = Screens.HomeTasks.route) {
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(true)
            }
            TasksScreen(navController, false, {})
        }

        composable(
            route = DetailTaskScreen.DetailedTaskScreen.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(false)
            }
            DetailTaskScreen(
                taskId = taskId.orEmpty(),
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(CreateTaskScreen.CreatedTaskScreen.routeFor(id))
                }
            )
        }

        composable(
            route = CreateTaskScreen.CreatedTaskScreen.route,
            arguments = listOf(
                navArgument(CreateTaskScreen.CreatedTaskScreen.ARG_TASK_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(false)
            }
            val taskId = backStackEntry.arguments?.getString(CreateTaskScreen.CreatedTaskScreen.ARG_TASK_ID)
            AddTaskScreen(
                taskId = taskId,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(ResponseScreen.SearchScreen.route) {
            val response = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<SearchResult>("searchResponse")

            SearchResultsScreen(
                response = response,
                navController = navController
            )
        }

        composable(TaskReminderReceiver.NAVIGATE_TO_ROUTE) {
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(false)
            }
            SearchResultsScreen(
                response = null,
                navController = navController
            )
        }
    }
}
