package com.example.ui.analytics

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BiometricFormulas
import com.example.model.BmiCategory
import com.example.model.BmiReading
import com.example.model.MeasurementTag
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.BmiMildThinnessColor
import com.example.ui.theme.BmiNormalOptimalColor
import com.example.ui.theme.BmiObeseClass1Color
import com.example.ui.theme.BmiOverweightColor
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsStudioScreen(
    uiState: UiState,
    listState: LazyListState,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val triggerHaptic = rememberHapticTrigger(uiState.hapticsEnabled)
    val chronologicalReadings = remember(uiState.allReadingsForProfile) {
        uiState.allReadingsForProfile.sortedBy { it.timestampMillis }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_studio_screen")
    ) {
        // 1. Longitudinal Trend Chart with Touch Scrubbing & 7-Reading Moving Average (#80, #81, #82)
        item(key = "longitudinal_canvas_trend_card") {
            ElevatedCard(
                shape = ExpressiveCornerTokens.HeroContainer,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Longitudinal Trajectory",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        // Feature #82: 7-Reading Moving Average Smoothing Overlay Toggle
                        FilterChip(
                            selected = uiState.showMovingAverageOverlay,
                            onClick = {
                                triggerHaptic()
                                onIntent(UiIntent.SetShowMovingAverageOverlay(!uiState.showMovingAverageOverlay))
                            },
                            label = { Text("Moving Avg") },
                            modifier = Modifier.testTag("moving_average_toggle_chip")
                        )
                    }

                    // Metric Switcher ("BMI", "Weight", "Body Fat %", "Waist")
                    val metrics = listOf("BMI", "Weight", "Body Fat %", "Waist")
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analytics_metric_selector")
                    ) {
                        metrics.forEachIndexed { idx, metric ->
                            SegmentedButton(
                                selected = uiState.selectedAnalyticsMetric == metric,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetSelectedAnalyticsMetric(metric))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = metrics.size
                                )
                            ) {
                                Text(metric, maxLines = 1)
                            }
                        }
                    }

                    // Feature #80 & #81: Interactive Touch-Scrubbing Canvas Chart
                    InteractiveTrendCanvasChart(
                        readings = chronologicalReadings,
                        selectedMetric = uiState.selectedAnalyticsMetric,
                        showMovingAverage = uiState.showMovingAverageOverlay,
                        scrubbedIndex = uiState.scrubbedReadingIndex,
                        onScrubIndex = { idx ->
                            onIntent(UiIntent.ScrubChartIndex(idx))
                        }
                    )
                }
            }
        }

        // 2. Weekly & Monthly Velocity Bento Analytics Cards (#85)
        item(key = "velocity_analytics_bento_grid") {
            val latest = chronologicalReadings.lastOrNull()
            val earliest = chronologicalReadings.firstOrNull()
            val totalWeightChange = if (latest != null && earliest != null) {
                latest.weightKg - earliest.weightKg
            } else 0f
            val totalBmiChange = if (latest != null && earliest != null) {
                latest.bmi - earliest.bmi
            } else 0f
            val daysSpan = if (latest != null && earliest != null) {
                ((latest.timestampMillis - earliest.timestampMillis) / (24f * 3600_000f)).coerceAtLeast(1f)
            } else 7f
            val weeklyRate = (totalWeightChange / daysSpan) * 7f

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VelocityStatCard(
                        label = "Total Weight Δ",
                        value = "${String.format("%+.1f", totalWeightChange)} kg",
                        subtitle = "Across ${daysSpan.roundToInt()} tracked days",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    VelocityStatCard(
                        label = "Weekly Velocity",
                        value = "${String.format("%+.2f", weeklyRate)} kg/wk",
                        subtitle = "Mean longitudinal pace",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VelocityStatCard(
                        label = "Net BMI Shift",
                        value = "${String.format("%+.2f", totalBmiChange)} kg/m²",
                        subtitle = "Prime ${String.format("%.2f", latest?.bmiPrime ?: uiState.bmiPrime)}",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    VelocityStatCard(
                        label = "Logged Consistency",
                        value = "${chronologicalReadings.size} Snapshots",
                        subtitle = "${chronologicalReadings.count { it.isPinned }} pinned milestones",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        // 3. Category Time-in-Zone Distribution Stacked Bar (#84)
        item(key = "category_distribution_card") {
            Card(
                shape = ExpressiveCornerTokens.CardExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Time-in-Zone Category Breakdown",
                        style = MaterialTheme.typography.titleLarge
                    )
                    val total = chronologicalReadings.size.coerceAtLeast(1)
                    val grouped = chronologicalReadings.groupingBy { it.category }.eachCount()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(ExpressiveCornerTokens.Pill)
                    ) {
                        grouped.entries.forEach { (cat, count) ->
                            Box(
                                modifier = Modifier
                                    .weight((count.toFloat() / total).coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .background(cat.color)
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        grouped.entries.forEach { (cat, count) ->
                            val pct = ((count.toFloat() / total) * 100f).roundToInt()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(cat.color)
                                )
                                Text(
                                    text = "${cat.shortLabel}: $pct% ($count)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Interactive 2D Height × Weight BMI Nomogram Matrix (#83)
        item(key = "interactive_nomogram_card") {
            InteractiveBmiNomogramCard(uiState = uiState)
        }

        // 5. Diurnal & Context Tag Breakdown (#87)
        item(key = "diurnal_context_comparison_card") {
            OutlinedCard(
                shape = ExpressiveCornerTokens.CardExpressive,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Diurnal & Context Condition Breakdown",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Text(
                        text = "Mean weight & BMI segmented by measurement condition tag:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    MeasurementTag.entries.forEach { tag ->
                        val matching = chronologicalReadings.filter { tag in it.tags }
                        if (matching.isNotEmpty()) {
                            val meanWeight = matching.map { it.weightKg }.average().toFloat()
                            val meanBmi = matching.map { it.bmi }.average().toFloat()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${tag.label} (${matching.size} logs)",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "${String.format("%.1f", meanWeight)} kg • BMI ${String.format("%.1f", meanBmi)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { (meanBmi / 35f).coerceIn(0.2f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(ExpressiveCornerTokens.Pill)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Personal Records & Milestone Badges Shelf (#86) & Clinical Report Generator (#96)
        item(key = "milestone_badges_and_report_card") {
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Biometric Milestone Badges",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    val badges = listOf(
                        Triple("First Snapshot", "Logged baseline BMI", chronologicalReadings.isNotEmpty()),
                        Triple("Optimal BMI Zone", "Reached Normal BMI corridor", uiState.currentCategory == BmiCategory.NORMAL),
                        Triple("Waist < 0.5x Height", "Healthy central WHtR", uiState.waistToHeightRatio < 0.50f),
                        Triple("Longitudinal Pro", "10+ historical entries", chronologicalReadings.size >= 10),
                        Triple("Pinned Milestone", "Bookmarked key progress", chronologicalReadings.any { it.isPinned })
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        badges.forEach { (title, desc, unlocked) ->
                            Surface(
                                shape = ExpressiveCornerTokens.CardCompact,
                                color = if (unlocked) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                },
                                modifier = Modifier
                                    .width(164.dp)
                                    .fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (unlocked) "UNLOCKED" else "IN PROGRESS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (unlocked) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // Feature #96: Generate Shareable Clinical Summary Report Card
                    Button(
                        onClick = { onIntent(UiIntent.ToggleReportCardSheet(true)) },
                        shape = ExpressiveCornerTokens.Pill,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_clinical_report_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Clinical Health Report Card")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }

    // Feature #96 & #97: Clinical Summary Report Card ModalBottomSheet with Share & Copy
    if (uiState.isReportCardSheetOpen) {
        ClinicalReportCardSheet(
            uiState = uiState,
            onDismiss = { onIntent(UiIntent.ToggleReportCardSheet(false)) }
        )
    }
}

@Composable
private fun InteractiveTrendCanvasChart(
    readings: List<BmiReading>,
    selectedMetric: String,
    showMovingAverage: Boolean,
    scrubbedIndex: Int?,
    onScrubIndex: (Int?) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val dateFormatter = remember { SimpleDateFormat("MMM d", Locale.US) }

    val values = remember(readings, selectedMetric) {
        readings.map { r ->
            when (selectedMetric) {
                "Weight" -> r.weightKg
                "Body Fat %" -> r.bodyFatPct
                "Waist" -> r.waistCm
                else -> r.bmi
            }
        }
    }

    val movingAvgValues = remember(values) {
        values.indices.map { i ->
            val windowStart = (i - 2).coerceAtLeast(0)
            val sub = values.subList(windowStart, i + 1)
            sub.average().toFloat()
        }
    }

    val activeIdx = (scrubbedIndex ?: values.lastIndex).coerceIn(0, (values.size - 1).coerceAtLeast(0))
    val inspectedReading = readings.getOrNull(activeIdx)
    val inspectedValue = values.getOrNull(activeIdx) ?: 0f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (inspectedReading != null) {
            Surface(
                shape = ExpressiveCornerTokens.CardCompact,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Scrubbed Point • ${dateFormatter.format(Date(inspectedReading.timestampMillis))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$selectedMetric: ${String.format("%.1f", inspectedValue)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "BMI ${String.format("%.1f", inspectedReading.bmi)} (${inspectedReading.category.shortLabel})",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(ExpressiveCornerTokens.CardCompact)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .pointerInput(values.size) {
                    detectTapGestures { offset ->
                        if (values.isNotEmpty()) {
                            val idx = ((offset.x / size.width) * (values.size - 1))
                                .roundToInt()
                                .coerceIn(0, values.lastIndex)
                            onScrubIndex(idx)
                        }
                    }
                }
                .pointerInput(values.size) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        if (values.isNotEmpty()) {
                            val idx = ((change.position.x / size.width) * (values.size - 1))
                                .roundToInt()
                                .coerceIn(0, values.lastIndex)
                            onScrubIndex(idx)
                        }
                    }
                }
                .padding(16.dp)
                .testTag("longitudinal_trend_canvas")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (values.isEmpty()) return@Canvas
                val minVal = (values.minOrNull() ?: 18f) - 1.5f
                val maxVal = (values.maxOrNull() ?: 28f) + 1.5f
                val span = (maxVal - minVal).coerceAtLeast(1f)

                fun pointFor(index: Int, value: Float): Offset {
                    val x = if (values.size <= 1) size.width / 2f
                    else (index.toFloat() / (values.size - 1).toFloat()) * size.width
                    val y = size.height - ((value - minVal) / span) * size.height
                    return Offset(x, y.coerceIn(0f, size.height))
                }

                // Draw primary path & gradient fill
                val linePath = Path()
                val fillPath = Path()
                values.forEachIndexed { idx, v ->
                    val pt = pointFor(idx, v)
                    if (idx == 0) {
                        linePath.moveTo(pt.x, pt.y)
                        fillPath.moveTo(pt.x, size.height)
                        fillPath.lineTo(pt.x, pt.y)
                    } else {
                        linePath.lineTo(pt.x, pt.y)
                        fillPath.lineTo(pt.x, pt.y)
                    }
                    if (idx == values.lastIndex) {
                        fillPath.lineTo(pt.x, size.height)
                        fillPath.close()
                    }
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.28f), Color.Transparent)
                    )
                )
                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw Moving Average Overlay if enabled
                if (showMovingAverage && movingAvgValues.size > 1) {
                    val maPath = Path()
                    movingAvgValues.forEachIndexed { idx, v ->
                        val pt = pointFor(idx, v)
                        if (idx == 0) maPath.moveTo(pt.x, pt.y) else maPath.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = maPath,
                        color = tertiaryColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                        )
                    )
                }

                // Draw data points & active scrub crosshair
                values.forEachIndexed { idx, v ->
                    val pt = pointFor(idx, v)
                    val isScrubbed = idx == activeIdx
                    if (isScrubbed) {
                        drawLine(
                            color = primaryColor.copy(alpha = 0.45f),
                            start = Offset(pt.x, 0f),
                            end = Offset(pt.x, size.height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 7.dp.toPx(),
                            center = pt
                        )
                    } else {
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }
    }
}

/**
 * Feature #83: Interactive 2D Height × Weight BMI Nomogram Matrix
 */
@Composable
private fun InteractiveBmiNomogramCard(uiState: UiState) {
    var inspectedHeightCm by remember(uiState.heightCm) { mutableStateOf(uiState.heightCm) }
    var inspectedWeightKg by remember(uiState.weightKg) { mutableStateOf(uiState.weightKg) }
    val inspectedBmi = BiometricFormulas.calculateStandardBmi(inspectedHeightCm, inspectedWeightKg)
    val inspectedCat = BiometricFormulas.classifyBmi(inspectedBmi, uiState.classificationStandard)

    Card(
        shape = ExpressiveCornerTokens.CardExpressive,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "2D Height × Weight BMI Nomogram",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Tap anywhere on the 2D coordinate grid (140–210 cm × 40–140 kg) to inspect BMI iso-zones:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(ExpressiveCornerTokens.CardCompact)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val xNorm = (offset.x / size.width).coerceIn(0f, 1f)
                            val yNorm = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                            inspectedHeightCm = 140f + xNorm * 70f
                            inspectedWeightKg = 40f + yNorm * 100f
                        }
                    }
                    .testTag("nomogram_2d_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cols = 24
                    val rows = 16
                    val cellW = size.width / cols
                    val cellH = size.height / rows

                    for (c in 0 until cols) {
                        for (r in 0 until rows) {
                            val hCm = 140f + ((c + 0.5f) / cols) * 70f
                            val wKg = 140f - ((r + 0.5f) / rows) * 100f
                            val cellBmi = BiometricFormulas.calculateStandardBmi(hCm, wKg)
                            val cellColor = when {
                                cellBmi < 18.5f -> BmiMildThinnessColor.copy(alpha = 0.28f)
                                cellBmi <= 24.9f -> BmiNormalOptimalColor.copy(alpha = 0.28f)
                                cellBmi <= 29.9f -> BmiOverweightColor.copy(alpha = 0.28f)
                                else -> BmiObeseClass1Color.copy(alpha = 0.28f)
                            }
                            drawRect(
                                color = cellColor,
                                topLeft = Offset(c * cellW, r * cellH),
                                size = Size(cellW + 1f, cellH + 1f)
                            )
                        }
                    }

                    // Plot User's Current Coordinate
                    val userX = ((uiState.heightCm - 140f) / 70f).coerceIn(0.03f, 0.97f) * size.width
                    val userY = (1f - ((uiState.weightKg - 40f) / 100f).coerceIn(0.03f, 0.97f)) * size.height
                    drawCircle(color = Color.White, radius = 9.dp.toPx(), center = Offset(userX, userY))
                    drawCircle(color = Color(0xFF0D2622), radius = 5.dp.toPx(), center = Offset(userX, userY))
                }
            }

            Surface(
                shape = ExpressiveCornerTokens.CardCompact,
                color = inspectedCat.color.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Coordinate: ${inspectedHeightCm.roundToInt()} cm • ${String.format("%.1f", inspectedWeightKg)} kg",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "BMI ${String.format("%.1f", inspectedBmi)} (${inspectedCat.shortLabel})",
                        style = MaterialTheme.typography.titleSmall,
                        color = inspectedCat.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun VelocityStatCard(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = ExpressiveCornerTokens.CardCompact,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Features #96 & #97: Formatted Clinical Summary Report Card Generator & Share Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicalReportCardSheet(
    uiState: UiState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val reportSummaryText = remember(uiState) {
        buildString {
            appendLine("=== BIOMETRIC CLINICAL SUMMARY REPORT ===")
            appendLine("Profile: ${uiState.activeProfile?.name ?: "Primary"} (${uiState.ageYears} yrs, ${uiState.biologicalSex.label})")
            appendLine("Reference Standard: ${uiState.classificationStandard.label}")
            appendLine("Stature & Mass: ${String.format("%.1f", uiState.heightCm)} cm | ${String.format("%.1f", uiState.weightKg)} kg")
            appendLine("Standard BMI: ${String.format("%.1f", uiState.standardBmi)} kg/m² (${uiState.currentCategory.label})")
            appendLine("BMI Prime: ${String.format("%.2f", uiState.bmiPrime)} | New Oxford BMI: ${String.format("%.1f", uiState.newOxfordBmi)}")
            appendLine("Waist-to-Height Ratio (WHtR): ${String.format("%.2f", uiState.waistToHeightRatio)}")
            appendLine("Estimated Body Fat: ${String.format("%.1f", uiState.activeBodyFatPct)}% | Lean Mass: ${String.format("%.1f", uiState.leanBodyMassKg)} kg")
            appendLine("FFMI: ${String.format("%.1f", uiState.ffmi)} kg/m² | Resting BMR: ${uiState.bmrKcal.roundToInt()} kcal/day")
            appendLine("Active Goal: Target BMI ${String.format("%.1f", uiState.targetBmi)} (${String.format("%.1f", uiState.targetWeightKg)} kg)")
        }
    }

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
                text = "Clinical Biometric Summary Report",
                style = MaterialTheme.typography.headlineSmall
            )
            Surface(
                shape = ExpressiveCornerTokens.CardCompact,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reportSummaryText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(reportSummaryText))
                    },
                    shape = ExpressiveCornerTokens.Pill,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Report")
                }

                // Feature #97: System Share Sheet Integration (ACTION_SEND)
                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Biometric BMI & Body Composition Report")
                            putExtra(Intent.EXTRA_TEXT, reportSummaryText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Clinical Summary"))
                    },
                    shape = ExpressiveCornerTokens.Pill,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Sheet")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
