# LiftLog Implementation Plan

## Product direction

LiftLog starts as an offline-first gym progress tracker. The first version should make the core workout flow fast: find an exercise, log sets, review history and see basic progress. The first screen is the progress dashboard, so the latest training trend is visible immediately after opening the app.

## MVP milestones

### 1. Project foundation

- Android app shell with Compose and Material 3.
- Bottom navigation for Exercises, Workout, Progress and Settings.
- Room database, Hilt, Coroutines, Flow and DataStore wiring.
- Exercise list with local starter data and FTS search.

### 2. Workout logging

- Start an active workout session.
- Add exercises to the active workout.
- Log sets with weight and reps.
- Edit or delete a set before finishing the workout.
- Finish or discard the active workout.

### 3. Exercise history

- Open exercise details from the exercise list.
- Show previous sets grouped by workout date.
- Show last used weight and reps when adding the same exercise again.

### 4. Progress basics

- Show estimated volume per exercise.
- Show personal bests by weight, reps and total volume.
- Make Progress the app's start screen.
- Show a compact training-volume chart at the top of the dashboard.
- Show exercise trend, personal bests by weight, reps and total volume.

### 5. Templates

- Create a reusable workout template.
- Start a workout from a template.
- Reorder exercises inside a template.

### 6. Settings and export

- Toggle kg/lb.
- Configure default rest time.
- Export all local data and settings to a versioned JSON backup.
- Import a LiftLog backup through the Android document picker, with a clear replace-data confirmation.
- Keep the backup format independent from the database implementation so it can later support cloud sync.

## Architecture rules

- Compose screens display immutable UI state and send user events to ViewModels.
- ViewModels coordinate screen state and call use cases.
- Use cases represent app actions and own business rules.
- Repositories hide local and future remote data sources.
- Room stores workout data.
- DataStore stores small preferences.
- Cloud sync should be added later behind repository interfaces.

## Implementation order

1. Finish workout logging: editable sets, deleting sets and safe validation.
2. Add progress queries, dashboard statistics and the compact dashboard chart.
3. Add exercise history and detail screen.
4. Add templates for repeatable workouts.
5. Add settings, versioned JSON export/import and backup validation.
6. Add focused tests for volume calculations, progress aggregation and backup parsing.

## Data transfer contract

- A backup contains exercises, workout sessions, workout exercises, set entries and settings.
- IDs are retained inside the backup so all relationships remain valid after restoring on another phone.
- Import replaces local LiftLog data only after the user confirms it in the UI.
- A backup includes a format version and is rejected when it is unsupported or malformed.
- Before export, the user chooses settings, exercises and/or the complete workout-data package.
- Import detects the sections present in the file and changes only those sections.
- Exercises are merged by name, primary muscle and equipment; matching local entries are updated and new ones are added.
