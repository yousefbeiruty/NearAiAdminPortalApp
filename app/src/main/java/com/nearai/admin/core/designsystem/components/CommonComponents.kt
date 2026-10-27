package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.theme.NearAiAdminTheme

/**
 * Displays a colored status dot with a label.
 */
@Composable
fun StatusIndicator(
    status: StatusType,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = when (status) {
            StatusType.Active -> MaterialTheme.colorScheme.primary
            StatusType.Inactive -> MaterialTheme.colorScheme.onSurfaceVariant
            StatusType.Warning -> com.nearai.admin.core.designsystem.theme.Warning
            StatusType.Error -> MaterialTheme.colorScheme.error
        }
        val icon = when (status) {
            StatusType.Active -> Icons.Default.CheckCircle
            StatusType.Inactive -> Icons.Default.Info
            StatusType.Warning -> Icons.Default.WarningAmber
            StatusType.Error -> Icons.Default.Error
        }
        Icon(
            imageVector = icon,
            contentDescription = status.label,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class StatusType(val label: String) {
    Active("Active"),
    Inactive("Inactive"),
    Warning("Warning"),
    Error("Error")
}

/**
 * Section header with a title and optional action button.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Search bar with leading search icon and trailing clear button.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onClear: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty() && onClear != null) {
                TextButton(onClick = onClear) {
                    Text("Clear", color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
        )
    )
}

/**
 * Filter chip row for selecting filter options.
 */
@Composable
fun FilterChips(
    filters: List<FilterChipItem>,
    selectedFilters: Set<String>,
    onFilterSelected: (String) -> Unit,
    onFilterDeselected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(filters.size) { index ->
            val filter = filters[index]
            val isSelected = selectedFilters.contains(filter.id)
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) onFilterDeselected(filter.id)
                    else onFilterSelected(filter.id)
                },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

data class FilterChipItem(
    val id: String,
    val label: String
)

/**
 * Preview for StatusIndicator.
 */
@Preview(showBackground = true, widthDp = 300)
@Composable
fun StatusIndicatorPreview() {
    NearAiAdminTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            StatusIndicator(status = StatusType.Active, label = "Active")
            StatusIndicator(status = StatusType.Warning, label = "Warning")
            StatusIndicator(status = StatusType.Error, label = "Error")
            StatusIndicator(status = StatusType.Inactive, label = "Inactive")
        }
    }
}

/**
 * Preview for SectionHeader.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun SectionHeaderPreview() {
    NearAiAdminTheme {
        SectionHeader(
            title = "Live Activity Stream",
            actionText = "View All",
            onActionClick = { }
        )
    }
}

/**
 * Preview for SearchBar.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun SearchBarPreview() {
    NearAiAdminTheme {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(16.dp)) {
            SearchBar(
                query = "",
                onQueryChanged = { },
                placeholder = "Search users...",
                onClear = { }
            )
        }
    }
}

/**
 * Preview for FilterChips.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun FilterChipsPreview() {
    NearAiAdminTheme {
        FilterChips(
            filters = listOf(
                FilterChipItem("all", "All Users"),
                FilterChipItem("pro", "Pro Tier"),
                FilterChipItem("free", "Free Plan"),
                FilterChipItem("flagged", "Flagged")
            ),
            selectedFilters = setOf("pro"),
            onFilterSelected = { },
            onFilterDeselected = { }
        )
    }
}
