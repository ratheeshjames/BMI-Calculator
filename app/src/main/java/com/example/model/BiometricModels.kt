package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BmiMildThinnessColor
import com.example.ui.theme.BmiModerateThinnessColor
import com.example.ui.theme.BmiNormalOptimalColor
import com.example.ui.theme.BmiObeseClass1Color
import com.example.ui.theme.BmiObeseClass2Color
import com.example.ui.theme.BmiObeseClass3Color
import com.example.ui.theme.BmiOverweightColor
import com.example.ui.theme.BmiSevereThinnessColor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class AppDestination(
    val routeId: String,
    val title: String,
    val shortLabel: String,
    val subtitle: String,
    val fabLabel: String
) {
    CALCULATOR_HUB(
        routeId = "calculator_hub",
        title = "Biometric BMI Workstation",
        shortLabel = "Calculator",
        subtitle = "Precision height, weight & multi-standard category engine",
        fabLabel = "Log Reading"
    ),
    BODY_COMP_LAB(
        routeId = "body_comp_lab",
        title = "Body Composition & Metabolic Lab",
        shortLabel = "Body Lab",
        subtitle = "WHtR, Navy Body Fat %, FFMI, BMR & TDEE macros",
        fabLabel = "Save Body Lab"
    ),
    HISTORY_JOURNAL(
        routeId = "history_journal",
        title = "Longitudinal History Journal",
        shortLabel = "Journal",
        subtitle = "Chronological logs, multi-tag filters & diff comparison",
        fabLabel = "New Entry"
    ),
    GOAL_SIMULATOR(
        routeId = "goal_simulator",
        title = "Goal Pacing & What-If Simulator",
        shortLabel = "Goals",
        subtitle = "Target BMI trajectory, calorie pacing & step milestones",
        fabLabel = "Commit Target"
    ),
    ANALYTICS_STUDIO(
        routeId = "analytics_studio",
        title = "Analytics & Nomogram Studio",
        shortLabel = "Analytics",
        subtitle = "Canvas trends, moving averages & 2D BMI nomogram",
        fabLabel = "Health Report"
    ),
    SETTINGS_CENTER(
        routeId = "settings_center",
        title = "Personalization & Clinical Reference",
        shortLabel = "Settings",
        subtitle = "M3 Expressive theme, standards, accessibility & CSV/JSON",
        fabLabel = "Export Data"
    )
}

enum class UnitSystem(val label: String, val shortLabel: String) {
    METRIC("Metric (cm / kg)", "Metric"),
    IMPERIAL("Imperial (ft·in / lbs)", "Imperial")
}

enum class ClassificationStandard(
    val label: String,
    val shortLabel: String,
    val normalMin: Float,
    val normalMax: Float,
    val overweightMax: Float,
    val description: String
) {
    WHO_GLOBAL(
        label = "WHO Global Standard",
        shortLabel = "WHO Global",
        normalMin = 18.5f,
        normalMax = 24.9f,
        overweightMax = 29.9f,
        description = "World Health Organization international adult BMI thresholds (18.5 – 24.9 kg/m²)."
    ),
    ASIAN_PACIFIC(
        label = "WHO Asian-Pacific",
        shortLabel = "Asian-Pacific",
        normalMin = 18.5f,
        normalMax = 22.9f,
        overweightMax = 24.9f,
        description = "Adjusted WHO Western Pacific thresholds reflecting higher cardiometabolic sensitivity (18.5 – 22.9 kg/m²)."
    ),
    SENIOR_65_PLUS(
        label = "Senior 65+ Protective",
        shortLabel = "Senior 65+",
        normalMin = 23.0f,
        normalMax = 27.9f,
        overweightMax = 31.9f,
        description = "Geriatric clinical reference range (23.0 – 27.9 kg/m²) associated with bone & lean mass resilience."
    )
}

enum class BiologicalSex(val label: String) {
    FEMALE("Female"),
    MALE("Male"),
    UNSPECIFIED("Unspecified")
}

enum class BodyFrameSize(val label: String, val weightMultiplier: Float, val hint: String) {
    SMALL("Small Frame", 0.90f, "-10% ideal weight adjustment"),
    MEDIUM("Medium Frame", 1.00f, "Standard reference frame"),
    LARGE("Large Frame", 1.10f, "+10% ideal weight adjustment")
}

