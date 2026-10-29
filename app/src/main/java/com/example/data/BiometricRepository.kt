package com.example.data

import com.example.model.BmiCategory
import com.example.model.BmiReading
import com.example.model.BiologicalSex
import com.example.model.BiometricFormulas
import com.example.model.BodyFrameSize
import com.example.model.ClassificationStandard
import com.example.model.MeasurementTag
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Reactive In-Memory Biometric Repository.
 * Pre-seeded with realistic multi-profile, 60-day longitudinal biometric history.
 */
class BiometricRepository {

    private val nowMillis = System.currentTimeMillis()
    private val dayMillis = 24L * 60L * 60L * 1000L

    private val initialProfiles = listOf(
        UserProfile(
            id = "profile_alex",
            name = "Alex Rivera",
            avatarBadge = "AR",
            accentColorHex = 0xFF006B58L,
            baselineHeightCm = 176.0f,
            baselineWeightKg = 74.2f,
            ageYears = 31,
            biologicalSex = BiologicalSex.MALE,
            frameSize = BodyFrameSize.MEDIUM,
            wristCm = 17.4f,
            waistCm = 81.0f,
            hipCm = 97.0f,
            neckCm = 38.5f,
            isAthleteMode = false,
            targetBmi = 22.4f,
            targetWeightKg = 69.5f,
            weeklyPaceKg = 0.5f
        ),
        UserProfile(
            id = "profile_maya",
            name = "Maya Lin",
            avatarBadge = "ML",
            accentColorHex = 0xFF2563EBL,
            baselineHeightCm = 164.0f,
            baselineWeightKg = 58.6f,
            ageYears = 28,
            biologicalSex = BiologicalSex.FEMALE,
            frameSize = BodyFrameSize.SMALL,
            wristCm = 14.8f,
            waistCm = 68.0f,
            hipCm = 92.0f,
            neckCm = 32.0f,
            isAthleteMode = false,
            targetBmi = 21.2f,
            targetWeightKg = 57.0f,
            weeklyPaceKg = 0.3f
        ),
        UserProfile(
            id = "profile_jordan",
            name = "Jordan Vance",
            avatarBadge = "JV",
            accentColorHex = 0xFF7C3AEDL,
            baselineHeightCm = 182.0f,
            baselineWeightKg = 88.4f,
            ageYears = 36,
            biologicalSex = BiologicalSex.MALE,
            frameSize = BodyFrameSize.LARGE,
            wristCm = 19.2f,
            waistCm = 83.0f,
            hipCm = 101.0f,
            neckCm = 41.0f,
            isAthleteMode = true,
            targetBmi = 24.5f,
            targetWeightKg = 81.0f,
            weeklyPaceKg = 0.6f
        )
    )

    private val _profiles = MutableStateFlow(initialProfiles)
    val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

    private val _readings = MutableStateFlow(generateRealisticSeedReadings())
    val readings: StateFlow<List<BmiReading>> = _readings.asStateFlow()

