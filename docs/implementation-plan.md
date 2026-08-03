# LiftLog Implementation Plan

## Product direction

LiftLog starts as an offline-first gym progress tracker. The first version should make the core workout flow fast: find an exercise, log sets, review history and see basic progress.

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
- Add simple charts after the data model is stable.

### 5. Templates

- Create a reusable workout template.
- Start a workout from a template.
- Reorder exercises inside a template.

### 6. Settings and export

- Toggle kg/lb.
- Configure default rest time.
- Export local data to JSON or CSV.

## Architecture rules

- Compose screens display immutable UI state and send user events to ViewModels.
- ViewModels coordinate screen state and call use cases.
- Use cases represent app actions and own business rules.
- Repositories hide local and future remote data sources.
- Room stores workout data.
- DataStore stores small preferences.
- Cloud sync should be added later behind repository interfaces.

## Next implementation slice

The next code slice is workout logging foundation:

- `WorkoutSession`
- `WorkoutExercise`
- `SetEntry`
- Room DAO queries for active workout state
- repository and use cases
- basic Workout screen with start and active-state UI

