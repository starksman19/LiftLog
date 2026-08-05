package com.liftlog.app.feature.report.data

import android.content.Context
import android.net.Uri
import com.liftlog.app.core.database.dao.WorkoutDao
import com.liftlog.app.core.database.model.TrainingReportRow
import com.liftlog.app.core.model.AppLanguage
import com.liftlog.app.feature.report.domain.TrainingReportRepository
import com.liftlog.app.feature.report.domain.TrainingReportSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalTrainingReportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutDao: WorkoutDao,
) : TrainingReportRepository {
    override suspend fun exportTo(
        destination: Uri,
        startDate: LocalDate,
        endDate: LocalDate,
        language: AppLanguage,
    ): TrainingReportSummary {
        val zone = ZoneId.systemDefault()
        val rows = workoutDao.getTrainingReportRows(
            startEpochMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli(),
            endExclusiveEpochMillis = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        val report = rows.toReport(startDate, endDate)
        context.contentResolver.openOutputStream(destination, "w")?.use { output ->
            TrainingReportWorkbook.write(report, language, output)
        } ?: error("Unable to open the selected file.")

        return TrainingReportSummary(
            workouts = report.workouts.size,
            exercises = report.workouts.sumOf { it.exercises.size },
            sets = report.workouts.sumOf { workout -> workout.exercises.sumOf { it.sets.size } },
        )
    }

    private fun List<TrainingReportRow>.toReport(
        startDate: LocalDate,
        endDate: LocalDate,
    ): TrainingReportData {
        val workouts = groupBy { it.workoutId }.values.map { workoutRows ->
            val first = workoutRows.first()
            TrainingReportWorkout(
                id = first.workoutId,
                startedAtEpochMillis = first.startedAtEpochMillis,
                finishedAtEpochMillis = first.finishedAtEpochMillis,
                gymLocation = first.gymLocation,
                notes = first.workoutNotes,
                exercises = workoutRows
                    .filter { it.workoutExerciseId != null }
                    .groupBy { requireNotNull(it.workoutExerciseId) }
                    .values
                    .map { exerciseRows ->
                    val exercise = exerciseRows.first()
                    TrainingReportExercise(
                        id = requireNotNull(exercise.workoutExerciseId),
                        exerciseId = requireNotNull(exercise.exerciseId),
                        name = exercise.exerciseName.orEmpty(),
                        category = exercise.exerciseCategory.orEmpty(),
                        primaryMuscle = exercise.primaryMuscle.orEmpty(),
                        equipment = exercise.equipment.orEmpty(),
                        notes = exercise.exerciseNotes,
                        sets = exerciseRows.mapNotNull { row ->
                            row.setId?.let {
                                TrainingReportSet(
                                    number = requireNotNull(row.setNumber),
                                    weight = requireNotNull(row.weight),
                                    reps = requireNotNull(row.reps),
                                    completedAtEpochMillis = requireNotNull(row.completedAtEpochMillis),
                                )
                            }
                        },
                    )
                },
            )
        }
        return TrainingReportData(
            startDate = startDate,
            endDate = endDate,
            generatedAtEpochMillis = System.currentTimeMillis(),
            workouts = workouts,
        )
    }
}
