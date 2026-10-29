# BMI Calculator — Clinical Biometric & Body Composition Workstation

![Android API](https://img.shields.io/badge/Android-API%2024%2B%20(7.0%2B)-006B58?style=for-the-badge&logo=android&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3 Expressive](https://img.shields.io/badge/Material%203-Expressive%20(M3E)-00866E?style=for-the-badge&logo=materialdesign&logoColor=white)
![License](https://img.shields.io/badge/License-Apache%202.0-1D6F5E?style=for-the-badge)
![Build Status](https://img.shields.io/badge/CI%2FCD-Passing%20(120%20FPS%20QA)-15803D?style=for-the-badge&logo=githubactions&logoColor=white)

**BMI Calculator** is a flagship Android application engineered with **Jetpack Compose** and **Material 3 Expressive (M3E)** design guidelines. It transforms height-and-weight BMI calculation into a 100-feature clinical biometric workstation covering **8-Tier WHO & Asian-Pacific Classifications**, **BMI Prime**, **New Oxford BMI (Exponent 2.5)**, **Ponderal Index**, **US Navy Tape Body Fat %**, **Fat-Free Mass Index (FFMI)**, **Mifflin-St Jeor & Katch-McArdle BMR/TDEE**, **Longitudinal Trend Scrubbing**, and **Interactive Goal Pacing Simulation**.

---

## Visual Highlights & Material 3 Expressive Philosophy

- **Semantic Biometric Tonal Spectrum**: Dynamic container colors, gauge arcs, and status badges smoothly morph across 8 physiological zones—from *Severe Thinness* (`#2563EB`), *Normal Optimal* (`#15803D`), and *Overweight* (`#D97706`) to *Obese Class I–III* (`#EA580C` → `#991B1B`).
- **Physics-Based Spring Motion (`ExpressiveMotionSpecs`)**: Zero abrupt linear jumps—all BMI readouts, custom Canvas spectrum needles, Lean-vs-Fat split bars, and priority drag cards respond with tuned spring physics (`Spring.DampingRatioMediumBouncy`, `Spring.StiffnessLow`) and instant Reduced-Motion accessibility support.
- **Custom Local Typography**: Pairs geometric **Space Grotesk** display numerals for clinical readouts with high-legibility **Plus Jakarta Sans** body and label copy.
- **120 FPS Frame-Paced Rendering**: `@Immutable` state contracts, draw-phase `graphicsLayer` press-scale modifiers, pre-allocated date formatters, and `IntrinsicSize.Min` locked multi-card rows eliminate layout thrashing.

---

## Architectural Overview (Clean UDF + Reactive StateFlow)

```text
┌───────────────────────────────────────────────────────────────────────────┐
│                    UI Layer (Jetpack Compose + M3E)                       │
│  ExpressiveOnboarding │ CalculatorHub │ BodyCompLab │ History │ Analytics │
└──────────────────┬───────────────────────────────────────▲────────────────┘
                   │ UiIntent (Sealed Interface)           │ StateFlow<UiState>
                   ▼                                       │ (@Immutable)
┌──────────────────────────────────────────────────────────┴────────────────┐
│                 Presentation Layer (BmiViewModel)                         │
│  • Real-Time Biometric Engine (WHO, Oxford 2.5, Prime, Ponderal, IBW)     │
│  • Multi-Attribute Filtering, Debounced Search & Undo-Delete Buffer       │
└──────────────────┬───────────────────────────────────────▲────────────────┘
                   │ Mutations                             │ Reactive Flows
                   ▼                                       │
┌──────────────────────────────────────────────────────────┴────────────────┐
│               Domain & Data Layer (BiometricRepository)                   │
│  • BiometricFormulas (Pure Math Engine)                                   │
│  • Rich 60-Day Longitudinal Seed Dataset Across 3 Pre-Built Profiles      │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## Complete 100-Feature Matrix Index

### Module 1: Expressive Onboarding, Identity & Adaptive Shell (`#1`–`#12`)
- [x] **#1** Interactive 3-Step HorizontalPager Onboarding
- [x] **#2** Spring-Animated Morphing Page Indicator Pills
- [x] **#3** Onboarding Step 1 — Interactive Clinical Standard Previewer
- [x] **#4** Onboarding Step 2 — Baseline Biometric & Unit Calibration
- [x] **#5** Onboarding Step 3 — Primary Health Objective & Theme Picker
- [x] **#6** Multi-Profile Identity Switcher (`Alex Rivera`, `Maya Lin`, `Jordan Vance`)
- [x] **#7** Profile Creation & Editing Modal Workflow
- [x] **#8** Profile Deletion with Active Fallback Guard
- [x] **#9** Adaptive Window Size Navigation Shell (`NavigationBar`, `NavigationRail`, `ModalNavigationDrawer`)
- [x] **#10** Collapsible Expressive Top App Bar (`exitUntilCollapsedScrollBehavior`)
- [x] **#11** Context-Aware Morphing Extended FAB
- [x] **#12** Quick Profile Baseline Reset Action

### Module 2: Core Precision Input & Unit Conversion Engine (`#13`–`#28`)
- [x] **#13** Seamless Metric / Imperial Real-Time Toggle
- [x] **#14** Expressive Height Slider with Live Stature Readout
- [x] **#15** Dual Feet & Inches Imperial Height Selector
- [x] **#16** Precision Height Micro-Stepper Buttons (`±0.5 cm`)
- [x] **#17** Expressive Weight Slider with Dynamic Range
- [x] **#18** UK Stones & Pounds (`st + lb`) Weight Display Mode
- [x] **#19** Precision Weight Micro-Stepper Buttons (`±0.1 kg` / `±0.5 kg`)
- [x] **#20** Direct Numeric Keyboard Input Sheet (`DirectNumericInputSheet`)
- [x] **#21** Age Years Selector (`2–100 yrs`)
- [x] **#22** Biological Sex Selector (`Female`, `Male`, `Neutral`)
- [x] **#23** Wrist Circumference & Body Frame Size Estimator (`Small`, `Medium`, `Large`)
- [x] **#24** Athlete / High Muscle Mass Mode Toggle
- [x] **#25** Clinical Amputation Weight Adjustment Selector (Osterkamp Method)
- [x] **#26** Pregnancy / Gestational Baseline Mode (IOM Guidelines)
- [x] **#27** Quick Weight Delta Preset Chips (`-5 kg`, `-2 kg`, `-1 kg`, `+1 kg`, `+2 kg`, `+5 kg`)
- [x] **#28** Undo Last Input Modification Action

### Module 3: Multi-Standard BMI Classification & Visual Feedback (`#29`–`#44`)
- [x] **#29** Live Animated Hero BMI Readout (`Space Grotesk` Display Numerals)
- [x] **#30** 8-Tier WHO Adult BMI Classification Engine
- [x] **#31** WHO Asian-Pacific Ethnic Cutoff Standard (`18.5–22.9` Normal)
- [x] **#32** Older Adult (`65+`) Clinical Longevity Standard (`23.0–27.5` Optimal)
- [x] **#33** Custom Interactive 180° Arc BMI Spectrum Gauge
- [x] **#34** Interactive Spectrum Bar Tap-to-Inspect
- [x] **#35** BMI Prime Ratio Calculation & Deviation Badge (`BMI / UpperLimit`)
- [x] **#36** New Oxford BMI (`Trefethen Exponent 2.5`) Calculator
- [x] **#37** Ponderal Index / Corpulence Index (`kg/m³`)
- [x] **#38** Body Surface Area (`BSA` — Mosteller Formula in `m²`)
- [x] **#39** Healthy Weight Range Corridor for Current Height
- [x] **#40** 4-Formula Ideal Body Weight (`IBW`) Matrix (`Devine`, `Robinson`, `Miller`, `Hamwi`)
- [x] **#41** Exact Distance to Nearest Healthy Boundary (`±kg` & `±lbs`)
- [x] **#42** Dynamic Category Health Guidance Drawer
- [x] **#43** All-Category Weight Cutoff Table for Current Height
- [x] **#44** Tactile Card Press Scale & Haptic Feedback

### Module 4: Anthropometric Body Composition & Metabolic Lab (`#45`–`#58`)
- [x] **#45** Waist Circumference Input Slider & Steppers
- [x] **#46** Hip & Neck Circumference Input Controls
- [x] **#47** Interactive Anatomical Tape Measurement Guide Sheet
- [x] **#48** Waist-to-Height Ratio (`WHtR`) Cardiometabolic Risk Gauge
- [x] **#49** Waist-to-Hip Ratio (`WHR`) Visceral Distribution Classifier
- [x] **#50** US Navy Circumference Body Fat % Estimator
- [x] **#51** Deurenberg BMI-Age-Sex Body Fat % Estimator & Method Switcher
- [x] **#52** Fat Mass vs. Lean Body Mass (`LBM`) Animated Split Bar
- [x] **#53** Fat-Free Mass Index (`FFMI` & Normalized `FFMI`)
- [x] **#54** Basal Metabolic Rate (`BMR`) Dual-Formula Engine (`Mifflin-St Jeor` & `Katch-McArdle`)
- [x] **#55** 5-Tier Activity Level `TDEE` Multiplier Selector
- [x] **#56** Goal-Linked Daily Calorie Target Calculator (`Cut`, `Maintain`, `Lean Gain`)
- [x] **#57** Interactive Macronutrient Split Estimator (`Balanced`, `High Protein`, `Low Carb`, `Endurance`)
- [x] **#58** Daily Hydration Baseline Estimator & Interactive Glass Tracker

### Module 5: Longitudinal History Journal, Filtering & Gestures (`#59`–`#74`)
- [x] **#59** One-Tap Biometric Snapshot Logging
- [x] **#60** Contextual Measurement Condition Tags (`Morning Fasted`, `Post-Workout`, `Evening`, `Post-Meal`, `Clinical Check`)
- [x] **#61** Energy & Wellbeing Score (`1–5`) on Log
- [x] **#62** Custom Note & Backdate Selector on Log
- [x] **#63** Inline Reading Editor Modal
- [x] **#64** Fluid Swipe-to-Delete (`EndToStart`) with Undo Snackbar
- [x] **#65** Fluid Swipe-to-Pin Milestone (`StartToEnd` Snapback)
- [x] **#66** Duplicate Entry to Today Action
- [x] **#67** Live Instant Search Across Notes, Tags, BMI & Categories
- [x] **#68** Multi-Tag & Category Filter Chip Bar with Active `InputChip` Removal
- [x] **#69** Date Range Window Filter (`7D`, `30D`, `90D`, `All`)
- [x] **#70** Multi-Attribute Sort Selector (`Newest`, `Oldest`, `Highest/Lowest BMI`, `Highest/Lowest Weight`, `Custom Priority`)
- [x] **#71** Long-Press Multi-Select Batch Mode
- [x] **#72** Batch Delete & Batch Tagging Actions
- [x] **#73** Side-by-Side 2-Entry Diff Inspector (`TwoEntryDiffComparisonSheet`)
- [x] **#74** Consecutive Entry Delta Badges (`±kg` & `±BMI`)

### Module 6: Interactive Goal Simulator & Pacing Engine (`#75`–`#79`)
- [x] **#75** Dual-Linked Target Weight & Target BMI Sliders
- [x] **#76** Weekly Pacing Rate & Calorie Deficit/Surplus Slider (`0.15–1.40 kg/wk`)
- [x] **#77** Projected Milestone Completion Date Calculator
- [x] **#78** Automatic 4-Step (`25% / 50% / 75% / 100%`) Intermediate Milestone Timeline
- [x] **#79** Interactive "What-If" Weight Step Matrix (`±1.0 kg`, `±2.5 kg`, `±5.0 kg`)

### Module 7: Visual Analytics Studio & Insights (`#80`–`#88`)
- [x] **#80** Longitudinal Multi-Metric Canvas Trend Chart (`BMI`, `Weight`, `Body Fat %`, `Waist`)
- [x] **#81** Interactive Chart Touch-Scrubbing & Crosshair Inspection
- [x] **#82** 7-Reading Moving Average Smoothing Overlay Toggle
- [x] **#83** Interactive 2D Height × Weight BMI Nomogram Matrix
- [x] **#84** Category Time-in-Zone Distribution Stacked Bar
- [x] **#85** Weekly & Monthly Velocity Analytics Bento Grid
- [x] **#86** Biometric Milestone Badges Shelf
- [x] **#87** Diurnal & Context Tag Condition Breakdown
- [x] **#88** Goal Adherence Progress Ring

### Module 8: Personalization, Data Portability & Preview Suite (`#89`–`#100`)
- [x] **#89** Live Theme Mode Controller (`System`, `Expressive Light`, `Expressive Dark`)
- [x] **#90** Wallpaper Dynamic Color (`Material You`) vs. Clinical Emerald Palette Toggle
- [x] **#91** High-Contrast Clinical Readout & Bold Numerals Mode
- [x] **#92** Reduced Motion Accessibility Controller
- [x] **#93** Tactile Haptic Feedback Controller
- [x] **#94** Metabolic Energy Unit Selector (`kcal` vs. `kJ`)
- [x] **#95** Interactive Clinical Science & Formula Reference Guide Accordion
- [x] **#96** Formatted Clinical Health Summary Report Card Generator
- [x] **#97** System Share Sheet (`Intent.ACTION_SEND`) & Clipboard Integration
- [x] **#98** CSV & JSON Portable Data Exporter
- [x] **#99** Interactive CSV Data Importer & Validator
- [x] **#100** Demo Dataset Re-Seeder (`PopulateDemoDataset`) & Dual Light/Dark `@Preview` Suite

---

## Local Build & Run Instructions

### Prerequisites
- **Android Studio**: Koala (2024.1.1) or newer.
- **JDK**: Java 17 (Eclipse Temurin recommended) or newer.
- **Android SDK**: API Level 36 (`compileSdk 36`, `minSdk 24`).

### Building via Command Line (CLI)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/<your-org>/bmi-calculator-m3e.git
   cd bmi-calculator-m3e
   ```

2. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   The compiled debug APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Run the Unit & Robolectric Test Suite**:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

4. **Build the Signed Release APK**:
   ```bash
   export KEYSTORE_PATH="/path/to/my-upload-key.jks"
   export STORE_PASSWORD="your_keystore_password"
   export KEY_PASSWORD="your_key_password"
   ./gradlew assembleRelease
   ```
   The signed release APK will be generated at:
   `app/build/outputs/apk/release/app-release.apk`

### Building via GitHub Actions
- Every push to `main` triggers `.github/workflows/build_and_release.yml`, running unit tests and uploading the compiled `.apk` artifact.
- Pushing a semantic version tag (`git tag v1.0.0 && git push origin v1.0.0`) automatically builds the APK, computes SHA-256/MD5 checksums, and publishes an official GitHub Release populated with `RELEASE_NOTES.md`.
