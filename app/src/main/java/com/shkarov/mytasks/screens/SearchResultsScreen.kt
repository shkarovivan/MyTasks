package com.shkarov.mytasks.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.SearchResult
import com.shkarov.mytasks.viewmodels.SearchResultScreenViewModel

@Composable
fun SearchResultsScreen(
    response: SearchResult? = null,
    navController: NavHostController,
) {

    val viewModel: SearchResultScreenViewModel = hiltViewModel()

    val tasks by viewModel.tasks.collectAsState()

    LaunchedEffect(Unit) {
        if (response != null && response.ids.isNotEmpty()) {
            viewModel.getTasks(response.ids)
        } else {
            viewModel.getTodayTasks()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.padding_main)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_main))
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_card_radius))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_main),
                        vertical = dimensionResource(id = R.dimen.padding_main)
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(
                    text = response?.request ?: stringResource(id = R.string.today_task_text),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                response?.let {
                    Text(
                        text = it.answer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_card_radius))
        ) {
            TasksList(
                navController = navController,
                tasks = tasks,
                onDelete = { taskId ->
                    viewModel.deleteTaskById(taskId)
                }
            )
        }
    }
}
