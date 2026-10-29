package com.example.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.BiometricFormulas
import com.example.model.BmiCategory
import com.example.model.BmiReading
import com.example.model.DateWindowFilter
import com.example.model.HistorySortOrder
import com.example.model.MeasurementTag
import com.example.model.UnitSystem
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.BmiNormalOptimalColor
import com.example.ui.theme.BmiOverweightColor
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.ui.theme.LocalExpressiveMotion
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class JournalViewMode(val label: String) {
    TIMELINE_CARDS("Timeline Cards"),
    COMPACT_LEDGER("Compact Table"),
    PRIORITY_REORDER("Drag Priority")
}

private val SharedTimelineDateFormatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)
private val SharedShortDateFormatter = SimpleDateFormat("MMM d", Locale.US)
private val SharedDiffDateFormatter = SimpleDateFormat("MMM d, yyyy", Locale.US)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HistoryJournalScreen(
    uiState: UiState,
    listState: LazyListState,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val triggerHaptic = rememberHapticTrigger(uiState.hapticsEnabled)
    val isMetric = uiState.unitSystem == UnitSystem.METRIC

    var viewMode by rememberSaveable { mutableStateOf(JournalViewMode.TIMELINE_CARDS) }
    var showSortDropdown by remember { mutableStateOf(false) }
    var showBatchTagMenu by remember { mutableStateOf(false) }

    val inSelectionMode = uiState.selectedReadingIds.isNotEmpty()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("history_journal_screen")
    ) {
        // Feature #71 & #72: Contextual Batch Action Bar when items are selected via long-press
        item(key = "contextual_batch_action_bar") {
            AnimatedVisibility(visible = inSelectionMode) {
                ElevatedCard(
                    shape = ExpressiveCornerTokens.CardExpressive,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_action_bar")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { onIntent(UiIntent.ClearReadingSelection) }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear selection")
                                }
                                Text(
                                    text = "${uiState.selectedReadingIds.size} Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Feature #73: Compare 2 Selected Readings Side-by-Side
                                if (uiState.selectedReadingIds.size == 2) {
                                    Button(
                                        onClick = { onIntent(UiIntent.ToggleComparisonSheet(true)) },
                                        shape = ExpressiveCornerTokens.Pill,
                                        modifier = Modifier.testTag("compare_selected_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CompareArrows,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Compare 2")
                                    }
                                }

                                Box {
                                    FilledTonalIconButton(
                                        onClick = { showBatchTagMenu = true }
                                    ) {
                                        Icon(Icons.Default.Label, contentDescription = "Batch tag selected")
                                    }
                                    DropdownMenu(
                                        expanded = showBatchTagMenu,
                                        onDismissRequest = { showBatchTagMenu = false }
                                    ) {
                                        MeasurementTag.entries.forEach { tag ->
                                            DropdownMenuItem(
                                                text = { Text("Add ${tag.label}") },
                                                onClick = {
                                                    onIntent(UiIntent.BatchTagSelectedReadings(tag))
                                                    showBatchTagMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                FilledTonalIconButton(
                                    onClick = { onIntent(UiIntent.BatchDeleteSelectedReadings) },
                                    modifier = Modifier.testTag("batch_delete_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Batch delete selected",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Multi-View Segmented Mode Switcher, Search & Date Window Filter (#67, #68, #69, #70)
        item(key = "history_discovery_controls_card") {
            ElevatedCard(
                shape = ExpressiveCornerTokens.CardExpressive,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Segmented Button Row for Multi-View Mode Switching
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_view_mode_selector")
                    ) {
                        JournalViewMode.entries.forEachIndexed { idx, mode ->
                            SegmentedButton(
                                selected = viewMode == mode,
                                onClick = {
                                    triggerHaptic()
                                    viewMode = mode
                                    if (mode == JournalViewMode.PRIORITY_REORDER) {
                                        onIntent(UiIntent.SetHistorySortOrder(HistorySortOrder.CUSTOM_ORDER))
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = JournalViewMode.entries.size
                                )
                            ) {
                                Text(mode.label, maxLines = 1)
                            }
                        }
                    }

                    // Feature #69: Date Range Window Filter
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_date_window_selector")
                    ) {
                        DateWindowFilter.entries.forEachIndexed { idx, window ->
                            SegmentedButton(
                                selected = uiState.dateWindowFilter == window,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetDateWindowFilter(window))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = DateWindowFilter.entries.size
                                )
                            ) {
                                Text(window.label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // Feature #67: Search & Sort Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { onIntent(UiIntent.UpdateSearchQuery(it)) },
                            placeholder = { Text("Search notes, tags, BMI...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onIntent(UiIntent.UpdateSearchQuery("")) }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = ExpressiveCornerTokens.Pill,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("journal_search_input")
                        )

                        Box {
                            FilledTonalIconButton(
                                onClick = { showSortDropdown = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort entries")
                            }
                            DropdownMenu(
                                expanded = showSortDropdown,
                                onDismissRequest = { showSortDropdown = false }
                            ) {
                                HistorySortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.label) },
                                        onClick = {
                                            onIntent(UiIntent.SetHistorySortOrder(order))
                                            showSortDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Feature #60 & #68: Contextual FilterChip & Active InputChip Bar with Remove Animation
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.pinnedOnlyFilter,
                            onClick = { onIntent(UiIntent.SetPinnedOnlyFilter(!uiState.pinnedOnlyFilter)) },
                            label = { Text("Pinned") }
                        )
                        MeasurementTag.entries.forEach { tag ->
                            val selected = tag in uiState.selectedTagFilters
                            FilterChip(
                                selected = selected,
                                onClick = { onIntent(UiIntent.ToggleTagFilter(tag)) },
                                label = { Text(tag.label) }
                            )
                        }
                    }

                    // Active Filter InputChips with one-tap removal
                    val hasActiveFilters = uiState.selectedCategoryFilters.isNotEmpty() ||
                        uiState.selectedTagFilters.isNotEmpty() ||
                        uiState.pinnedOnlyFilter ||
                        uiState.searchQuery.isNotBlank()

                    AnimatedVisibility(visible = hasActiveFilters) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.selectedCategoryFilters.forEach { cat ->
                                InputChip(
                                    selected = true,
                                    onClick = { onIntent(UiIntent.ToggleCategoryFilter(cat)) },
                                    label = { Text(cat.shortLabel) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                    }
                                )
                            }
                            AssistChip(
                                onClick = { onIntent(UiIntent.ClearAllHistoryFilters) },
                                label = { Text("Clear All Filters") }
                            )
                        }
                    }

                    // Quick 2-Entry Diff Inspector Button even without manual long-press
                    if (uiState.filteredReadings.size >= 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tip: Swipe right to pin, swipe left to delete, or long-press to batch select.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalButton(
                                onClick = { onIntent(UiIntent.ToggleComparisonSheet(true)) },
                                modifier = Modifier.testTag("quick_compare_latest_earliest_button")
                            ) {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Diff Inspector")
                            }
                        }
                    }
                }
            }
        }

        // 2. Longitudinal Readings List with Swipe-to-Action, Consecutive Deltas & Drag Reorder
        itemsIndexed(
            items = uiState.filteredReadings,
            key = { _, item -> item.id },
            contentType = { _, _ -> viewMode.name }
        ) { index, reading ->
            val previousChronological = uiState.filteredReadings.getOrNull(index + 1)
            val weightDelta = previousChronological?.let { reading.weightKg - it.weightKg }
            val bmiDelta = previousChronological?.let { reading.bmi - it.bmi }
            val isSelected = reading.id in uiState.selectedReadingIds

            when (viewMode) {
                JournalViewMode.COMPACT_LEDGER -> {
                    CompactLedgerRowItem(
                        reading = reading,
                        weightDelta = weightDelta,
                        isSelected = isSelected,
                        isMetric = isMetric,
                        onClick = {
                            if (inSelectionMode) {
                                onIntent(UiIntent.ToggleReadingSelection(reading.id))
                            } else {
                                onIntent(UiIntent.OpenLogModal(reading))
                            }
                        },
                        onLongClick = {
                            triggerHaptic()
                            onIntent(UiIntent.ToggleReadingSelection(reading.id))
                        }
                    )
                }

                JournalViewMode.PRIORITY_REORDER -> {
                    PriorityReorderReadingCard(
                        reading = reading,
                        index = index,
                        totalCount = uiState.filteredReadings.size,
                        isMetric = isMetric,
                        onMoveUp = {
                            triggerHaptic()
                            onIntent(UiIntent.MoveReadingPriority(reading.id, moveUp = true))
                        },
                        onMoveDown = {
                            triggerHaptic()
                            onIntent(UiIntent.MoveReadingPriority(reading.id, moveUp = false))
                        },
                        onTogglePin = { onIntent(UiIntent.TogglePinReading(reading.id)) }
                    )
                }

                JournalViewMode.TIMELINE_CARDS -> {
                    SwipeableTimelineReadingCard(
                        reading = reading,
                        weightDelta = weightDelta,
                        bmiDelta = bmiDelta,
                        isSelected = isSelected,
                        isMetric = isMetric,
                        onSelectToggle = {
                            triggerHaptic()
                            onIntent(UiIntent.ToggleReadingSelection(reading.id))
                        },
                        onEdit = {
                            if (inSelectionMode) {
                                onIntent(UiIntent.ToggleReadingSelection(reading.id))
                            } else {
                                onIntent(UiIntent.OpenLogModal(reading))
                            }
                        },
                        onPinToggle = {
                            triggerHaptic()
                            onIntent(UiIntent.TogglePinReading(reading.id))
                        },
                        onDuplicate = { onIntent(UiIntent.DuplicateReadingToToday(reading)) },
                        onDelete = {
                            triggerHaptic()
                            onIntent(UiIntent.DeleteReading(reading.id))
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }

    // Feature #73: Side-by-Side 2-Entry Diff Inspector ModalBottomSheet
    if (uiState.isComparisonSheetOpen) {
        val selectedList = uiState.allReadingsForProfile.filter { it.id in uiState.selectedReadingIds }
        val entryA = selectedList.getOrNull(0) ?: uiState.filteredReadings.firstOrNull()
        val entryB = selectedList.getOrNull(1) ?: uiState.filteredReadings.lastOrNull()
        if (entryA != null && entryB != null) {
            TwoEntryDiffComparisonSheet(
                newerEntry = if (entryA.timestampMillis >= entryB.timestampMillis) entryA else entryB,
                olderEntry = if (entryA.timestampMillis >= entryB.timestampMillis) entryB else entryA,
                isMetric = isMetric,
                onDismiss = { onIntent(UiIntent.ToggleComparisonSheet(false)) }
            )
        }
    }
}

/**
 * Features #64, #65, #71, #74: SwipeToDismissBox Card with Pin Snapback, Delete + Undo,
 * Consecutive Delta Badges, and Long-Press Selection.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun SwipeableTimelineReadingCard(
    reading: BmiReading,
    weightDelta: Float?,
    bmiDelta: Float?,
    isSelected: Boolean,
    isMetric: Boolean,
    onSelectToggle: () -> Unit,
    onEdit: () -> Unit,
    onPinToggle: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // Feature #65: Fluid Swipe-to-Pin Milestone without dismissing card
                    onPinToggle()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    // Feature #64: Fluid Swipe-to-Delete with Undo Snackbar
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val bgColor by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiaryContainer
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "swipe_bg_color"
            )
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(ExpressiveCornerTokens.CardCompact)
                    .background(bgColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin milestone",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = if (reading.isPinned) "Unpin" else "Pin Milestone",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Delete Entry",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    ) {
        val weightStr = if (isMetric) {
            "${String.format("%.1f", reading.weightKg)} kg"
        } else {
            "${String.format("%.1f", BiometricFormulas.kgToLbs(reading.weightKg))} lbs"
        }

        OutlinedCard(
            shape = ExpressiveCornerTokens.CardCompact,
            border = BorderStroke(
                width = when {
                    isSelected -> 2.5.dp
                    reading.isPinned -> 1.8.dp
                    else -> 1.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    reading.isPinned -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
            ),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onEdit,
                    onLongClick = onSelectToggle
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(reading.category.color.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = String.format("%.1f", reading.bmi),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = reading.category.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = weightStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                // Feature #74: Consecutive Entry Delta Badge
                                if (weightDelta != null && abs(weightDelta) >= 0.05f) {
                                    val deltaColor = if (weightDelta <= 0f) BmiNormalOptimalColor else BmiOverweightColor
                                    Surface(
                                        shape = ExpressiveCornerTokens.Pill,
                                        color = deltaColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${String.format("%+.1f", weightDelta)} kg",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = deltaColor,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${SharedTimelineDateFormatter.format(Date(reading.timestampMillis))} • Waist ${String.format("%.0f", reading.waistCm)} cm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onPinToggle) {
                            Icon(
                                imageVector = if (reading.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin milestone",
                                tint = if (reading.isPinned) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        IconButton(onClick = onDuplicate) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate to today")
                        }
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }

                if (reading.note.isNotBlank()) {
                    Text(
                        text = reading.note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = ExpressiveCornerTokens.Pill,
                        color = reading.category.color.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = reading.category.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = reading.category.color,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    reading.tags.forEach { tag ->
                        Surface(
                            shape = ExpressiveCornerTokens.Pill,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = tag.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactLedgerRowItem(
    reading: BmiReading,
    weightDelta: Float?,
    isSelected: Boolean,
    isMetric: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val wText = if (isMetric) {
        "${String.format("%.1f", reading.weightKg)} kg"
    } else {
        "${String.format("%.1f", BiometricFormulas.kgToLbs(reading.weightKg))} lb"
    }

    Surface(
        shape = ExpressiveCornerTokens.CardCompact,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SharedShortDateFormatter.format(Date(reading.timestampMillis)),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(64.dp)
            )
            Text(
                text = wText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "BMI ${String.format("%.1f", reading.bmi)}",
                style = MaterialTheme.typography.titleSmall,
                color = reading.category.color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (weightDelta != null) String.format("%+.1f kg", weightDelta) else "Baseline",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Inline Priority Reordering Card with physics-based drag offset spring feedback.
 */
@Composable
private fun PriorityReorderReadingCard(
    reading: BmiReading,
    index: Int,
    totalCount: Int,
    isMetric: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onTogglePin: () -> Unit
) {
    val motion = LocalExpressiveMotion.current
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = motion.floatBouncy(),
        label = "reorder_spring_offset"
    )

    Card(
        shape = ExpressiveCornerTokens.CardCompact,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            .pointerInput(reading.id) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY = (dragOffsetY + dragAmount).coerceIn(-68f, 68f)
                        if (dragOffsetY < -48f) {
                            onMoveUp()
                            dragOffsetY = 0f
                        } else if (dragOffsetY > 48f) {
                            onMoveDown()
                            dragOffsetY = 0f
                        }
                    },
                    onDragEnd = { dragOffsetY = 0f },
                    onDragCancel = { dragOffsetY = 0f }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = "Drag to reorder",
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "#${index + 1} • BMI ${String.format("%.1f", reading.bmi)} (${if (isMetric) "${String.format("%.1f", reading.weightKg)} kg" else "${String.format("%.1f", BiometricFormulas.kgToLbs(reading.weightKg))} lbs"})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (reading.note.isNotBlank()) {
                        Text(
                            text = reading.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move up", modifier = Modifier.size(16.dp))
                }
                FilledTonalIconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move down", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = if (reading.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Toggle pin"
                    )
                }
            }
        }
    }
}

/**
 * Feature #73: Side-by-Side 2-Entry Diff Inspector ModalBottomSheet
 * Uses rememberModalBottomSheetState(skipPartiallyExpanded = false)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoEntryDiffComparisonSheet(
    newerEntry: BmiReading,
    olderEntry: BmiReading,
    isMetric: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val elapsedDays = ((newerEntry.timestampMillis - olderEntry.timestampMillis) / (24L * 3600_000L))
        .coerceAtLeast(1L)
    val weightDiff = newerEntry.weightKg - olderEntry.weightKg
    val bmiDiff = newerEntry.bmi - olderEntry.bmi
    val primeDiff = newerEntry.bmiPrime - olderEntry.bmiPrime
    val waistDiff = newerEntry.waistCm - olderEntry.waistCm
    val bfDiff = newerEntry.bodyFatPct - olderEntry.bodyFatPct

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = ExpressiveCornerTokens.SheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Side-by-Side Biometric Diff Inspector",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Comparing ${SharedDiffDateFormatter.format(Date(olderEntry.timestampMillis))} → ${SharedDiffDateFormatter.format(Date(newerEntry.timestampMillis))} ($elapsedDays days elapsed)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val diffs = listOf(
                Triple(
                    "Weight Change",
                    "${String.format("%.1f", olderEntry.weightKg)} → ${String.format("%.1f", newerEntry.weightKg)} kg",
                    "${String.format("%+.2f", weightDiff)} kg"
                ),
                Triple(
                    "BMI Change",
                    "${String.format("%.1f", olderEntry.bmi)} → ${String.format("%.1f", newerEntry.bmi)}",
                    "${String.format("%+.2f", bmiDiff)} kg/m²"
                ),
                Triple(
                    "BMI Prime Δ",
                    "${String.format("%.2f", olderEntry.bmiPrime)} → ${String.format("%.2f", newerEntry.bmiPrime)}",
                    String.format("%+.2f", primeDiff)
                ),
                Triple(
                    "Waist Circumference Δ",
                    "${String.format("%.1f", olderEntry.waistCm)} → ${String.format("%.1f", newerEntry.waistCm)} cm",
                    "${String.format("%+.1f", waistDiff)} cm"
                ),
                Triple(
                    "Estimated Body Fat % Δ",
                    "${String.format("%.1f", olderEntry.bodyFatPct)}% → ${String.format("%.1f", newerEntry.bodyFatPct)}%",
                    "${String.format("%+.1f", bfDiff)}%"
                )
            )

            diffs.forEach { (metric, span, delta) ->
                Surface(
                    shape = ExpressiveCornerTokens.CardCompact,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(metric, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = span,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = ExpressiveCornerTokens.Pill,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = delta,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onDismiss, shape = ExpressiveCornerTokens.Pill) {
                    Text("Close Diff")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
