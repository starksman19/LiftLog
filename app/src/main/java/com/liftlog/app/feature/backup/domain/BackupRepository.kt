package com.liftlog.app.feature.backup.domain

import android.net.Uri
import javax.inject.Inject

interface BackupRepository {
    suspend fun exportTo(destination: Uri, selection: BackupSelection): BackupSummary
    suspend fun inspect(source: Uri): BackupContents
    suspend fun importFrom(source: Uri): BackupSummary
}

enum class BackupSection(val label: String) {
    Settings("Settings"),
    Exercises("Exercises"),
    WorkoutSessions("Workouts"),
    WorkoutExercises("Exercises in workouts"),
    SetEntries("Sets"),
    WorkoutTemplates("Workout templates"),
}

data class BackupSelection(
    val settings: Boolean = true,
    val exercises: Boolean = true,
    val workoutSessions: Boolean = true,
    val workoutExercises: Boolean = true,
    val setEntries: Boolean = true,
    val workoutTemplates: Boolean = true,
) {
    fun includes(section: BackupSection): Boolean = when (section) {
        BackupSection.Settings -> settings
        BackupSection.Exercises -> exercises
        BackupSection.WorkoutSessions -> workoutSessions
        BackupSection.WorkoutExercises -> workoutExercises
        BackupSection.SetEntries -> setEntries
        BackupSection.WorkoutTemplates -> workoutTemplates
    }

    fun toggled(section: BackupSection, enabled: Boolean): BackupSelection {
        val updated = when (section) {
            BackupSection.Settings -> copy(settings = enabled)
            BackupSection.Exercises -> if (enabled) copy(exercises = true) else copy(
                exercises = false,
                workoutSessions = false,
                workoutExercises = false,
                setEntries = false,
                workoutTemplates = false,
            )
            BackupSection.WorkoutSessions -> if (enabled) copy(workoutSessions = true) else copy(
                workoutSessions = false,
                workoutExercises = false,
                setEntries = false,
            )
            BackupSection.WorkoutExercises -> if (enabled) copy(workoutExercises = true) else copy(
                workoutSessions = false,
                workoutExercises = false,
                setEntries = false,
            )
            BackupSection.SetEntries -> if (enabled) copy(setEntries = true) else copy(
                workoutSessions = false,
                workoutExercises = false,
                setEntries = false,
            )
            BackupSection.WorkoutTemplates -> if (enabled) copy(workoutTemplates = true) else copy(
                workoutTemplates = false,
            )
        }
        return updated.normalized()
    }

    fun normalized(): BackupSelection {
        val needsWorkoutData = workoutSessions || workoutExercises || setEntries
        val needsExercises = exercises || needsWorkoutData || workoutTemplates
        return copy(
            exercises = needsExercises,
            workoutSessions = needsWorkoutData,
            workoutExercises = needsWorkoutData,
            setEntries = needsWorkoutData,
        )
    }

    fun hasAnySelection(): Boolean = settings || exercises || workoutSessions || workoutExercises || setEntries || workoutTemplates

    fun hasWorkoutData(): Boolean = workoutSessions || workoutExercises || setEntries

    companion object {
        val Everything = BackupSelection()
    }
}

data class BackupContents(
    val selection: BackupSelection,
    val summary: BackupSummary,
)

data class BackupSummary(
    val exercises: Int = 0,
    val workouts: Int = 0,
    val workoutExercises: Int = 0,
    val sets: Int = 0,
    val templates: Int = 0,
)

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(destination: Uri, selection: BackupSelection): BackupSummary {
        require(selection.hasAnySelection()) { "Select at least one item to export." }
        return repository.exportTo(destination, selection.normalized())
    }
}

class InspectBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(source: Uri): BackupContents = repository.inspect(source)
}

class ImportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(source: Uri): BackupSummary = repository.importFrom(source)
}
