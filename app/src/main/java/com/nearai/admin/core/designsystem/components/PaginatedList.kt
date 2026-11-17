package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.theme.Indigo
import com.nearai.admin.core.designsystem.theme.PaddingLarge
import com.nearai.admin.core.designsystem.theme.Spacing16dp
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * LazyColumn with built-in scroll observation for infinite scroll pagination.
 *
 * When the user scrolls near the end of the list (within [loadMoreThreshold] items),
 * [onLoadMore] is triggered automatically. This eliminates the need to duplicate
 * scroll observation logic in every paginated screen.
 *
 * Uses [snapshotFlow] for reactive scroll observation instead of a one-shot check,
 * ensuring the trigger fires reliably during scrolling and after new items load.
 *
 * @param dataItems The list of items to display.
 * @param key A stable, unique key provider for each item.
 * @param onLoadMore Called when the user scrolls near the end and more data is available.
 * @param isLoadingMore True while the next page is being fetched.
 * @param hasMore True if there are more pages to load.
 * @param loadMoreThreshold Number of items from the end to trigger load more (default: 3).
 * @param contentPadding Content padding for the list.
 * @param verticalArrangement Spacing between items.
 * @param header Optional header content before the list items.
 * @param item Content composable for each item.
 * @param footer Optional footer content after the list items.
 */
@Composable
fun <T : Any> PaginatedLazyColumn(
    dataItems: List<T>,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    modifier: Modifier = Modifier,
    loadMoreThreshold: Int = 3,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = PaddingLarge,
        vertical = Spacing16dp
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
    key: ((T) -> Any)? = null,
    header: LazyListScope.() -> Unit = {},
    item: @Composable (T) -> Unit,
    footer: LazyListScope.() -> Unit = {}
) {
    PaginatedLazyColumn(
        listState = rememberLazyListState(),
        dataItems = dataItems,
        onLoadMore = onLoadMore,
        isLoadingMore = isLoadingMore,
        hasMore = hasMore,
        modifier = modifier,
        loadMoreThreshold = loadMoreThreshold,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        key = key,
        header = header,
        item = item,
        footer = footer
    )
}

/**
 * Overload that accepts an external [LazyListState] for screens that manage
 * their own list state (e.g., for scroll-to-top or anchored scrolling).
 */
@Composable
fun <T : Any> PaginatedLazyColumn(
    listState: LazyListState,
    dataItems: List<T>,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    modifier: Modifier = Modifier,
    loadMoreThreshold: Int = 3,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = PaddingLarge,
        vertical = Spacing16dp
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(10.dp),
    key: ((T) -> Any)? = null,
    header: LazyListScope.() -> Unit = {},
    item: @Composable (T) -> Unit,
    footer: LazyListScope.() -> Unit = {}
) {
    // Capture the latest callback/state values so the coroutine body always uses
    // the current values without restarting the LaunchedEffect on every recomposition.
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val currentIsLoadingMore by rememberUpdatedState(isLoadingMore)
    val currentHasMore by rememberUpdatedState(hasMore)

    LaunchedEffect(listState, currentHasMore, currentIsLoadingMore, dataItems.size) {
        // Guard to fire at most once per data set. When new items arrive,
        // dataItems.size changes and restarts the LaunchedEffect with a fresh guard.
        var lastTriggeredSize = -1

        if (!currentHasMore || currentIsLoadingMore) return@LaunchedEffect

        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemCount = dataItems.size
            if (totalItemCount == 0) return@snapshotFlow false

            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val thresholdIndex = maxOf(0, totalItemCount - loadMoreThreshold)
            lastVisible >= thresholdIndex
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && !currentIsLoadingMore && currentHasMore) {
                    // Fire only once per item count. When new items arrive,
                    // dataItems.size changes → LaunchedEffect restarts → lastTriggeredSize resets.
                    if (lastTriggeredSize != dataItems.size) {
                        lastTriggeredSize = dataItems.size
                        currentOnLoadMore()
                    }
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        header()
        if (key != null) {
            items(dataItems, key = key) { entry ->
                item(entry)
            }
        } else {
            items(dataItems) { entry ->
                item(entry)
            }
        }
        // Loading more indicator — rendered as a lazy item so the list
        // accounts for its height and the user can see it at the bottom.
        if (isLoadingMore) {
            item {
                LoadMoreIndicator()
            }
        }
        footer()
    }
}

/**
 * Loading indicator shown at the bottom of a paginated list while more items are being fetched.
 */
@Composable
fun LoadMoreIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(PaddingLarge),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Indigo,
            strokeWidth = 2.dp
        )
    }
}
