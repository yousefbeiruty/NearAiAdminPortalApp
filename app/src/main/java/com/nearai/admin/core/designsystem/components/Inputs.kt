package com.nearai.admin.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nearai.admin.core.designsystem.theme.*

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
    LazyRow(
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
 * Preview for SearchBar.
 */
@Preview(showBackground = true, widthDp = 390)
@Composable
fun SearchBarPreview() {
    NearAiAdminTheme {
        Box(modifier = Modifier.padding(16.dp)) {
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