    fun generateRealisticSeedReadings(): List<BmiReading> {
        val list = mutableListOf<BmiReading>()

        // 12 realistic longitudinal readings over 55 days for Alex Rivera showing steady progress
        val alexTrajectory = listOf(
            Triple(55, 78.4f, 85.0f) to Pair(setOf(MeasurementTag.MORNING_FASTED), "Initial baseline check after summer travel"),
            Triple(49, 77.9f, 84.5f) to Pair(setOf(MeasurementTag.MORNING_FASTED), "Started 8,500 daily step target"),
            Triple(44, 78.2f, 84.5f) to Pair(setOf(MeasurementTag.EVENING, MeasurementTag.HIGH_SODIUM), "Post ramen dinner—expected water retention"),
            Triple(38, 77.1f, 83.8f) to Pair(setOf(MeasurementTag.MORNING_FASTED), "Consistent sleep and hydration week"),
            Triple(32, 76.6f, 83.2f) to Pair(setOf(MeasurementTag.POST_WORKOUT), "After 45-min upper body resistance session"),
            Triple(27, 76.2f, 82.8f) to Pair(setOf(MeasurementTag.MORNING_FASTED), "Waist down over 2cm from start"),
            Triple(21, 75.8f, 82.4f) to Pair(setOf(MeasurementTag.MORNING_FASTED, MeasurementTag.CLINICAL_CHECK), "Annual wellness checkup scale verification"),
            Triple(16, 75.3f, 82.0f) to Pair(setOf(MeasurementTag.MORNING_FASTED), "Entering Normal BMI threshold corridor"),
            Triple(11, 75.0f, 81.6f) to Pair(setOf(MeasurementTag.POST_WORKOUT), "Strong energy levels during training"),
            Triple(6, 74.6f, 81.3f) to Pair(setOf(MeasurementTag.MORNING_FASTED), "BMI Prime dropped below 0.98"),
            Triple(3, 74.4f, 81.1f) to Pair(setOf(MeasurementTag.EVENING), "Mid-week evening check-in"),
            Triple(0, 74.2f, 81.0f) to Pair(setOf(MeasurementTag.MORNING_FASTED), "Current morning baseline reading")
        )

        alexTrajectory.forEachIndexed { index, (dayWeightWaist, tagsAndNote) ->
            val (daysAgo, weightKg, waistCm) = dayWeightWaist
            val (tags, note) = tagsAndNote
            val heightCm = 176.0f
            val bmi = BiometricFormulas.calculateStandardBmi(heightCm, weightKg)
            val bf = BiometricFormulas.calculateUsNavyBodyFat(
                sex = BiologicalSex.MALE,
                heightCm = heightCm,
                waistCm = waistCm,
                neckCm = 38.5f,
                hipCm = 97.0f
            )
            list.add(
                BmiReading(
                    id = "seed_alex_$index",
                    profileId = "profile_alex",
                    timestampMillis = nowMillis - (daysAgo * dayMillis),
                    heightCm = heightCm,
                    weightKg = weightKg,
                    bmi = bmi,
                    bmiPrime = BiometricFormulas.calculateBmiPrime(bmi, ClassificationStandard.WHO_GLOBAL),
                    ponderalIndex = BiometricFormulas.calculatePonderalIndex(heightCm, weightKg),
                    newOxfordBmi = BiometricFormulas.calculateNewOxfordBmi(heightCm, weightKg),
                    category = BiometricFormulas.classifyBmi(bmi, ClassificationStandard.WHO_GLOBAL),
                    waistCm = waistCm,
                    hipCm = 97.0f,
                    neckCm = 38.5f,
                    bodyFatPct = bf,
                    tags = tags,
                    wellbeingScore = if (index % 3 == 0) 5 else 4,
                    note = note,
                    isPinned = index == 0 || index == 6 || index == 11
                )
            )
        }

        // Seed entries for Maya Lin
        val mayaTrajectory = listOf(
            Triple(28, 59.8f, 69.5f),
            Triple(14, 59.1f, 68.6f),
            Triple(2, 58.6f, 68.0f)
        )
        mayaTrajectory.forEachIndexed { idx, (daysAgo, weightKg, waistCm) ->
            val heightCm = 164.0f
            val bmi = BiometricFormulas.calculateStandardBmi(heightCm, weightKg)
            list.add(
                BmiReading(
                    id = "seed_maya_$idx",
                    profileId = "profile_maya",
                    timestampMillis = nowMillis - (daysAgo * dayMillis),
                    heightCm = heightCm,
                    weightKg = weightKg,
                    bmi = bmi,
                    bmiPrime = BiometricFormulas.calculateBmiPrime(bmi, ClassificationStandard.ASIAN_PACIFIC),
                    ponderalIndex = BiometricFormulas.calculatePonderalIndex(heightCm, weightKg),
                    newOxfordBmi = BiometricFormulas.calculateNewOxfordBmi(heightCm, weightKg),
                    category = BiometricFormulas.classifyBmi(bmi, ClassificationStandard.ASIAN_PACIFIC),
                    waistCm = waistCm,
                    hipCm = 92.0f,
                    neckCm = 32.0f,
                    bodyFatPct = 21.8f - idx * 0.4f,
                    tags = setOf(MeasurementTag.MORNING_FASTED),
                    wellbeingScore = 5,
                    note = "Asian-Pacific standard maintenance tracking",
                    isPinned = idx == 2
                )
            )
        }

        // Seed entries for Jordan Vance (Athlete profile)
        val jordanTrajectory = listOf(
            Triple(30, 90.2f, 84.5f),
            Triple(15, 89.1f, 83.6f),
            Triple(1, 88.4f, 83.0f)
        )
        jordanTrajectory.forEachIndexed { idx, (daysAgo, weightKg, waistCm) ->
            val heightCm = 182.0f
            val bmi = BiometricFormulas.calculateStandardBmi(heightCm, weightKg)
            list.add(
                BmiReading(
                    id = "seed_jordan_$idx",
                    profileId = "profile_jordan",
                    timestampMillis = nowMillis - (daysAgo * dayMillis),
                    heightCm = heightCm,
                    weightKg = weightKg,
                    bmi = bmi,
                    bmiPrime = BiometricFormulas.calculateBmiPrime(bmi, ClassificationStandard.WHO_GLOBAL),
                    ponderalIndex = BiometricFormulas.calculatePonderalIndex(heightCm, weightKg),
                    newOxfordBmi = BiometricFormulas.calculateNewOxfordBmi(heightCm, weightKg),
                    category = BiometricFormulas.classifyBmi(bmi, ClassificationStandard.WHO_GLOBAL),
                    waistCm = waistCm,
                    hipCm = 101.0f,
                    neckCm = 41.0f,
                    bodyFatPct = 14.2f - idx * 0.3f,
                    tags = setOf(MeasurementTag.POST_WORKOUT),
                    wellbeingScore = 5,
                    note = "Strength block—high FFMI lean mass check",
                    isPinned = idx == 2
                )
            )
        }

        return list.sortedByDescending { it.timestampMillis }
    }

