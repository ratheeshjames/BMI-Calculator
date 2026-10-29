package com.example.ui.calculator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.BiometricFormulas
import com.example.model.BmiCategory
import com.example.model.LimbAmputationSegment
import com.example.model.UnitSystem
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState
import kotlin.math.pow

/**
 * Feature #20: Direct Numeric Keyboard Input Sheet for exact decimal height & weight typing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectNumericInputSheet(
    uiState: UiState,
    onDismiss: () -> Unit,
    onIntent: (UiIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val isMetric = uiState.unitSystem == UnitSystem.METRIC

    var heightText by remember(uiState.heightCm, isMetric) {
        val displayVal = if (isMetric) uiState.heightCm else (uiState.heightCm / 2.54f)
        mutableStateOf(String.format("%.1f", displayVal))
    }
    var weightText by remember(uiState.weightKg, isMetric) {
        val displayVal = if (isMetric) uiState.weightKg else BiometricFormulas.kgToLbs(uiState.weightKg)
        mutableStateOf(String.format("%.1f", displayVal))
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = ExpressiveCornerTokens.SheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Precision Numeric Entry",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = if (isMetric) "Enter exact Metric (cm & kg) values" else "Enter exact Imperial (total inches & lbs) values",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = heightText,
                onValueChange = {
                    heightText = it
                    errorMessage = null
                },
                label = { Text(if (isMetric) "Height (100 – 240 cm)" else "Total Height (39 – 94 inches)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = ExpressiveCornerTokens.CardCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("numeric_height_input")
            )

            OutlinedTextField(
                value = weightText,
                onValueChange = {
                    weightText = it
                    errorMessage = null
                },
                label = { Text(if (isMetric) "Weight (25 – 250 kg)" else "Weight (55 – 550 lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = ExpressiveCornerTokens.CardCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("numeric_weight_input")
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val hVal = heightText.toFloatOrNull()
                        val wVal = weightText.toFloatOrNull()
                        if (hVal == null || wVal == null) {
                            errorMessage = "Please enter valid decimal numbers."
                            return@Button
                        }
                        val cm = if (isMetric) hVal else hVal * 2.54f
                        val kg = if (isMetric) wVal else BiometricFormulas.lbsToKg(wVal)
                        if (cm !in 100f..240f || kg !in 25f..250f) {
                            errorMessage = "Values out of supported physiological range."
                            return@Button
                        }
                        onIntent(UiIntent.UpdateHeightCm(cm))
                        onIntent(UiIntent.UpdateWeightKg(kg))
                        onDismiss()
                    },
                    shape = ExpressiveCornerTokens.Pill,
                    modifier = Modifier.testTag("apply_numeric_input_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Values")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Features #25 & #26: Clinical Modifiers Sheet (Osterkamp Amputation Adjustment & Gestational IOM Mode).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClinicalModifiersSheet(
    uiState: UiState,
    onDismiss: () -> Unit,
    onIntent: (UiIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var prePregWeight by remember(uiState.prePregnancyWeightKg) {
        mutableFloatStateOf(uiState.prePregnancyWeightKg)
    }
    var gestWeeks by remember(uiState.gestationalWeeks) {
        mutableIntStateOf(uiState.gestationalWeeks)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Clinical & Physiological Modifiers",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Adjust BMI equations for limb loss (Osterkamp method) or gestational weight tracking (IOM guidelines).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Feature #25: Clinical Amputation Weight Adjustment
            OutlinedCard(
                shape = ExpressiveCornerTokens.CardCompact,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Limb Segment Adjustments (Osterkamp %)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Select any missing limb segments to compute estimated whole-body reference BMI:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LimbAmputationSegment.entries.forEach { segment ->
                            val selected = segment in uiState.selectedAmputations
                            FilterChip(
                                selected = selected,
                                onClick = { onIntent(UiIntent.ToggleAmputationSegment(segment)) },
                                label = { Text(segment.label) }
                            )
                        }
                    }
                    if (uiState.selectedAmputations.isNotEmpty()) {
                        val adjWeight = BiometricFormulas.calculateAmputationAdjustedWeight(
                            uiState.weightKg,
                            uiState.selectedAmputations
                        )
                        Text(
                            text = "Adjusted Reference Weight: ${String.format("%.1f", adjWeight)} kg",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Feature #26: Pregnancy / Gestational Baseline Mode
            OutlinedCard(
                shape = ExpressiveCornerTokens.CardCompact,
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gestational Weight Gain Mode (IOM)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Evaluates pregnancy gain against pre-pregnancy BMI baseline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isPregnancyMode,
                            onCheckedChange = {
                                onIntent(UiIntent.SetPregnancyMode(it, prePregWeight, gestWeeks))
                            }
                        )
                    }

                    if (uiState.isPregnancyMode) {
                        Text(
                            text = "Pre-Pregnancy Weight: ${String.format("%.1f", prePregWeight)} kg",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = prePregWeight,
                            onValueChange = {
                                prePregWeight = it
                                onIntent(UiIntent.SetPregnancyMode(true, it, gestWeeks))
                            },
                            valueRange = 40f..140f
                        )
                        Text(
                            text = "Gestational Week: Week $gestWeeks",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = gestWeeks.toFloat(),
                            onValueChange = {
                                gestWeeks = it.toInt()
                                onIntent(UiIntent.SetPregnancyMode(true, prePregWeight, gestWeeks))
                            },
                            valueRange = 4f..40f
                        )
                        val preBmi = BiometricFormulas.calculateStandardBmi(uiState.heightCm, prePregWeight)
                        val gained = uiState.weightKg - prePregWeight
                        val totalRecommendedGain = when {
                            preBmi < 18.5f -> "12.5 – 18.0 kg"
                            preBmi < 25.0f -> "11.5 – 16.0 kg"
                            preBmi < 30.0f -> "7.0 – 11.5 kg"
                            else -> "5.0 – 9.0 kg"
                        }
                        Surface(
                            shape = ExpressiveCornerTokens.CardCompact,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Pre-Pregnancy BMI: ${String.format("%.1f", preBmi)} • Current Change: ${String.format("%+.1f", gained)} kg",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "IOM Full-Term Recommended Gain Range: $totalRecommendedGain",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onDismiss,
                    shape = ExpressiveCornerTokens.Pill
                ) {
                    Text("Done")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Feature #42: Dynamic Category Health Guidance Drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryHealthGuidanceSheet(
    uiState: UiState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val category = uiState.inspectedGaugeCategory ?: uiState.currentCategory

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = ExpressiveCornerTokens.Pill,
                    color = category.color.copy(alpha = 0.18f)
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Column {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.headlineSmall,
                        color = category.color
                    )
                    Text(
                        text = category.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = ExpressiveCornerTokens.CardCompact,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = category.clinicalGuidance,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                text = "Evidence-Based Next Steps",
                style = MaterialTheme.typography.titleMedium
            )

            category.actionSteps.forEach { step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onDismiss,
                    shape = ExpressiveCornerTokens.Pill
                ) {
                    Text("Close Guidance")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Features #40 & #43: 4-Formula Ideal Weight Matrix & All-Category Threshold Table
 */
