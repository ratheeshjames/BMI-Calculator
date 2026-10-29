package com.example.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ClassificationStandard
import com.example.model.EnergyUnit
import com.example.model.ThemeMode
import com.example.model.UnitSystem
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.BmiMildThinnessColor
import com.example.ui.theme.BmiNormalOptimalColor
import com.example.ui.theme.BmiObeseClass1Color
import com.example.ui.theme.BmiOverweightColor
import com.example.ui.theme.EmeraldPrimaryLight
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState

@Composable
fun SettingsCenterScreen(
    uiState: UiState,
    listState: LazyListState,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val triggerHaptic = rememberHapticTrigger(uiState.hapticsEnabled)
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_center_screen")
    ) {
        // 1. Live Theme Mode, Dynamic Colors, High-Contrast & Motion Physics (#89, #90, #91, #92, #93)
        item(key = "theme_and_accessibility_card") {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "M3 Expressive Theme & Accessibility",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    // Feature #89: Live Theme Mode Controller
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_theme_mode_selector")
                    ) {
                        ThemeMode.entries.forEachIndexed { idx, mode ->
                            SegmentedButton(
                                selected = uiState.themeMode == mode,
                                onClick = {
                                    triggerHaptic()
                                    onIntent(UiIntent.SetThemeMode(mode))
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = ThemeMode.entries.size
                                )
                            ) {
                                Text(mode.label)
                            }
                        }
                    }

                    // Live 5-Swatch Biometric Spectrum Preview Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            EmeraldPrimaryLight,
                            BmiMildThinnessColor,
                            BmiNormalOptimalColor,
                            BmiOverweightColor,
                            BmiObeseClass1Color
                        ).forEach { swatch ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(ExpressiveCornerTokens.Pill)
                                    .background(swatch)
                            )
                        }
                    }

                    // Feature #90: Dynamic Material You vs. Expressive Health Palette
                    SettingsToggleRow(
                        title = "Wallpaper Dynamic Colors (Material You)",
                        subtitle = "Adapt surfaces to Android 12+ system wallpaper vs. Clinical Emerald palette",
                        checked = uiState.useDynamicColor,
                        testTag = "dynamic_color_switch",
                        onCheckedChange = {
                            triggerHaptic()
                            onIntent(UiIntent.SetUseDynamicColor(it))
                        }
                    )

                    // Feature #91: High-Contrast Accessibility & Bold Numerals Mode
                    SettingsToggleRow(
                        title = "High-Contrast Clinical Readout",
                        subtitle = "Boosts contrast ratios, outline strokes, and bold display numerals",
                        checked = uiState.highContrastMode,
                        testTag = "high_contrast_switch",
                        onCheckedChange = {
                            triggerHaptic()
                            onIntent(UiIntent.SetHighContrastMode(it))
                        }
                    )

                    // Feature #92: Spring Motion & Reduced Animation Controller
                    SettingsToggleRow(
                        title = "Reduced Motion Mode",
                        subtitle = "Replaces bouncy spring physics with crisp linear transitions",
                        checked = uiState.reducedMotionMode,
                        testTag = "reduced_motion_switch",
                        onCheckedChange = {
                            triggerHaptic()
                            onIntent(UiIntent.SetReducedMotionMode(it))
                        }
                    )

                    // Feature #93: Tactile Haptic Feedback Controller
                    SettingsToggleRow(
                        title = "Tactile Slider & Stepper Haptics",
                        subtitle = "Physical vibration pulses when adjusting steppers and spectrum gauges",
                        checked = uiState.hapticsEnabled,
                        testTag = "haptics_enabled_switch",
                        onCheckedChange = {
                            onIntent(UiIntent.SetHapticsEnabled(it))
                        }
                    )
                }
            }
        }

        // 2. Units & Clinical Reference Standards Radio List (#94)
        item(key = "units_and_clinical_standards_card") {
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
                    Text(
                        text = "Measurement Units & Standards",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text("Default Measurement System", style = MaterialTheme.typography.labelLarge)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        UnitSystem.entries.forEachIndexed { idx, unit ->
                            SegmentedButton(
                                selected = uiState.unitSystem == unit,
                                onClick = { onIntent(UiIntent.SetUnitSystem(unit)) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = UnitSystem.entries.size
                                )
                            ) {
                                Text(unit.label)
                            }
                        }
                    }

                    // Feature #94: Energy Unit Selector (kcal vs kJ)
                    Text("Metabolic Energy Unit (BMR & TDEE)", style = MaterialTheme.typography.labelLarge)
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("energy_unit_selector")
                    ) {
                        EnergyUnit.entries.forEachIndexed { idx, unit ->
                            SegmentedButton(
                                selected = uiState.energyUnit == unit,
                                onClick = { onIntent(UiIntent.SetEnergyUnit(unit)) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = idx,
                                    count = EnergyUnit.entries.size
                                )
                            ) {
                                Text("${unit.label} (${if (unit == EnergyUnit.KCAL) "Kilocalories" else "Kilojoules 4.184x"})")
                            }
                        }
                    }

                    HorizontalDivider()
                    Text("Clinical BMI Classification Standard", style = MaterialTheme.typography.labelLarge)
                    ClassificationStandard.entries.forEach { std ->
                        val selected = uiState.classificationStandard == std
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(ExpressiveCornerTokens.CardCompact)
                                .clickable { onIntent(UiIntent.SetClassificationStandard(std)) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { onIntent(UiIntent.SetClassificationStandard(std)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(std.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = std.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Interactive BMI Science, Equations & Limitations Reference Guide (#95)
        item(key = "clinical_science_reference_guide") {
            ClinicalScienceReferenceAccordion()
        }

        // 4. Data Portability (CSV/JSON Export & Import) & Demo Dataset Reset (#98, #99, #100)
        item(key = "data_portability_and_reset_card") {
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
                        text = "Data Portability, Backup & Reset",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Export or import longitudinal records in structured CSV or JSON format, re-seed the 60-day demo dataset, or replay the onboarding tour.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onIntent(UiIntent.ToggleExportImportSheet(true)) },
                            shape = ExpressiveCornerTokens.Pill,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_export_import_button")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CSV / JSON Backup")
                        }

                        FilledTonalButton(
                            onClick = { onIntent(UiIntent.PopulateDemoDataset) },
                            shape = ExpressiveCornerTokens.Pill,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reseed_demo_data_button")
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("60-Day Demo Seed")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onIntent(UiIntent.ReplayOnboarding) },
                            shape = ExpressiveCornerTokens.Pill,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Replay Onboarding")
                        }

                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            shape = ExpressiveCornerTokens.Pill,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("clear_profile_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Profile Logs", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Clear Profile Biometric Logs?") },
            text = {
                Text("This will remove all logged readings for ${uiState.activeProfile?.name}. You can immediately restore them using the Undo snackbar.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onIntent(UiIntent.ClearProfileReadings)
                    }
                ) {
                    Text("Clear Logs")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Features #98 & #99: CSV & JSON Portable Data Exporter & Interactive Importer Sheet
    if (uiState.isExportImportSheetOpen) {
        CsvJsonPortabilityModalSheet(
            uiState = uiState,
            onDismiss = { onIntent(UiIntent.ToggleExportImportSheet(false)) },
            onIntent = onIntent
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
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
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

/**
 * Feature #95: Interactive BMI Science & Limitations Reference Guide Accordion
 */
@Composable
private fun ClinicalScienceReferenceAccordion() {
    val topics = listOf(
        "Standard WHO BMI vs. New Oxford BMI (Exponent 2.5)" to
            "Adolphe Quetelet's 1832 BMI divides mass by height² (kg/m²). Because human volume scales closer to height³ while cross-sectional area scales with height², tall individuals can appear artificially high and short individuals artificially low. Prof. Nick Trefethen's New Oxford formula (1.3 × weight / height^2.5) corrects stature scaling distortion.",
        "Why Muscularity & Waist-to-Height Ratio (WHtR) Matter" to
            "Raw BMI cannot differentiate skeletal muscle from visceral adipose tissue. A strength athlete may have a BMI of 27.5 (Overweight) with 12% body fat and a WHtR of 0.44. Keeping Waist-to-Height Ratio under 0.50 and evaluating Fat-Free Mass Index (FFMI) resolves false-positive BMI classifications.",
        "WHO Global vs. Asian-Pacific Thresholds" to
            "Epidemiological studies across Asian-Pacific populations demonstrate elevated type-2 diabetes and cardiovascular risk at lower BMI values due to visceral fat distribution patterns. The WHO Western Pacific guidance lowers the Normal upper bound from 24.9 to 22.9 kg/m²."
    )

    var expandedIndex by remember { mutableStateOf<Int?>(0) }

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Clinical Science & Formula Reference",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            topics.forEachIndexed { idx, (title, body) ->
                val isExpanded = expandedIndex == idx
                Surface(
                    shape = ExpressiveCornerTokens.CardCompact,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { expandedIndex = if (isExpanded) null else idx }
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
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                        if (isExpanded) {
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Features #98 & #99: CSV & JSON Portable Data Exporter & Interactive Importer/Validator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvJsonPortabilityModalSheet(
    uiState: UiState,
    onDismiss: () -> Unit,
    onIntent: (UiIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val clipboard = LocalClipboardManager.current
    var format by remember { mutableStateOf("CSV") }
    var importInput by remember {
        mutableStateOf("176.0,73.8,80.5,Morning fasted check\n176.0,73.4,80.2,Mid-week progress")
    }

    val serializedExport = remember(uiState.allReadingsForProfile, format) {
        if (format == "CSV") {
            buildString {
                appendLine("heightCm,weightKg,bmi,waistCm,category,note")
                uiState.allReadingsForProfile.forEach { r ->
                    appendLine("${r.heightCm},${r.weightKg},${String.format("%.2f", r.bmi)},${r.waistCm},${r.category.name},${r.note.replace(",", ";")}")
                }
            }
        } else {
            buildString {
                appendLine("[")
                uiState.allReadingsForProfile.forEachIndexed { idx, r ->
                    val comma = if (idx < uiState.allReadingsForProfile.lastIndex) "," else ""
                    appendLine("  {\"heightCm\":${r.heightCm},\"weightKg\":${r.weightKg},\"bmi\":${String.format("%.2f", r.bmi)},\"waistCm\":${r.waistCm},\"note\":\"${r.note}\"}$comma")
                }
                appendLine("]")
            }
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
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Portable Biometric Data (CSV & JSON)",
                style = MaterialTheme.typography.headlineSmall
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf("CSV", "JSON").forEachIndexed { idx, fmt ->
                    SegmentedButton(
                        selected = format == fmt,
                        onClick = { format = fmt },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = 2)
                    ) {
                        Text("Export $fmt")
                    }
                }
            }

            Surface(
                shape = ExpressiveCornerTokens.CardCompact,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Text(
                    text = serializedExport,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            FilledTonalButton(
                onClick = { clipboard.setText(AnnotatedString(serializedExport)) },
                shape = ExpressiveCornerTokens.Pill,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy $format Export Payload")
            }

            HorizontalDivider()

            // Feature #99: Interactive CSV Data Importer & Validator
            Text(
                text = "Import CSV Rows (heightCm,weightKg,waistCm,note)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = importInput,
                onValueChange = { importInput = it },
                minLines = 2,
                maxLines = 4,
                shape = ExpressiveCornerTokens.CardCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("csv_import_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onIntent(UiIntent.ImportCsvReadings(importInput)) },
                    shape = ExpressiveCornerTokens.Pill,
                    modifier = Modifier.testTag("validate_and_import_csv_button")
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Validate & Import")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
