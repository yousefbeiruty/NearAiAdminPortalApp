package com.nearai.admin

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Empty host Activity used by Compose UI tests via [createAndroidComposeRule].
 *
 * ComponentActivity provides access to Android resources (strings, themes) without
 * requiring a production Activity. The [AndroidEntryPoint] annotation enables Hilt
 * injection in tests that use this activity as their compose host.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