@Composable
fun IdealWeightAndThresholdMatrixCard(
    uiState: UiState,
    modifier: Modifier = Modifier
) {
    val isMetric = uiState.unitSystem == UnitSystem.METRIC
    val heightM = (uiState.heightCm / 100f).coerceAtLeast(0.5f)
    val heightSq = heightM.pow(2)

    fun formatWeight(kg: Float): String {
        return if (isMetric) {
            "${String.format("%.1f", kg)} kg"
        } else {
            "${String.format("%.1f", BiometricFormulas.kgToLbs(kg))} lbs"
        }
    }

    OutlinedCard(
        shape = ExpressiveCornerTokens.CardExpressive,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "4-Formula Ideal Body Weight Matrix",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Adjusted for ${uiState.biologicalSex.label} & ${uiState.frameSize.label} (${uiState.frameSize.hint})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val ibw = uiState.idealWeightBreakdown
            val formulas = listOf(
                "Devine Clinical Formula" to ibw.devineKg,
                "Robinson Equation" to ibw.robinsonKg,
                "Miller Equation" to ibw.millerKg,
                "Hamwi Reference" to ibw.hamwiKg
            )
            formulas.forEach { (name, kg) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = formatWeight(kg),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (kg / 110f).coerceIn(0.2f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Feature #43: Category Boundary Breakdown Table for User's Exact Height
            Text(
                text = "All Category Weight Cutoffs at ${String.format("%.0f", uiState.heightCm)} cm",
                style = MaterialTheme.typography.titleMedium
            )
            val std = uiState.classificationStandard
            val tiers = listOf(
                Triple(BmiCategory.SEVERE_THINNESS, 0f, 16.0f),
                Triple(BmiCategory.MODERATE_THINNESS, 16.0f, 17.0f),
                Triple(BmiCategory.MILD_THINNESS, 17.0f, std.normalMin),
                Triple(BmiCategory.NORMAL, std.normalMin, std.normalMax),
                Triple(BmiCategory.OVERWEIGHT, std.normalMax, std.overweightMax),
                Triple(BmiCategory.OBESE_CLASS_I, std.overweightMax, std.overweightMax + 5f),
                Triple(BmiCategory.OBESE_CLASS_II, std.overweightMax + 5f, std.overweightMax + 10f),
                Triple(BmiCategory.OBESE_CLASS_III, std.overweightMax + 10f, 50f)
            )

            tiers.forEach { (cat, minBmi, maxBmi) ->
                val isCurrent = uiState.currentCategory == cat
                val minW = minBmi * heightSq
                val maxW = maxBmi * heightSq
                Surface(
                    shape = ExpressiveCornerTokens.CardCompact,
                    color = if (isCurrent) {
                        cat.color.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = cat.shortLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isCurrent) cat.color else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                            )
                            if (isCurrent) {
                                Text(
                                    text = "• YOU",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cat.color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        val weightSpan = when (cat) {
                            BmiCategory.SEVERE_THINNESS -> "< ${formatWeight(maxW)}"
                            BmiCategory.OBESE_CLASS_III -> "≥ ${formatWeight(minW)}"
                            else -> "${formatWeight(minW)} – ${formatWeight(maxW)}"
                        }
                        Text(
                            text = weightSpan,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
