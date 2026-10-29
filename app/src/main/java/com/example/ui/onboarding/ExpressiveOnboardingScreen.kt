package com.example.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.BiologicalSex
import com.example.model.BiometricFormulas
import com.example.model.ClassificationStandard
import com.example.model.UnitSystem
import com.example.ui.theme.BmiMildThinnessColor
import com.example.ui.theme.BmiNormalOptimalColor
import com.example.ui.theme.BmiObeseClass1Color
import com.example.ui.theme.BmiOverweightColor
import com.example.ui.theme.ExpressiveCornerTokens
import com.example.ui.theme.LocalExpressiveMotion
import com.example.ui.theme.ProfileAccentColors
import com.example.viewmodel.UiIntent
import com.example.viewmodel.UiState
import kotlinx.coroutines.launch

@Composable
fun ExpressiveOnboardingScreen(
    uiState: UiState,
    onIntent: (UiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.onboardingPage,
        pageCount = { 4 }
    )
    val coroutineScope = rememberCoroutineScope()
    val motion = LocalExpressiveMotion.current

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != uiState.onboardingPage) {
            onIntent(UiIntent.SetOnboardingPage(pagerState.currentPage))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Feature #2: Spring-Animated Pager Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("onboarding_pager_indicators")
                    ) {
                        repeat(4) { index ->
                            val isSelected = pagerState.currentPage == index
                            val indicatorWidth by animateDpAsState(
                                targetValue = if (isSelected) 34.dp else 10.dp,
                                animationSpec = motion.dpBouncy(),
                                label = "indicator_width_$index"
                            )
                            val indicatorColor by animateColorAsState(
                                targetValue = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                label = "indicator_color_$index"
                            )
                            Box(
                                modifier = Modifier
                                    .height(10.dp)
                                    .width(indicatorWidth)
                                    .clip(ExpressiveCornerTokens.Pill)
                                    .background(indicatorColor)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage > 0) {
                            FilledTonalIconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("onboarding_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous slide"
                                )
                            }
                        } else {
                            TextButton(
                                onClick = { onIntent(UiIntent.CompleteOnboarding) },
                                modifier = Modifier.testTag("onboarding_skip_button")
                            ) {
                                Text("Skip Setup")
                            }
                        }

                        val isLastPage = pagerState.currentPage == 3
                        Button(
                            onClick = {
                                if (isLastPage) {
                                    onIntent(UiIntent.CompleteOnboarding)
                                } else {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            shape = ExpressiveCornerTokens.Pill,
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("onboarding_next_or_launch_button")
                        ) {
                            AnimatedContent(
                                targetState = isLastPage,
                                transitionSpec = {
                                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                                },
                                label = "onboarding_cta_text"
                            ) { last ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (last) "Launch Workstation" else "Continue",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (last) {
                                            Icons.Default.RocketLaunch
                                        } else {
                                            Icons.AutoMirrored.Filled.ArrowForward
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Brand Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = ExpressiveCornerTokens.Pill,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "M3 Expressive Biometric Studio",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Text(
                    text = "Step ${pagerState.currentPage + 1} of 4",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Feature #1: Interactive 4-Step Onboarding Carousel
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("onboarding_pager")
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (page) {
                            0 -> OnboardingSlideOneHero()
                            1 -> OnboardingSlideTwoBodyLab()
                            2 -> OnboardingSlideThreePreferences(
                                uiState = uiState,
                                onIntent = onIntent
                            )
                            3 -> OnboardingSlideFourBaselineSetup(
                                uiState = uiState,
                                onIntent = onIntent
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingSlideOneHero() {
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expressive Spectrum Gauge Illustration
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 22.dp.toPx()
                    val diameter = size.minDimension - stroke
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    val segments = listOf(
                        BmiMildThinnessColor to 42f,
                        BmiNormalOptimalColor to 68f,
                        BmiOverweightColor to 45f,
                        BmiObeseClass1Color to 45f
                    )
                    var startAngle = 160f
                    segments.forEach { (color, sweep) ->
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweep - 4f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "22.4",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = ExpressiveCornerTokens.Pill,
                        color = BmiNormalOptimalColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "Optimal Zone",
                            style = MaterialTheme.typography.labelMedium,
                            color = BmiNormalOptimalColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "Beyond Single-Formula BMI",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Simultaneously compute Standard WHO BMI, New Oxford height-adjusted BMI, Ponderal Index, BMI Prime, and 4 Ideal Weight equations in real time.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height( androidx.compose.foundation.layout.IntrinsicSize.Min ),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureHighlightPill(
                    title = "8 WHO Tiers",
                    subtitle = "Semantic risk feedback",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(92.dp)
                )
                FeatureHighlightPill(
                    title = "BMI Prime",
                    subtitle = "Optimal limit ratio",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(92.dp)
                )
                FeatureHighlightPill(
                    title = "3 Standards",
                    subtitle = "Global, Asian & 65+",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(92.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingSlideTwoBodyLab() {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Body Composition & Metabolic Lab",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Distinguish lean muscle mass from central adiposity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                shape = ExpressiveCornerTokens.CardCompact,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Why Athlete & Waist Context Matters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "High skeletal muscle mass can elevate raw BMI into the Overweight band even when body fat is low. Our Body Lab pairs BMI with Waist-to-Height Ratio (WHtR), US Navy Body Fat %, and Fat-Free Mass Index (FFMI).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureHighlightPill(
                    title = "WHtR & WHR",
                    subtitle = "Central visceral check",
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                )
                FeatureHighlightPill(
                    title = "FFMI & LBM",
                    subtitle = "Muscularity index",
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureHighlightPill(
                    title = "BMR & TDEE",
                    subtitle = "Mifflin & Katch formulas",
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                )
                FeatureHighlightPill(
                    title = "Goal Pacing",
                    subtitle = "Milestone projections",
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingSlideThreePreferences(
    uiState: UiState,
    onIntent: (UiIntent) -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Select Measurement Standards",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Configure your preferred units & clinical thresholds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Feature #3: Baseline Unit Preference Seeding in Onboarding
            Text(
                text = "Measurement Unit System",
                style = MaterialTheme.typography.titleMedium
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_unit_selector")
            ) {
                UnitSystem.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = uiState.unitSystem == unit,
                        onClick = { onIntent(UiIntent.SetUnitSystem(unit)) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = UnitSystem.entries.size
                        )
                    ) {
                        Text(unit.label, maxLines = 1, softWrap = false)
                    }
                }
            }

            Text(
                text = "Clinical BMI Reference Standard",
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ClassificationStandard.entries.forEach { standard ->
                    val isSelected = uiState.classificationStandard == standard
                    Card(
                        shape = ExpressiveCornerTokens.CardCompact,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onIntent(UiIntent.SetClassificationStandard(standard)) }
                            .testTag("onboarding_standard_${standard.name.lowercase()}")
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
                                    text = standard.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = standard.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingSlideFourBaselineSetup(
    uiState: UiState,
    onIntent: (UiIntent) -> Unit
) {
    val motion = LocalExpressiveMotion.current
    val animatedBmi by animateFloatAsState(
        targetValue = uiState.standardBmi,
        animationSpec = motion.floatBouncy(),
        label = "onboarding_live_bmi"
    )

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(uiState.onboardingDraftAccentHex),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.onboardingDraftBadge.ifBlank { "AR" },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Personal Baseline Setup",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Customize your primary profile & initial reading",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Feature #4: Initial Profile Quick-Setup in Onboarding
            OutlinedTextField(
                value = uiState.onboardingDraftName,
                onValueChange = { newName ->
                    val initials = newName.trim().split(" ")
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .joinToString("")
                        .take(2)
                        .ifEmpty { "AR" }
                    onIntent(
                        UiIntent.UpdateOnboardingDraft(
                            name = newName,
                            badge = initials,
                            accentHex = uiState.onboardingDraftAccentHex
                        )
                    )
                },
                label = { Text("Profile Name") },
                singleLine = true,
                shape = ExpressiveCornerTokens.CardCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_profile_name_input")
            )

            Text(
                text = "Avatar Badge Accent",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileAccentColors.forEach { hex ->
                    val isSelected = uiState.onboardingDraftAccentHex == hex
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                onIntent(
                                    UiIntent.UpdateOnboardingDraft(
                                        name = uiState.onboardingDraftName,
                                        badge = uiState.onboardingDraftBadge,
                                        accentHex = hex
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected color",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Initial Height & Weight Sliders with Live BMI Readout
            val heightText = if (uiState.unitSystem == UnitSystem.METRIC) {
                "${String.format("%.1f", uiState.heightCm)} cm"
            } else {
                val (ft, inches) = BiometricFormulas.cmToFeetInches(uiState.heightCm)
                "${ft}' ${String.format("%.1f", inches)}\""
            }
            val weightText = if (uiState.unitSystem == UnitSystem.METRIC) {
                "${String.format("%.1f", uiState.weightKg)} kg"
            } else {
                "${String.format("%.1f", BiometricFormulas.kgToLbs(uiState.weightKg))} lbs"
            }

            Card(
                shape = ExpressiveCornerTokens.CardCompact,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Baseline Height", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = heightText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.heightCm,
                        onValueChange = { onIntent(UiIntent.UpdateHeightCm(it)) },
                        valueRange = 120f..220f,
                        modifier = Modifier.testTag("onboarding_height_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Baseline Weight", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = weightText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = uiState.weightKg,
                        onValueChange = { onIntent(UiIntent.UpdateWeightKg(it)) },
                        valueRange = 35f..180f,
                        modifier = Modifier.testTag("onboarding_weight_slider")
                    )
                }
            }

            // Live BMI Preview Pill
            Surface(
                shape = ExpressiveCornerTokens.CardCompact,
                color = uiState.currentCategory.color.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live Baseline BMI Preview",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = uiState.currentCategory.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = uiState.currentCategory.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = String.format("%.1f", animatedBmi),
                        style = MaterialTheme.typography.headlineLarge,
                        color = uiState.currentCategory.color
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureHighlightPill(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = ExpressiveCornerTokens.CardCompact,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
