package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.ui.theme.LocalExpressiveMotion

/**
 * Applies a physics-based spring press scale animation and optional tactile haptic feedback.
 */
fun Modifier.expressivePressScale(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    hapticsEnabled: Boolean = true,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    val motion = LocalExpressiveMotion.current
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = motion.snappySpring(),
        label = "expressive_press_scale"
    )

    val pointerModifier = if (enabled) {
        Modifier.pointerInput(enabled) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
    } else {
        Modifier
    }

    val clickModifier = if (onClick != null && enabled) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                if (hapticsEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick()
            }
        )
    } else {
        Modifier
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(pointerModifier)
        .then(clickModifier)
}

@Composable
fun rememberHapticTrigger(enabled: Boolean): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(enabled, haptic) {
        {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }
}
