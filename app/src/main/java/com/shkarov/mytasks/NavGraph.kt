package com.shkarov.mytasks

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shkarov.mytasks.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.WorkTasks.route
    ) {
        composable(
            route = Screens.WorkTasks.route
        ) {
            TasksScreen(navController, true,{})
        }

        composable(route = Screens.HomeTasks.route) {
            TasksScreen(navController,false,{})
        }

        composable(
            route = DetailTaskScreen.DetailedTaskScreen.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            DetailTaskScreen(taskId = taskId!!) {
                navController.navigateUp()
            }
        }

        composable(
            route = CreateTaskScreen.CreatedTaskScreen.route
        ){
            AddTaskScreen {
                navController.navigateUp()
            }
        }

    }
}