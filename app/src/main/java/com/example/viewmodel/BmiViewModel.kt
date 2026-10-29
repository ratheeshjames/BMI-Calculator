package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BiometricRepository
import com.example.model.BmiReading
import com.example.model.BiometricFormulas
import com.example.model.BodyFatMethod
import com.example.model.ClassificationStandard
import com.example.model.HistorySortOrder
import com.example.model.MeasurementTag
import com.example.model.StepMilestone
import com.example.model.UserProfile
import com.example.model.WhatIfRow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

class BmiViewModel(
    private val repository: BiometricRepository = BiometricRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    private val inputUndoStack = ArrayDeque<Pair<Float, Float>>()
    private var lastDeletedReadings: List<BmiReading> = emptyList()

    init {
        viewModelScope.launch {
            repository.profiles.collectLatest { profiles ->
                _uiState.update { state ->
                    val validActiveId = if (profiles.any { it.id == state.activeProfileId }) {
                        state.activeProfileId
                    } else {
                        profiles.firstOrNull()?.id.orEmpty()
                    }
                    recalculateDerivedMetrics(
                        state.copy(
                            profiles = profiles,
                            activeProfileId = validActiveId
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.readings.collectLatest {
                _uiState.update { state -> recalculateDerivedMetrics(state) }
            }
        }
    }

    fun onIntent(intent: UiIntent) {
        when (intent) {
            is UiIntent.SetOnboardingPage -> {
                _uiState.update { it.copy(onboardingPage = intent.page.coerceIn(0, 3)) }
            }

            is UiIntent.UpdateOnboardingDraft -> {
                _uiState.update {
                    it.copy(
                        onboardingDraftName = intent.name,
                        onboardingDraftBadge = intent.badge,
                        onboardingDraftAccentHex = intent.accentHex
                    )
                }
            }

            UiIntent.CompleteOnboarding -> {
                val current = _uiState.value
                val primary = current.profiles.firstOrNull()
                if (primary != null) {
                    val updatedProfile = primary.copy(
                        name = current.onboardingDraftName.ifBlank { "Alex Rivera" },
                        avatarBadge = current.onboardingDraftBadge.ifBlank { "AR" }.take(2).uppercase(),
                        accentColorHex = current.onboardingDraftAccentHex,
                        baselineHeightCm = current.heightCm,
                        baselineWeightKg = current.weightKg,
                        ageYears = current.ageYears,
                        biologicalSex = current.biologicalSex
                    )
                    repository.upsertProfile(updatedProfile)
                }
                _uiState.update {
                    recalculateDerivedMetrics(it.copy(hasCompletedOnboarding = true))
                }
                emitSnackbar("Workstation ready for ${current.onboardingDraftName.ifBlank { "Alex Rivera" }}")
            }

            UiIntent.ReplayOnboarding -> {
                _uiState.update {
                    it.copy(hasCompletedOnboarding = false, onboardingPage = 0)
                }
            }

            is UiIntent.NavigateTo -> {
                _uiState.update { it.copy(currentDestination = intent.destination) }
            }

            is UiIntent.ToggleProfileSheet -> {
                _uiState.update {
                    it.copy(
                        isProfileSheetOpen = intent.open,
                        editingProfile = if (!intent.open) null else it.editingProfile
                    )
                }
            }

            is UiIntent.SelectProfile -> {
                val targetProfile = _uiState.value.profiles.firstOrNull { it.id == intent.profileId } ?: return
                inputUndoStack.clear()
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            activeProfileId = targetProfile.id,
                            heightCm = targetProfile.baselineHeightCm,
                            weightKg = targetProfile.baselineWeightKg,
                            ageYears = targetProfile.ageYears,
                            biologicalSex = targetProfile.biologicalSex,
                            frameSize = targetProfile.frameSize,
                            wristCm = targetProfile.wristCm,
                            waistCm = targetProfile.waistCm,
                            hipCm = targetProfile.hipCm,
                            neckCm = targetProfile.neckCm,
                            isAthleteMode = targetProfile.isAthleteMode,
                            targetBmi = targetProfile.targetBmi,
                            targetWeightKg = targetProfile.targetWeightKg,
                            weeklyPaceKg = targetProfile.weeklyPaceKg,
                            isProfileSheetOpen = false,
                            selectedReadingIds = emptySet(),
                            canUndoInput = false
                        )
                    )
                }
                emitSnackbar("Switched active profile to ${targetProfile.name}")
            }

            is UiIntent.OpenEditProfile -> {
                _uiState.update { it.copy(editingProfile = intent.profile, isProfileSheetOpen = true) }
            }

            is UiIntent.SaveProfile -> {
                val profileId = intent.id ?: "profile_${UUID.randomUUID().toString().take(6)}"
                val heightM = (intent.heightCm / 100f).coerceAtLeast(0.5f)
                val defaultTargetWeight = (22.0f * heightM * heightM).coerceIn(40f, 120f)
                val newProfile = UserProfile(
                    id = profileId,
                    name = intent.name.ifBlank { "New Profile" },
                    avatarBadge = intent.badge.ifBlank { intent.name.take(2).uppercase() },
                    accentColorHex = intent.accentHex,
                    baselineHeightCm = intent.heightCm.coerceIn(100f, 240f),
                    baselineWeightKg = intent.weightKg.coerceIn(25f, 250f),
                    ageYears = intent.ageYears.coerceIn(2, 100),
                    biologicalSex = intent.sex,
                    targetBmi = 22.0f,
                    targetWeightKg = defaultTargetWeight
                )
                repository.upsertProfile(newProfile)
                if (intent.id == null) {
                    // Seed an initial reading for the newly created profile
                    val bmi = BiometricFormulas.calculateStandardBmi(newProfile.baselineHeightCm, newProfile.baselineWeightKg)
                    repository.addOrUpdateReading(
                        BmiReading(
                            id = repository.createNewReadingId(),
                            profileId = newProfile.id,
                            timestampMillis = System.currentTimeMillis(),
                            heightCm = newProfile.baselineHeightCm,
                            weightKg = newProfile.baselineWeightKg,
                            bmi = bmi,
                            bmiPrime = BiometricFormulas.calculateBmiPrime(bmi, _uiState.value.classificationStandard),
                            ponderalIndex = BiometricFormulas.calculatePonderalIndex(newProfile.baselineHeightCm, newProfile.baselineWeightKg),
                            newOxfordBmi = BiometricFormulas.calculateNewOxfordBmi(newProfile.baselineHeightCm, newProfile.baselineWeightKg),
                            category = BiometricFormulas.classifyBmi(bmi, _uiState.value.classificationStandard),
                            tags = setOf(MeasurementTag.MORNING_FASTED),
                            note = "Initial profile baseline reading"
                        )
                    )
                }
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            activeProfileId = newProfile.id,
                            heightCm = newProfile.baselineHeightCm,
                            weightKg = newProfile.baselineWeightKg,
                            ageYears = newProfile.ageYears,
                            biologicalSex = newProfile.biologicalSex,
                            editingProfile = null,
                            isProfileSheetOpen = false
                        )
                    )
                }
                emitSnackbar("Saved profile ${newProfile.name}")
            }

            is UiIntent.DeleteProfile -> {
                val fallbackId = repository.deleteProfile(intent.profileId)
                onIntent(UiIntent.SelectProfile(fallbackId))
                emitSnackbar("Removed profile")
            }

            UiIntent.ResetInputsToProfileBaseline -> {
                val active = _uiState.value.activeProfile ?: return
                pushUndoSnapshot(_uiState.value.heightCm, _uiState.value.weightKg)
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            heightCm = active.baselineHeightCm,
                            weightKg = active.baselineWeightKg,
                            ageYears = active.ageYears,
                            biologicalSex = active.biologicalSex,
                            frameSize = active.frameSize,
                            wristCm = active.wristCm,
                            waistCm = active.waistCm,
                            hipCm = active.hipCm,
                            neckCm = active.neckCm,
                            canUndoInput = inputUndoStack.isNotEmpty()
                        )
                    )
                }
                emitSnackbar("Restored ${active.name}'s baseline metrics")
            }

            is UiIntent.SetUnitSystem -> {
                _uiState.update { it.copy(unitSystem = intent.unitSystem) }
            }

            is UiIntent.SetUseStones -> {
                _uiState.update { it.copy(useStonesInImperial = intent.enabled) }
            }

            is UiIntent.UpdateHeightCm -> {
                val clamped = intent.heightCm.coerceIn(100f, 240f)
                if (abs(clamped - _uiState.value.heightCm) >= 0.4f) {
                    pushUndoSnapshot(_uiState.value.heightCm, _uiState.value.weightKg)
                }
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            heightCm = clamped,
                            canUndoInput = inputUndoStack.isNotEmpty()
                        )
                    )
                }
            }

            is UiIntent.UpdateWeightKg -> {
                val clamped = intent.weightKg.coerceIn(25f, 250f)
                if (abs(clamped - _uiState.value.weightKg) >= 0.4f) {
                    pushUndoSnapshot(_uiState.value.heightCm, _uiState.value.weightKg)
                }
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            weightKg = clamped,
                            canUndoInput = inputUndoStack.isNotEmpty()
                        )
                    )
                }
            }

            is UiIntent.OffsetWeightKg -> {
                pushUndoSnapshot(_uiState.value.heightCm, _uiState.value.weightKg)
                val updated = (_uiState.value.weightKg + intent.deltaKg).coerceIn(25f, 250f)
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            weightKg = updated,
                            canUndoInput = inputUndoStack.isNotEmpty()
                        )
                    )
                }
            }

            is UiIntent.UpdateAgeYears -> {
                val clampedAge = intent.ageYears.coerceIn(2, 100)
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(ageYears = clampedAge))
                }
            }

            is UiIntent.SetBiologicalSex -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(biologicalSex = intent.sex))
                }
            }

            is UiIntent.SetBodyFrameSize -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(frameSize = intent.frameSize))
                }
            }

            is UiIntent.UpdateWristCm -> {
                val wrist = intent.wristCm.coerceIn(11f, 25f)
                val autoFrame = BiometricFormulas.estimateFrameFromWrist(
                    heightCm = _uiState.value.heightCm,
                    wristCm = wrist,
                    sex = _uiState.value.biologicalSex
                )
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(wristCm = wrist, frameSize = autoFrame)
                    )
                }
            }

            is UiIntent.ToggleAmputationSegment -> {
                _uiState.update { state ->
                    val next = if (intent.segment in state.selectedAmputations) {
                        state.selectedAmputations - intent.segment
                    } else {
                        state.selectedAmputations + intent.segment
                    }
                    recalculateDerivedMetrics(state.copy(selectedAmputations = next))
                }
            }

            is UiIntent.SetPregnancyMode -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            isPregnancyMode = intent.enabled,
                            prePregnancyWeightKg = intent.preWeightKg.coerceIn(35f, 180f),
                            gestationalWeeks = intent.weeks.coerceIn(1, 42)
                        )
                    )
                }
            }

            is UiIntent.SetAthleteMode -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(isAthleteMode = intent.enabled))
                }
            }

            UiIntent.UndoLastInputChange -> {
                val previous = inputUndoStack.removeLastOrNull() ?: return
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            heightCm = previous.first,
                            weightKg = previous.second,
                            canUndoInput = inputUndoStack.isNotEmpty()
                        )
                    )
                }
                emitSnackbar("Restored previous height & weight values")
            }

            is UiIntent.SetClassificationStandard -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(classificationStandard = intent.standard))
                }
            }

            is UiIntent.InspectGaugeCategory -> {
                _uiState.update { it.copy(inspectedGaugeCategory = intent.category) }
            }

            is UiIntent.ToggleCategoryGuidanceSheet -> {
                _uiState.update { it.copy(isCategoryGuidanceSheetOpen = intent.open) }
            }

            is UiIntent.UpdateWaistCm -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(waistCm = intent.waistCm.coerceIn(45f, 180f)))
                }
            }

            is UiIntent.UpdateHipCm -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(hipCm = intent.hipCm.coerceIn(55f, 190f)))
                }
            }

            is UiIntent.UpdateNeckCm -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(neckCm = intent.neckCm.coerceIn(25f, 65f)))
                }
            }

            is UiIntent.SetBodyFatMethod -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(bodyFatMethod = intent.method))
                }
            }

            is UiIntent.SetBmrFormula -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(bmrFormula = intent.formula))
                }
            }

            is UiIntent.SetActivityLevel -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(activityLevel = intent.level))
                }
            }

            is UiIntent.SetCalorieGoalOffset -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(calorieGoalOffsetKcal = intent.offsetKcal))
                }
            }

            is UiIntent.SetMacroPreset -> {
                _uiState.update { it.copy(macroPreset = intent.preset) }
            }

            is UiIntent.ToggleTapeGuideSheet -> {
                _uiState.update { it.copy(isTapeGuideSheetOpen = intent.open) }
            }

            is UiIntent.OpenLogModal -> {
                _uiState.update {
                    it.copy(isLogModalOpen = true, editingReading = intent.editingReading)
                }
            }

            UiIntent.CloseLogModal -> {
                _uiState.update { it.copy(isLogModalOpen = false, editingReading = null) }
            }

            is UiIntent.SaveReading -> {
                val state = _uiState.value
                val bmi = BiometricFormulas.calculateStandardBmi(intent.heightCm, intent.weightKg)
                val bodyFat = BiometricFormulas.calculateUsNavyBodyFat(
                    sex = state.biologicalSex,
                    heightCm = intent.heightCm,
                    waistCm = intent.waistCm,
                    neckCm = state.neckCm,
                    hipCm = state.hipCm
                )
                val reading = BmiReading(
                    id = intent.existingId ?: repository.createNewReadingId(),
                    profileId = state.activeProfileId,
                    timestampMillis = intent.timestampMillis,
                    heightCm = intent.heightCm,
                    weightKg = intent.weightKg,
                    bmi = bmi,
                    bmiPrime = BiometricFormulas.calculateBmiPrime(bmi, state.classificationStandard),
                    ponderalIndex = BiometricFormulas.calculatePonderalIndex(intent.heightCm, intent.weightKg),
                    newOxfordBmi = BiometricFormulas.calculateNewOxfordBmi(intent.heightCm, intent.weightKg),
                    category = BiometricFormulas.classifyBmi(bmi, state.classificationStandard),
                    waistCm = intent.waistCm,
                    hipCm = state.hipCm,
                    neckCm = state.neckCm,
                    bodyFatPct = bodyFat,
                    tags = intent.tags,
                    wellbeingScore = intent.wellbeingScore,
                    note = intent.note,
                    isPinned = intent.isPinned
                )
                repository.addOrUpdateReading(reading)
                // Also sync profile baseline
                state.activeProfile?.let { profile ->
                    repository.upsertProfile(
                        profile.copy(
                            baselineHeightCm = intent.heightCm,
                            baselineWeightKg = intent.weightKg,
                            waistCm = intent.waistCm
                        )
                    )
                }
                _uiState.update { it.copy(isLogModalOpen = false, editingReading = null) }
                emitSnackbar(
                    if (intent.existingId == null) "Logged BMI ${String.format("%.1f", bmi)} to journal"
                    else "Updated journal entry"
                )
            }

            is UiIntent.DeleteReading -> {
                lastDeletedReadings = repository.deleteReadings(setOf(intent.readingId))
                _uiState.update { it.copy(selectedReadingIds = it.selectedReadingIds - intent.readingId) }
                emitSnackbar("Entry removed", actionLabel = "Undo", isUndoDelete = true)
            }

            UiIntent.UndoDeleteReadings -> {
                if (lastDeletedReadings.isNotEmpty()) {
                    repository.restoreReadings(lastDeletedReadings)
                    lastDeletedReadings = emptyList()
                    emitSnackbar("Restored deleted entries")
                }
            }

            is UiIntent.TogglePinReading -> {
                repository.togglePinReading(intent.readingId)
            }

            is UiIntent.DuplicateReadingToToday -> {
                val clone = intent.reading.copy(
                    id = repository.createNewReadingId(),
                    timestampMillis = System.currentTimeMillis(),
                    note = "${intent.reading.note} (Cloned)".trim()
                )
                repository.addOrUpdateReading(clone)
                emitSnackbar("Duplicated reading to today")
            }

            is UiIntent.UpdateSearchQuery -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(searchQuery = intent.query))
                }
            }

            is UiIntent.ToggleCategoryFilter -> {
                _uiState.update { state ->
                    val next = if (intent.category in state.selectedCategoryFilters) {
                        state.selectedCategoryFilters - intent.category
                    } else {
                        state.selectedCategoryFilters + intent.category
                    }
                    recalculateDerivedMetrics(state.copy(selectedCategoryFilters = next))
                }
            }

            is UiIntent.ToggleTagFilter -> {
                _uiState.update { state ->
                    val next = if (intent.tag in state.selectedTagFilters) {
                        state.selectedTagFilters - intent.tag
                    } else {
                        state.selectedTagFilters + intent.tag
                    }
                    recalculateDerivedMetrics(state.copy(selectedTagFilters = next))
                }
            }

            is UiIntent.SetPinnedOnlyFilter -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(pinnedOnlyFilter = intent.pinnedOnly))
                }
            }

            UiIntent.ClearAllHistoryFilters -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            searchQuery = "",
                            selectedCategoryFilters = emptySet(),
                            selectedTagFilters = emptySet(),
                            pinnedOnlyFilter = false
                        )
                    )
                }
            }

            is UiIntent.SetDateWindowFilter -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(dateWindowFilter = intent.window))
                }
            }

            is UiIntent.SetHistorySortOrder -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(sortOrder = intent.sortOrder))
                }
            }

            is UiIntent.ToggleReadingSelection -> {
                _uiState.update { state ->
                    val next = if (intent.readingId in state.selectedReadingIds) {
                        state.selectedReadingIds - intent.readingId
                    } else {
                        state.selectedReadingIds + intent.readingId
                    }
                    state.copy(selectedReadingIds = next)
                }
            }

            UiIntent.ClearReadingSelection -> {
                _uiState.update { it.copy(selectedReadingIds = emptySet(), isComparisonSheetOpen = false) }
            }

            UiIntent.BatchDeleteSelectedReadings -> {
                val ids = _uiState.value.selectedReadingIds
                if (ids.isNotEmpty()) {
                    lastDeletedReadings = repository.deleteReadings(ids)
                    _uiState.update { it.copy(selectedReadingIds = emptySet()) }
                    emitSnackbar(
                        "Deleted ${ids.size} selected readings",
                        actionLabel = "Undo",
                        isUndoDelete = true
                    )
                }
            }

            is UiIntent.BatchTagSelectedReadings -> {
                val ids = _uiState.value.selectedReadingIds
                if (ids.isNotEmpty()) {
                    repository.assignTagToReadings(ids, intent.tag)
                    _uiState.update { it.copy(selectedReadingIds = emptySet()) }
                    emitSnackbar("Tagged ${ids.size} readings with ${intent.tag.label}")
                }
            }

            is UiIntent.ToggleComparisonSheet -> {
                _uiState.update { it.copy(isComparisonSheetOpen = intent.open) }
            }

            is UiIntent.MoveReadingPriority -> {
                repository.moveReadingRelative(intent.readingId, intent.moveUp)
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(sortOrder = HistorySortOrder.CUSTOM_ORDER))
                }
            }

            is UiIntent.UpdateHydrationGlasses -> {
                _uiState.update { it.copy(hydrationGlassesLogged = intent.glasses.coerceIn(0, 24)) }
            }

            is UiIntent.UpdateTargetBmi -> {
                val clampedBmi = intent.targetBmi.coerceIn(16.5f, 34.0f)
                val heightM = (_uiState.value.heightCm / 100f).coerceAtLeast(0.5f)
                val correspondingWeight = (clampedBmi * heightM * heightM).coerceIn(30f, 220f)
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            targetBmi = clampedBmi,
                            targetWeightKg = correspondingWeight
                        )
                    )
                }
            }

            is UiIntent.UpdateTargetWeightKg -> {
                val clampedWeight = intent.targetWeightKg.coerceIn(30f, 220f)
                val correspondingBmi = BiometricFormulas.calculateStandardBmi(_uiState.value.heightCm, clampedWeight)
                _uiState.update { state ->
                    recalculateDerivedMetrics(
                        state.copy(
                            targetWeightKg = clampedWeight,
                            targetBmi = correspondingBmi
                        )
                    )
                }
            }

            is UiIntent.UpdateWeeklyPaceKg -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(weeklyPaceKg = intent.weeklyPaceKg.coerceIn(0.15f, 1.5f)))
                }
            }

            is UiIntent.SetWhatIfIntervalKg -> {
                _uiState.update { state ->
                    recalculateDerivedMetrics(state.copy(whatIfIntervalKg = intent.intervalKg))
                }
            }

            is UiIntent.SetSelectedAnalyticsMetric -> {
                _uiState.update { it.copy(selectedAnalyticsMetric = intent.metric, scrubbedReadingIndex = null) }
            }

            is UiIntent.SetShowMovingAverageOverlay -> {
                _uiState.update { it.copy(showMovingAverageOverlay = intent.show) }
            }

            is UiIntent.ScrubChartIndex -> {
                _uiState.update { it.copy(scrubbedReadingIndex = intent.index) }
            }

            is UiIntent.SetThemeMode -> {
                _uiState.update { it.copy(themeMode = intent.mode) }
            }

            is UiIntent.SetUseDynamicColor -> {
                _uiState.update { it.copy(useDynamicColor = intent.enabled) }
            }

            is UiIntent.SetHighContrastMode -> {
                _uiState.update { it.copy(highContrastMode = intent.enabled) }
            }

            is UiIntent.SetReducedMotionMode -> {
                _uiState.update { it.copy(reducedMotionMode = intent.enabled) }
            }

            is UiIntent.SetHapticsEnabled -> {
                _uiState.update { it.copy(hapticsEnabled = intent.enabled) }
            }

            is UiIntent.SetEnergyUnit -> {
                _uiState.update { it.copy(energyUnit = intent.unit) }
            }

            is UiIntent.ToggleReportCardSheet -> {
                _uiState.update { it.copy(isReportCardSheetOpen = intent.open) }
            }

            is UiIntent.ToggleExportImportSheet -> {
                _uiState.update { it.copy(isExportImportSheetOpen = intent.open) }
            }

            is UiIntent.ImportCsvReadings -> {
                importCsvPayload(intent.rawPayload)
            }

            UiIntent.PopulateDemoDataset -> {
                repository.resetToDemoDataset()
                emitSnackbar("Populated 60-day realistic multi-profile dataset")
            }

            UiIntent.ClearProfileReadings -> {
                lastDeletedReadings = repository.clearAllReadingsForProfile(_uiState.value.activeProfileId)
                emitSnackbar("Cleared profile readings", actionLabel = "Undo", isUndoDelete = true)
            }
        }
    }

    private fun pushUndoSnapshot(heightCm: Float, weightKg: Float) {
        if (inputUndoStack.size >= 15) {
            inputUndoStack.removeFirst()
        }
        inputUndoStack.addLast(heightCm to weightKg)
    }

    private fun importCsvPayload(rawPayload: String) {
        val lines = rawPayload.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var importedCount = 0
        val state = _uiState.value
        lines.forEach { line ->
            // Expected CSV format: heightCm,weightKg,waistCm,note
            val parts = line.split(",")
            if (parts.size >= 2) {
                val h = parts[0].trim().toFloatOrNull()
                val w = parts[1].trim().toFloatOrNull()
                val waist = parts.getOrNull(2)?.trim()?.toFloatOrNull() ?: state.waistCm
                val note = parts.getOrNull(3)?.trim() ?: "Imported CSV reading"
                if (h != null && w != null && h in 100f..240f && w in 25f..250f) {
                    val bmi = BiometricFormulas.calculateStandardBmi(h, w)
                    repository.addOrUpdateReading(
                        BmiReading(
                            id = repository.createNewReadingId(),
                            profileId = state.activeProfileId,
                            timestampMillis = System.currentTimeMillis() - (importedCount * 3600_000L),
                            heightCm = h,
                            weightKg = w,
                            bmi = bmi,
                            bmiPrime = BiometricFormulas.calculateBmiPrime(bmi, state.classificationStandard),
                            ponderalIndex = BiometricFormulas.calculatePonderalIndex(h, w),
                            newOxfordBmi = BiometricFormulas.calculateNewOxfordBmi(h, w),
                            category = BiometricFormulas.classifyBmi(bmi, state.classificationStandard),
                            waistCm = waist,
                            tags = setOf(MeasurementTag.MORNING_FASTED),
                            note = note
                        )
                    )
                    importedCount++
                }
            }
        }
        if (importedCount > 0) {
            _uiState.update { it.copy(isExportImportSheetOpen = false) }
            emitSnackbar("Validated & imported $importedCount biometric records")
        } else {
            emitSnackbar("No valid 'heightCm,weightKg,waistCm,note' rows found")
        }
    }

    private fun recalculateDerivedMetrics(state: UiState): UiState {
        val effectiveWeightKg = BiometricFormulas.calculateAmputationAdjustedWeight(
            rawWeightKg = state.weightKg,
            segments = state.selectedAmputations
        )
        val bmi = BiometricFormulas.calculateStandardBmi(state.heightCm, effectiveWeightKg)
        val newOxford = BiometricFormulas.calculateNewOxfordBmi(state.heightCm, effectiveWeightKg)
        val ponderal = BiometricFormulas.calculatePonderalIndex(state.heightCm, effectiveWeightKg)
        val prime = BiometricFormulas.calculateBmiPrime(bmi, state.classificationStandard)
        val category = BiometricFormulas.classifyBmi(bmi, state.classificationStandard)
        val (healthyMin, healthyMax) = BiometricFormulas.calculateHealthyWeightRange(
            heightCm = state.heightCm,
            standard = state.classificationStandard,
            frameSize = state.frameSize
        )
        val weightDeltaToHealthy = when {
            effectiveWeightKg < healthyMin -> healthyMin - effectiveWeightKg
            effectiveWeightKg > healthyMax -> healthyMax - effectiveWeightKg
            else -> 0f
        }
        val idealBreakdown = BiometricFormulas.calculateIdealWeightBreakdown(
            heightCm = state.heightCm,
            sex = state.biologicalSex,
            standard = state.classificationStandard,
            frameSize = state.frameSize
        )
        val bsaMosteller = BiometricFormulas.calculateBodySurfaceAreaMosteller(state.heightCm, effectiveWeightKg)
        val bsaDuBois = BiometricFormulas.calculateBodySurfaceAreaDuBois(state.heightCm, effectiveWeightKg)
        val pediatricPct = BiometricFormulas.calculatePediatricPercentile(bmi, state.ageYears, state.biologicalSex)

        // Body Composition & Metabolic Lab
        val whtr = BiometricFormulas.calculateWaistToHeightRatio(state.waistCm, state.heightCm)
        val whr = BiometricFormulas.calculateWaistToHipRatio(state.waistCm, state.hipCm)
        val navyBf = BiometricFormulas.calculateUsNavyBodyFat(
            sex = state.biologicalSex,
            heightCm = state.heightCm,
            waistCm = state.waistCm,
            neckCm = state.neckCm,
            hipCm = state.hipCm
        )
        val deurenbergBf = BiometricFormulas.calculateDeurenbergBodyFat(bmi, state.ageYears, state.biologicalSex)
        val activeBf = if (state.bodyFatMethod == BodyFatMethod.US_NAVY) navyBf else deurenbergBf
        val (lbmKg, fatMassKg) = BiometricFormulas.calculateLeanBodyMass(effectiveWeightKg, activeBf)
        val (ffmi, normFfmi) = BiometricFormulas.calculateFfmi(lbmKg, state.heightCm)
        val bmr = BiometricFormulas.calculateBmr(
            weightKg = effectiveWeightKg,
            heightCm = state.heightCm,
            ageYears = state.ageYears,
            sex = state.biologicalSex,
            leanBodyMassKg = lbmKg,
            formula = state.bmrFormula
        )
        val tdee = bmr * state.activityLevel.multiplier
        val hydrationLiters = ((effectiveWeightKg * 0.035f) + (state.activityLevel.multiplier - 1.2f) * 1.1f).coerceIn(1.5f, 6.0f)

        // Goal Milestones & What-If Matrix
        val totalDeltaKg = state.targetWeightKg - effectiveWeightKg
        val milestones = (1..4).map { step ->
            val fraction = step / 4f
            val stepWeight = effectiveWeightKg + (totalDeltaKg * fraction)
            val stepBmi = BiometricFormulas.calculateStandardBmi(state.heightCm, stepWeight)
            val weeks = abs(totalDeltaKg * fraction) / state.weeklyPaceKg.coerceAtLeast(0.1f)
            StepMilestone(
                stepIndex = step,
                title = "Phase $step (${step * 25}% Target)",
                targetWeightKg = stepWeight,
                targetBmi = stepBmi,
                weeksToReach = weeks,
                isReached = abs(totalDeltaKg) < 0.3f
            )
        }

        val stepInterval = state.whatIfIntervalKg.coerceAtLeast(0.5f)
        val whatIfRows = (-3..3).map { multiplier ->
            val delta = multiplier * stepInterval
            val projWeight = (effectiveWeightKg + delta).coerceIn(25f, 250f)
            val projBmi = BiometricFormulas.calculateStandardBmi(state.heightCm, projWeight)
            WhatIfRow(
                deltaKg = delta,
                projectedWeightKg = projWeight,
                projectedBmi = projBmi,
                projectedPrime = BiometricFormulas.calculateBmiPrime(projBmi, state.classificationStandard),
                category = BiometricFormulas.classifyBmi(projBmi, state.classificationStandard)
            )
        }

        // History Filtering & Sorting
        val profileReadings = repository.readings.value
            .filter { it.profileId == state.activeProfileId }

        val now = System.currentTimeMillis()
        val windowCutoff = state.dateWindowFilter.days?.let { now - it * 24L * 3600_000L }

        val filtered = profileReadings.filter { reading ->
            val matchesWindow = windowCutoff == null || reading.timestampMillis >= windowCutoff
            val matchesCategory = state.selectedCategoryFilters.isEmpty() || reading.category in state.selectedCategoryFilters
            val matchesTags = state.selectedTagFilters.isEmpty() || reading.tags.any { it in state.selectedTagFilters }
            val matchesPin = !state.pinnedOnlyFilter || reading.isPinned
            val q = state.searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                reading.note.lowercase().contains(q) ||
                reading.category.label.lowercase().contains(q) ||
                reading.tags.any { it.label.lowercase().contains(q) } ||
                String.format("%.1f", reading.bmi).contains(q)
            matchesWindow && matchesCategory && matchesTags && matchesPin && matchesSearch
        }.let { list ->
            when (state.sortOrder) {
                HistorySortOrder.NEWEST_FIRST -> list.sortedByDescending { it.timestampMillis }
                HistorySortOrder.OLDEST_FIRST -> list.sortedBy { it.timestampMillis }
                HistorySortOrder.LOWEST_BMI -> list.sortedBy { it.bmi }
                HistorySortOrder.HIGHEST_BMI -> list.sortedByDescending { it.bmi }
                HistorySortOrder.LARGEST_DELTA -> list.sortedByDescending { abs(it.bmi - bmi) }
                HistorySortOrder.CUSTOM_ORDER -> list
            }
        }

        return state.copy(
            standardBmi = bmi,
            newOxfordBmi = newOxford,
            ponderalIndex = ponderal,
            bmiPrime = prime,
            currentCategory = category,
            healthyWeightMinKg = healthyMin,
            healthyWeightMaxKg = healthyMax,
            weightDeltaToHealthyKg = weightDeltaToHealthy,
            idealWeightBreakdown = idealBreakdown,
            bsaMostellerM2 = bsaMosteller,
            bsaDuBoisM2 = bsaDuBois,
            pediatricPercentile = pediatricPct,
            waistToHeightRatio = whtr,
            waistToHipRatio = whr,
            usNavyBodyFatPct = navyBf,
            deurenbergBodyFatPct = deurenbergBf,
            activeBodyFatPct = activeBf,
            leanBodyMassKg = lbmKg,
            fatMassKg = fatMassKg,
            ffmi = ffmi,
            normalizedFfmi = normFfmi,
            bmrKcal = bmr,
            tdeeKcal = tdee,
            hydrationLitersPerDay = hydrationLiters,
            stepMilestones = milestones,
            whatIfRows = whatIfRows,
            allReadingsForProfile = profileReadings,
            filteredReadings = filtered
        )
    }

    private fun emitSnackbar(
        message: String,
        actionLabel: String? = null,
        isUndoDelete: Boolean = false
    ) {
        viewModelScope.launch {
            _uiEvents.send(
                UiEvent.ShowSnackbar(
                    message = message,
                    actionLabel = actionLabel,
                    isUndoDelete = isUndoDelete
                )
            )
        }
    }
}
