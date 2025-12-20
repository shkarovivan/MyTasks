package com.shkarov.mytasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shkarov.mytasks.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    onFABVisibilityChanged: (Boolean) -> Unit) {
    NavHost(
        navController = navController,
        startDestination = Screens.WorkTasks.route
    ) {
        composable(
            route = Screens.WorkTasks.route
        ) {
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(true)
            }
            TasksScreen(navController, true,{})
        }

        composable(route = Screens.HomeTasks.route) {
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(true)
            }
            TasksScreen(navController,false,{})
        }

        composable(
            route = DetailTaskScreen.DetailedTaskScreen.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(false)
            }
            DetailTaskScreen(taskId = taskId!!) {
                navController.navigateUp()
            }
            onFABVisibilityChanged(false)
        }

        composable(
            route = CreateTaskScreen.CreatedTaskScreen.route
        ){
            LaunchedEffect(Unit) {
                onFABVisibilityChanged(false)
            }
            AddTaskScreen {
                navController.navigateUp()
            }
        }

    }
}