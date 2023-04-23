package com.shkarov.mytasks

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shkarov.mytasks.screens.Screens
import com.shkarov.mytasks.screens.TasksScreen

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

    }
}