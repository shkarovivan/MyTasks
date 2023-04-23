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

sealed class DetailScreen(
    val route: String
) {
    object DetailedScreen : DetailScreen(
        route = "detail_screen/{id}"
    ){
        val id = DEFAULT_ID
    }

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach{ arg ->
                append("/$arg")
            }
        }
    }

    // build and setup route format (in navigation graph)
    fun withArgsFormat(vararg args: String) : String {
        return buildString {
            append(route)
            args.forEach{ arg ->
                append("/{$arg}")
            }
        }
    }
    companion object{
        const val DEFAULT_ID= "1"
    }

}


