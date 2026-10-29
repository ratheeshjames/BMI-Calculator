package com.example.ui.bodylab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ActivityLevel
import com.example.model.BiologicalSex
import com.example.model.BiometricFormulas
import com.example.model.BmrFormula
import com.example.model.BodyFatMethod
import com.example.model.MacroPreset
import com.example.model.UnitSystem
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.BmiNormalOptimalColor
import com.example.ui.theme.BmiObeseClass1Color
import com.example.ui.theme.BmiOverweightColor
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.ui.theme.LocalExpressiveMotion
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BodyCompositionLabScreen(
    uiState: UiState,
    listState: LazyListState,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = LocalExpressiveMotion.current
    val triggerHaptic = rememberHapticTrigger(uiState.hapticsEnabled)
    val isMetric = uiState.unitSystem == UnitSystem.METRIC
    val energyMult = uiState.energyUnit.multiplierFromKcal
    val energyLabel = uiState.energyUnit.label

    val animatedBodyFat by animateFloatAsState(
        targetValue = uiState.activeBodyFatPct,
        animationSpec = motion.floatBouncy(),
        label = "lab_body_fat_pct"
    )
    val animatedLeanFraction by animateFloatAsState(
        targetValue = (1f - (uiState.activeBodyFatPct / 100f)).coerceIn(0.4f, 0.96f),
        animationSpec = motion.floatBouncy(),
        label = "lab_lean_fraction"
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("body_comp_lab_screen")
    ) {
        // 1. Hero Body Fat %, Lean Mass Split Bar & FFMI Card (#50, #51, #52, #53)
        item(key = "hero_body_comp_card") {
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Feature #51: US Navy Tape vs Deurenberg BMI Formula Switcher
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("body_fat_method_selector")
                    ) {
                        BodyFatMethod.entries.forEachIndexed { idx, method ->
                            SegmentedButton(
                                selected = uiState.bodyFatMethod == method,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetBodyFatMethod(method))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = BodyFatMethod.entries.size
                                )
                            ) {
                                Text(method.label)
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
                                text = "Estimated Body Adiposity",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format("%.1f", animatedBodyFat)}%",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val bfTier = classifyBodyFatTier(uiState.activeBodyFatPct, uiState.biologicalSex)
                        Surface(
                            shape = ExpressiveCornerTokens.Pill,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = bfTier,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Variance comparison between Navy Tape & Deurenberg BMI
                    val variance = uiState.usNavyBodyFatPct - uiState.deurenbergBodyFatPct
                    Text(
                        text = "US Navy Tape: ${String.format("%.1f", uiState.usNavyBodyFatPct)}% • Deurenberg BMI: ${String.format("%.1f", uiState.deurenbergBodyFatPct)}% (Δ ${String.format("%+.1f", variance)}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Feature #52: Fat Mass vs Lean Body Mass (LBM) Animated Split Bar
                    val lbmDisplay = if (isMetric) {
                        "${String.format("%.1f", uiState.leanBodyMassKg)} kg"
                    } else {
                        "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.leanBodyMassKg))} lbs"
                    }
                    val fatDisplay = if (isMetric) {
                        "${String.format("%.1f", uiState.fatMassKg)} kg"
                    } else {
                        "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.fatMassKg))} lbs"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lean Body Mass: $lbmDisplay",
                                style = MaterialTheme.typography.labelLarge,
                                color = BmiNormalOptimalColor
                            )
                            Text(
                                text = "Fat Mass: $fatDisplay",
                                style = MaterialTheme.typography.labelLarge,
                                color = BmiOverweightColor
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(18.dp)
                                .clip(ExpressiveCornerTokens.Pill)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(animatedLeanFraction)
                                    .fillMaxHeight()
                                    .background(BmiNormalOptimalColor)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .weight((1f - animatedLeanFraction).coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .background(BmiOverweightColor)
                            )
                        }
                    }

                    // Feature #53: Fat-Free Mass Index (FFMI) & Normalized FFMI
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fat-Free Mass Index (FFMI)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", uiState.ffmi)} kg/m² (Normalized: ${String.format("%.1f", uiState.normalizedFfmi)})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (uiState.ffmi >= 21.0f) {
                                        "High skeletal muscularity — explains elevated BMI even with healthy waist."
                                    } else {
                                        "Standard lean mass density for current stature."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Anthropometric Tape Inputs: Waist, Hip & Neck (#45, #46, #47)
        item(key = "anthropometric_circumference_inputs") {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Circumference Tape Inputs",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        // Feature #47: Interactive Tape Measurement Guide Sheet Trigger
                        AssistChip(
                            onClick = { onIntent(UiIntent.ToggleTapeGuideSheet(true)) },
                            label = { Text("Tape Guide") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("open_tape_guide_chip")
                        )
                    }

                    CircumferenceSliderControl(
                        label = "Waist Circumference (at navel)",
                        valueCm = uiState.waistCm,
                        isMetric = isMetric,
                        range = 45f..160f,
                        testTag = "waist_slider",
                        onChange = {
                            onIntent(UiIntent.UpdateWaistCm(it))
                        }
                    )

                    CircumferenceSliderControl(
                        label = "Hip Circumference (widest point)",
                        valueCm = uiState.hipCm,
                        isMetric = isMetric,
                        range = 55f..170f,
                        testTag = "hip_slider",
                        onChange = {
                            onIntent(UiIntent.UpdateHipCm(it))
                        }
                    )

                    CircumferenceSliderControl(
                        label = "Neck Circumference (below larynx)",
                        valueCm = uiState.neckCm,
                        isMetric = isMetric,
                        range = 25f..60f,
                        testTag = "neck_slider",
                        onChange = {
                            onIntent(UiIntent.UpdateNeckCm(it))
                        }
                    )
                }
            }
        }

        // 3. Central Adiposity Ratios: WHtR (#48) & WHR (#49)
        item(key = "central_adiposity_ratios_row", contentType = "ratios_row") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Feature #48: Waist-to-Height Ratio (WHtR)
                val whtr = uiState.waistToHeightRatio
                val whtrStatus = when {
                    whtr < 0.40f -> "Slender (<0.40)" to MaterialTheme.colorScheme.tertiary
                    whtr < 0.50f -> "Healthy (<0.50)" to BmiNormalOptimalColor
                    whtr < 0.60f -> "Elevated (0.50–0.59)" to BmiOverweightColor
                    else -> "High Risk (≥0.60)" to BmiObeseClass1Color
                }
                OutlinedCard(
                    shape = ExpressiveCornerTokens.CardCompact,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Waist-to-Height (WHtR)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.2f", whtr),
                            style = MaterialTheme.typography.headlineMedium,
                            color = whtrStatus.second,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = whtrStatus.first,
                            style = MaterialTheme.typography.labelLarge,
                            color = whtrStatus.second
                        )
                        Text(
                            text = "Target: Keep waist under 50% of height (< ${String.format("%.0f", uiState.heightCm * 0.5f)} cm)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Feature #49: Waist-to-Hip Ratio (WHR)
                val whr = uiState.waistToHipRatio
                val whrThreshold = if (uiState.biologicalSex == BiologicalSex.FEMALE) 0.85f else 0.90f
                val whrHealthy = whr < whrThreshold
                OutlinedCard(
                    shape = ExpressiveCornerTokens.CardCompact,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Waist-to-Hip (WHR)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.2f", whr),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (whrHealthy) BmiNormalOptimalColor else BmiOverweightColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (whrHealthy) "Low Visceral Risk" else "Android Distribution",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (whrHealthy) BmiNormalOptimalColor else BmiOverweightColor
                        )
                        Text(
                            text = "WHO ${uiState.biologicalSex.label} cutoff: < ${String.format("%.2f", whrThreshold)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 4. Metabolic Energy Engine: BMR, TDEE, Calorie Target & Macros (#54, #55, #56, #57)
        item(key = "metabolic_energy_engine_card") {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Metabolic Energy & Macro Lab",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    // Feature #54: BMR Dual-Formula Selector
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bmr_formula_selector")
                    ) {
                        BmrFormula.entries.forEachIndexed { idx, formula ->
                            SegmentedButton(
                                selected = uiState.bmrFormula == formula,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetBmrFormula(formula))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = BmrFormula.entries.size
                                )
                            ) {
                                Text(formula.label)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = ExpressiveCornerTokens.CardCompact,
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Resting BMR",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${(uiState.bmrKcal * energyMult).roundToInt()} $energyLabel",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Surface(
                            shape = ExpressiveCornerTokens.CardCompact,
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Total Daily (TDEE)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${(uiState.tdeeKcal * energyMult).roundToInt()} $energyLabel",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Feature #55: 5-Tier Activity Level TDEE Selector
                    Text("Daily Activity Multiplier", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ActivityLevel.entries.forEach { level ->
                            val selected = uiState.activityLevel == level
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetActivityLevel(level))
                                },
                                label = { Text("${level.label} (${level.multiplier}x)") }
                            )
                        }
                    }

                    // Feature #56: Goal-Linked Daily Calorie Target Calculator
                    Text("Target Nutritional Phase", style = MaterialTheme.typography.labelLarge)
                    val goalOptions = listOf(
                        -500 to "Cut (-500 kcal)",
                        0 to "Maintain (TDEE)",
                        300 to "Lean Gain (+300)"
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        goalOptions.forEachIndexed { idx, (offset, label) ->
                            SegmentedButton(
                                selected = uiState.calorieGoalOffsetKcal == offset,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetCalorieGoalOffset(offset))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = goalOptions.size
                                )
                            ) {
                                Text(label, maxLines = 1)
                            }
                        }
                    }

                    // Feature #57: Interactive Macronutrient Split Estimator
                    val targetDailyKcal = (uiState.tdeeKcal + uiState.calorieGoalOffsetKcal).coerceAtLeast(1200f)
                    Text(
                        text = "Daily Macro Split for ${(targetDailyKcal * energyMult).roundToInt()} $energyLabel/day",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MacroPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = uiState.macroPreset == preset,
                                onClick = { onIntent(UiIntent.SetMacroPreset(preset)) },
                                label = { Text(preset.label) }
                            )
                        }
                    }

                    val pGrams = ((targetDailyKcal * (uiState.macroPreset.proteinPct / 100f)) / 4f).roundToInt()
                    val cGrams = ((targetDailyKcal * (uiState.macroPreset.carbsPct / 100f)) / 4f).roundToInt()
                    val fGrams = ((targetDailyKcal * (uiState.macroPreset.fatPct / 100f)) / 9f).roundToInt()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MacroRingCard(
                            name = "Protein",
                            grams = "${pGrams}g",
                            percent = uiState.macroPreset.proteinPct,
                            color = BmiNormalOptimalColor,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        MacroRingCard(
                            name = "Carbs",
                            grams = "${cGrams}g",
                            percent = uiState.macroPreset.carbsPct,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        MacroRingCard(
                            name = "Fats",
                            grams = "${fGrams}g",
                            percent = uiState.macroPreset.fatPct,
                            color = BmiOverweightColor,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }

        // 5. Hydration Baseline Estimator & Interactive Glass Tracker (#58)
        item(key = "hydration_baseline_card") {
            val targetGlasses = ((uiState.hydrationLitersPerDay * 1000f) / 250f).roundToInt().coerceAtLeast(6)
            Card(
                shape = ExpressiveCornerTokens.CardExpressive,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Daily Hydration Baseline",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Target: ${String.format("%.1f", uiState.hydrationLitersPerDay)} L/day (${targetGlasses} × 250ml glasses)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.UpdateHydrationGlasses(uiState.hydrationGlassesLogged - 1))
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus 1 glass")
                            }
                            Text(
                                text = "${uiState.hydrationGlassesLogged}/$targetGlasses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalIconButton(
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.UpdateHydrationGlasses(uiState.hydrationGlassesLogged + 1))
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus 1 glass")
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = {
                            (uiState.hydrationGlassesLogged.toFloat() / targetGlasses.toFloat()).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(ExpressiveCornerTokens.Pill)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }

    // Feature #47: Interactive Tape Measurement Guide ModalBottomSheet with skipPartiallyExpanded = false
    if (uiState.isTapeGuideSheetOpen) {
        TapeMeasurementGuideSheet(
            onDismiss = { onIntent(UiIntent.ToggleTapeGuideSheet(false)) }
        )
    }
}

@Composable
private fun CircumferenceSliderControl(
    label: String,
    valueCm: Float,
    isMetric: Boolean,
    range: ClosedFloatingPointRange<Float>,
    testTag: String,
    onChange: (Float) -> Unit
) {
    val formatted = if (isMetric) {
        "${String.format("%.1f", valueCm)} cm"
    } else {
        "${String.format("%.1f", valueCm / 2.54f)} in"
    }
    Surface(
        shape = ExpressiveCornerTokens.CardCompact,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { onChange((valueCm - 0.5f).coerceIn(range)) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalIconButton(
                        onClick = { onChange((valueCm + 0.5f).coerceIn(range)) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase $label", modifier = Modifier.size(16.dp))
                    }
                }
            }
            Slider(
                value = valueCm,
                onValueChange = onChange,
                valueRange = range,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun MacroRingCard(
    name: String,
    grams: String,
    percent: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ExpressiveCornerTokens.CardCompact,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 7.dp.toPx()
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * (percent / 100f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(text = grams, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TapeMeasurementGuideSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

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
                text = "Anatomical Tape Measurement Guide",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "For accurate US Navy Body Fat % and Waist-to-Height Ratio, keep the tape snug and horizontal against bare skin without compressing tissue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val landmarks = listOf(
                "1. Waist Circumference" to "Measure at the midpoint between the lower margin of the last palpable rib and the top of the iliac crest (typically at the level of the navel) at the end of a normal exhalation.",
                "2. Hip Circumference" to "Stand with feet together and wrap the tape around the widest portion of the buttocks, keeping the tape parallel to the floor.",
                "3. Neck Circumference" to "Measure immediately below the laryngeal prominence (Adam's apple) with shoulders relaxed and looking straight ahead.",
                "4. Wrist Circumference" to "Measure around the narrowest part of the wrist just distal to the radial and ulnar styloid processes to determine skeletal frame size."
            )

            landmarks.forEach { (title, desc) ->
                OutlinedCard(
                    shape = ExpressiveCornerTokens.CardCompact,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text(desc, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onDismiss, shape = ExpressiveCornerTokens.Pill) {
                    Text("Got It")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun classifyBodyFatTier(bfPct: Float, sex: BiologicalSex): String {
    return if (sex == BiologicalSex.FEMALE) {
        when {
            bfPct < 14f -> "Essential Fat"
            bfPct < 21f -> "Athletic Fit"
            bfPct < 25f -> "Fitness Zone"
            bfPct < 32f -> "Healthy Average"
            else -> "Elevated Adiposity"
        }
    } else {
        when {
            bfPct < 6f -> "Essential Fat"
            bfPct < 14f -> "Athletic Fit"
            bfPct < 18f -> "Fitness Zone"
            bfPct < 25f -> "Healthy Average"
            else -> "Elevated Adiposity"
        }
    }
}
