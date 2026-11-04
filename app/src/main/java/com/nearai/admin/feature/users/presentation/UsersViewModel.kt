package com.nearai.admin.feature.users.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.users.domain.AdminUser
import com.nearai.admin.feature.users.domain.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI state for the Users screen.
 */
data class UsersUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val users: List<AdminUser> = emptyList(),
    val selectedUser: AdminUser? = null,
    val error: String? = null,
    val searchQuery: String = "",
    val filterRole: String? = null,
    val filterIsActive: Boolean? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalItems: Int = 0,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isUpdating: Boolean = false,
    val updateMessage: String? = null
)

/**
 * One-time events emitted from the ViewModel.
 */
sealed interface UsersEvent {
    object NoOp : UsersEvent
}

/**
 * ViewModel for the Users Management screen.
 * Manages user listing, search, filtering, pagination, and admin actions.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<UsersEvent?>(null)
    val event: StateFlow<UsersEvent?> = _event.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private companion object {
        const val PAGE_SIZE = 20
        const val SEARCH_DEBOUNCE_MS = 400L
    }

    init {
        // Debounced search
        viewModelScope.launch {
            _searchQuery
                .debounce(SEARCH_DEBOUNCE_MS)
                .collect { query ->
                    _uiState.update { it.copy(searchQuery = query) }
                    loadUsers()
                }
        }
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = _uiState.value.users.isEmpty(),
                    isRefreshing = _uiState.value.users.isNotEmpty(),
                    error = null,
                    currentPage = 1,
                    hasMore = false,
                    isLoadingMore = false,
                    updateMessage = null
                )
            }

            val state = _uiState.value
            when (val result = usersRepository.listUsers(
                page = 1,
                size = PAGE_SIZE,
                search = if (state.searchQuery.isBlank()) null else state.searchQuery,
                role = state.filterRole,
                isActive = state.filterIsActive
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            users = result.data.users,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems,
                            hasMore = result.data.page < result.data.totalPages
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.exception.message
                        )
                    }
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = state.currentPage + 1
            when (val result = usersRepository.listUsers(
                page = nextPage,
                size = PAGE_SIZE,
                search = if (state.searchQuery.isBlank()) null else state.searchQuery,
                role = state.filterRole,
                isActive = state.filterIsActive
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            users = it.users + result.data.users,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            totalItems = result.data.totalItems,
                            hasMore = result.data.page < result.data.totalPages
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoadingMore = false, error = result.exception.message)
                    }
                }
                is Result.Loading -> {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Update UI state immediately for responsive search bar, but loadUsers is debounced
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilterRole(role: String?) {
        // Selecting a role clears the active/inactive filter to keep "All" logic simple
        _uiState.update { it.copy(filterRole = role, filterIsActive = null) }
        loadUsers()
    }

    fun setFilterIsActive(isActive: Boolean?) {
        // Selecting a status clears the role filter to keep "All" logic simple
        _uiState.update { it.copy(filterIsActive = isActive, filterRole = null) }
        loadUsers()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(filterRole = null, filterIsActive = null, searchQuery = "")
        }
        loadUsers()
    }

    fun onUserClicked(user: AdminUser) {
        _uiState.update { it.copy(selectedUser = user) }
    }

    fun dismissSelectedUser() {
        _uiState.update { it.copy(selectedUser = null) }
    }

    fun updateUserRole(userId: String, role: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, updateMessage = null) }
            when (val result = usersRepository.updateUser(userId, role = role)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isUpdating = false,
                            users = state.users.map { u ->
                                if (u.id == result.data.id) result.data else u
                            },
                            selectedUser = if (state.selectedUser?.id == result.data.id) result.data else state.selectedUser,
                            updateMessage = "Role updated successfully"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isUpdating = false, error = result.exception.message)
                    }
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun toggleUserActive(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, updateMessage = null) }
            when (val result = usersRepository.updateUser(userId, isActive = isActive)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isUpdating = false,
                            users = state.users.map { u ->
                                if (u.id == result.data.id) result.data else u
                            },
                            selectedUser = if (state.selectedUser?.id == result.data.id) result.data else state.selectedUser,
                            updateMessage = if (isActive) "User activated" else "User suspended"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isUpdating = false, error = result.exception.message)
                    }
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun revokeSessions(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, updateMessage = null) }
            when (val result = usersRepository.revokeSessions(userId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            updateMessage = "${result.data} session(s) revoked"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isUpdating = false, error = result.exception.message)
                    }
                }
                is Result.Loading -> { /* No-op */ }
            }
        }
    }

    fun onRetry() {
        loadUsers()
    }

    fun onRefresh() {
        loadUsers()
    }

    fun onEventConsumed() {
        _event.value = null
    }
}
