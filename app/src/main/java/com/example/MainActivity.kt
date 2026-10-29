package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ThemeMode
import com.example.ui.onboarding.ExpressiveOnboardingScreen
import com.example.ui.shell.RootAdaptiveShell
import com.example.ui.theme.BmiCalculatorTheme
import com.example.viewmodel.BmiViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.flow.emptyFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val bmiViewModel: BmiViewModel = viewModel()
            val uiState by bmiViewModel.uiState.collectAsStateWithLifecycle()

            val darkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            BmiCalculatorTheme(
                darkTheme = darkTheme,
                dynamicColor = uiState.useDynamicColor,
                highContrastMode = uiState.highContrastMode,
                reducedMotion = uiState.reducedMotionMode
            ) {
                // Feature #5: Dynamic Container-Morph Transition from Onboarding into Root Shell
                AnimatedContent(
                    targetState = uiState.hasCompletedOnboarding,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(320)) + scaleIn(initialScale = 0.94f))
                            .togetherWith(fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 1.04f))
                    },
                    label = "root_onboarding_to_shell_morph",
                    modifier = Modifier.fillMaxSize()
                ) { completed ->
                    if (!completed) {
                        ExpressiveOnboardingScreen(
                            uiState = uiState,
                            onIntent = bmiViewModel::onIntent
                        )
                    } else {
                        RootAdaptiveShell(
                            uiState = uiState,
                            uiEvents = bmiViewModel.uiEvents,
                            onIntent = bmiViewModel::onIntent
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Onboarding - Light", showBackground = true)
@Preview(
    name = "Onboarding - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun OnboardingPreview() {
    BmiCalculatorTheme {
        ExpressiveOnboardingScreen(
            uiState = UiState(hasCompletedOnboarding = false),
            onIntent = {}
        )
    }
}

@Preview(name = "Root Shell - Light", showBackground = true)
@Preview(
    name = "Root Shell - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun RootAdaptiveShellPreview() {
    BmiCalculatorTheme {
        RootAdaptiveShell(
            uiState = UiState(hasCompletedOnboarding = true),
            uiEvents = emptyFlow(),
            onIntent = {}
        )
    }
}