enum class LimbAmputationSegment(val label: String, val bodyWeightPercent: Float) {
    HAND("Hand (0.8%)", 0.008f),
    FOREARM_AND_HAND("Forearm & Hand (2.3%)", 0.023f),
    ENTIRE_ARM("Entire Arm (5.0%)", 0.050f),
    FOOT("Foot (1.5%)", 0.015f),
    LOWER_LEG_AND_FOOT("Lower Leg & Foot (5.9%)", 0.059f),
    ENTIRE_LEG("Entire Leg (16.0%)", 0.160f)
}

enum class BmiCategory(
    val label: String,
    val shortLabel: String,
    val color: Color,
    val summary: String,
    val clinicalGuidance: String,
    val actionSteps: List<String>
) {
    SEVERE_THINNESS(
        label = "Severe Thinness",
        shortLabel = "Severe Thin",
        color = BmiSevereThinnessColor,
        summary = "Significantly below optimal body mass reserve.",
        clinicalGuidance = "Indicates depleted energy reserves that can impact immunity, bone mineral density, and thermoregulation.",
        actionSteps = listOf(
            "Consult a primary care clinician or registered dietitian for a structured nutrition evaluation.",
            "Prioritize nutrient-dense, caloric-surplus meals (+300 to +500 kcal/day) rich in healthy fats and complete proteins.",
            "Track weekly trend stability using the Morning Fasted tag."
        )
    ),
    MODERATE_THINNESS(
        label = "Moderate Thinness",
        shortLabel = "Mod. Thin",
        color = BmiModerateThinnessColor,
        summary = "Moderately below baseline healthy weight range.",
        clinicalGuidance = "Associated with lower lean muscle reserve and potential micronutrient gaps.",
        actionSteps = listOf(
            "Aim for gradual lean mass accretion of 0.25–0.4 kg per week.",
            "Pair progressive resistance training with 1.6g/kg daily protein intake.",
            "Monitor Fat-Free Mass Index (FFMI) in the Body Composition Lab."
        )
    ),
    MILD_THINNESS(
        label = "Mild Thinness",
        shortLabel = "Underweight",
        color = BmiMildThinnessColor,
        summary = "Slightly below the lower normal BMI threshold.",
        clinicalGuidance = "Can be constitutional in naturally ectomorphic frames; evaluate alongside energy levels and FFMI.",
        actionSteps = listOf(
            "Verify frame size adjustment (Small Frame reduces baseline ideal weight by ~10%).",
            "Add one balanced high-protein snack daily if aiming to reach BMI Prime 0.85+.",
            "Check Waist-to-Height Ratio and Lean Body Mass balance."
        )
    ),
    NORMAL(
        label = "Normal / Optimal",
        shortLabel = "Normal",
        color = BmiNormalOptimalColor,
        summary = "Within the protective reference BMI corridor.",
        clinicalGuidance = "Associated with the lowest statistical incidence of weight-related cardiometabolic conditions.",
        actionSteps = listOf(
            "Cross-check Waist-to-Height Ratio (< 0.50) to confirm healthy central visceral distribution.",
            "Maintain TDEE caloric equilibrium and consistent resistance + aerobic conditioning.",
            "Log periodic monthly snapshots to preserve longitudinal stability."
        )
    ),
    OVERWEIGHT(
        label = "Overweight / Elevated",
        shortLabel = "Overweight",
        color = BmiOverweightColor,
        summary = "Above standard BMI threshold; evaluate muscularity vs. adiposity.",
        clinicalGuidance = "Raw BMI does not distinguish skeletal muscle from fat tissue. Cross-reference WHtR and FFMI.",
        actionSteps = listOf(
            "Measure Waist-to-Height Ratio in the Body Lab—if WHtR < 0.50 and FFMI is high, elevated BMI likely reflects lean mass.",
            "If central adiposity is elevated, a modest 300–500 kcal/day deficit yields sustainable 0.4 kg/week reduction.",
            "Use the Goal Simulator to model a 5%–7% baseline weight optimization."
        )
    ),
    OBESE_CLASS_I(
        label = "Obese Class I",
        shortLabel = "Obese I",
        color = BmiObeseClass1Color,
        summary = "Class I clinical adiposity threshold.",
        clinicalGuidance = "Elevated mechanical and metabolic load; modest 5%–10% weight reduction delivers major cardiometabolic benefits.",
        actionSteps = listOf(
            "Focus on progressive 5% step milestones in the Goal Simulator rather than an abrupt end-goal.",
            "Prioritize high-satiety protein (1.6–2.0g per kg of ideal body weight) and daily walking step cadence.",
            "Track waist circumference alongside weight—waist often drops even during muscle recomposition."
        )
    ),
    OBESE_CLASS_II(
        label = "Obese Class II",
        shortLabel = "Obese II",
        color = BmiObeseClass2Color,
        summary = "Class II clinical adiposity threshold.",
        clinicalGuidance = "Higher cardiometabolic and joint load risk; structured multidisciplinary lifestyle support is beneficial.",
        actionSteps = listOf(
            "Set a sustainable pacing target (0.5–0.75 kg/week) with intermediate 2.5 kg milestone checkpoints.",
            "Combine low-impact resistance and zone-2 cardiovascular activity to protect lean mass.",
            "Generate a Clinical Summary Report Card to review trends with your healthcare provider."
        )
    ),
    OBESE_CLASS_III(
        label = "Obese Class III",
        shortLabel = "Obese III",
        color = BmiObeseClass3Color,
        summary = "Class III high-priority clinical threshold.",
        clinicalGuidance = "Warrants comprehensive medical partnership, metabolic screening, and personalized care pacing.",
        actionSteps = listOf(
            "Partner with a clinical specialist to tailor nutrition, mobility, and metabolic support.",
            "Celebrate every 2%–3% reduction in BMI Prime and waist circumference.",
            "Use consistent Morning Fasted logging to filter out daily hydration noise."
        )
    )
}

