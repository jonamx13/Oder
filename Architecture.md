# Role
You are an expert Native Android Developer specializing in Jetpack Compose, Room (SQLite), and Clean Architecture. Your task is to build "Oder", a premium language-learning engine for German and Polish.

# Core Tech Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Navigation:** Navigation Compose
*   **Local Storage:** Room Database (Offline-first)
*   **Architecture:** Clean Architecture (Presentation, Domain, Data) with MVI/MVVM in the UI layer.
*   **Async/State:** Coroutines & StateFlow.

# Project Constraints & Rules
1.  **Aesthetic:** Strictly Dark Mode (#000000 to #1E1E1E). No light mode themes.
2.  **Animations:** Use Jetpack Compose `spring` physics (`Spring.StiffnessLow`, `Spring.DampingRatioMediumBouncy`) for all transitions. Avoid linear easing. UI must feel premium and tactile.
3.  **Data Flow:** Unidirectional Data Flow. The database is the single source of truth.
4.  **No Gamification:** Do not add points, streaks, or generic flashcard mechanics.
5.  **Colors:** Implement semantic variables for noun genders (MASCULINE_BLUE, FEMININE_RED, NEUTER_GREEN).

# Directory Structure Enforcement
Ensure all generated code strictly follows this tree inside `app/src/main/java/com/oder/`:
├── core/ (fsrs math, theme, util)
├── data/ (local/db, local/dao, local/entity, repository)
├── domain/ (model, repository, usecase)
└── presentation/ (navigation, onboarding, dashboard, engine/components)

---

# Oder: Project Architecture

## 1. The Gradle Build System (The `package.json` equivalent)
Android projects are managed by Gradle using Kotlin DSL (`.kts`).
*   `build.gradle.kts (Project)`: Global configurations.
*   `app/build.gradle.kts (Module)`: Where we declare our dependencies (Room, Navigation, Compose, ViewModel) and set the Min SDK.

## 2. The Native Directory Tree
Everything lives inside `app/src/main/`.

### `assets/`
*   `seed_data.json` -> The massive JSON file containing our B2 German/Polish vocabulary and grammar rules. Parsed on first launch to populate the DB.

### `res/` (Resources)
*   `values/strings.xml` -> All UI text. (Important: Do not hardcode strings in Compose files).
*   `mipmap/` -> App icons.

### `java/com/oder/` (The Core Codebase)

#### ├── core/ (Shared Utilities)
*   `fsrs/` -> Pure math functions for the spaced repetition algorithm.
*   `theme/` -> `Color.kt`, `Type.kt`, `Theme.kt` (Strictly dark mode, semantic gender colors).
*   `util/` -> Time and date formatters.

#### ├── data/ (Layer 1: The Truth)
*   `local/`
    *   `OderDatabase.kt` -> The Room Database configuration.
    *   `dao/` -> Data Access Objects (SQL queries like `SELECT * FROM lexemes`).
    *   `entity/` -> `LexemeEntity.kt`, `SrsStateEntity.kt`, `GrammarRuleEntity.kt`.
*   `repository/` -> Implements the interfaces from the Domain layer. 

#### ├── domain/ (Layer 2: The Brain)
*   `model/` -> Pure Kotlin data classes representing the UI's view of a word.
*   `repository/` -> Interfaces defining what data the app needs.
*   `usecase/` -> Single-responsibility rules.
    *   `SeedDatabaseUseCase.kt` -> Parses `assets/seed_data.json`.
    *   `GetDailyQueueUseCase.kt` -> The Rule of 5 engine.
    *   `EvaluateReviewUseCase.kt` -> Triggers Grammar Interceptors.

#### └── presentation/ (Layer 3: The UI / Jetpack Compose)
*   `navigation/` -> Compose Navigation graph (routes and screen transitions).
*   `onboarding/` -> Placement test screens and view models.
*   `dashboard/` -> The main hub, language switcher, and progress matrices.
*   `engine/` (The Step Engine)
    *   `ReviewScreen.kt`
    *   `ReviewViewModel.kt` -> Manages the unidirectional state flow.
    *   `components/` -> Modular UI puzzle pieces (`GenderSelector.kt`, `AspectPairGrid.kt`).