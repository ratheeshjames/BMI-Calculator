package com.example.ui.previews

import android.content.res.Configuration
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.BiometricRepository
import com.example.model.AppDestination
import com.example.model.StepMilestone
import com.example.model.WhatIfRow
import com.example.model.BmiCategory
import com.example.ui.analytics.AnalyticsStudioScreen
import com.example.ui.bodylab.BodyCompositionLabScreen
import com.example.ui.calculator.CalculatorHubScreen
import com.example.ui.goals.GoalSimulatorScreen
import com.example.ui.history.HistoryJournalScreen
import com.example.ui.onboarding.ExpressiveOnboardingScreen
import com.example.ui.settings.SettingsCenterScreen
import com.example.ui.shell.RootAdaptiveShell
import com.example.ui.theme.BmiCalculatorTheme
import com.example.viewmodel.UiState
import kotlinx.coroutines.flow.emptyFlow

@Composable
private fun rememberPreviewUiState(
    destination: AppDestination = AppDestination.CALCULATOR_HUB
): UiState {
    return remember(destination) {
        val repo = BiometricRepository()
        val profiles = repo.profiles.value
        val readings = repo.readings.value.filter { it.profileId == "profile_alex" }
        UiState(
            hasCompletedOnboarding = true,
            currentDestination = destination,
            profiles = profiles,
            activeProfileId = "profile_alex",
            allReadingsForProfile = readings,
            filteredReadings = readings,
            stepMilestones = listOf(
                StepMilestone(1, "Phase 1 (25% Target)", 73.0f, 23.6f, 2.4f, false),
                StepMilestone(2, "Phase 2 (50% Target)", 71.8f, 23.2f, 4.8f, false),
                StepMilestone(3, "Phase 3 (75% Target)", 70.6f, 22.8f, 7.2f, false),
                StepMilestone(4, "Phase 4 (100% Target)", 69.5f, 22.4f, 9.4f, false)
            ),
            whatIfRows = listOf(
                WhatIfRow(-5.0f, 69.2f, 22.3f, 0.90f, BmiCategory.NORMAL),
                WhatIfRow(-2.5f, 71.7f, 23.1f, 0.93f, BmiCategory.NORMAL),
                WhatIfRow(0.0f, 74.2f, 24.0f, 0.96f, BmiCategory.NORMAL),
                WhatIfRow(2.5f, 76.7f, 24.8f, 0.99f, BmiCategory.NORMAL),
                WhatIfRow(5.0f, 79.2f, 25.6f, 1.03f, BmiCategory.OVERWEIGHT)
            )
        )
    }
}

@Preview(name = "1. Calculator Hub — Light Mode", showBackground = true)
@Preview(
    name = "1. Calculator Hub — Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CalculatorHubScreenPreview() {
    val state = rememberPreviewUiState(AppDestination.CALCULATOR_HUB)
    BmiCalculatorTheme {
        CalculatorHubScreen(
            uiState = state,
            listState = rememberLazyListState(),
            onIntent = {}
        )
    }
}

@Preview(name = "2. Body Composition Lab — Light Mode", showBackground = true)
@Preview(
    name = "2. Body Composition Lab — Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun BodyCompositionLabScreenPreview() {
    val state = rememberPreviewUiState(AppDestination.BODY_COMP_LAB)
    BmiCalculatorTheme {
        BodyCompositionLabScreen(
            uiState = state,
            listState = rememberLazyListState(),
            onIntent = {}
        )
    }
}

@Preview(name = "3. History Journal — Light Mode", showBackground = true)
@Preview(
    name = "3. History Journal — Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun HistoryJournalScreenPreview() {
    val state = rememberPreviewUiState(AppDestination.HISTORY_JOURNAL)
    BmiCalculatorTheme {
        HistoryJournalScreen(
            uiState = state,
            listState = rememberLazyListState(),
            onIntent = {}
        )
    }
}

@Preview(name = "4. Goal Simulator — Light Mode", showBackground = true)
@Preview(
    name = "4. Goal Simulator — Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun GoalSimulatorScreenPreview() {
    val state = rememberPreviewUiState(AppDestination.GOAL_SIMULATOR)
    BmiCalculatorTheme {
        GoalSimulatorScreen(
            uiState = state,
            listState = rememberLazyListState(),
            onIntent = {}
        )
    }
}

@Preview(name = "5. Analytics Studio — Light Mode", showBackground = true)
@Preview(
    name = "5. Analytics Studio — Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AnalyticsStudioScreenPreview() {
    val state = rememberPreviewUiState(AppDestination.ANALYTICS_STUDIO)
    BmiCalculatorTheme {
        AnalyticsStudioScreen(
            uiState = state,
            listState = rememberLazyListState(),
            onIntent = {}
        )
    }
}

@Preview(name = "6. Settings Center — Light Mode", showBackground = true)
@Preview(
    name = "6. Settings Center — Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettingsCenterScreenPreview() {
    val state = rememberPreviewUiState(AppDestination.SETTINGS_CENTER)
    BmiCalculatorTheme {
        SettingsCenterScreen(
            uiState = state,
            listState = rememberLazyListState(),
            onIntent = {}
        )
    }
}

@Preview(name = "7. Expressive Onboarding — Light Mode", showBackground = true)
@Preview(
    name = "7. Expressive Onboarding — Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ExpressiveOnboardingPreview() {
    val state = rememberPreviewUiState().copy(hasCompletedOnboarding = false)
    BmiCalculatorTheme {
        ExpressiveOnboardingScreen(
            uiState = state,
            onIntent = {}
        )
    }
}

@Preview(name = "8. Adaptive Tablet Shell — Light Mode", showBackground = true, widthDp = 840, heightDp = 640)
@Preview(
    name = "8. Adaptive Tablet Shell — Dark Mode",
    showBackground = true,
    widthDp = 840,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun AdaptiveTabletShellPreview() {
    val state = rememberPreviewUiState(AppDestination.CALCULATOR_HUB)
    BmiCalculatorTheme {
        RootAdaptiveShell(
            uiState = state,
            uiEvents = emptyFlow(),
            onIntent = {}
        )
    }
}
