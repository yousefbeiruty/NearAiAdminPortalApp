package com.nearai.admin

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner that configures Hilt's test Application for all instrumented tests.
 *
 * This replaces the default AndroidJUnitRunner so that every instrumented test
 * runs with [HiltTestApplication], enabling constructor injection in test code.
 */
class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
