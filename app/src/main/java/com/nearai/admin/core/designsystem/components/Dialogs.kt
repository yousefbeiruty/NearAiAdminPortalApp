package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nearai.admin.core.designsystem.theme.*

// =============================================================================
// Alert Type Enum
// =============================================================================

enum class NearAiAlertType {
    ErrorType,
    WarningType,
    InfoType,
    SuccessType,
    NoneType,
}

@Composable
private fun NearAiAlertType.accentColor(): Color = when (this) {
    NearAiAlertType.ErrorType -> RoseError
    NearAiAlertType.WarningType -> AmberWarning
    NearAiAlertType.InfoType -> Info
    NearAiAlertType.SuccessType -> EmeraldSuccess
    NearAiAlertType.NoneType -> Navy
}

@Composable
private fun NearAiAlertType.badgeBg(): Color = when (this) {
    NearAiAlertType.ErrorType -> RoseBg
    NearAiAlertType.WarningType -> AmberBg
    NearAiAlertType.InfoType -> PurpleBg
    NearAiAlertType.SuccessType -> EmeraldBg
    NearAiAlertType.NoneType -> PrimaryContainerLight
}

// =============================================================================
// Icon Drawables
// =============================================================================

@Composable
private fun AlertIcon(type: NearAiAlertType) {
    val accent = type.accentColor()
    val bg = type.badgeBg()

    Canvas(
        modifier = Modifier.size(56.dp),
    ) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val iconSize = diameter * 0.32f
        val strokeWidth = 3.5f

        // Background circle
        drawCircle(
            color = bg,
            radius = radius,
            center = Offset(radius, radius)
        )

        when (type) {
            NearAiAlertType.ErrorType -> {
                val half = iconSize * 0.7f
                drawLine(
                    color = accent,
                    start = Offset(radius - half, radius - half),
                    end = Offset(radius + half, radius + half),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = accent,
                    start = Offset(radius + half, radius - half),
                    end = Offset(radius - half, radius + half),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            NearAiAlertType.WarningType -> {
                val lineTop = radius - iconSize * 0.5f
                val lineBottom = radius + iconSize * 0.1f
                val dotCenterY = radius + iconSize * 0.4f
                val dotRadius = iconSize * 0.18f

                drawLine(
                    color = accent,
                    start = Offset(radius, lineTop),
                    end = Offset(radius, lineBottom),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
                drawCircle(
                    color = accent,
                    radius = dotRadius,
                    center = Offset(radius, dotCenterY)
                )
            }
            NearAiAlertType.InfoType -> {
                val dotCenterY = radius - iconSize * 0.4f
                val dotRadius = iconSize * 0.18f
                val lineTop = radius - iconSize * 0.05f
                val lineBottom = radius + iconSize * 0.45f

                drawCircle(
                    color = accent,
                    radius = dotRadius,
                    center = Offset(radius, dotCenterY)
                )
                drawLine(
                    color = accent,
                    start = Offset(radius, lineTop),
                    end = Offset(radius, lineBottom),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
            NearAiAlertType.SuccessType -> {
                val path = Path().apply {
                    val s = iconSize
                    moveTo(radius - s * 0.4f, radius)
                    lineTo(radius - s * 0.05f, radius + s * 0.35f)
                    lineTo(radius + s * 0.45f, radius - s * 0.3f)
                }
                drawPath(
                    path = path,
                    color = accent,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = null,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }
            NearAiAlertType.NoneType -> {
                val qSize = iconSize * 0.5f
                val curvePath = Path().apply {
                    moveTo(radius - qSize * 0.4f, radius - qSize * 0.3f)
                    quadraticTo(
                        radius - qSize * 0.5f, radius - qSize * 0.6f,
                        radius, radius - qSize * 0.6f
                    )
                    quadraticTo(
                        radius + qSize * 0.5f, radius - qSize * 0.6f,
                        radius + qSize * 0.4f, radius
                    )
                    lineTo(radius + qSize * 0.4f, radius + qSize * 0.15f)
                }
                drawPath(
                    path = curvePath,
                    color = accent,
                    style = Stroke(
                        width = strokeWidth * 0.8f,
                        cap = StrokeCap.Round,
                    ),
                )
                drawCircle(
                    color = accent,
                    radius = iconSize * 0.12f,
                    center = Offset(radius, radius + qSize * 0.5f)
                )
            }
        }
    }
}

// =============================================================================
// NearAiAlertDialog — Unified Dialog Component
// =============================================================================

/**
 * Unified alert dialog matching the NearAiDiscovery customer app design.
 *
 * Supports five alert types with custom-drawn icons, consistent styling,
 * and flexible button layouts (single confirm or confirm + dismiss).
 *
 * @param isVisible Controls dialog visibility
 * @param title Dialog title (bold, prominent)
 * @param message Dialog body text
 * @param confirmText Label for the confirm button
 * @param dismissText Label for the dismiss button (null for single-button mode)
 * @param type Alert type determining icon and accent color
 * @param onConfirm Called when confirm is pressed
 * @param onDismiss Called when dismiss is pressed or dialog is dismissed
 * @param isLoading Shows loading indicator in confirm button
 */
@Composable
fun NearAiAlertDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    confirmText: String? = null,
    dismissText: String? = null,
    type: NearAiAlertType = NearAiAlertType.ErrorType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = onConfirm,
    isLoading: Boolean = false,
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Dialog card with shadow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                )
                .background(SurfaceLight, RoundedCornerShape(24.dp)),
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                AlertIcon(type)
                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.2).sp,
                    lineHeight = 26.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Message
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondaryLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                )
                Spacer(modifier = Modifier.height(28.dp))

                // Buttons
                if (dismissText != null) {
                    // Two-button layout: Dismiss + Confirm
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextSecondaryLight,
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                width = androidx.compose.ui.unit.Dp.Hairline,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                        ) {
                            Text(
                                text = dismissText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = type.accentColor(),
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp,
                                focusedElevation = 4.dp,
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                        ) {
                            if (isLoading) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = confirmText ?: "OK",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                } else {
                    // Single button layout — full width
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = type.accentColor(),
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp,
                            focusedElevation = 4.dp,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    ) {
                        if (isLoading) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = confirmText ?: "OK",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// ConfirmationDialog — Legacy wrapper for backward compatibility
// =============================================================================

/**
 * Confirmation dialog for sensitive administrative actions.
 * Delegates to [NearAiAlertDialog] for consistent styling.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val alertType = if (isDestructive) NearAiAlertType.WarningType else NearAiAlertType.NoneType
    NearAiAlertDialog(
        isVisible = true,
        title = title,
        message = message,
        confirmText = confirmText,
        dismissText = dismissText,
        type = alertType,
        onConfirm = {
            onConfirm()
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true, widthDp = 390)
@Composable
fun ConfirmationDialogPreview() {
    NearAiAdminTheme {
        ConfirmationDialog(
            title = "Suspend Account",
            message = "Are you sure you want to suspend this user account? This action will require re-authentication.",
            confirmText = "Suspend",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = { },
            onDismiss = { }
        )
    }
}

@Preview(name = "Error Dialog", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewNearAiAlertDialogError() {
    NearAiAdminTheme {
        Box(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
            NearAiAlertDialog(
                isVisible = true,
                title = "Connection Error",
                message = "Unable to reach the server. Please check your internet connection and try again.",
                confirmText = "Try Again",
                type = NearAiAlertType.ErrorType,
                onConfirm = {},
            )
        }
    }
}

@Preview(name = "Warning Dialog", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewNearAiAlertDialogWarning() {
    NearAiAdminTheme {
        Box(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
            NearAiAlertDialog(
                isVisible = true,
                title = "Cancel Order",
                message = "Are you sure you want to cancel this order? This action cannot be undone.",
                confirmText = "Cancel Order",
                dismissText = "Keep Order",
                type = NearAiAlertType.WarningType,
                onConfirm = {},
            )
        }
    }
}

@Preview(name = "Success Dialog", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewNearAiAlertDialogSuccess() {
    NearAiAdminTheme {
        Box(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
            NearAiAlertDialog(
                isVisible = true,
                title = "Order Approved",
                message = "The order has been approved successfully and is now ready for fulfillment.",
                confirmText = "OK",
                type = NearAiAlertType.SuccessType,
                onConfirm = {},
            )
        }
    }
}

@Preview(name = "Info Dialog", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewNearAiAlertDialogInfo() {
    NearAiAdminTheme {
        Box(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
            NearAiAlertDialog(
                isVisible = true,
                title = "Update Available",
                message = "A new version of the admin portal is available with improvements and bug fixes.",
                confirmText = "Update Now",
                dismissText = "Later",
                type = NearAiAlertType.InfoType,
                onConfirm = {},
            )
        }
    }
}

@Preview(name = "None Dialog", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewNearAiAlertDialogNone() {
    NearAiAdminTheme {
        Box(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
            NearAiAlertDialog(
                isVisible = true,
                title = "Sign Out",
                message = "Are you sure you want to sign out? Your session data will be cleared from this device.",
                confirmText = "Sign Out",
                dismissText = "Cancel",
                type = NearAiAlertType.NoneType,
                onConfirm = {},
            )
        }
    }
}
