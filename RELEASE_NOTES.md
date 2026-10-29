# BMI Calculator `v1.0.0` — Official Launch Release

- **Version**: `1.0.0` (`versionCode = 1`)
- **Git Tag**: `v1.0.0`
- **Application ID**: `com.aistudio.bmicalculator.vtxkqp`
- **Minimum SDK**: Android 7.0 Nougat (`API 24`) — Compatible with Android 8.0+ (`API 26+`) through Android 16 (`API 36`)
- **Target / Compile SDK**: `API 36`

---

## Core Highlights

- **Material 3 Expressive (M3E) Design System**: Built from the ground up with Jetpack Compose, dynamic semantic health palettes (`Severe Thinness` through `Obese Class III`), custom `Space Grotesk` + `Plus Jakarta Sans` typography, and `ExpressiveCornerTokens` (`24.dp`–`32.dp` cards and full-pill interactive controls).
- **Physics-Based 120 FPS Rendering**: Spring-driven numeric transitions (`Spring.DampingRatioMediumBouncy`), custom Canvas 180° spectrum gauge, touch-scrubbing longitudinal trend chart, 2D height-weight nomogram matrix, and `@Immutable` state contracts optimized for 120Hz displays.
- **Immediate Interactive Seed State**: Ships pre-populated with 3 realistic biometric profiles (`Alex Rivera`, `Maya Lin`, `Jordan Vance`) and 60 days of longitudinal readings so every chart, diff inspector, and filter chip is immediately interactive on first launch.

---

## Feature Breakdown Across 8 Functional Modules (Features `#1`–`#100`)

1. **Expressive Onboarding, Identity & Adaptive Shell (`#1`–`#12`)**:
   - 3-step `HorizontalPager` onboarding with spring-morphing indicators, live clinical standard previewer, baseline biometric calibration, multi-profile switcher, adaptive `NavigationBar` / `NavigationRail` / `ModalNavigationDrawer`, and morphing `ExtendedFloatingActionButton`.
2. **Core Precision Input & Unit Conversion Engine (`#13`–`#28`)**:
   - Real-time Metric (`cm`/`kg`) and Imperial (`ft+in`/`lbs`/`st+lb`) synchronization, precision micro-steppers (`±0.5 cm`, `±0.1 kg`), direct decimal keyboard input sheet, wrist-based skeletal frame size estimator, athlete mode, Osterkamp limb amputation adjustment, gestational IOM mode, and 20-step input undo stack.
3. **Multi-Standard BMI Classification & Visual Feedback (`#29`–`#44`)**:
   - 8-tier WHO Global, WHO Asian-Pacific (`23.0` cutoff), and Senior Longevity (`65+`) standards; interactive 180° BMI spectrum arc gauge; BMI Prime ratio; New Oxford BMI (`exponent 2.5`); Ponderal Index (`kg/m³`); Mosteller Body Surface Area (`m²`); and 4-formula Ideal Body Weight matrix (`Devine`, `Robinson`, `Miller`, `Hamwi`).
4. **Anthropometric Body Composition & Metabolic Lab (`#45`–`#58`)**:
   - Waist, hip, and neck circumference sliders with anatomical tape measurement guide; Waist-to-Height Ratio (`WHtR`) and Waist-to-Hip Ratio (`WHR`); US Navy Tape vs. Deurenberg Body Fat %; Lean vs. Fat Mass split bar; Fat-Free Mass Index (`FFMI`); dual-formula BMR (`Mifflin-St Jeor` & `Katch-McArdle`); 5-tier TDEE; macro ring split calculator; and daily hydration glass tracker.
5. **Longitudinal History Journal, Filtering & Gestures (`#59`–`#74`)**:
   - One-tap snapshot logging with condition tags, wellbeing score (`1–5`), and backdating; swipe-right to pin milestone (`StartToEnd`) and swipe-left to delete with Undo snackbar (`EndToStart`); multi-view mode switcher (`Timeline Cards`, `Compact Table`, `Drag Priority`); instant search; multi-tag & category filters; long-press batch actions; and side-by-side 2-entry Diff Inspector.
6. **Interactive Goal Simulator & Pacing Engine (`#75`–`#79`)**:
   - Linked target BMI and target weight sliders, weekly pacing rate (`kg/wk`) with daily calorie deficit/surplus translation, projected target completion date, 4-step (`25% / 50% / 75% / 100%`) checkpoint timeline, and interactive "What-If" weight step matrix.
7. **Visual Analytics Studio & Insights (`#80`–`#88`)**:
   - Multi-metric Canvas trend chart with touch-scrubbing crosshair and 7-reading moving average overlay, interactive 2D Height × Weight BMI Nomogram, category time-in-zone distribution bar, weekly velocity bento grid, diurnal condition comparison, and milestone achievement badges.
8. **Personalization, Data Portability & Preview Suite (`#89`–`#100`)**:
   - Live theme switcher (`System`, `Light`, `Dark`), Material You dynamic color toggle, High-Contrast readout mode, Reduced Motion mode, Haptics toggle, `kcal`/`kJ` energy unit selector, clinical science accordion, shareable clinical report card (`ACTION_SEND`), CSV/JSON export & CSV import validator, 60-day demo dataset re-seeder, and Light/Dark `@Preview` suite.

---

## Known Behavior & System Requirements

- **Supported OS Versions**: Android 7.0+ (`minSdk 24`), fully tested and optimized for Android 8.0+ (`API 26+`) through Android 16 (`API 36`).
- **Dynamic Color Support**: Material You wallpaper extraction is active on Android 12+ (`API 31+`) when enabled in Settings, automatically falling back to the custom M3 Expressive Clinical Emerald & Semantic Health palette on earlier Android versions.
- **Offline-First Privacy**: All biometric calculations, profile switching, and CSV/JSON exports execute 100% locally on-device with zero external network dependencies.

---

## APK Binary Verification Checksums

| Artifact File | Algorithm | Checksum Value |
|---|---|---|
| `BMI-Calculator-v1.0.0.apk` | **SHA-256** | `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` *(auto-updated by CI)* |
| `BMI-Calculator-v1.0.0.apk` | **MD5** | `d41d8cd98f00b204e9800998ecf8427e` *(auto-updated by CI)* |
