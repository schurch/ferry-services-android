package com.stefanchurch.ferryservicesandroid.ui.screens.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun ServicesScreen(
    openSettings: () -> Unit,
    openService: (Int) -> Unit,
    viewModel: ServicesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.loading,
        onRefresh = viewModel::refresh,
    )

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullRefresh(pullRefreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
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
                            SectionHeading(section.title)
                            section.imageRes?.let { resId ->
                                Image(
                                    painter = painterResource(resId),
                                    contentDescription = section.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.height(28.dp),
                                )
                                Spacer(Modifier.height(12.dp))
                            }
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

            PullRefreshIndicator(
                refreshing = state.loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}
