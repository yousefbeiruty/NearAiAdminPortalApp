package com.nearai.admin.feature.auth.presentation

import app.cash.turbine.test
import com.nearai.admin.core.common.AppError
import com.nearai.admin.core.common.Result
import com.nearai.admin.core.security.SessionManager
import com.nearai.admin.feature.auth.domain.AdminSession
import com.nearai.admin.feature.auth.domain.AuthRepository
import com.nearai.admin.feature.auth.domain.MfaChallenge
import com.nearai.admin.feature.auth.domain.SignInResponse
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
class AuthViewModelTest {

    @RelaxedMockK
    private lateinit var authRepository: AuthRepository

    @RelaxedMockK
    private lateinit var sessionManager: SessionManager

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty fields`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals(6, state.totpDigits.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isMfaRequired)
    }

    @Test
    fun `onEmailChanged updates email and clears error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChanged("test@nearai.dev")

        assertEquals("test@nearai.dev", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onPasswordChanged updates password and clears error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("secret123")

        assertEquals("secret123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `onSignIn with empty email shows error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onPasswordChanged("password")
        viewModel.onSignIn()

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Please enter email and password.", viewModel.uiState.value.error)
    }

    @Test
    fun `onSignIn with empty password shows error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChanged("test@nearai.dev")
        viewModel.onSignIn()

        assertNotNull(viewModel.uiState.value.error)
        assertEquals("Please enter email and password.", viewModel.uiState.value.error)
    }

    @Test
    fun `onSignIn success with direct session navigates to dashboard`() = runTest {
        val session = AdminSession("admin@nearai.dev", "access", "refresh")
        coEvery { authRepository.signIn(any(), any()) } returns Result.Success(
            SignInResponse.Session(session)
        )

        val viewModel = createViewModel()
        viewModel.onEmailChanged("admin@nearai.dev")
        viewModel.onPasswordChanged("password")
        viewModel.onSignIn()

        coVerify { sessionManager.login("access", "refresh", "admin@nearai.dev") }

        viewModel.event.test {
            val event = awaitItem()
            assertTrue(event is AuthEvent.NavigateToDashboard)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSignIn success with MFA required shows MFA state`() = runTest {
        val challenge = MfaChallenge("ch-001", "totp", "Enter 6-digit code")
        coEvery { authRepository.signIn(any(), any()) } returns Result.Success(
            SignInResponse.MfaRequired(challenge)
        )

        val viewModel = createViewModel()
        viewModel.onEmailChanged("admin@nearai.dev")
        viewModel.onPasswordChanged("password")
        viewModel.onSignIn()

        val state = viewModel.uiState.value
        assertTrue(state.isMfaRequired)
        assertEquals("Enter 6-digit code", state.mfaHint)
    }

    @Test
    fun `onSignIn error sets error message`() = runTest {
        coEvery { authRepository.signIn(any(), any()) } returns Result.Error(
            AppError.AuthenticationError("Invalid credentials")
        )

        val viewModel = createViewModel()
        viewModel.onEmailChanged("admin@nearai.dev")
        viewModel.onPasswordChanged("wrong")
        viewModel.onSignIn()

        assertEquals("Invalid credentials", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onTotpDigitChanged updates digit at index`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTotpDigitChanged(0, "5")

        assertEquals("5", viewModel.uiState.value.totpDigits[0])
        assertEquals("", viewModel.uiState.value.totpDigits[1])
    }

    @Test
    fun `onTotpDigitChanged rejects non-digit input`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTotpDigitChanged(0, "a")

        assertEquals("", viewModel.uiState.value.totpDigits[0])
    }

    @Test
    fun `onTotpDigitChanged rejects multi-digit input`() = runTest {
        val viewModel = createViewModel()
        viewModel.onTotpDigitChanged(0, "12")

        assertEquals("", viewModel.uiState.value.totpDigits[0])
    }

    @Test
    fun `onVerifyMfa with incomplete code shows error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onVerifyMfa()

        assertEquals("Please enter all 6 digits.", viewModel.uiState.value.error)
    }

    @Test
    fun `onVerifyMfa success navigates to dashboard`() = runTest {
        // First set up MFA state by simulating signIn -> MFA
        val challenge = MfaChallenge("ch-001", "totp")
        coEvery { authRepository.signIn(any(), any()) } returns Result.Success(
            SignInResponse.MfaRequired(challenge)
        )
        val session = AdminSession("admin@nearai.dev", "access", "refresh")
        coEvery { authRepository.verifyMfa("ch-001", "123456") } returns Result.Success(session)

        val viewModel = createViewModel()
        viewModel.onEmailChanged("admin@nearai.dev")
        viewModel.onPasswordChanged("password")
        viewModel.onSignIn()

        // Now fill TOTP digits
        viewModel.onTotpDigitChanged(0, "1")
        viewModel.onTotpDigitChanged(1, "2")
        viewModel.onTotpDigitChanged(2, "3")
        viewModel.onTotpDigitChanged(3, "4")
        viewModel.onTotpDigitChanged(4, "5")
        viewModel.onTotpDigitChanged(5, "6")
        viewModel.onVerifyMfa()

        coVerify { sessionManager.login("access", "refresh", "admin@nearai.dev") }

        viewModel.event.test {
            val event = awaitItem()
            assertTrue(event is AuthEvent.NavigateToDashboard)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onVerifyMfa error sets error message`() = runTest {
        val challenge = MfaChallenge("ch-001", "totp")
        coEvery { authRepository.signIn(any(), any()) } returns Result.Success(
            SignInResponse.MfaRequired(challenge)
        )
        coEvery { authRepository.verifyMfa("ch-001", "000000") } returns Result.Error(
            AppError.ValidationError("Invalid MFA code")
        )

        val viewModel = createViewModel()
        viewModel.onEmailChanged("admin@nearai.dev")
        viewModel.onPasswordChanged("password")
        viewModel.onSignIn()

        for (i in 0..5) viewModel.onTotpDigitChanged(i, "0")
        viewModel.onVerifyMfa()

        assertEquals("Invalid MFA code", viewModel.uiState.value.error)
    }

    @Test
    fun `clearError removes error message`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSignIn() // Triggers "Please enter email and password."
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    private fun createViewModel(): AuthViewModel {
        return AuthViewModel(authRepository, sessionManager)
    }
}
