package com.revest.feature.productlist.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revest.core.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    state: ProductListState,
    onEvent: (ProductListEvent) -> Unit,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // ── Infinite scroll trigger ───────────────────────────────────────────────
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total       = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 4 && !state.isLoading && state.canLoadMore && !state.isSearchMode
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onEvent(ProductListEvent.LoadMore)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Revest Catalog",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.totalProducts > 0) {
                            Text(
                                text = "${state.totalProducts} products",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* filter sheet */ }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { padding ->
        when {
            state.isLoading && state.products.isEmpty() ->
                LoadingView(Modifier.padding(padding))

            state.error != null && state.products.isEmpty() ->
                ErrorView(
                    message = state.error,
                    onRetry = { onEvent(ProductListEvent.Refresh) },
                    modifier = Modifier.padding(padding)
                )

            else -> ProductList(
                state       = state,
                padding     = padding,
                listState   = listState,
                onEvent     = onEvent,
                onProductClick = onProductClick
            )
        }
    }
}

@Composable
private fun ProductList(
    state: ProductListState,
    padding: PaddingValues,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onEvent: (ProductListEvent) -> Unit,
    onProductClick: (Int) -> Unit
) {
    LazyColumn(
        state            = listState,
        modifier         = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding   = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search bar
        item(key = "search") {
            RevestSearchBar(
                query         = state.searchQuery,
                onQueryChange = { onEvent(ProductListEvent.SearchQueryChanged(it)) },
                modifier      = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Categories
        if (state.categories.isNotEmpty()) {
            item(key = "categories") {
                LazyRow(
                    contentPadding      = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        RevestFilterChip(
                            label    = "All",
                            selected = state.selectedCategory == null,
                            onClick  = { onEvent(ProductListEvent.CategorySelected(null)) }
                        )
                    }
                    items(state.categories, key = { it }) { cat ->
                        RevestFilterChip(
                            label    = cat,
                            selected = state.selectedCategory == cat,
                            onClick  = { onEvent(ProductListEvent.CategorySelected(cat)) }
                        )
                    }
                }
            }
        }

        // Search mode header
        if (state.isSearchMode && state.searchQuery.isNotBlank()) {
            item(key = "search_header") {
                SearchResultHeader(
                    query = state.searchQuery,
                    count = state.products.size,
                    onClear = { onEvent(ProductListEvent.SearchQueryChanged("")) }
                )
            }
        }

        // Product items
        items(items = state.products, key = { it.id }) { product ->
            ProductCard(
                id                 = product.id,
                title              = product.title,
                brand              = product.brand,
                price              = product.price,
                rating             = product.rating,
                discountPercentage = product.discountPercentage,
                thumbnail          = product.thumbnail,
                onClick            = { onProductClick(product.id) },
                modifier           = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Load more spinner
        if (state.isLoading && state.products.isNotEmpty()) {
            item(key = "loading_more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(28.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // Empty state
        if (state.products.isEmpty() && !state.isLoading) {
            item(key = "empty") {
                EmptyState(
                    message = if (state.isSearchMode)
                        "No results for \"${state.searchQuery}\""
                    else
                        "No products available"
                )
            }
        }
    }
}

@Composable
private fun SearchResultHeader(query: String, count: Int, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = "$count results for \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onClear) {
            Text("Clear", color = MaterialTheme.colorScheme.primary)
        }
    }
}
