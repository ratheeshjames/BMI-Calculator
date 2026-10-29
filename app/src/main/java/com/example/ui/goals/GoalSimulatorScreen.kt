package com.example.ui.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BiometricFormulas
import com.example.model.UnitSystem
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.BmiNormalOptimalColor
import com.example.ui.theme.BmiObeseClass1Color
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

@Composable
fun GoalSimulatorScreen(
    uiState: UiState,
    listState: LazyListState,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = LocalExpressiveMotion.current
    val triggerHaptic = rememberHapticTrigger(uiState.hapticsEnabled)
    val isMetric = uiState.unitSystem == UnitSystem.METRIC

    val totalWeightDeltaKg = uiState.targetWeightKg - uiState.weightKg
    val weeksRemaining = abs(totalWeightDeltaKg) / uiState.weeklyPaceKg.coerceAtLeast(0.1f)
    val projectedMillis = System.currentTimeMillis() + (weeksRemaining * 7f * 24f * 3600_000f).toLong()
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }

    // Calculate adherence progress from earliest profile reading toward target
    val startWeight = uiState.allReadingsForProfile.lastOrNull()?.weightKg ?: (uiState.weightKg + 4f)
    val initialDistance = abs(uiState.targetWeightKg - startWeight).coerceAtLeast(0.5f)
    val currentDistance = abs(uiState.targetWeightKg - uiState.weightKg)
    val rawProgress = ((initialDistance - currentDistance) / initialDistance).coerceIn(0.08f, 1.0f)

    val animatedProgress by animateFloatAsState(
        targetValue = if (currentDistance < 0.25f) 1f else rawProgress,
        animationSpec = motion.floatBouncy(),
        label = "goal_adherence_ring"
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("goal_simulator_screen")
    ) {
        // 1. Hero Goal Progress Ring & Projected Milestone Countdown (#77, #88)
        item(key = "hero_goal_progress_card") {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Target Trajectory & Pacing",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "BMI ${String.format("%.1f", uiState.targetBmi)}",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val targetWStr = if (isMetric) {
                                "${String.format("%.1f", uiState.targetWeightKg)} kg"
                            } else {
                                "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.targetWeightKg))} lbs"
                            }
                            Text(
                                text = "Target Weight: $targetWStr (${String.format("%+.1f", totalWeightDeltaKg)} kg)",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Feature #88: Custom Canvas Goal Progress Ring
                        val primaryColor = MaterialTheme.colorScheme.primary
                        Box(
                            modifier = Modifier.size(104.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 11.dp.toPx()
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.16f),
                                    style = Stroke(width = stroke)
                                )
                                drawArc(
                                    color = primaryColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(animatedProgress * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Adherence",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Feature #77: Projected Milestone Completion Date Banner
                    Surface(
                        shape = ExpressiveCornerTokens.CardCompact,
                        color = MaterialTheme.colorScheme.primaryContainer,
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Column {
                                    Text(
                                        text = "Projected Target Date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = if (abs(totalWeightDeltaKg) < 0.2f) {
                                            "Target Reached Today!"
                                        } else {
                                            dateFormatter.format(Date(projectedMillis))
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${String.format("%.1f", weeksRemaining)} wks",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Dual-Linked Target BMI / Weight & Weekly Pacing Sliders (#75, #76)
        item(key = "goal_interactive_sliders_card") {
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
                    Text(
                        text = "Interactive Goal & Pacing Simulator",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // Feature #75: Linked Target BMI Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target BMI", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${String.format("%.1f", uiState.targetBmi)} kg/m²",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = uiState.targetBmi,
                        onValueChange = { onIntent(UiIntent.UpdateTargetBmi(it)) },
                        valueRange = 17.5f..32.0f,
                        modifier = Modifier.testTag("goal_target_bmi_slider")
                    )

                    // Linked Target Weight Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target Weight", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (isMetric) {
                                "${String.format("%.1f", uiState.targetWeightKg)} kg"
                            } else {
                                "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.targetWeightKg))} lbs"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = uiState.targetWeightKg,
                        onValueChange = { onIntent(UiIntent.UpdateTargetWeightKg(it)) },
                        valueRange = 40f..160f,
                        modifier = Modifier.testTag("goal_target_weight_slider")
                    )

                    // Feature #76: Weekly Pacing Rate & Calorie Deficit/Surplus Slider
                    val dailyKcalDelta = ((uiState.weeklyPaceKg * 7700f) / 7f).roundToInt()
                    val paceSafety = when {
                        uiState.weeklyPaceKg <= 0.6f -> "Sustainable Clinical Pace" to BmiNormalOptimalColor
                        uiState.weeklyPaceKg <= 1.0f -> "Moderate Athletic Pace" to BmiOverweightColor
                        else -> "Aggressive Pace — Monitor LBM" to BmiObeseClass1Color
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Weekly Rate: ${String.format("%.2f", uiState.weeklyPaceKg)} kg/wk",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "≈ $dailyKcalDelta kcal/day energy ${if (totalWeightDeltaKg <= 0) "deficit" else "surplus"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = ExpressiveCornerTokens.Pill,
                            color = paceSafety.second.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = paceSafety.first,
                                style = MaterialTheme.typography.labelSmall,
                                color = paceSafety.second,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Slider(
                        value = uiState.weeklyPaceKg,
                        onValueChange = { onIntent(UiIntent.UpdateWeeklyPaceKg(it)) },
                        valueRange = 0.15f..1.4f,
                        modifier = Modifier.testTag("goal_weekly_pace_slider")
                    )
                }
            }
        }

        // 3. Automatic 4-Step Intermediate Milestone Timeline (#78)
        item(key = "intermediate_milestones_card") {
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Progressive 25% Checkpoint Milestones",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    uiState.stepMilestones.forEach { milestone ->
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (milestone.isReached) {
                                            BmiNormalOptimalColor
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    Column {
                                        Text(
                                            text = milestone.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "BMI ${String.format("%.1f", milestone.targetBmi)} • ${String.format("%.1f", milestone.targetWeightKg)} kg",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "+${String.format("%.1f", milestone.weeksToReach)} wks",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Interactive "What-If" Weight Step Matrix (#79)
        item(key = "what_if_weight_matrix_card") {
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
                    Text(
                        text = "Interactive What-If Weight Step Matrix",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Preview how weight increments shift BMI, BMI Prime, and WHO Category:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val intervals = listOf(1.0f to "±1.0 kg", 2.5f to "±2.5 kg", 5.0f to "±5.0 kg")
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("what_if_interval_selector")
                    ) {
                        intervals.forEachIndexed { idx, (stepKg, label) ->
                            SegmentedButton(
                                selected = abs(uiState.whatIfIntervalKg - stepKg) < 0.1f,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetWhatIfIntervalKg(stepKg))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = intervals.size
                                )
                            ) {
                                Text(label)
                            }
                        }
                    }

                    uiState.whatIfRows.forEach { row ->
                        val isZero = abs(row.deltaKg) < 0.05f
                        Surface(
                            shape = ExpressiveCornerTokens.CardCompact,
                            color = if (isZero) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            },
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
                                    text = if (isZero) "Current" else String.format("%+.1f kg", row.deltaKg),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(74.dp)
                                )
                                Text(
                                    text = "${String.format("%.1f", row.projectedWeightKg)} kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "BMI ${String.format("%.1f", row.projectedBmi)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = ExpressiveCornerTokens.Pill,
                                    color = row.category.color.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = row.category.shortLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = row.category.color,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}
