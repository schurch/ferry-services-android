package com.stefanchurch.ferryservicesandroid.ui.screens.services

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stefanchurch.ferryservicesandroid.ui.components.SectionHeading
import com.stefanchurch.ferryservicesandroid.ui.components.ServiceRowCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ServicesScreen(
    openSettings: () -> Unit,
    openService: (Int) -> Unit,
    viewModel: ServicesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showSearch = searchVisible || state.searchText.isNotBlank()

    LaunchedEffect(showSearch) {
        if (showSearch) {
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
                            if (showSearch) {
                                searchVisible = false
                            } else {
                                searchVisible = true
                            }
                            if (showSearch && state.searchText.isNotEmpty()) {
                                viewModel.updateSearchText("")
                            }
                        },
                    ) {
                        Icon(
                            if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = if (showSearch) "Close search" else "Search",
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
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (showSearch) {
                    item {
                        TextField(
                            value = state.searchText,
                            onValueChange = viewModel::updateSearchText,
                            placeholder = { Text("Search routes or areas") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                            },
                            trailingIcon = if (state.searchText.isNotEmpty()) {
                                {
                                    IconButton(onClick = { viewModel.updateSearchText("") }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                                    }
                                }
                            } else {
                                null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            ),
                        )
                    }
                }

                state.sections.forEach { section ->
                    stickyHeader(key = "heading-${section.title}-${section.subscribed}") {
                        OperatorSectionHeading(
                            title = section.title,
                            imageRes = section.imageRes,
                            subscribed = section.subscribed,
                        )
                    }
                    items(
                        items = section.rows,
                        key = { row -> "service-${section.title}-${row.service.serviceId}" },
                    ) { row ->
                        ServiceRowCard(
                            row = row,
                            onClick = { openService(row.service.serviceId) },
                        )
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
    subscribed: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (imageRes == null && !subscribed) {
            SectionHeading(title = title, modifier = Modifier.padding(bottom = 2.dp))
            return@Surface
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                subscribed -> {
                    Icon(
                        imageVector = Icons.Rounded.RssFeed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                imageRes != null -> {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
