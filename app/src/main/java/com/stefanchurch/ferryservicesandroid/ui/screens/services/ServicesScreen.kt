package com.stefanchurch.ferryservicesandroid.ui.screens.services

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stefanchurch.ferryservicesandroid.ui.components.SectionHeading
import com.stefanchurch.ferryservicesandroid.ui.components.ServiceRowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    openSettings: () -> Unit,
    openService: (Int) -> Unit,
    viewModel: ServicesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Services") },
                actions = {
                    IconButton(
                        onClick = {
                            searchVisible = !searchVisible
                            if (!searchVisible && state.searchText.isNotEmpty()) {
                                viewModel.updateSearchText("")
                            }
                        },
                    ) {
                        Icon(
                            if (searchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = if (searchVisible) "Close search" else "Search",
                        )
                    }
                    IconButton(onClick = openSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (searchVisible) {
                    item {
                        OutlinedTextField(
                            value = state.searchText,
                            onValueChange = viewModel::updateSearchText,
                            label = { Text("Search routes or areas") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }

                state.sections.forEach { section ->
                    item {
                        Column {
                            OperatorSectionHeading(
                                title = section.title,
                                imageRes = section.imageRes,
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                section.rows.forEach { row ->
                                    ServiceRowCard(row = row, onClick = { openService(row.service.serviceId) })
                                }
                            }
                        }
                    }
                }

                if (!state.loading && state.sections.sumOf { it.rows.size } == 0) {
                    item {
                        Text(
                            "No matching services.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                if (state.loading && state.sections.isEmpty()) {
                    item {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun OperatorSectionHeading(
    title: String,
    @DrawableRes imageRes: Int?,
    modifier: Modifier = Modifier,
) {
    if (imageRes == null) {
        SectionHeading(title = title, modifier = modifier)
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
