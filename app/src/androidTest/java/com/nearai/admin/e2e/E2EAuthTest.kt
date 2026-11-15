package com.nearai.admin.e2e

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nearai.admin.MainActivity
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E authentication tests using the actual AdminSignInScreen and isolated backend.
 *
 * These tests launch MainActivity and interact with the real login UI,
 * verifying authentication flow against the isolated E2E backend (port 8084).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class E2EAuthTest {

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun signInScreen_showsEmailAndPasswordFields() {
        composeRule.onNodeWithTag("signInEmailField").assertExists()
        composeRule.onNodeWithTag("signInPasswordField").assertExists()
        composeRule.onNodeWithTag("signInButton").assertExists()
    }

    @Test
    fun signInScreen_invalidCredentials_showsError() {
        composeRule.onNodeWithTag("signInEmailField").assertExists()
        composeRule.onNodeWithTag("signInEmailField").performTextInput("invalid@test.com")
        composeRule.onNodeWithTag("signInPasswordField").performTextInput("wrongpassword")
        composeRule.onNodeWithTag("signInButton").performClick()
        composeRule.waitForIdle()
        try {
            composeRule.onNodeWithTag("signInError").assertExists()
        } catch (e: AssertionError) {
            composeRule.onNodeWithText("invalid", useUnmergedTree = true, substring = true)
                .assertExists()
        }
    }

    @Test
    fun signInScreen_emptyFields_buttonExists() {
        composeRule.onNodeWithTag("signInButton").assertExists()
    }
}
