package com.example

import com.example.model.BiologicalSex
import com.example.model.BiometricFormulas
import com.example.model.BmiCategory
import com.example.model.ClassificationStandard
import com.example.model.UnitSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `standard and new oxford bmi formulas compute accurately`() {
        val bmi = BiometricFormulas.calculateStandardBmi(heightCm = 176f, weightKg = 74.2f)
        assertEquals(23.95f, bmi, 0.05f)

        val oxfordBmi = BiometricFormulas.calculateNewOxfordBmi(heightCm = 176f, weightKg = 74.2f)
        assertTrue(oxfordBmi in 23.0f..24.5f)

        val globalCategory = BiometricFormulas.classifyBmi(bmi, ClassificationStandard.WHO_GLOBAL)
        assertEquals(BmiCategory.NORMAL, globalCategory)

        val asianCategory = BiometricFormulas.classifyBmi(bmi, ClassificationStandard.ASIAN_PACIFIC)
        assertEquals(BmiCategory.OVERWEIGHT, asianCategory)
    }

    @Test
    fun `unit conversions are lossless within rounding tolerance`() {
        val (feet, inches) = BiometricFormulas.cmToFeetInches(180.34f)
        assertEquals(5, feet)
        assertEquals(11.0f, inches, 0.5f)

        val lbs = BiometricFormulas.kgToLbs(70f)
        val backToKg = BiometricFormulas.lbsToKg(lbs)
        assertEquals(70f, backToKg, 0.05f)
    }

    @Test
    fun `body composition and metabolic equations return valid physiological ranges`() {
        val whtr = BiometricFormulas.calculateWaistToHeightRatio(waistCm = 80f, heightCm = 176f)
        assertEquals(0.454f, whtr, 0.01f)

        val navyBf = BiometricFormulas.calculateUsNavyBodyFat(
            sex = BiologicalSex.MALE,
            heightCm = 176f,
            waistCm = 81f,
            neckCm = 38.5f,
            hipCm = 97f
        )
        assertTrue(navyBf in 10f..25f)
        assertEquals("Metric", UnitSystem.METRIC.shortLabel)
    }
}
