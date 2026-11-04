package com.nearai.admin.feature.auditlogs.presentation

import com.nearai.admin.core.common.Result
import com.nearai.admin.feature.auditlogs.domain.AuditLog
import com.nearai.admin.feature.auditlogs.domain.AuditLogsRepository
import com.nearai.admin.feature.auditlogs.domain.PaginatedAuditLogs
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuditLogsViewModelTest {

    @RelaxedMockK
    private lateinit var repository: AuditLogsRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AuditLogsViewModel(repository)

    @Test
    fun `loadLogs on init fetches first page`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Success(paginatedLogs(page = 1, totalPages = 1))

        createViewModel()

        coVerify { repository.listAuditLogs(page = 1, size = 20, any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `loadLogs success updates UI state`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Success(paginatedLogs(page = 1, totalPages = 2, totalItems = 42))

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals(2, state.logs.size)
        assertEquals(42, state.totalItems)
        assertEquals(2, state.totalPages)
        assertTrue(state.hasMore)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadLogs failure sets error state`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Error(com.nearai.admin.core.common.AppError.NetworkError("No connection"))

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.logs.isEmpty())
        assertNotNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadMore appends items and increments page`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(paginatedLogs(page = 1, totalPages = 2, logPrefix = "p1")),
            Result.Success(paginatedLogs(page = 2, totalPages = 2, logPrefix = "p2"))
        )

        val viewModel = createViewModel()
        assertEquals(2, viewModel.uiState.value.logs.size)

        viewModel.loadMore()

        val state = viewModel.uiState.value
        assertEquals(4, state.logs.size)
        assertEquals(2, state.currentPage)
        assertFalse(state.hasMore)
    }

    @Test
    fun `loadMore does not call repository when hasMore is false`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Success(paginatedLogs(page = 1, totalPages = 1))

        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.hasMore)

        viewModel.loadMore()

        coVerify(exactly = 1) { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `loadMore failure preserves existing items`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(paginatedLogs(page = 1, totalPages = 2)),
            Result.Error(com.nearai.admin.core.common.AppError.NetworkError("Timeout"))
        )

        val viewModel = createViewModel()
        val beforeCount = viewModel.uiState.value.logs.size

        viewModel.loadMore()

        val state = viewModel.uiState.value
        assertEquals(beforeCount, state.logs.size)
        assertNotNull(state.error)
        assertFalse(state.hasMore)
    }

    @Test
    fun `onRetryLoadMore restores hasMore and retries`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(paginatedLogs(page = 1, totalPages = 2)),
            Result.Error(com.nearai.admin.core.common.AppError.NetworkError("Timeout")),
            Result.Success(paginatedLogs(page = 2, totalPages = 2, logPrefix = "p2"))
        )

        val viewModel = createViewModel()
        viewModel.loadMore()
        assertFalse(viewModel.uiState.value.hasMore)

        viewModel.onRetryLoadMore()

        val state = viewModel.uiState.value
        assertEquals(4, state.logs.size)
    }

    @Test
    fun `onRefresh reloads data and resets pagination`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(paginatedLogs(page = 1, totalPages = 2, logPrefix = "old")),
            Result.Success(paginatedLogs(page = 1, totalPages = 1, logPrefix = "fresh"))
        )

        val viewModel = createViewModel()
        viewModel.loadMore()
        assertEquals(4, viewModel.uiState.value.logs.size)

        viewModel.onRefresh()

        val state = viewModel.uiState.value
        assertEquals(2, state.logs.size)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `setFilterAction updates filter and reloads`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(paginatedLogs(page = 1, totalPages = 1)),
            Result.Success(paginatedLogs(page = 1, totalPages = 1, logPrefix = "filtered"))
        )

        val viewModel = createViewModel()
        viewModel.setFilterAction("product.create")

        val state = viewModel.uiState.value
        assertEquals("product.create", state.filterAction)
        assertEquals(1, state.currentPage)
        coVerify { repository.listAuditLogs(any(), any(), any(), action = "product.create", any(), any(), any(), any()) }
    }

    @Test
    fun `setFilterResourceType updates filter and reloads`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(paginatedLogs(page = 1, totalPages = 1)),
            Result.Success(paginatedLogs(page = 1, totalPages = 1, logPrefix = "filtered"))
        )

        val viewModel = createViewModel()
        viewModel.setFilterResourceType("order")

        val state = viewModel.uiState.value
        assertEquals("order", state.filterResourceType)
        coVerify { repository.listAuditLogs(any(), any(), any(), any(), resourceType = "order", any(), any(), any()) }
    }

    @Test
    fun `clearFilters resets all filters and reloads`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returnsMany listOf(
            Result.Success(paginatedLogs(page = 1, totalPages = 1)),
            Result.Success(paginatedLogs(page = 1, totalPages = 1, logPrefix = "cleared"))
        )

        val viewModel = createViewModel()
        viewModel.setFilterAction("product.create")
        viewModel.setFilterResourceType("order")

        viewModel.clearFilters()

        val state = viewModel.uiState.value
        assertNull(state.filterAction)
        assertNull(state.filterResourceType)
        assertNull(state.filterResourceId)
    }

    @Test
    fun `onToggleExpand toggles expanded log ID`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Success(paginatedLogs(page = 1, totalPages = 1))

        val viewModel = createViewModel()
        val logId = viewModel.uiState.value.logs[0].id

        viewModel.onToggleExpand(logId)
        assertEquals(logId, viewModel.uiState.value.expandedLogId)

        viewModel.onToggleExpand(logId)
        assertNull(viewModel.uiState.value.expandedLogId)
    }

    @Test
    fun `onSelectLog sets selected log`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Success(paginatedLogs(page = 1, totalPages = 1))

        val viewModel = createViewModel()
        val log = viewModel.uiState.value.logs[0]

        viewModel.onSelectLog(log)
        assertEquals(log, viewModel.uiState.value.selectedLog)

        viewModel.onDismissSelectedLog()
        assertNull(viewModel.uiState.value.selectedLog)
    }

    @Test
    fun `empty data shows empty state`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Success(PaginatedAuditLogs(emptyList(), 1, 20, 0, 0))

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertTrue(state.logs.isEmpty())
        assertNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loading flags reset after exception`() = runTest {
        coEvery { repository.listAuditLogs(any(), any(), any(), any(), any(), any(), any(), any()) } returns
            Result.Error(com.nearai.admin.core.common.AppError.UnknownError("Crash"))

        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNotNull(state.error)
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private fun paginatedLogs(
        page: Int = 1,
        totalPages: Int = 1,
        totalItems: Int = 2,
        logPrefix: String = "log"
    ): PaginatedAuditLogs {
        return PaginatedAuditLogs(
            logs = listOf(
                AuditLog(
                    id = "${logPrefix}-1",
                    actorId = "actor-1",
                    actorRole = "admin",
                    action = "product.create",
                    resourceType = "product",
                    resourceId = "prod-1",
                    requestId = "req-1",
                    changes = null,
                    metadata = null,
                    createdAt = "2025-11-04T10:00:00Z"
                ),
                AuditLog(
                    id = "${logPrefix}-2",
                    actorId = "actor-2",
                    actorRole = "admin",
                    action = "stock.adjust",
                    resourceType = "stock",
                    resourceId = "variant-1",
                    requestId = "req-2",
                    changes = """{"before":100,"after":85}""",
                    metadata = null,
                    createdAt = "2025-11-04T09:00:00Z"
                )
            ),
            page = page,
            size = 20,
            totalPages = totalPages,
            totalItems = totalItems
        )
    }
}