enum class MeasurementTag(val label: String, val iconName: String) {
    MORNING_FASTED("Morning Fasted", "wb_sunny"),
    POST_WORKOUT("Post-Workout", "fitness_center"),
    EVENING("Evening", "nights_stay"),
    HIGH_SODIUM("High Sodium Meal", "restaurant"),
    TRAVEL("Travel / Away", "flight"),
    CLINICAL_CHECK("Clinical Scale", "local_hospital")
}

enum class ActivityLevel(
    val label: String,
    val subtitle: String,
    val multiplier: Float
) {
    SEDENTARY("Sedentary", "Desk job, minimal planned exercise", 1.20f),
    LIGHT("Lightly Active", "Light exercise or 6,000–8,000 steps 1–3 days/wk", 1.375f),
    MODERATE("Moderately Active", "Structured training 3–5 days/wk", 1.55f),
    VERY_ACTIVE("Very Active", "Hard training or physical work 6–7 days/wk", 1.725f),
    EXTRA_ACTIVE("Extra Active", "Endurance athlete or dual daily sessions", 1.90f)
}

enum class MacroPreset(
    val label: String,
    val proteinPct: Int,
    val carbsPct: Int,
    val fatPct: Int
) {
    BALANCED("Balanced (30P / 40C / 30F)", 30, 40, 30),
    HIGH_PROTEIN("High Protein (40P / 35C / 25F)", 40, 35, 25),
    LOW_CARB("Lower Carb (35P / 20C / 45F)", 35, 20, 45),
    ENDURANCE("Athletic Carb (25P / 55C / 20F)", 25, 55, 20)
}

enum class BmrFormula(val label: String, val description: String) {
    MIFFLIN_ST_JEOR("Mifflin-St Jeor", "Gold-standard clinical equation using weight, height, age & sex."),
    KATCH_MCARDLE("Katch-McArdle", "Lean-mass driven equation factoring in estimated Body Fat %.")
}

