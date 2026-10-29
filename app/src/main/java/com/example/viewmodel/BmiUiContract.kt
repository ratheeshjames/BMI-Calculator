package com.example.viewmodel

import androidx.compose.runtime.Immutable
import com.example.model.ActivityLevel
import com.example.model.AppDestination
import com.example.model.BiologicalSex
import com.example.model.BmiCategory
import com.example.model.BmiReading
import com.example.model.BmrFormula
import com.example.model.BodyFatMethod
import com.example.model.BodyFrameSize
import com.example.model.ClassificationStandard
import com.example.model.DateWindowFilter
import com.example.model.EnergyUnit
import com.example.model.HistorySortOrder
import com.example.model.IdealWeightBreakdown
import com.example.model.LimbAmputationSegment
import com.example.model.MacroPreset
import com.example.model.MeasurementTag
import com.example.model.StepMilestone
import com.example.model.ThemeMode
import com.example.model.UnitSystem
import com.example.model.UserProfile
import com.example.model.WhatIfRow

/**
 * Immutable Unidirectional Data Flow (UDF) State for the 100-Feature BMI Workstation.
 */
@Immutable
data class UiState(
    // Module 1: Onboarding, Identity & Adaptive Navigation Shell
    val hasCompletedOnboarding: Boolean = true,
    val onboardingPage: Int = 0,
    val onboardingDraftName: String = "Alex Rivera",
    val onboardingDraftBadge: String = "AR",
    val onboardingDraftAccentHex: Long = 0xFF006B58L,
    val currentDestination: AppDestination = AppDestination.CALCULATOR_HUB,
    val profiles: List<UserProfile> = emptyList(),
    val activeProfileId: String = "profile_alex",
    val isProfileSheetOpen: Boolean = false,
    val editingProfile: UserProfile? = null,

    // Module 2: Precision Biometric Input & Unit Conversion Engine
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val useStonesInImperial: Boolean = false,
    val heightCm: Float = 176.0f,
    val weightKg: Float = 74.2f,
    val ageYears: Int = 31,
    val biologicalSex: BiologicalSex = BiologicalSex.MALE,
    val frameSize: BodyFrameSize = BodyFrameSize.MEDIUM,
    val wristCm: Float = 17.4f,
    val selectedAmputations: Set<LimbAmputationSegment> = emptySet(),
    val isPregnancyMode: Boolean = false,
    val prePregnancyWeightKg: Float = 62.0f,
    val gestationalWeeks: Int = 20,
    val isAthleteMode: Boolean = false,
    val canUndoInput: Boolean = false,

    // Module 3: Real-Time BMI & Multi-Standard Category Feedback Engine
    val classificationStandard: ClassificationStandard = ClassificationStandard.WHO_GLOBAL,
    val standardBmi: Float = 23.95f,
    val newOxfordBmi: Float = 23.62f,
    val ponderalIndex: Float = 13.61f,
    val bmiPrime: Float = 0.96f,
    val currentCategory: BmiCategory = BmiCategory.NORMAL,
    val inspectedGaugeCategory: BmiCategory? = null,
    val healthyWeightMinKg: Float = 57.3f,
    val healthyWeightMaxKg: Float = 77.1f,
    val weightDeltaToHealthyKg: Float = 0f,
    val idealWeightBreakdown: IdealWeightBreakdown = IdealWeightBreakdown(
        devineKg = 71.6f,
        robinsonKg = 69.8f,
        millerKg = 69.5f,
        hamwiKg = 73.4f,
        consensusMeanKg = 71.1f,
        healthyMinKg = 57.3f,
        healthyMaxKg = 77.1f
    ),
    val bsaMostellerM2: Float = 1.90f,
    val bsaDuBoisM2: Float = 1.91f,
    val pediatricPercentile: Int = 55,
    val isCategoryGuidanceSheetOpen: Boolean = false,

    // Module 4: Advanced Body Composition & Metabolic Lab
    val waistCm: Float = 81.0f,
    val hipCm: Float = 97.0f,
    val neckCm: Float = 38.5f,
    val waistToHeightRatio: Float = 0.46f,
    val waistToHipRatio: Float = 0.84f,
    val bodyFatMethod: BodyFatMethod = BodyFatMethod.US_NAVY,
    val usNavyBodyFatPct: Float = 15.8f,
    val deurenbergBodyFatPct: Float = 19.7f,
    val activeBodyFatPct: Float = 15.8f,
    val leanBodyMassKg: Float = 62.5f,
    val fatMassKg: Float = 11.7f,
    val ffmi: Float = 20.18f,
    val normalizedFfmi: Float = 20.42f,
    val bmrFormula: BmrFormula = BmrFormula.MIFFLIN_ST_JEOR,
    val bmrKcal: Float = 1710f,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val tdeeKcal: Float = 2650f,
    val calorieGoalOffsetKcal: Int = -400, // -500 loss, 0 maint, +300 gain
    val macroPreset: MacroPreset = MacroPreset.BALANCED,
    val hydrationLitersPerDay: Float = 2.8f,
    val hydrationGlassesLogged: Int = 6,
    val isTapeGuideSheetOpen: Boolean = false,

    // Module 5: Longitudinal History Journal, Discovery & Batch Workflows
    val allReadingsForProfile: List<BmiReading> = emptyList(),
    val filteredReadings: List<BmiReading> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilters: Set<BmiCategory> = emptySet(),
    val selectedTagFilters: Set<MeasurementTag> = emptySet(),
    val pinnedOnlyFilter: Boolean = false,
    val dateWindowFilter: DateWindowFilter = DateWindowFilter.ALL_TIME,
    val sortOrder: HistorySortOrder = HistorySortOrder.NEWEST_FIRST,
    val selectedReadingIds: Set<String> = emptySet(),
    val isComparisonSheetOpen: Boolean = false,
    val isLogModalOpen: Boolean = false,
    val editingReading: BmiReading? = null,

    // Module 6: Goal Simulator, Pacing Automation & Analytics Studio
    val targetBmi: Float = 22.4f,
    val targetWeightKg: Float = 69.5f,
    val weeklyPaceKg: Float = 0.5f,
    val whatIfIntervalKg: Float = 2.5f,
    val stepMilestones: List<StepMilestone> = emptyList(),
    val whatIfRows: List<WhatIfRow> = emptyList(),
    val selectedAnalyticsMetric: String = "BMI", // "BMI", "Weight", "Body Fat %", "Waist"
    val showMovingAverageOverlay: Boolean = true,
    val scrubbedReadingIndex: Int? = null,

    // Module 7: Personalization, Accessibility & System Data Portability
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = false,
    val highContrastMode: Boolean = false,
    val reducedMotionMode: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val energyUnit: EnergyUnit = EnergyUnit.KCAL,
    val isReportCardSheetOpen: Boolean = false,
    val isExportImportSheetOpen: Boolean = false
) {
    val activeProfile: UserProfile?
        get() = profiles.firstOrNull { it.id == activeProfileId } ?: profiles.firstOrNull()
}