    fun upsertProfile(profile: UserProfile) {
        _profiles.update { current ->
            val index = current.indexOfFirst { it.id == profile.id }
            if (index >= 0) {
                current.toMutableList().apply { set(index, profile) }
            } else {
                current + profile
            }
        }
    }

    fun deleteProfile(profileId: String): String {
        if (_profiles.value.size <= 1) return _profiles.value.first().id
        _profiles.update { list -> list.filterNot { it.id == profileId } }
        _readings.update { list -> list.filterNot { it.profileId == profileId } }
        return _profiles.value.first().id
    }

    fun addOrUpdateReading(reading: BmiReading) {
        _readings.update { current ->
            val existingIndex = current.indexOfFirst { it.id == reading.id }
            val updated = if (existingIndex >= 0) {
                current.toMutableList().apply { set(existingIndex, reading) }
            } else {
                current + reading
            }
            updated.sortedByDescending { it.timestampMillis }
        }
    }

    fun deleteReadings(ids: Set<String>): List<BmiReading> {
        val removed = _readings.value.filter { it.id in ids }
        _readings.update { current -> current.filterNot { it.id in ids } }
        return removed
    }

    fun restoreReadings(restored: List<BmiReading>) {
        _readings.update { current ->
            (current + restored)
                .distinctBy { it.id }
                .sortedByDescending { it.timestampMillis }
        }
    }

    fun togglePinReading(readingId: String) {
        _readings.update { current ->
            current.map {
                if (it.id == readingId) it.copy(isPinned = !it.isPinned) else it
            }
        }
    }

    fun assignTagToReadings(ids: Set<String>, tag: MeasurementTag) {
        _readings.update { current ->
            current.map {
                if (it.id in ids) it.copy(tags = it.tags + tag) else it
            }
        }
    }

    fun moveReadingRelative(readingId: String, moveUp: Boolean) {
        _readings.update { current ->
            val mutable = current.toMutableList()
            val idx = mutable.indexOfFirst { it.id == readingId }
            if (idx < 0) return@update current
            val targetIdx = if (moveUp) (idx - 1).coerceAtLeast(0) else (idx + 1).coerceAtMost(mutable.lastIndex)
            if (idx != targetIdx) {
                val item = mutable.removeAt(idx)
                mutable.add(targetIdx, item)
            }
            mutable
        }
    }

    fun resetToDemoDataset() {
        _profiles.value = initialProfiles
        _readings.value = generateRealisticSeedReadings()
    }

    fun clearAllReadingsForProfile(profileId: String): List<BmiReading> {
        val removed = _readings.value.filter { it.profileId == profileId }
        _readings.update { current -> current.filterNot { it.profileId == profileId } }
        return removed
    }

    fun createNewReadingId(): String = "reading_${UUID.randomUUID().toString().take(8)}"
}