enum class BodyFatMethod(val label: String) {
    US_NAVY("US Navy Tape Method"),
    DEURENBERG_BMI("Deurenberg BMI Formula")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class EnergyUnit(val label: String, val multiplierFromKcal: Float) {
    KCAL("kcal", 1.0f),
    KJ("kJ", 4.184f)
}

enum class HistorySortOrder(val label: String) {
    NEWEST_FIRST("Newest First"),
    OLDEST_FIRST("Oldest First"),
    LOWEST_BMI("Lowest BMI"),
    HIGHEST_BMI("Highest BMI"),
    LARGEST_DELTA("Largest Change"),
    CUSTOM_ORDER("Custom Priority")
}

enum class DateWindowFilter(val label: String, val days: Int?) {
    DAYS_7("7 Days", 7),
    DAYS_30("30 Days", 30),
    DAYS_90("90 Days", 90),
    YEAR_1("1 Year", 365),
    ALL_TIME("All Time", null)
}

@Immutable
data class UserProfile(
    val id: String,
    val name: String,
    val avatarBadge: String,
    val accentColorHex: Long,
    val baselineHeightCm: Float,
    val baselineWeightKg: Float,
    val ageYears: Int,
    val biologicalSex: BiologicalSex,
    val frameSize: BodyFrameSize = BodyFrameSize.MEDIUM,
    val wristCm: Float = 17.0f,
    val waistCm: Float = 78.0f,
    val hipCm: Float = 96.0f,
    val neckCm: Float = 37.0f,
    val isAthleteMode: Boolean = false,
    val targetBmi: Float = 22.0f,
    val targetWeightKg: Float = 68.0f,
    val weeklyPaceKg: Float = 0.5f
)

@Immutable
data class BmiReading(
    val id: String,
    val profileId: String,
    val timestampMillis: Long,
    val heightCm: Float,
    val weightKg: Float,
    val bmi: Float,
    val bmiPrime: Float,
    val ponderalIndex: Float,
    val newOxfordBmi: Float,
    val category: BmiCategory,
    val waistCm: Float = 78.0f,
    val hipCm: Float = 96.0f,
    val neckCm: Float = 37.0f,
    val bodyFatPct: Float = 19.5f,
    val tags: Set<MeasurementTag> = emptySet(),
    val wellbeingScore: Int = 4, // 1..5
    val note: String = "",
    val isPinned: Boolean = false
)

@Immutable
data class IdealWeightBreakdown(
    val devineKg: Float,
    val robinsonKg: Float,
    val millerKg: Float,
    val hamwiKg: Float,
    val consensusMeanKg: Float,
    val healthyMinKg: Float,
    val healthyMaxKg: Float
)

@Immutable
data class StepMilestone(
    val stepIndex: Int,
    val title: String,
    val targetWeightKg: Float,
    val targetBmi: Float,
    val weeksToReach: Float,
    val isReached: Boolean
)

@Immutable
data class WhatIfRow(
    val deltaKg: Float,
    val projectedWeightKg: Float,
    val projectedBmi: Float,
    val projectedPrime: Float,
    val category: BmiCategory
)

/**
 * Pure domain calculation engine supporting all 100 features.
 */
object BiometricFormulas {

    fun calculateStandardBmi(heightCm: Float, weightKg: Float): Float {
        val heightM = (heightCm / 100f).coerceAtLeast(0.5f)
        return (weightKg / (heightM * heightM)).coerceIn(8f, 90f)
    }

    fun calculateNewOxfordBmi(heightCm: Float, weightKg: Float): Float {
        val heightM = (heightCm / 100f).coerceAtLeast(0.5f)
        return (1.3f * weightKg / heightM.pow(2.5f)).coerceIn(8f, 90f)
    }

    fun calculatePonderalIndex(heightCm: Float, weightKg: Float): Float {
        val heightM = (heightCm / 100f).coerceAtLeast(0.5f)
        return (weightKg / heightM.pow(3f)).coerceIn(4f, 60f)
    }

    fun calculateBmiPrime(bmi: Float, standard: ClassificationStandard): Float {
        return (bmi / standard.normalMax).coerceIn(0.2f, 4.0f)
    }

    fun calculateAmputationAdjustedWeight(
        rawWeightKg: Float,
        segments: Set<LimbAmputationSegment>
    ): Float {
        val missingFraction = segments.sumOf { it.bodyWeightPercent.toDouble() }.toFloat().coerceAtMost(0.45f)
        if (missingFraction <= 0f) return rawWeightKg
        return rawWeightKg / (1f - missingFraction)
    }

    fun classifyBmi(
        bmi: Float,
        standard: ClassificationStandard = ClassificationStandard.WHO_GLOBAL
    ): BmiCategory {
        return when {
            bmi < 16.0f -> BmiCategory.SEVERE_THINNESS
            bmi < 17.0f -> BmiCategory.MODERATE_THINNESS
            bmi < standard.normalMin -> BmiCategory.MILD_THINNESS
            bmi <= standard.normalMax -> BmiCategory.NORMAL
            bmi <= standard.overweightMax -> BmiCategory.OVERWEIGHT
            bmi <= standard.overweightMax + 5.0f -> BmiCategory.OBESE_CLASS_I
            bmi <= standard.overweightMax + 10.0f -> BmiCategory.OBESE_CLASS_II
            else -> BmiCategory.OBESE_CLASS_III
        }
    }

