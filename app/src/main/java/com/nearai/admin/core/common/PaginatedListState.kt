package com.nearai.admin.core.common

/**
 * Paginated list response from the backend.
 *
 * This is the domain-layer representation of a paginated API response.
 * Each feature's repository returns a feature-specific type (e.g., [PaginatedOrders]),
 * but they all share this common structure.
 *
 * @param T The type of items in the paginated list.
 * @param items The items on the current page.
 * @param page The current page number (1-based).
 * @param size The number of items per page.
 * @param totalPages The total number of pages available.
 * @param totalItems The total number of items across all pages.
 */
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
) {
    /**
     * Returns true if there are more pages to load.
     */
    val hasMore: Boolean
        get() = page < totalPages
}

/**
 * Shared pagination state that can be embedded in any UiState.
 *
 * Instead of forcing ViewModels into a base class hierarchy, each paginated
 * screen's UiState includes a [PaginatedListState] field. This keeps pagination
 * logic reusable while preserving feature-specific state fields.
 *
 * @param T The type of items in the list.
 * @param items The accumulated items loaded so far.
 * @param currentPage The current page number (1-based).
 * @param totalPages The total number of pages from the backend.
 * @param totalItems The total number of items across all pages.
 * @param hasMore True if there are more pages to load.
 * @param isLoadingMore True while the next page is being fetched.
 * @param isLoading True during the initial load.
 * @param isRefreshing True during pull-to-refresh.
 * @param error The current error message, if any.
 */
data class PaginatedListState<T>(
    val items: List<T> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /**
     * Returns the initial loading state.
     */
    fun initialLoading(): PaginatedListState<T> = copy(
        isLoading = items.isEmpty(),
        isRefreshing = items.isNotEmpty(),
        error = null,
        currentPage = 1,
        hasMore = false,
        isLoadingMore = false
    )

    /**
     * Applies a successful page load result.
     */
    fun onLoadSuccess(response: PaginatedResponse<T>): PaginatedListState<T> {
        val newItems = if (currentPage == 1) {
            response.items
        } else {
            items + response.items
        }
        return copy(
            items = newItems,
            currentPage = response.page,
            totalPages = response.totalPages,
            totalItems = response.totalItems,
            hasMore = response.hasMore,
            isLoading = false,
            isRefreshing = false,
            isLoadingMore = false
        )
    }

    /**
     * Applies a load more success result (appends to existing items).
     */
    fun onLoadMoreSuccess(response: PaginatedResponse<T>): PaginatedListState<T> = copy(
        items = items + response.items,
        currentPage = response.page,
        totalPages = response.totalPages,
        totalItems = response.totalItems,
        hasMore = response.hasMore,
        isLoadingMore = false
    )

    /**
     * Applies an error state.
     */
    fun onError(message: String?): PaginatedListState<T> = copy(
        isLoading = false,
        isRefreshing = false,
        isLoadingMore = false,
        error = message
    )

    /**
     * Sets the loading more flag.
     */
    fun setLoadingMore(loading: Boolean): PaginatedListState<T> = copy(
        isLoadingMore = loading
    )

    /**
     * Clears the error message.
     */
    fun clearError(): PaginatedListState<T> = copy(error = null)
}
