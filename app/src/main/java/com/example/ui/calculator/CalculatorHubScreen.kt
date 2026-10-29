package com.example.ui.calculator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BiologicalSex
import com.example.model.BiometricFormulas
import com.example.model.BmiCategory
import com.example.model.BmiReading
import com.example.model.BodyFrameSize
import com.example.model.ClassificationStandard
import com.example.model.HistorySortOrder
import com.example.model.UnitSystem
import com.example.ui.components.expressivePressScale
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.ui.theme.LocalExpressiveMotion
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorHubScreen(
    uiState: UiState,
    listState: LazyListState,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = LocalExpressiveMotion.current
    val triggerHaptic = rememberHapticTrigger(uiState.hapticsEnabled)

    var showNumericInputSheet by remember { mutableStateOf(false) }
    var showClinicalModifiersSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val animatedBmi by animateFloatAsState(
        targetValue = uiState.standardBmi,
        animationSpec = motion.floatBouncy(),
        label = "hub_animated_bmi"
    )
    val categoryColor by animateColorAsState(
        targetValue = uiState.currentCategory.color,
        label = "hub_category_color"
    )

    val isMetric = uiState.unitSystem == UnitSystem.METRIC

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("calculator_hub_screen")
    ) {
        // 1. Expressive Hero BMI Readout & Interactive Spectrum Gauge (#29 - #35, #38, #39, #44)
        item(key = "hero_bmi_readout_card") {
            ElevatedCard(
                shape = ExpressiveCornerTokens.HeroContainer,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .expressivePressScale(
                        hapticsEnabled = uiState.hapticsEnabled,
                        onClick = { onIntent(UiIntent.ToggleCategoryGuidanceSheet(true)) }
                    )
                    .testTag("hero_bmi_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Feature #32: WHO Global vs Asian-Pacific vs Senior 65+ Threshold Switcher
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("standard_selector_row")
                    ) {
                        ClassificationStandard.entries.forEachIndexed { index, std ->
                            SegmentedButton(
                                selected = uiState.classificationStandard == std,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetClassificationStandard(std))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = ClassificationStandard.entries.size
                                )
                            ) {
                                Text(std.shortLabel, maxLines = 1)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Body Mass Index (kg/m²)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f", animatedBmi),
                                style = MaterialTheme.typography.displayLarge,
                                color = categoryColor,
                                modifier = Modifier.testTag("hero_bmi_value_text")
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            // Feature #31: 8-Tier Detailed WHO Category Classifier Badge
                            Surface(
                                shape = ExpressiveCornerTokens.Pill,
                                color = categoryColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = uiState.currentCategory.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = categoryColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Feature #39: Target Threshold Distance Indicator
                            val deltaKg = uiState.weightDeltaToHealthyKg
                            val deltaLabel = if (abs(deltaKg) < 0.05f) {
                                "Within Optimal Corridor"
                            } else {
                                val formatted = if (isMetric) {
                                    "${String.format("%+.1f", deltaKg)} kg"
                                } else {
                                    "${String.format("%+.1f", BiometricFormulas.kgToLbs(deltaKg))} lbs"
                                }
                                "$formatted to Optimal"
                            }
                            Text(
                                text = deltaLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (abs(deltaKg) < 0.05f) {
                                    BmiCategory.NORMAL.color
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Feature #30: Interactive Multi-Zone BMI Spectrum Gauge with Tap Inspection
                    InteractiveBmiSpectrumGauge(
                        bmi = animatedBmi,
                        inspectedCategory = uiState.inspectedGaugeCategory,
                        onSelectCategory = { cat ->
                            triggerHaptic()
                            onIntent(UiIntent.InspectGaugeCategory(cat))
                        }
                    )

                    // Feature #38: Healthy Weight Range Span & Feature #35: BMI Prime Ratio
                    val minW = if (isMetric) {
                        "${String.format("%.1f", uiState.healthyWeightMinKg)} kg"
                    } else {
                        "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.healthyWeightMinKg))} lbs"
                    }
                    val maxW = if (isMetric) {
                        "${String.format("%.1f", uiState.healthyWeightMaxKg)} kg"
                    } else {
                        "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.healthyWeightMaxKg))} lbs"
                    }

                    Surface(
                        shape = ExpressiveCornerTokens.CardCompact,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
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
                                Text(
                                    text = "Healthy Weight Corridor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$minW – $maxW",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "BMI Prime Ratio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.2f", uiState.bmiPrime)} (${String.format("%+.0f%%", (uiState.bmiPrime - 1f) * 100f)})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Action Buttons Row (#42 Guidance Drawer & #44 Quick Log Snapshot)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onIntent(UiIntent.ToggleCategoryGuidanceSheet(true)) },
                            shape = ExpressiveCornerTokens.Pill,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_category_guidance_button")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clinical Guidance")
                        }
                        Button(
                            onClick = { onIntent(UiIntent.OpenLogModal(null)) },
                            shape = ExpressiveCornerTokens.Pill,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hero_log_snapshot_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Snapshot")
                        }
                    }
                }
            }
        }

        // Feature #33 & #34: Life-Stage Contextual Banners (Pediatric Percentile or Senior 65+ Protective Mode)
        if (uiState.ageYears in 2..19) {
            item(key = "pediatric_percentile_card") {
                OutlinedCard(
                    shape = ExpressiveCornerTokens.CardCompact,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Pediatric & Teen Growth Mode (Age ${uiState.ageYears})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Estimated Age & Sex-Adjusted Percentile: ${uiState.pediatricPercentile}th Percentile",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(
                            progress = { uiState.pediatricPercentile / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }
            }
        } else if (uiState.ageYears >= 65 && uiState.classificationStandard != ClassificationStandard.SENIOR_65_PLUS) {
            item(key = "senior_standard_suggestion_banner") {
                OutlinedCard(
                    shape = ExpressiveCornerTokens.CardCompact,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Senior 65+ Protective Range Available",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clinical geriatric guidelines recommend a 23.0–27.9 BMI protective reserve.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                onIntent(UiIntent.SetClassificationStandard(ClassificationStandard.SENIOR_65_PLUS))
                            }
                        ) {
                            Text("Apply 65+")
                        }
                    }
                }
            }
        }

        // 2. Precision Height, Weight & Unit Workstation (#13 - #21)
        item(key = "precision_input_workstation_card") {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Biometric Dimensions",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { showNumericInputSheet = true },
                                label = { Text("Type Exact") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = "Direct numeric input",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("open_numeric_input_chip")
                            )
                        }
                    }

                    // Feature #13 & #14: Metric / Imperial Unit Synchronizer + Stones Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("calculator_unit_segmented_row")
                        ) {
                            UnitSystem.entries.forEachIndexed { idx, unit ->
                                SegmentedButton(
                                    selected = uiState.unitSystem == unit,
                                    onClick = {
                                        triggerHaptic()
                                        onIntent(UiIntent.SetUnitSystem(unit))
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = idx,
                                        count = UnitSystem.entries.size
                                    )
                                ) {
                                    Text(unit.label)
                                }
                            }
                        }

                        if (!isMetric) {
                            FilterChip(
                                selected = uiState.useStonesInImperial,
                                onClick = {
                                    onIntent(UiIntent.SetUseStones(!uiState.useStonesInImperial))
                                },
                                label = { Text("st + lbs") },
                                modifier = Modifier.testTag("stones_submode_chip")
                            )
                        }
                    }

                    // Feature #15, #16, #17: Tactile Height Control + Micro-Steppers + Imperial Ft/In Steppers
                    val (feet, inches) = BiometricFormulas.cmToFeetInches(uiState.heightCm)
                    val heightReadout = if (isMetric) {
                        "${String.format("%.1f", uiState.heightCm)} cm"
                    } else {
                        "${feet}' ${String.format("%.1f", inches)}\" (${String.format("%.0f", uiState.heightCm)} cm)"
                    }

                    Surface(
                        shape = ExpressiveCornerTokens.CardCompact,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Stature / Height",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = heightReadout,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { showNumericInputSheet = true }
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            triggerHaptic()
                                            onIntent(UiIntent.UpdateHeightCm(uiState.heightCm - 0.5f))
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("height_minus_button")
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease height by 0.5 cm")
                                    }
                                    FilledTonalIconButton(
                                        onClick = {
                                            triggerHaptic()
                                            onIntent(UiIntent.UpdateHeightCm(uiState.heightCm + 0.5f))
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("height_plus_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase height by 0.5 cm")
                                    }
                                }
                            }

                            Slider(
                                value = uiState.heightCm,
                                onValueChange = { onIntent(UiIntent.UpdateHeightCm(it)) },
                                valueRange = 100f..240f,
                                modifier = Modifier.testTag("calculator_height_slider")
                            )

                            // Feature #17: Dual Feet & Inches Interactive Stepper in Imperial Mode
                            AnimatedVisibility(visible = !isMetric) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedCard(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    onIntent(
                                                        UiIntent.UpdateHeightCm(
                                                            BiometricFormulas.feetInchesToCm((feet - 1).coerceAtLeast(3), inches)
                                                        )
                                                    )
                                                }
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Minus 1 foot")
                                            }
                                            Text("${feet} ft", style = MaterialTheme.typography.titleSmall)
                                            IconButton(
                                                onClick = {
                                                    onIntent(
                                                        UiIntent.UpdateHeightCm(
                                                            BiometricFormulas.feetInchesToCm((feet + 1).coerceAtMost(7), inches)
                                                        )
                                                    )
                                                }
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Plus 1 foot")
                                            }
                                        }
                                    }
                                    OutlinedCard(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    onIntent(
                                                        UiIntent.UpdateHeightCm(
                                                            BiometricFormulas.feetInchesToCm(feet, (inches - 0.5f).coerceAtLeast(0f))
                                                        )
                                                    )
                                                }
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = "Minus 0.5 inch")
                                            }
                                            Text("${String.format("%.1f", inches)} in", style = MaterialTheme.typography.titleSmall)
                                            IconButton(
                                                onClick = {
                                                    onIntent(
                                                        UiIntent.UpdateHeightCm(
                                                            BiometricFormulas.feetInchesToCm(feet, (inches + 0.5f).coerceAtMost(11.5f))
                                                        )
                                                    )
                                                }
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Plus 0.5 inch")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Feature #18, #19, #21: Tactile Weight Control + Micro-Steppers + Quick Weight Delta Chips
                    val weightReadout = when {
                        isMetric -> "${String.format("%.1f", uiState.weightKg)} kg"
                        uiState.useStonesInImperial -> {
                            val (st, remLbs) = BiometricFormulas.kgToStonesAndLbs(uiState.weightKg)
                            "${st} st ${String.format("%.1f", remLbs)} lbs"
                        }
                        else -> "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.weightKg))} lbs"
                    }

                    Surface(
                        shape = ExpressiveCornerTokens.CardCompact,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Body Mass / Weight",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = weightReadout,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { showNumericInputSheet = true }
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalIconButton(
                                        onClick = {
                                            triggerHaptic()
                                            onIntent(UiIntent.OffsetWeightKg(-0.1f))
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("weight_minus_button")
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease weight by 0.1 kg")
                                    }
                                    FilledTonalIconButton(
                                        onClick = {
                                            triggerHaptic()
                                            onIntent(UiIntent.OffsetWeightKg(0.1f))
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("weight_plus_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase weight by 0.1 kg")
                                    }
                                }
                            }

                            Slider(
                                value = uiState.weightKg,
                                onValueChange = { onIntent(UiIntent.UpdateWeightKg(it)) },
                                valueRange = 25f..250f,
                                modifier = Modifier.testTag("calculator_weight_slider")
                            )

                            // Feature #21: Quick Weight Delta Preset Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(-5.0f, -1.0f, -0.5f, 0.5f, 1.0f, 5.0f).forEach { delta ->
                                    val label = if (delta > 0) "+$delta kg" else "$delta kg"
                                    SuggestionChip(
                                        onClick = {
                                            triggerHaptic()
                                            onIntent(UiIntent.OffsetWeightKg(delta))
                                        },
                                        label = { Text(label) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Age, Biological Sex, Frame Size & Athlete Context (#22 - #27)
        item(key = "demographic_and_frame_modifiers_card") {
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Profile Context & Frame",
                            style = MaterialTheme.typography.titleLarge
                        )
                        AssistChip(
                            onClick = { showClinicalModifiersSheet = true },
                            label = {
                                val badgeCount = uiState.selectedAmputations.size + (if (uiState.isPregnancyMode) 1 else 0)
                                Text(if (badgeCount > 0) "Clinical ($badgeCount)" else "Clinical Modifiers")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("open_clinical_modifiers_chip")
                        )
                    }

                    // Feature #22: Age Selector with Life-Stage Cohort
                    val cohortLabel = when {
                        uiState.ageYears < 20 -> "Pediatric / Teen (2–19)"
                        uiState.ageYears >= 65 -> "Senior 65+ Cohort"
                        else -> "Adult Cohort (20–64)"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Age: ${uiState.ageYears} years", style = MaterialTheme.typography.titleMedium)
                        Surface(
                            shape = ExpressiveCornerTokens.Pill,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = cohortLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Slider(
                        value = uiState.ageYears.toFloat(),
                        onValueChange = { onIntent(UiIntent.UpdateAgeYears(it.toInt())) },
                        valueRange = 2f..100f,
                        modifier = Modifier.testTag("calculator_age_slider")
                    )

                    // Feature #23: Biological Sex Modifier Control
                    Text("Biological Sex (for BMR, Navy Body Fat & IBW)", style = MaterialTheme.typography.labelLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        BiologicalSex.entries.forEachIndexed { idx, sex ->
                            SegmentedButton(
                                selected = uiState.biologicalSex == sex,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetBiologicalSex(sex))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = BiologicalSex.entries.size
                                )
                            ) {
                                Text(sex.label)
                            }
                        }
                    }

                    // Feature #24: Wrist Circumference & Body Frame Estimator
                    Text(
                        text = "Skeletal Frame Size • Wrist: ${String.format("%.1f", uiState.wristCm)} cm",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = uiState.wristCm,
                        onValueChange = { onIntent(UiIntent.UpdateWristCm(it)) },
                        valueRange = 12f..23f,
                        modifier = Modifier.testTag("calculator_wrist_slider")
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        BodyFrameSize.entries.forEachIndexed { idx, frame ->
                            SegmentedButton(
                                selected = uiState.frameSize == frame,
                                onClick = { onIntent(UiIntent.SetBodyFrameSize(frame)) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = BodyFrameSize.entries.size
                                )
                            ) {
                                Text(frame.label)
                            }
                        }
                    }

                    // Feature #27: Athlete / High Muscle Mass Context Toggle
                    Surface(
                        shape = ExpressiveCornerTokens.CardCompact,
                        color = if (uiState.isAthleteMode) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Strength Athlete / High Muscularity Mode",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = if (uiState.isAthleteMode) {
                                            "Active: Prioritizing FFMI (${String.format("%.1f", uiState.ffmi)}) & WHtR (${String.format("%.2f", uiState.waistToHeightRatio)}) over raw BMI"
                                        } else {
                                            "Enable if resistance training elevates lean skeletal mass"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.isAthleteMode,
                                onCheckedChange = { onIntent(UiIntent.SetAthleteMode(it)) },
                                modifier = Modifier.testTag("athlete_mode_switch")
                            )
                        }
                    }
                }
            }
        }

        // 4. Secondary Biometric Indices Bento Grid (#36 New Oxford, #37 Ponderal, #41 BSA)
        item(key = "secondary_indices_bento_grid", contentType = "bento_grid") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoIndexCard(
                        title = "New Oxford BMI",
                        value = String.format("%.1f", uiState.newOxfordBmi),
                        subtitle = "Exponent 2.5 (${String.format("%+.1f", uiState.newOxfordBmi - uiState.standardBmi)} vs WHO)",
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    )
                    BentoIndexCard(
                        title = "Ponderal Index",
                        value = "${String.format("%.2f", uiState.ponderalIndex)} kg/m³",
                        subtitle = "Tri-dimensional corpulence index",
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoIndexCard(
                        title = "Body Surface Area",
                        value = "${String.format("%.2f", uiState.bsaMostellerM2)} m²",
                        subtitle = "Mosteller (${String.format("%.2f", uiState.bsaDuBoisM2)} m² Du Bois)",
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    )
                    BentoIndexCard(
                        title = "Consensus Ideal",
                        value = if (isMetric) {
                            "${String.format("%.1f", uiState.idealWeightBreakdown.consensusMeanKg)} kg"
                        } else {
                            "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.idealWeightBreakdown.consensusMeanKg))} lbs"
                        },
                        subtitle = "4-equation mean (${uiState.frameSize.label})",
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                    )
                }
            }
        }

        // 5. 4-Formula Ideal Body Weight Matrix & All-Category Threshold Table (#40, #43)
        item(key = "ideal_weight_and_thresholds_matrix", contentType = "ideal_matrix") {
            IdealWeightAndThresholdMatrixCard(uiState = uiState)
        }

        // 6. Categorized Snapshots Dashboard with Live Search, Multi-Attribute Sorting & Inline CRUD
        item(key = "inline_snapshots_dashboard_header", contentType = "snapshots_header") {
            Card(
                shape = ExpressiveCornerTokens.CardExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recent Biometric Snapshots",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "${uiState.filteredReadings.size} of ${uiState.allReadingsForProfile.size} entries matching filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box {
                            FilledTonalButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.testTag("hub_sort_button")
                            ) {
                                Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(uiState.sortOrder.label, maxLines = 1, softWrap = false)
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                HistorySortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = { Text(order.label) },
                                        onClick = {
                                            onIntent(UiIntent.SetHistorySortOrder(order))
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Live Search Input
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { onIntent(UiIntent.UpdateSearchQuery(it)) },
                        placeholder = { Text("Search notes, BMI value, or category...") },
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
                            .fillMaxWidth()
                            .testTag("hub_search_input")
                    )

                    // Category & Pin Filter Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = uiState.pinnedOnlyFilter,
                            onClick = { onIntent(UiIntent.SetPinnedOnlyFilter(!uiState.pinnedOnlyFilter)) },
                            label = { Text("Pinned Milestones", maxLines = 1) }
                        )
                        listOf(
                            BmiCategory.MILD_THINNESS,
                            BmiCategory.NORMAL,
                            BmiCategory.OVERWEIGHT,
                            BmiCategory.OBESE_CLASS_I
                        ).forEach { cat ->
                            val selected = cat in uiState.selectedCategoryFilters
                            FilterChip(
                                selected = selected,
                                onClick = { onIntent(UiIntent.ToggleCategoryFilter(cat)) },
                                label = { Text(cat.shortLabel, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        }

        // Snapshot Items with Press Scale, Pin Toggle, Edit, Duplicate & Delete
        items(
            items = uiState.filteredReadings,
            key = { it.id },
            contentType = { "snapshot_reading_card" }
        ) { reading ->
            SnapshotReadingCard(
                reading = reading,
                isMetric = isMetric,
                hapticsEnabled = uiState.hapticsEnabled,
                onTogglePin = { onIntent(UiIntent.TogglePinReading(reading.id)) },
                onEdit = { onIntent(UiIntent.OpenLogModal(reading)) },
                onDuplicate = { onIntent(UiIntent.DuplicateReadingToToday(reading)) },
                onDelete = { onIntent(UiIntent.DeleteReading(reading.id)) }
            )
        }

        item(key = "hub_bottom_spacer", contentType = "spacer") {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    if (showNumericInputSheet) {
        DirectNumericInputSheet(
            uiState = uiState,
            onDismiss = { showNumericInputSheet = false },
            onIntent = onIntent
        )
    }

    if (showClinicalModifiersSheet) {
        ClinicalModifiersSheet(
            uiState = uiState,
            onDismiss = { showClinicalModifiersSheet = false },
            onIntent = onIntent
        )
    }

    if (uiState.isCategoryGuidanceSheetOpen) {
        CategoryHealthGuidanceSheet(
            uiState = uiState,
            onDismiss = {
                onIntent(UiIntent.InspectGaugeCategory(null))
                onIntent(UiIntent.ToggleCategoryGuidanceSheet(false))
            }
        )
    }
}

/**
 * Feature #30: Interactive Multi-Zone BMI Spectrum Gauge with touch inspection
 */
@Composable
private fun InteractiveBmiSpectrumGauge(
    bmi: Float,
    inspectedCategory: BmiCategory?,
    onSelectCategory: (BmiCategory?) -> Unit
) {
    val tiers = listOf(
        BmiCategory.SEVERE_THINNESS,
        BmiCategory.MODERATE_THINNESS,
        BmiCategory.MILD_THINNESS,
        BmiCategory.NORMAL,
        BmiCategory.OVERWEIGHT,
        BmiCategory.OBESE_CLASS_I,
        BmiCategory.OBESE_CLASS_II,
        BmiCategory.OBESE_CLASS_III
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val index = ((offset.x / size.width) * tiers.size)
                            .toInt()
                            .coerceIn(0, tiers.lastIndex)
                        val tapped = tiers[index]
                        onSelectCategory(if (inspectedCategory == tapped) null else tapped)
                    }
                }
                .testTag("interactive_bmi_spectrum_gauge")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val segWidth = size.width / tiers.size
                val barHeight = 16.dp.toPx()
                val barTop = (size.height - barHeight) / 2f

                tiers.forEachIndexed { idx, cat ->
                    val isInspected = inspectedCategory == cat
                    val yOffset = if (isInspected) barTop - 3.dp.toPx() else barTop
                    val h = if (isInspected) barHeight + 6.dp.toPx() else barHeight
                    drawRoundRect(
                        color = cat.color,
                        topLeft = Offset(idx * segWidth + 2.dp.toPx(), yOffset),
                        size = Size((segWidth - 4.dp.toPx()).coerceAtLeast(4f), h),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }

                // Draw Needle Marker for Current BMI (mapped across 13..40 BMI range)
                val normalized = ((bmi - 13f) / (40f - 13f)).coerceIn(0.02f, 0.98f)
                val markerX = normalized * size.width
                drawCircle(
                    color = Color.White,
                    radius = 12.dp.toPx(),
                    center = Offset(markerX, size.height / 2f)
                )
                drawCircle(
                    color = Color(0xFF0D2622),
                    radius = 7.dp.toPx(),
                    center = Offset(markerX, size.height / 2f)
                )
            }
        }

        if (inspectedCategory != null) {
            Surface(
                shape = ExpressiveCornerTokens.CardCompact,
                color = inspectedCategory.color.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inspecting: ${inspectedCategory.label} — ${inspectedCategory.summary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = inspectedCategory.color,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onSelectCategory(null) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear gauge selection",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoIndexCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = ExpressiveCornerTokens.CardCompact,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

private val SharedSnapshotDateFormatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SnapshotReadingCard(
    reading: BmiReading,
    isMetric: Boolean,
    hapticsEnabled: Boolean,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val weightStr = if (isMetric) {
        "${String.format("%.1f", reading.weightKg)} kg"
    } else {
        "${String.format("%.1f", BiometricFormulas.kgToLbs(reading.weightKg))} lbs"
    }
    val formattedDate = remember(reading.timestampMillis) {
        synchronized(SharedSnapshotDateFormatter) {
            SharedSnapshotDateFormatter.format(Date(reading.timestampMillis))
        }
    }

    OutlinedCard(
        shape = ExpressiveCornerTokens.CardCompact,
        border = BorderStroke(
            width = if (reading.isPinned) 1.8.dp else 1.dp,
            color = if (reading.isPinned) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        modifier = Modifier
            .fillMaxWidth()
            .expressivePressScale(
                hapticsEnabled = hapticsEnabled,
                onClick = onEdit
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Enforce Modifier.weight(1f) so long text never squeezes trailing action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(reading.category.color.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format("%.1f", reading.bmi),
                            style = MaterialTheme.typography.titleMedium,
                            color = reading.category.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$weightStr • ${reading.category.shortLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Row {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (reading.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Toggle pin milestone",
                            tint = if (reading.isPinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(onClick = onDuplicate) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate to today"
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit reading"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete reading",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (reading.note.isNotBlank()) {
                Text(
                    text = reading.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (reading.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