    fun calculateHealthyWeightRange(
        heightCm: Float,
        standard: ClassificationStandard,
        frameSize: BodyFrameSize
    ): Pair<Float, Float> {
        val heightM = (heightCm / 100f).coerceAtLeast(0.5f)
        val rawMin = standard.normalMin * heightM * heightM
        val rawMax = standard.normalMax * heightM * heightM
        // Frame size subtly shifts optimal target corridor
        val frameOffset = (frameSize.weightMultiplier - 1.0f) * 0.35f
        return Pair(
            (rawMin * (1f + frameOffset)).coerceAtLeast(20f),
            (rawMax * (1f + frameOffset)).coerceAtLeast(25f)
        )
    }

    fun calculateIdealWeightBreakdown(
        heightCm: Float,
        sex: BiologicalSex,
        standard: ClassificationStandard,
        frameSize: BodyFrameSize
    ): IdealWeightBreakdown {
        val inchesOver5Ft = ((heightCm / 2.54f) - 60f).coerceAtLeast(0f)
        val isMale = sex == BiologicalSex.MALE
        val devine = (if (isMale) 50.0f + 2.3f * inchesOver5Ft else 45.5f + 2.3f * inchesOver5Ft) * frameSize.weightMultiplier
        val robinson = (if (isMale) 52.0f + 1.9f * inchesOver5Ft else 49.0f + 1.7f * inchesOver5Ft) * frameSize.weightMultiplier
        val miller = (if (isMale) 56.2f + 1.41f * inchesOver5Ft else 53.1f + 1.36f * inchesOver5Ft) * frameSize.weightMultiplier
        val hamwi = (if (isMale) 48.0f + 2.7f * inchesOver5Ft else 45.5f + 2.2f * inchesOver5Ft) * frameSize.weightMultiplier
        val mean = (devine + robinson + miller + hamwi) / 4f
        val (minHealthy, maxHealthy) = calculateHealthyWeightRange(heightCm, standard, frameSize)
        return IdealWeightBreakdown(
            devineKg = devine,
            robinsonKg = robinson,
            millerKg = miller,
            hamwiKg = hamwi,
            consensusMeanKg = mean,
            healthyMinKg = minHealthy,
            healthyMaxKg = maxHealthy
        )
    }

    fun calculateBodySurfaceAreaMosteller(heightCm: Float, weightKg: Float): Float {
        return sqrt((heightCm * weightKg) / 3600f).coerceIn(0.4f, 4.0f)
    }

    fun calculateBodySurfaceAreaDuBois(heightCm: Float, weightKg: Float): Float {
        return (0.007184f * heightCm.pow(0.725f) * weightKg.pow(0.425f)).coerceIn(0.4f, 4.0f)
    }

    fun estimateFrameFromWrist(heightCm: Float, wristCm: Float, sex: BiologicalSex): BodyFrameSize {
        if (wristCm <= 5f) return BodyFrameSize.MEDIUM
        val r = heightCm / wristCm
        return when (sex) {
            BiologicalSex.MALE -> when {
                r > 10.4f -> BodyFrameSize.SMALL
                r < 9.6f -> BodyFrameSize.LARGE
                else -> BodyFrameSize.MEDIUM
            }
            else -> when {
                r > 11.0f -> BodyFrameSize.SMALL
                r < 10.1f -> BodyFrameSize.LARGE
                else -> BodyFrameSize.MEDIUM
            }
        }
    }

    fun calculateWaistToHeightRatio(waistCm: Float, heightCm: Float): Float {
        return (waistCm / heightCm.coerceAtLeast(100f)).coerceIn(0.25f, 1.2f)
    }

    fun calculateWaistToHipRatio(waistCm: Float, hipCm: Float): Float {
        return (waistCm / hipCm.coerceAtLeast(50f)).coerceIn(0.45f, 1.5f)
    }

