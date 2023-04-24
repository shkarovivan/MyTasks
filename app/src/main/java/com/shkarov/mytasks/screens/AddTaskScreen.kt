package com.shkarov.mytasks.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.shkarov.mytasks.R
import com.shkarov.mytasks.ui.theme.MyTasksTheme

@Composable
fun AddTaskScreen(
    onBackClick: () -> Unit
) {
    var titleValue by remember {
        mutableStateOf("")
    }

    var descriptionValue by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = dimensionResource(id = R.dimen.padding_small))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(all = dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                text = stringResource(id = R.string.task_title)
            )

            TextInput(value = titleValue, onValueChange ={
                titleValue = it
            } )

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .padding(all = dimensionResource(id = R.dimen.padding_main))
                        .fillMaxWidth(),
                    onClick = { /*TODO*/ })
                {
                    Text(text = stringResource(id = R.string.save_text))
                }
            }

        }

    }
}

@Composable
fun TextInput(
    value:String,
    onValueChange:(String)->Unit
){
    BasicTextField(
        modifier = Modifier
            .padding(
                start = dimensionResource(id = R.dimen.padding_main),
                end = dimensionResource(id = R.dimen.padding_main),
                top = dimensionResource(id = R.dimen.padding_smallest),
                bottom = dimensionResource(id = R.dimen.padding_smallest),
            )
            .fillMaxWidth()
            .testTag("searchTextFieldTag"),

        singleLine = false,
        minLines = dimensionResource(id = R.dimen.task_title_min_lines).value.toInt(),
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        textStyle = TextStyle(
            fontSize = dimensionResource(id = R.dimen.main_text_size).value.sp,
            fontWeight = FontWeight.Medium,
            //color = Color.DarkGray
        ),
        enabled = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .padding(
                        start = dimensionResource(id = R.dimen.padding_small),
                        end = dimensionResource(id = R.dimen.padding_main)
                    )
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colors.background,
                        shape = RoundedCornerShape(
                            size = dimensionResource(id = R.dimen.corner_radius)
                        )
                    )
                    .border(
                        width = dimensionResource(id = R.dimen.border_width),
                        color = MaterialTheme.colors.onBackground,
                        shape = RoundedCornerShape(size = dimensionResource(id = R.dimen.corner_radius))
                    )
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.padding_main),
                        vertical = dimensionResource(id = R.dimen.padding_small)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Favorite icon",
                    // tint = Color.DarkGray
                )
                innerTextField()
            }
        }
    )
}

@Preview
@Composable
fun previewAddTaskScreen() {
    MyTasksTheme {
        AddTaskScreen {

        }
    }
}