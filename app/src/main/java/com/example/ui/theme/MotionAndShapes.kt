package com.example.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive (M3E) Shape Tokens
 * Uses generous 24.dp - 32.dp surface curvature and full pill interactive controls.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object ExpressiveCornerTokens {
    val CardCompact = RoundedCornerShape(24.dp)
    val CardExpressive = RoundedCornerShape(28.dp)
    val HeroContainer = RoundedCornerShape(32.dp)
    val SheetTop = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val Pill = CircleShape
}

/**
 * Material 3 Expressive Motion & Physics Tokens
 * Supports both Bouncy Spring physics and accessibility Reduced Motion fallbacks.
 */
@Immutable
data class ExpressiveMotionSpecs(
    val reducedMotion: Boolean = false
) {
    fun <T> bouncySpring(): AnimationSpec<T> =
        if (reducedMotion) tween(durationMillis = 180)
        else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )

    fun <T> snappySpring(): AnimationSpec<T> =
        if (reducedMotion) tween(durationMillis = 150)
        else spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

    fun floatBouncy(): AnimationSpec<Float> =
        if (reducedMotion) tween(durationMillis = 200)
        else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )

    fun dpBouncy(): AnimationSpec<Dp> =
        if (reducedMotion) tween(durationMillis = 200)
        else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )

    fun offsetSpring(): AnimationSpec<IntOffset> =
        if (reducedMotion) tween(durationMillis = 200)
        else spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
}

val LocalExpressiveMotion = staticCompositionLocalOf { ExpressiveMotionSpecs() }
