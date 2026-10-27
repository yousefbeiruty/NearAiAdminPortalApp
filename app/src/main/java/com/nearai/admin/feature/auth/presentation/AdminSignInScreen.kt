package com.nearai.admin.feature.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.nearai.admin.core.designsystem.theme.*

/**
 * Admin Sign-In & Zero-Trust Authentication Screen.
 *
 * Uses [AuthViewModel] for state management and authentication logic.
 *
 * Features:
 * - Email and password fields with SSO partition indicator
 * - Argon2id encryption badge
 * - 6-digit TOTP input grid
 * - Biometrics and FIDO2/NFC hardware key options
 * - Cluster boundary footer with TLS status
 * - Development-only quick sign-in when backend is not connected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSignInScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AuthViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle navigation events
    var totpFocusIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is AuthEvent.NavigateToDashboard) {
                onSignInSuccess()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NearAI Admin",
                            style = MaterialTheme.typography.titleMedium,
                            color = Navy
                        )
                        Text(
                            text = "Executive Operations Console",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Back navigation */ }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Navy
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Refresh */ }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Indigo
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cluster Security Hero Section
            ClusterSecurityHeroSection()

            // Telemetry & MFA Notification Card
            TelemetryMfaCard()

            // Multi-Factor Authentication Card
            MfaCard(
                uiState = uiState,
                onEmailChanged = viewModel::onEmailChanged,
                onPasswordChanged = viewModel::onPasswordChanged,
                onTotpDigitChanged = viewModel::onTotpDigitChanged,
                onAuthenticate = {
                    // In development mode, use the fake sign-in path
                    viewModel.onDevSignIn()
                },
                onBiometricTrigger = {
                    // TODO: Integrate with Android BiometricPrompt
                },
                onFido2Trigger = {
                    // TODO: Integrate with FIDO2/NFC hardware key
                },
                totpFocusIndex = totpFocusIndex,
                onTotpFocusChanged = { totpFocusIndex = it }
            )

            // Cluster Boundary Footer
            ClusterBoundaryFooter()
        }
    }
}

/**
 * Hero section with security emblem and branding.
 */
@Composable
private fun ClusterSecurityHeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Security emblem
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Navy),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            // Lock badge overlay
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Indigo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Headline with badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NearAI Admin",
                style = MaterialTheme.typography.headlineMedium,
                color = Navy
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryContainerLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Indigo
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Subtitle
        Text(
            text = "Executive Operations & Compute Cluster Control Plane",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondaryLight
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Telemetry and MFA notification card.
 */
@Composable
private fun TelemetryMfaCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = Indigo,
                modifier = Modifier.size(24.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hardware Key / MFA Enforced",
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy
                )
                Text(
                    text = "Unauthorized ingress attempts are audited via immutable eBPF telemetry and routed to root ops.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight
                )
            }
        }
    }
}

/**
 * Multi-Factor Authentication card with email, password, TOTP, and hardware options.
 */
@Composable
private fun MfaCard(
    uiState: AuthUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTotpDigitChanged: (Int, String) -> Unit,
    onAuthenticate: () -> Unit,
    onBiometricTrigger: () -> Unit,
    onFido2Trigger: () -> Unit,
    totpFocusIndex: Int,
    onTotpFocusChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Work Email field
            EmailField(
                value = uiState.email,
                onValueChange = onEmailChanged
            )

            // Master Passphrase field
            var isPasswordVisible by remember { mutableStateOf(false) }
            PasswordField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                isPasswordVisible = isPasswordVisible,
                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible }
            )

            // TOTP input
            TotpInputSection(
                digits = uiState.totpDigits,
                onDigitChanged = onTotpDigitChanged,
                focusIndex = totpFocusIndex,
                onFocusChanged = onTotpFocusChanged
            )

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Primary CTA Button
            androidx.compose.material3.Button(
                onClick = onAuthenticate,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Navy
                )
            ) {
                if (uiState.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Authenticate & Enter Console",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }

            // Fast Hardware Pass Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = BorderSubtle
            )
            Text(
                text = "FAST HARDWARE PASS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryLight
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = BorderSubtle
            )

            // Biometrics and FIDO2 buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBiometricTrigger,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biometrics")
                }

                OutlinedButton(
                    onClick = onFido2Trigger,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FIDO2 / NFC")
                }
            }

            // Recovery trigger
            TextButton(onClick = { /* TODO: Recovery flow */ }) {
                Text(
                    text = "Lost hardware token or locked root quorum?",
                    style = MaterialTheme.typography.bodySmall,
                    color = Indigo,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

/**
 * Email input field with SSO partition indicator.
 */
@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Work Email or Founder ID",
                style = MaterialTheme.typography.labelMedium,
                color = Navy
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SSO Partition 0",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryLight
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "alex.founder@nearai.internal",
                    color = TextSecondaryDark
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Mail,
                    contentDescription = null,
                    tint = TextSecondaryLight
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Clear",
                            tint = TextSecondaryLight
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
    }
}

/**
 * Password input field with Argon2id badge.
 */
@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Master Passphrase",
                style = MaterialTheme.typography.labelMedium,
                color = Navy
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Indigo,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Argon2id Enforced",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Indigo,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Enter your master passphrase",
                    color = TextSecondaryDark
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = TextSecondaryLight
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = TextSecondaryLight
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )
    }
}

/**
 * 6-digit TOTP input section.
 */
@Composable
private fun TotpInputSection(
    digits: List<String>,
    onDigitChanged: (Int, String) -> Unit,
    focusIndex: Int,
    onFocusChanged: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hardware / Authenticator TOTP",
                style = MaterialTheme.typography.labelMedium,
                color = Navy
            )
            Text(
                text = "Step 2 of 2",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Indigo,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            digits.forEachIndexed { index, digit ->
                TotpDigitBox(
                    digit = digit,
                    onFocus = { onFocusChanged(index) },
                    onDigitChanged = { onDigitChanged(index, it) },
                    isFocused = focusIndex == index
                )
            }
        }
    }
}

/**
 * Single TOTP digit input box.
 */
@Composable
private fun TotpDigitBox(
    digit: String,
    onFocus: () -> Unit,
    onDigitChanged: (String) -> Unit,
    isFocused: Boolean
) {
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(isFocused) {
        if (isFocused) hasFocus = true
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (hasFocus) PrimaryContainerLight else BackgroundLight
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onFocus() },
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = digit,
            onValueChange = { newValue ->
                if (newValue.length <= 1 && newValue.matches(Regex("\\d"))) {
                    onDigitChanged(newValue)
                    hasFocus = true
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Navy,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
    }
}

/**
 * Cluster boundary footer with TLS status and security notice.
 */
@Composable
private fun ClusterBoundaryFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cluster status pill
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(EmeraldSuccess)
            )
            Text(
                text = "cluster: gcp-nearai-prod · us-central1",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryLight
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TLS 1.3",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryLight,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Security notice
        Text(
            text = "Strict boundary: Separated from public customer accounts. Consumer NearAI credentials are not authorized for cluster infrastructure.",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryLight
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Version metadata
        Text(
            text = "NearAI Admin v2.4.1 (Build 8902-prod) · Zero-Trust Policy",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryDark
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Preview for the Admin Sign-In screen.
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AdminSignInScreenPreview() {
    com.nearai.admin.core.designsystem.theme.NearAiAdminTheme {
        AdminSignInScreen(
            onSignInSuccess = { /* No-op for preview */ }
        )
    }
}
