package com.shkarov.mytasks.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shkarov.mytasks.NavGraph
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.VoiceRequestType
import com.shkarov.mytasks.ui.buttons.FloatingButtonAddByText
import com.shkarov.mytasks.ui.buttons.FloatingButtonAddByVoice
import com.shkarov.mytasks.ui.buttons.FloatingButtonFiredTasks
import com.shkarov.mytasks.ui.buttons.FloatingButtonSearchByVoice
import com.shkarov.mytasks.ui.dialogs.VoiceDialog
import com.shkarov.mytasks.ui.theme.LoaderColor
import com.shkarov.mytasks.viewmodels.MainScreenViewModel
import com.shkarov.mytasks.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navigateTo: String? = null
) {
    val navController = rememberNavController()
    var showVoiceDialog by remember { mutableStateOf(false) }
    var requestType: VoiceRequestType by remember { mutableStateOf(VoiceRequestType.UNKNOWN) }

    var showFAB by remember { mutableStateOf(true) }

    val mainScreenViewModel: MainScreenViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val darkThemeEnabled by settingsViewModel.darkThemeEnabled.collectAsStateWithLifecycle()

    val llmDirectConnectionType by settingsViewModel.llmConnectionDirectType.collectAsStateWithLifecycle()
    val llmProvider by settingsViewModel.llmProvider.collectAsStateWithLifecycle()
    val llmModel by settingsViewModel.llmModel.collectAsStateWithLifecycle()
    val backendUrl by settingsViewModel.backendUrlFlow.collectAsStateWithLifecycle()
    val backendApiKey by settingsViewModel.backendApiKeyFlow.collectAsStateWithLifecycle()

    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val notificationTime by settingsViewModel.notificationTime.collectAsStateWithLifecycle()
    val account by settingsViewModel.account.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentBottomScreen by remember { mutableStateOf<Screens?>(null) }
    val currentDestination by navController.currentBackStackEntryAsState()

    val lastTabRoute by settingsViewModel.lastTabRoute.collectAsStateWithLifecycle(
        initialValue = null
    )

    if (lastTabRoute == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(currentDestination) {
        val route = currentDestination?.destination?.route

        if (route == Screens.WorkTasks.route || route == Screens.HomeTasks.route) {
            settingsViewModel.saveLastTabRoute(route)
        }
    }

    LaunchedEffect(currentDestination) {
        val route = currentDestination?.destination?.route
        if (route != null) {
            currentBottomScreen = listOf(Screens.WorkTasks, Screens.HomeTasks)
                .find { screen -> route.startsWith(screen.route) }
        } else {
            currentBottomScreen = null
        }
    }

    var navGraphReady by remember { mutableStateOf(false) }
    var handled by remember { mutableStateOf(false) }

    LaunchedEffect(navigateTo, navGraphReady) {
        if (navGraphReady && navigateTo != null && !handled) {
            delay(100)
            try {
                Timber.d("MainScreen: navigating to $navigateTo")
                navController.navigate(navigateTo) {
                    launchSingleTop = true
                }
                handled = true
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Navigation route not found: $navigateTo")
            }
        }
    }

    LaunchedEffect(navigateTo) {
        handled = false
    }


    val searchResponse by mainScreenViewModel.searchResultFlow.collectAsState(initial = null)

    val loading by mainScreenViewModel.loading.collectAsState(initial = false)

    if (loading) {
        Dialog(onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.loader_size)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = LoaderColor)
            }
        }
    }

    LaunchedEffect(searchResponse) {
        if (searchResponse != null) {
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("searchResponse", searchResponse)

            navController.navigate(ResponseScreen.SearchScreen.route)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                notificationsEnabled = notificationsEnabled,
                darkThemeEnabled = darkThemeEnabled,
                notificationTime = notificationTime,
                onNotificationsChanged = { enabled ->
                    settingsViewModel.onNotificationsEnabled(enabled)
                },
                llmConnectionDirectType = llmDirectConnectionType,
                omLlmTypeChanged = { enabled ->
                    settingsViewModel.setConnectionDirectType(enabled)
                },
                onDarkThemeChanged = { enabled ->
                    settingsViewModel.setDarkThemeEnabled(enabled)
                },
                llmProvider = llmProvider,
                onLlmProviderChanged = { provider ->
                    settingsViewModel.setLlmProvider(provider)
                },
                llmModel = llmModel,
                onLlmModelChanged = { model ->
                    settingsViewModel.setLlmModel(model)
                },
                providers = settingsViewModel.aiProviders,
                onProviderKeyChanged = { providerKey ->
                    settingsViewModel.updateProviderKey(providerKey)
                },
                backendUrl = backendUrl,
                backendApiKey = backendApiKey,
                onBackendUrlChanged = { url ->
                    settingsViewModel.setBackendUrl(url)
                },
                onBackendApiKeyChanged = { key ->
                    settingsViewModel.setBackendApiKey(key)
                },
                accountEmail = account?.email,
                onSignIn = { settingsViewModel.signIn(context) },
                onSignOut = { settingsViewModel.signOut(context) },
                onNotificationTimeChanged = { hour, minute ->
                    settingsViewModel.updateNotificationTime(hour, minute)
                },
            )
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Мои задачи") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Открыть меню"
                            )
                        }
                    }
                )
            },
            bottomBar = { BottomBar(navController = navController) },
        ) { paddingValue ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValue)) {
                lastTabRoute?.let {
                    NavGraph(
                        navController = navController,
                        startDestination = lastTabRoute!!,
                        onFABVisibilityChanged = { visible ->
                            showFAB = visible
                        },
                        onGraphReady = { navGraphReady = true }
                    )
                }
                if (showFAB) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        FloatingButtonFiredTasks(navController)
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
            }
        }
    }

    VoiceDialog(
        showDialog = showVoiceDialog,
        requestType = requestType,
        onDismiss = { text ->
            showVoiceDialog = false
            if (text.isNotBlank()) {
                when (requestType) {
                    VoiceRequestType.ADD_TASK -> mainScreenViewModel.saveTaskRequest(
                        request = text,
                        isWorkTask = currentBottomScreen == Screens.WorkTasks
                    )

                    VoiceRequestType.SEARCH -> mainScreenViewModel.searchRequest(
                        request = text,
                        isWorkTask = currentBottomScreen == Screens.WorkTasks
                    )

                    VoiceRequestType.UNKNOWN -> Unit
                }
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
