package com.shkarov.mytasks.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.shkarov.mytasks.R
import com.shkarov.mytasks.domain.model.SearchResult

@Composable
fun SearchResultsScreen(
    response: SearchResult
) {
    Text(
        text = response.answer,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(
                start = dimensionResource(id = R.dimen.padding_main),
                top = dimensionResource(id = R.dimen.padding_main)
            )
    )
}