/**
 * Sealed Interface of User Intents for Unidirectional Data Flow (UDF).
 */
sealed interface UiIntent {
    // Onboarding & Navigation
    data class SetOnboardingPage(val page: Int) : UiIntent
    data class UpdateOnboardingDraft(
        val name: String,
        val badge: String,
        val accentHex: Long
    ) : UiIntent
    data object CompleteOnboarding : UiIntent
    data object ReplayOnboarding : UiIntent
    data class NavigateTo(val destination: AppDestination) : UiIntent

    // Profile Identity
    data class ToggleProfileSheet(val open: Boolean) : UiIntent
    data class SelectProfile(val profileId: String) : UiIntent
    data class OpenEditProfile(val profile: UserProfile?) : UiIntent
    data class SaveProfile(
        val id: String?,
        val name: String,
        val badge: String,
        val accentHex: Long,
        val heightCm: Float,
        val weightKg: Float,
        val ageYears: Int,
        val sex: BiologicalSex
    ) : UiIntent
    data class DeleteProfile(val profileId: String) : UiIntent
    data object ResetInputsToProfileBaseline : UiIntent

    // Biometric Inputs & Units
    data class SetUnitSystem(val unitSystem: UnitSystem) : UiIntent
    data class SetUseStones(val enabled: Boolean) : UiIntent
    data class UpdateHeightCm(val heightCm: Float) : UiIntent
    data class UpdateWeightKg(val weightKg: Float) : UiIntent
    data class OffsetWeightKg(val deltaKg: Float) : UiIntent
    data class UpdateAgeYears(val ageYears: Int) : UiIntent
    data class SetBiologicalSex(val sex: BiologicalSex) : UiIntent
    data class SetBodyFrameSize(val frameSize: BodyFrameSize) : UiIntent
    data class UpdateWristCm(val wristCm: Float) : UiIntent
    data class ToggleAmputationSegment(val segment: LimbAmputationSegment) : UiIntent
    data class SetPregnancyMode(val enabled: Boolean, val preWeightKg: Float, val weeks: Int) : UiIntent
    data class SetAthleteMode(val enabled: Boolean) : UiIntent
    data object UndoLastInputChange : UiIntent

