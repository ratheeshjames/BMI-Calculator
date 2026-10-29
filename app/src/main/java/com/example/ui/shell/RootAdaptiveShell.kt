package com.example.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppDestination
import com.example.model.BiologicalSex
import com.example.model.BiometricFormulas
import com.example.model.ClassificationStandard
import com.example.model.MeasurementTag
import com.example.model.ThemeMode
import com.example.model.UnitSystem
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.ui.theme.LocalExpressiveMotion
import com.example.ui.theme.ProfileAccentColors
import com.example.viewmodel.UiEvent
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootAdaptiveShell(
    uiState: UiState,
    uiEvents: Flow<UiEvent>,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    // Feature #11: Expressive Extended FAB that morphs/shrinks dynamically on scroll
    val isFabExpanded by remember {
        derivedStateOf {
            !listState.isScrollInProgress &&
                (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 120)
        }
    }

    LaunchedEffect(uiEvents) {
        uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed && event.isUndoDelete) {
                        onIntent(UiIntent.UndoDeleteReadings)
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = ExpressiveCornerTokens.CardExpressive,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "BMI Workstation",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "100-Feature M3 Expressive Architecture",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                AppDestination.entries.forEach { destination ->
                    val selected = uiState.currentDestination == destination
                    NavigationDrawerItem(
                        label = {
                            Column {
                                Text(destination.shortLabel, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = destination.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        selected = selected,
                        onClick = {
                            onIntent(UiIntent.NavigateTo(destination))
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                imageVector = destinationIcon(destination, selected),
                                contentDescription = destination.shortLabel
                            )
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onIntent(UiIntent.ReplayOnboarding)
                        }
                    ) {
                        Text("Replay Onboarding")
                    }
                    AssistChip(
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            onIntent(UiIntent.ToggleProfileSheet(true))
                        },
                        label = { Text(uiState.activeProfile?.name ?: "Profile") }
                    )
                }
            }
        }
    ) {
        // Feature #9: Adaptive Window Size Navigation Shell
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        header = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                modifier = Modifier.testTag("nav_drawer_button_rail")
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
                            }
                        },
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AppDestination.entries.forEach { destination ->
                            val selected = uiState.currentDestination == destination
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onIntent(UiIntent.NavigateTo(destination)) },
                                icon = {
                                    Icon(
                                        imageVector = destinationIcon(destination, selected),
                                        contentDescription = destination.shortLabel
                                    )
                                },
                                label = { Text(destination.shortLabel) },
                                modifier = Modifier.testTag("rail_tab_${destination.routeId}")
                            )
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        // Feature #10: Collapsible Expressive Top App Bar
                        LargeTopAppBar(
                            scrollBehavior = scrollBehavior,
                            navigationIcon = {
                                if (!isWideScreen) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch { drawerState.open() }
                                        },
                                        modifier = Modifier.testTag("nav_drawer_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open navigation menu"
                                        )
                                    }
                                }
                            },
                            title = {
                                Column {
                                    Text(
                                        text = uiState.currentDestination.title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = uiState.currentDestination.subtitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                // Feature #28: Undo Last Input Modification
                                AnimatedVisibility(visible = uiState.canUndoInput) {
                                    IconButton(
                                        onClick = { onIntent(UiIntent.UndoLastInputChange) },
                                        modifier = Modifier.testTag("top_bar_undo_input")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Undo,
                                            contentDescription = "Undo last height or weight change"
                                        )
                                    }
                                }

                                // Feature #12: Quick Profile Baseline Reset
                                IconButton(
                                    onClick = { onIntent(UiIntent.ResetInputsToProfileBaseline) },
                                    modifier = Modifier.testTag("top_bar_reset_baseline")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset sliders to profile baseline"
                                    )
                                }

                                // Feature #6: Multi-Profile Identity Switcher
                                val activeProfile = uiState.activeProfile
                                AssistChip(
                                    onClick = { onIntent(UiIntent.ToggleProfileSheet(true)) },
                                    label = {
                                        Text(
                                            text = activeProfile?.name?.split(" ")?.firstOrNull() ?: "Profile",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Color(activeProfile?.accentColorHex ?: 0xFF006B58L)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = activeProfile?.avatarBadge ?: "AR",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    },
                                    shape = ExpressiveCornerTokens.Pill,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .testTag("profile_switcher_chip")
                                )
                            }
                        )
                    },
                    floatingActionButton = {
                        // Feature #11: Context-Aware Morphing Extended FAB
                        ExtendedFloatingActionButton(
                            onClick = {
                                when (uiState.currentDestination) {
                                    AppDestination.CALCULATOR_HUB,
                                    AppDestination.BODY_COMP_LAB,
                                    AppDestination.HISTORY_JOURNAL -> onIntent(UiIntent.OpenLogModal(null))
                                    AppDestination.GOAL_SIMULATOR -> onIntent(UiIntent.OpenLogModal(null))
                                    AppDestination.ANALYTICS_STUDIO -> onIntent(UiIntent.ToggleReportCardSheet(true))
                                    AppDestination.SETTINGS_CENTER -> onIntent(UiIntent.ToggleExportImportSheet(true))
                                }
                            },
                            expanded = isFabExpanded,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = uiState.currentDestination.fabLabel
                                )
                            },
                            text = { Text(uiState.currentDestination.fabLabel) },
                            shape = ExpressiveCornerTokens.Pill,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.testTag("root_morphing_fab")
                        )
                    },
                    bottomBar = {
                        if (!isWideScreen) {
                            NavigationBar(
                                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                AppDestination.entries.forEach { destination ->
                                    val selected = uiState.currentDestination == destination
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { onIntent(UiIntent.NavigateTo(destination)) },
                                        icon = {
                                            Icon(
                                                imageVector = destinationIcon(destination, selected),
                                                contentDescription = destination.shortLabel
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = destination.shortLabel,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("bottom_tab_${destination.routeId}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = uiState.currentDestination,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.97f))
                                .togetherWith(fadeOut() + scaleOut(targetScale = 0.97f))
                        },
                        label = "destination_content_transition",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { destination ->
                        when (destination) {
                            AppDestination.CALCULATOR_HUB -> {
                                com.example.ui.calculator.CalculatorHubScreen(
                                    uiState = uiState,
                                    listState = listState,
                                    onIntent = onIntent
                                )
                            }
                            AppDestination.BODY_COMP_LAB -> {
                                com.example.ui.bodylab.BodyCompositionLabScreen(
                                    uiState = uiState,
                                    listState = listState,
                                    onIntent = onIntent
                                )
                            }
                            AppDestination.HISTORY_JOURNAL -> {
                                com.example.ui.history.HistoryJournalScreen(
                                    uiState = uiState,
                                    listState = listState,
                                    onIntent = onIntent
                                )
                            }
                            AppDestination.GOAL_SIMULATOR -> {
                                com.example.ui.goals.GoalSimulatorScreen(
                                    uiState = uiState,
                                    listState = listState,
                                    onIntent = onIntent
                                )
                            }
                            AppDestination.ANALYTICS_STUDIO -> {
                                com.example.ui.analytics.AnalyticsStudioScreen(
                                    uiState = uiState,
                                    listState = listState,
                                    onIntent = onIntent
                                )
                            }
                            AppDestination.SETTINGS_CENTER -> {
                                com.example.ui.settings.SettingsCenterScreen(
                                    uiState = uiState,
                                    listState = listState,
                                    onIntent = onIntent
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Multi-Profile Identity Switcher & Create/Edit Sheet (Features #6, #7, #8)
    if (uiState.isProfileSheetOpen) {
        ProfileIdentityModalSheet(
            uiState = uiState,
            onIntent = onIntent
        )
    }

    // Quick-Log Reading Modal Sheet
    if (uiState.isLogModalOpen) {
        QuickLogModalSheet(
            uiState = uiState,
            onIntent = onIntent
        )
    }
}

@Composable
private fun ModuleHostContent(
    destination: AppDestination,
    uiState: UiState,
    listState: LazyListState,
    onIntent: (UiIntent) -> Unit
) {
    val motion = LocalExpressiveMotion.current
    val animatedBmi by animateFloatAsState(
        targetValue = uiState.standardBmi,
        animationSpec = motion.floatBouncy(),
        label = "shell_live_bmi"
    )
    val categoryColor by animateColorAsState(
        targetValue = uiState.currentCategory.color,
        label = "shell_category_color"
    )

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("module_host_${destination.routeId}")
    ) {
        // Live Expressive Hero Banner across the Workstation
        item(key = "live_biometric_hero") {
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
                        Column {
                            Text(
                                text = "${uiState.activeProfile?.name ?: "Profile"} • ${uiState.classificationStandard.shortLabel}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f", animatedBmi),
                                style = MaterialTheme.typography.displayLarge,
                                color = categoryColor
                            )
                        }

                        Surface(
                            shape = ExpressiveCornerTokens.Pill,
                            color = categoryColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = uiState.currentCategory.label,
                                style = MaterialTheme.typography.titleMedium,
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Text(
                        text = uiState.currentCategory.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Multi-Index Pill Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricSummaryChip(
                            label = "BMI Prime",
                            value = String.format("%.2f", uiState.bmiPrime),
                            modifier = Modifier.weight(1f)
                        )
                        MetricSummaryChip(
                            label = "New Oxford",
                            value = String.format("%.1f", uiState.newOxfordBmi),
                            modifier = Modifier.weight(1f)
                        )
                        MetricSummaryChip(
                            label = "Ponderal",
                            value = "${String.format("%.1f", uiState.ponderalIndex)} kg/m³",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Interactive Height, Weight & Unit Controls
        item(key = "interactive_biometric_controls") {
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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
                                Text(unit.shortLabel)
                            }
                        }
                    }

                    val heightDisplay = if (uiState.unitSystem == UnitSystem.METRIC) {
                        "${String.format("%.1f", uiState.heightCm)} cm"
                    } else {
                        val (ft, inc) = BiometricFormulas.cmToFeetInches(uiState.heightCm)
                        "${ft}' ${String.format("%.1f", inc)}\""
                    }
                    val weightDisplay = if (uiState.unitSystem == UnitSystem.METRIC) {
                        "${String.format("%.1f", uiState.weightKg)} kg"
                    } else {
                        "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.weightKg))} lbs"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Height", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = heightDisplay,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.heightCm,
                        onValueChange = { onIntent(UiIntent.UpdateHeightCm(it)) },
                        valueRange = 100f..230f,
                        modifier = Modifier.testTag("shell_height_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Weight", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = weightDisplay,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.weightKg,
                        onValueChange = { onIntent(UiIntent.UpdateWeightKg(it)) },
                        valueRange = 25f..220f,
                        modifier = Modifier.testTag("shell_weight_slider")
                    )
                }
            }
        }

        // Destination-Specific Module Hook Preview Card
        item(key = "module_hook_${destination.routeId}") {
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
                    Text(
                        text = "${destination.title} Active Module",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = destination.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    when (destination) {
                        AppDestination.CALCULATOR_HUB -> {
                            Text(
                                text = "Healthy Weight Range: ${String.format("%.1f", uiState.healthyWeightMinKg)} – ${String.format("%.1f", uiState.healthyWeightMaxKg)} kg • Consensus Ideal: ${String.format("%.1f", uiState.idealWeightBreakdown.consensusMeanKg)} kg",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        AppDestination.BODY_COMP_LAB -> {
                            Text(
                                text = "WHtR: ${String.format("%.2f", uiState.waistToHeightRatio)} • Navy Body Fat: ${String.format("%.1f", uiState.usNavyBodyFatPct)}% • Lean Mass: ${String.format("%.1f", uiState.leanBodyMassKg)} kg • TDEE: ${uiState.tdeeKcal.toInt()} kcal",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        AppDestination.HISTORY_JOURNAL -> {
                            Text(
                                text = "${uiState.filteredReadings.size} longitudinal readings loaded for ${uiState.activeProfile?.name}.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        AppDestination.GOAL_SIMULATOR -> {
                            Text(
                                text = "Target BMI: ${String.format("%.1f", uiState.targetBmi)} (${String.format("%.1f", uiState.targetWeightKg)} kg) at ${String.format("%.2f", uiState.weeklyPaceKg)} kg/week.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        AppDestination.ANALYTICS_STUDIO -> {
                            Text(
                                text = "Tracking ${uiState.allReadingsForProfile.size} historical points across 60-day trajectory with moving average smoothing.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        AppDestination.SETTINGS_CENTER -> {
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                ThemeMode.entries.forEachIndexed { idx, mode ->
                                    SegmentedButton(
                                        selected = uiState.themeMode == mode,
                                        onClick = { onIntent(UiIntent.SetThemeMode(mode)) },
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = idx,
                                            count = ThemeMode.entries.size
                                        )
                                    ) {
                                        Text(mode.label)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Longitudinal Readings Feed Preview
        items(
            items = uiState.filteredReadings.take(6),
            key = { it.id }
        ) { reading ->
            ElevatedCard(
                shape = ExpressiveCornerTokens.CardCompact,
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
                            text = "BMI ${String.format("%.1f", reading.bmi)} • ${String.format("%.1f", reading.weightKg)} kg",
                            style = MaterialTheme.typography.titleMedium,
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
                    Surface(
                        shape = ExpressiveCornerTokens.Pill,
                        color = reading.category.color.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = reading.category.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = reading.category.color,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
private fun MetricSummaryChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ExpressiveCornerTokens.CardCompact,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProfileIdentityModalSheet(
    uiState: UiState,
    onIntent: (UiIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var isCreatingOrEditing by remember(uiState.editingProfile) {
        mutableStateOf(uiState.editingProfile != null)
    }
    var draftName by remember(uiState.editingProfile) {
        mutableStateOf(uiState.editingProfile?.name ?: "")
    }
    var draftAccent by remember(uiState.editingProfile) {
        mutableLongStateOf(uiState.editingProfile?.accentColorHex ?: ProfileAccentColors.first())
    }
    var draftHeight by remember(uiState.editingProfile) {
        mutableFloatStateOf(uiState.editingProfile?.baselineHeightCm ?: 172f)
    }
    var draftWeight by remember(uiState.editingProfile) {
        mutableFloatStateOf(uiState.editingProfile?.baselineWeightKg ?: 68f)
    }
    var draftAge by remember(uiState.editingProfile) {
        mutableIntStateOf(uiState.editingProfile?.ageYears ?: 30)
    }
    var draftSex by remember(uiState.editingProfile) {
        mutableStateOf(uiState.editingProfile?.biologicalSex ?: BiologicalSex.FEMALE)
    }

    ModalBottomSheet(
        onDismissRequest = { onIntent(UiIntent.ToggleProfileSheet(false)) },
        sheetState = sheetState,
        shape = ExpressiveCornerTokens.SheetTop
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!isCreatingOrEditing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Biometric Profiles",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    FilledTonalButton(
                        onClick = {
                            draftName = ""
                            isCreatingOrEditing = true
                        },
                        modifier = Modifier.testTag("add_new_profile_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Profile")
                    }
                }

                uiState.profiles.forEach { profile ->
                    val isActive = profile.id == uiState.activeProfileId
                    Card(
                        shape = ExpressiveCornerTokens.CardCompact,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIntent(UiIntent.SelectProfile(profile.id)) }
                            .testTag("select_profile_${profile.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(profile.accentColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile.avatarBadge,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${profile.baselineHeightCm.toInt()} cm • ${profile.baselineWeightKg} kg • ${profile.ageYears} yrs",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        onIntent(UiIntent.OpenEditProfile(profile))
                                        isCreatingOrEditing = true
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit ${profile.name}")
                                }
                                if (uiState.profiles.size > 1) {
                                    IconButton(
                                        onClick = { onIntent(UiIntent.DeleteProfile(profile.id)) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete ${profile.name}",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = if (uiState.editingProfile == null) "New Biometric Profile" else "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = ExpressiveCornerTokens.CardCompact,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProfileAccentColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { draftAccent = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        width = if (draftAccent == hex) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
                Text("Baseline Height: ${draftHeight.toInt()} cm", style = MaterialTheme.typography.labelLarge)
                Slider(value = draftHeight, onValueChange = { draftHeight = it }, valueRange = 120f..220f)
                Text("Baseline Weight: ${String.format("%.1f", draftWeight)} kg", style = MaterialTheme.typography.labelLarge)
                Slider(value = draftWeight, onValueChange = { draftWeight = it }, valueRange = 35f..180f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { isCreatingOrEditing = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val badge = draftName.trim().split(" ")
                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                .joinToString("")
                                .take(2)
                                .ifEmpty { "NP" }
                            onIntent(
                                UiIntent.SaveProfile(
                                    id = uiState.editingProfile?.id,
                                    name = draftName.ifBlank { "New Profile" },
                                    badge = badge,
                                    accentHex = draftAccent,
                                    heightCm = draftHeight,
                                    weightKg = draftWeight,
                                    ageYears = draftAge,
                                    sex = draftSex
                                )
                            )
                        },
                        shape = ExpressiveCornerTokens.Pill
                    ) {
                        Text("Save Profile")
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun QuickLogModalSheet(
    uiState: UiState,
    onIntent: (UiIntent) -> Unit
) {
    val editing = uiState.editingReading
    var draftHeightCm by remember(editing, uiState.heightCm) {
        mutableFloatStateOf(editing?.heightCm ?: uiState.heightCm)
    }
    var draftWeightKg by remember(editing, uiState.weightKg) {
        mutableFloatStateOf(editing?.weightKg ?: uiState.weightKg)
    }
    var draftWaistCm by remember(editing, uiState.waistCm) {
        mutableFloatStateOf(editing?.waistCm ?: uiState.waistCm)
    }
    var note by remember(editing) { mutableStateOf(editing?.note ?: "") }
    var selectedTags by remember(editing) {
        mutableStateOf(editing?.tags ?: setOf(MeasurementTag.MORNING_FASTED))
    }
    var wellbeing by remember(editing) { mutableIntStateOf(editing?.wellbeingScore ?: 4) }
    var isPinned by remember(editing) { mutableStateOf(editing?.isPinned ?: false) }
    var daysBackOffset by remember(editing) { mutableIntStateOf(0) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val previewBmi = BiometricFormulas.calculateStandardBmi(draftHeightCm, draftWeightKg)
    val previewCat = BiometricFormulas.classifyBmi(previewBmi, uiState.classificationStandard)

    ModalBottomSheet(
        onDismissRequest = { onIntent(UiIntent.CloseLogModal) },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (editing == null) "Log Biometric Snapshot" else "Edit Snapshot Entry",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "BMI ${String.format("%.1f", previewBmi)} (${previewCat.shortLabel}) • ${String.format("%.1f", draftWeightKg)} kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = previewCat.color,
                        fontWeight = FontWeight.Bold
                    )
                }
                FilterChip(
                    selected = isPinned,
                    onClick = { isPinned = !isPinned },
                    label = { Text(if (isPinned) "Pinned" else "Pin Milestone") }
                )
            }

            Text(
                text = "Weight: ${String.format("%.1f", draftWeightKg)} kg • Waist: ${String.format("%.1f", draftWaistCm)} cm",
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = draftWeightKg,
                onValueChange = { draftWeightKg = it },
                valueRange = 25f..220f
            )

            Text("Context Tags", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MeasurementTag.entries.forEach { tag ->
                    val selected = tag in selectedTags
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedTags = if (selected) selectedTags - tag else selectedTags + tag
                        },
                        label = { Text(tag.label) }
                    )
                }
            }

            // Feature #61: Energy & Mood Snapshot Rating on Log (1..5)
            Text("Energy & Wellbeing Snapshot (1–5)", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val labels = listOf("1 Low", "2 Fair", "3 Good", "4 Strong", "5 Peak")
                labels.forEachIndexed { idx, label ->
                    val score = idx + 1
                    SegmentedButton(
                        selected = wellbeing == score,
                        onClick = { wellbeing = score },
                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = labels.size)
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Feature #62: Timestamp / Backdate Selector when creating a new entry
            if (editing == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Date:", style = MaterialTheme.typography.labelLarge)
                    listOf(0 to "Today", 1 to "Yesterday", 3 to "3d Ago", 7 to "1w Ago").forEach { (days, label) ->
                        FilterChip(
                            selected = daysBackOffset == days,
                            onClick = { daysBackOffset = days },
                            label = { Text(label) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Session Note (optional)") },
                shape = ExpressiveCornerTokens.CardCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_log_note_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onIntent(UiIntent.CloseLogModal) }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val timestamp = editing?.timestampMillis
                            ?: (System.currentTimeMillis() - daysBackOffset * 24L * 3600_000L)
                        onIntent(
                            UiIntent.SaveReading(
                                existingId = editing?.id,
                                heightCm = draftHeightCm,
                                weightKg = draftWeightKg,
                                waistCm = draftWaistCm,
                                tags = selectedTags,
                                wellbeingScore = wellbeing,
                                note = note,
                                timestampMillis = timestamp,
                                isPinned = isPinned
                            )
                        )
                    },
                    shape = ExpressiveCornerTokens.Pill,
                    modifier = Modifier.testTag("confirm_save_reading_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (editing == null) "Save Reading" else "Update Entry")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun destinationIcon(destination: AppDestination, selected: Boolean): ImageVector {
    return when (destination) {
        AppDestination.CALCULATOR_HUB -> if (selected) Icons.Filled.Calculate else Icons.Outlined.Calculate
        AppDestination.BODY_COMP_LAB -> if (selected) Icons.Filled.Science else Icons.Outlined.Science
        AppDestination.HISTORY_JOURNAL -> if (selected) Icons.Filled.History else Icons.Outlined.History
        AppDestination.GOAL_SIMULATOR -> if (selected) Icons.Filled.Flag else Icons.Outlined.Flag
        AppDestination.ANALYTICS_STUDIO -> if (selected) Icons.Filled.Analytics else Icons.Outlined.Analytics
        AppDestination.SETTINGS_CENTER -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
    }
}
