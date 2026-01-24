package com.shkarov.mytasks.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shkarov.mytasks.NavGraph
import com.shkarov.mytasks.domain.model.VoiceRequestType
import com.shkarov.mytasks.ui.buttons.FloatingButtonAddByText
import com.shkarov.mytasks.ui.buttons.FloatingButtonAddByVoice
import com.shkarov.mytasks.ui.buttons.FloatingButtonSearchByVoice
import com.shkarov.mytasks.ui.dialogs.VoiceDialog
import com.shkarov.mytasks.viewmodels.MainScreenViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var showVoiceDialog by remember { mutableStateOf(false) }
    var requestType: VoiceRequestType by remember { mutableStateOf(VoiceRequestType.UNKNOWN) }

    var showFAB by remember { mutableStateOf(true) }

    val viewModel: MainScreenViewModel = hiltViewModel()

    var currentBottomScreen by remember { mutableStateOf<Screens?>(null) }
    val currentDestination by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentDestination) {
        val route = currentDestination?.destination?.route
        if (route != null) {
            currentBottomScreen = listOf(Screens.WorkTasks, Screens.HomeTasks)
                .find { screen -> route.startsWith(screen.route) }
        } else {
            currentBottomScreen = null
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
        floatingActionButton = {
            if (showFAB) {
                Row(
                    //horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    FloatingButtonSearchByVoice(onShowDialog = {
                        requestType = VoiceRequestType.SEARCH
                        showVoiceDialog = true
                    })
                    FloatingButtonAddByText(navController)
                    FloatingButtonAddByVoice(onShowDialog = {
                        requestType = VoiceRequestType.ADD_TASK
                        showVoiceDialog = true
                    })
                }
            }
        },
    ) { paddingValue ->
        Box(modifier = Modifier.padding(paddingValue)) {
            NavGraph(
                navController = navController,
                onFABVisibilityChanged = { visible ->
                    showFAB = visible
                })
        }
    }

    VoiceDialog(
        showDialog = showVoiceDialog,
        requestType = requestType,
        onDismiss = { text ->
            showVoiceDialog = false
            if (text.isNotBlank()) {
                when (requestType) {
                    VoiceRequestType.ADD_TASK -> viewModel.saveTaskRequest(
                        request = text,
                        isWorkTask = currentBottomScreen == Screens.WorkTasks
                    )

                    VoiceRequestType.SEARCH -> viewModel.searchRequest(
                        request = text,
                        isWorkTask = currentBottomScreen == Screens.WorkTasks
                    )

                    VoiceRequestType.UNKNOWN -> Unit
                }
                viewModel.saveTaskRequest(text, currentBottomScreen == Screens.WorkTasks)
            }
        }
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        Screens.WorkTasks,
        Screens.HomeTasks,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: Screens,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    NavigationBarItem(
        label = { Text(text = stringResource(id = screen.title)) },
        icon = {
            Icon(
                painter = painterResource(screen.icon),
                contentDescription = "Navigation Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}