    fun calculateUsNavyBodyFat(
        sex: BiologicalSex,
        heightCm: Float,
        waistCm: Float,
        neckCm: Float,
        hipCm: Float
    ): Float {
        val h = heightCm.coerceAtLeast(120f).toDouble()
        val w = waistCm.coerceAtLeast(45f).toDouble()
        val n = neckCm.coerceAtLeast(25f).toDouble()
        val hip = hipCm.coerceAtLeast(55f).toDouble()

        val bf = if (sex == BiologicalSex.FEMALE) {
            val diff = (w + hip - n).coerceAtLeast(20.0)
            163.205 * log10(diff) - 97.684 * log10(h) - 78.387
        } else {
            val diff = (w - n).coerceAtLeast(12.0)
            86.010 * log10(diff) - 70.041 * log10(h) + 36.76
        }
        return bf.toFloat().coerceIn(4.0f, 55.0f)
    }

    fun calculateDeurenbergBodyFat(bmi: Float, ageYears: Int, sex: BiologicalSex): Float {
        val sexFactor = if (sex == BiologicalSex.MALE) 1f else 0f
        val bf = (1.20f * bmi) + (0.23f * ageYears) - (10.8f * sexFactor) - 5.4f
        return bf.coerceIn(4.0f, 55.0f)
    }

    fun calculateLeanBodyMass(weightKg: Float, bodyFatPct: Float): Pair<Float, Float> {
        val fatMass = (weightKg * (bodyFatPct / 100f)).coerceIn(1f, weightKg * 0.7f)
        val leanMass = (weightKg - fatMass).coerceAtLeast(15f)
        return Pair(leanMass, fatMass)
    }

    fun calculateFfmi(leanBodyMassKg: Float, heightCm: Float): Pair<Float, Float> {
        val heightM = (heightCm / 100f).coerceAtLeast(0.5f)
        val rawFfmi = (leanBodyMassKg / (heightM * heightM)).coerceIn(10f, 35f)
        val normalizedFfmi = (rawFfmi + 6.1f * (1.8f - heightM)).coerceIn(10f, 35f)
        return Pair(rawFfmi, normalizedFfmi)
    }

    fun calculateBmr(
        weightKg: Float,
        heightCm: Float,
        ageYears: Int,
        sex: BiologicalSex,
        leanBodyMassKg: Float,
        formula: BmrFormula
    ): Float {
        return when (formula) {
            BmrFormula.KATCH_MCARDLE -> (370f + (21.6f * leanBodyMassKg)).coerceIn(800f, 4500f)
            BmrFormula.MIFFLIN_ST_JEOR -> {
                val sexOffset = when (sex) {
                    BiologicalSex.MALE -> 5f
                    BiologicalSex.FEMALE -> -161f
                    BiologicalSex.UNSPECIFIED -> -78f
                }
                ((10f * weightKg) + (6.25f * heightCm) - (5f * ageYears) + sexOffset).coerceIn(800f, 4500f)
            }
        }
    }

    fun calculatePediatricPercentile(bmi: Float, ageYears: Int, sex: BiologicalSex): Int {
        // Smooth clinical approximation for ages 2..19 CDC median & SD curves
        val clampedAge = ageYears.coerceIn(2, 19)
        val medianBmi = 15.2f + (clampedAge - 2) * 0.42f + (if (sex == BiologicalSex.MALE) 0.2f else 0.0f)
        val sd = 1.6f + (clampedAge - 2) * 0.11f
        val z = (bmi - medianBmi) / sd
        val percentile = (50f + (z * 34f)).roundToInt().coerceIn(1, 99)
        return percentile
    }

    fun cmToFeetInches(cm: Float): Pair<Int, Float> {
        val totalInches = cm / 2.54f
        val feet = (totalInches / 12f).toInt()
        val inches = (totalInches - feet * 12f).coerceIn(0f, 11.9f)
        return Pair(feet, (inches * 2f).roundToInt() / 2f)
    }

    fun feetInchesToCm(feet: Int, inches: Float): Float {
        return ((feet * 12f + inches) * 2.54f).coerceIn(100f, 240f)
    }

    fun kgToLbs(kg: Float): Float = kg * 2.20462f
    fun lbsToKg(lbs: Float): Float = (lbs / 2.20462f).coerceIn(25f, 250f)

    fun kgToStonesAndLbs(kg: Float): Pair<Int, Float> {
        val totalLbs = kgToLbs(kg)
        val stones = (totalLbs / 14f).toInt()
        val remLbs = totalLbs - (stones * 14f)
        return Pair(stones, (remLbs * 10f).roundToInt() / 10f)
    }
}
