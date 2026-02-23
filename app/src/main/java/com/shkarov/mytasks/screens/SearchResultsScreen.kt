package com.shkarov.mytasks.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.viewmodels.SearchResultScreenViewModel

@Composable
fun SearchResultsScreen(
    response: SearchResult,
    navController: NavHostController,
) {

    val viewModel: SearchResultScreenViewModel = hiltViewModel()

    val tasks by viewModel.tasks.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getTasks(response.ids)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.padding_small).value.dp)
    ) {

        Text(
            text = response.request,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.padding_main),
                    top = dimensionResource(id = R.dimen.padding_main)
                )
        )
        GrayDivider()
        Text(
            text = response.answer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.padding_main),
                    top = dimensionResource(id = R.dimen.padding_main)
                )
        )
        GrayDivider()
        TasksList(
            navController = navController,
            tasks = tasks,
            onDelete = { taskId ->
                viewModel.deleteTaskById(taskId)
            }
        )
    }
}