    // Category Standards & Guidance
    data class SetClassificationStandard(val standard: ClassificationStandard) : UiIntent
    data class InspectGaugeCategory(val category: BmiCategory?) : UiIntent
    data class ToggleCategoryGuidanceSheet(val open: Boolean) : UiIntent

    // Body Composition & Metabolic Lab
    data class UpdateWaistCm(val waistCm: Float) : UiIntent
    data class UpdateHipCm(val hipCm: Float) : UiIntent
    data class UpdateNeckCm(val neckCm: Float) : UiIntent
    data class SetBodyFatMethod(val method: BodyFatMethod) : UiIntent
    data class SetBmrFormula(val formula: BmrFormula) : UiIntent
    data class SetActivityLevel(val level: ActivityLevel) : UiIntent
    data class SetCalorieGoalOffset(val offsetKcal: Int) : UiIntent
    data class SetMacroPreset(val preset: MacroPreset) : UiIntent
    data class ToggleTapeGuideSheet(val open: Boolean) : UiIntent

    // History Journal & Batch Workflows
    data class OpenLogModal(val editingReading: BmiReading? = null) : UiIntent
    data object CloseLogModal : UiIntent
    data class SaveReading(
        val existingId: String?,
        val heightCm: Float,
        val weightKg: Float,
        val waistCm: Float,
        val tags: Set<MeasurementTag>,
        val wellbeingScore: Int,
        val note: String,
        val timestampMillis: Long,
        val isPinned: Boolean
    ) : UiIntent
    data class DeleteReading(val readingId: String) : UiIntent
    data object UndoDeleteReadings : UiIntent
    data class TogglePinReading(val readingId: String) : UiIntent
    data class DuplicateReadingToToday(val reading: BmiReading) : UiIntent
    data class UpdateSearchQuery(val query: String) : UiIntent
    data class ToggleCategoryFilter(val category: BmiCategory) : UiIntent
    data class ToggleTagFilter(val tag: MeasurementTag) : UiIntent
    data class SetPinnedOnlyFilter(val pinnedOnly: Boolean) : UiIntent
    data object ClearAllHistoryFilters : UiIntent
    data class SetDateWindowFilter(val window: DateWindowFilter) : UiIntent
    data class SetHistorySortOrder(val sortOrder: HistorySortOrder) : UiIntent
    data class ToggleReadingSelection(val readingId: String) : UiIntent
    data object ClearReadingSelection : UiIntent
    data object BatchDeleteSelectedReadings : UiIntent
    data class BatchTagSelectedReadings(val tag: MeasurementTag) : UiIntent
    data class ToggleComparisonSheet(val open: Boolean) : UiIntent
    data class MoveReadingPriority(val readingId: String, val moveUp: Boolean) : UiIntent
    data class UpdateHydrationGlasses(val glasses: Int) : UiIntent

    // Goal Simulator & Analytics Studio
    data class UpdateTargetBmi(val targetBmi: Float) : UiIntent
    data class UpdateTargetWeightKg(val targetWeightKg: Float) : UiIntent
    data class UpdateWeeklyPaceKg(val weeklyPaceKg: Float) : UiIntent
    data class SetWhatIfIntervalKg(val intervalKg: Float) : UiIntent
    data class SetSelectedAnalyticsMetric(val metric: String) : UiIntent
    data class SetShowMovingAverageOverlay(val show: Boolean) : UiIntent
    data class ScrubChartIndex(val index: Int?) : UiIntent

    // Personalization & Data Portability
    data class SetThemeMode(val mode: ThemeMode) : UiIntent
    data class SetUseDynamicColor(val enabled: Boolean) : UiIntent
    data class SetHighContrastMode(val enabled: Boolean) : UiIntent
    data class SetReducedMotionMode(val enabled: Boolean) : UiIntent
    data class SetHapticsEnabled(val enabled: Boolean) : UiIntent
    data class SetEnergyUnit(val unit: EnergyUnit) : UiIntent
    data class ToggleReportCardSheet(val open: Boolean) : UiIntent
    data class ToggleExportImportSheet(val open: Boolean) : UiIntent
    data class ImportCsvReadings(val rawPayload: String) : UiIntent
    data object PopulateDemoDataset : UiIntent
    data object ClearProfileReadings : UiIntent
}

/**
 * One-shot UI Events (Snackbars, Undo prompts, Share triggers).
 */
sealed interface UiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val isUndoDelete: Boolean = false
    ) : UiEvent
}
