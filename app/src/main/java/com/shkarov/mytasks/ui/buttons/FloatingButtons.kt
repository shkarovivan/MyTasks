package com.shkarov.mytasks.ui.buttons

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavHostController
import com.shkarov.mytasks.R.dimen
import com.shkarov.mytasks.screens.CreateTaskScreen
import com.shkarov.mytasks.screens.ResponseScreen


@Composable
fun FloatingButtonAddByText(navController: NavHostController) {
    FloatingActionButton(
        modifier = Modifier.padding(
            end = dimensionResource(id = dimen.floating_button_padding),
            bottom = dimensionResource(id = dimen.padding_smallest)
        ),
        onClick = {
            navController.navigate(CreateTaskScreen.CreatedTaskScreen.routeFor(null)) {
                launchSingleTop = true
            }
        },
        containerColor = Color.Blue,
        contentColor = Color.White,
        shape = RoundedCornerShape(dimensionResource(id = dimen.floating_button_corner_radius)),
    ) {
        Icon(Icons.Filled.AddTask, contentDescription = "Add task by text")
    }
}

@Composable
fun FloatingButtonFiredTasks(navController: NavHostController) {
    FloatingActionButton(
        modifier = Modifier.padding(
            end = dimensionResource(id = dimen.floating_button_padding),
            bottom = dimensionResource(id = dimen.padding_small)
        ),
        onClick = {
            navController.navigate(ResponseScreen.SearchScreen.route) {
                launchSingleTop = true
            }
        },
        containerColor = Color.Blue,
        contentColor = Color.White,
        shape = RoundedCornerShape(dimensionResource(id = dimen.floating_button_corner_radius)),
    ) {
        Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Add task by text")
    }
}

@Composable
fun FloatingButtonAddByVoice(
    onShowDialog: () -> Unit) {
    FloatingActionButton(
        modifier = Modifier.padding(
            end = dimensionResource(id = dimen.floating_button_padding),
            bottom = dimensionResource(id = dimen.padding_small)
        ),
        onClick = {
            onShowDialog()
        },
        containerColor = Color.Blue,
        contentColor = Color.White,
        shape = RoundedCornerShape(dimensionResource(id = dimen.floating_button_corner_radius)),
    ) {
        Icon(Icons.Filled.Mic,  contentDescription = "Add task by voice")
    }
}

@Composable
fun FloatingButtonSearchByVoice(
    onShowDialog: () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = Modifier
            .padding(
                end = dimensionResource(id = dimen.floating_button_padding) * 2,
                bottom = dimensionResource(id = dimen.padding_small)
            )
            .width(dimensionResource(id = dimen.floating_search_button_width)),
        onClick = {
            onShowDialog()
        },
        containerColor = Color.Blue,
        contentColor = Color.White,
        shape = RoundedCornerShape(dimensionResource(id = dimen.floating_button_corner_radius)),
    ) {
        Icon(Icons.Filled.Search, contentDescription = "Add task by voice")
    }
}
