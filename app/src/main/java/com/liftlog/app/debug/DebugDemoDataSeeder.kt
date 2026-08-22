package com.liftlog.app.debug

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import com.liftlog.app.core.database.dao.ExerciseDao
import com.liftlog.app.core.database.dao.GymLocationDao
import com.liftlog.app.core.database.dao.WorkoutDao
import com.liftlog.app.core.database.dao.WorkoutTemplateDao
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.GymLocationEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.model.ExerciseCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class DebugDemoDataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exerciseDao: ExerciseDao,
    private val locationDao: GymLocationDao,
    private val workoutDao: WorkoutDao,
    private val templateDao: WorkoutTemplateDao,
) {
    fun seedOnEmulatorOnce() {
        if (!isDebugBuild() || !isEmulator()) return
        val preferences = context.getSharedPreferences("debug_demo_data", Context.MODE_PRIVATE)
        if (preferences.getBoolean("seeded", false)) return
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            seed()
            preferences.edit().putBoolean("seeded", true).apply()
        }
    }

    private suspend fun seed() {
        val now = System.currentTimeMillis()
        val mainGym = "Demo Fitness Center"
        val homeGym = "Home Gym"
        locationDao.insertLocation(GymLocationEntity(name = mainGym, createdAtEpochMillis = now))
        locationDao.insertLocation(GymLocationEntity(name = homeGym, createdAtEpochMillis = now))

        val benchPress = exerciseDao.insertExerciseWithSearch(
            ExerciseEntity(name = "Demo Barbell Bench Press", primaryMuscle = "Chest", equipment = "Barbell", category = ExerciseCategory.FreeWeights.name, createdAtEpochMillis = now),
        )
        val squat = exerciseDao.insertExerciseWithSearch(
            ExerciseEntity(name = "Demo Back Squat", primaryMuscle = "Legs", equipment = "Barbell", category = ExerciseCategory.FreeWeights.name, createdAtEpochMillis = now),
        )
        val row = exerciseDao.insertExerciseWithSearch(
            ExerciseEntity(name = "Demo Seated Cable Row", primaryMuscle = "Back", equipment = "Cable machine", category = ExerciseCategory.Machine.name, gymLocation = mainGym, createdAtEpochMillis = now),
        )
        val press = exerciseDao.insertExerciseWithSearch(
            ExerciseEntity(name = "Demo Shoulder Press Machine", primaryMuscle = "Shoulders", equipment = "Machine", category = ExerciseCategory.Machine.name, gymLocation = mainGym, createdAtEpochMillis = now),
        )
        val curl = exerciseDao.insertExerciseWithSearch(
            ExerciseEntity(name = "Demo Dumbbell Curl", primaryMuscle = "Biceps", equipment = "Dumbbells", category = ExerciseCategory.FreeWeights.name, createdAtEpochMillis = now),
        )
        val plank = exerciseDao.insertExerciseWithSearch(
            ExerciseEntity(name = "Demo Weighted Plank", primaryMuscle = "Core", equipment = "Mat", category = ExerciseCategory.Timed.name, createdAtEpochMillis = now),
        )

        val upperPlanId = templateDao.insertPlan(WorkoutPlanEntity(name = "Demo Upper Strength", createdAtEpochMillis = now))
        val fullBodyPlanId = templateDao.insertPlan(WorkoutPlanEntity(name = "Demo Full Body", createdAtEpochMillis = now))
        templateDao.insertPlan(WorkoutPlanEntity(name = "Demo Empty Plan", createdAtEpochMillis = now))
        templateDao.createTemplate("Demo Upper A", listOf(benchPress, row, press), listOf(upperPlanId, fullBodyPlanId))
        templateDao.createTemplate("Demo Lower A", listOf(squat, curl), listOf(fullBodyPlanId))
        templateDao.createTemplate("Demo Free Template", listOf(benchPress, curl))

        seedWorkout(now - 8 * DayMillis, now - 8 * DayMillis + 70 * MinuteMillis, mainGym, "Demo upper workout", listOf(
            benchPress to listOf(70.0 to 8, 72.5 to 8, 75.0 to 6),
            row to listOf(45.0 to 12, 45.0 to 12, 50.0 to 10),
        ))
        seedWorkout(now - 4 * DayMillis, now - 4 * DayMillis + 65 * MinuteMillis, mainGym, "Demo lower workout", listOf(
            squat to listOf(80.0 to 8, 85.0 to 6, 85.0 to 6),
            curl to listOf(12.0 to 12, 12.0 to 11),
        ))
        seedWorkout(now - DayMillis, now - DayMillis + 55 * MinuteMillis, homeGym, "Demo full body workout", listOf(
            benchPress to listOf(75.0 to 8, 77.5 to 7, 80.0 to 5),
            press to listOf(35.0 to 12, 35.0 to 10),
            squat to listOf(85.0 to 8, 90.0 to 5),
            plank to listOf(0.0 to 45, 0.0 to 50),
        ))
    }

    private suspend fun seedWorkout(
        startedAt: Long,
        finishedAt: Long,
        location: String,
        notes: String,
        exercises: List<Pair<Long, List<Pair<Double, Int>>>>,
    ) {
        val sessionId = workoutDao.insertWorkoutSession(
            WorkoutSessionEntity(startedAtEpochMillis = startedAt, finishedAtEpochMillis = finishedAt, gymLocation = location, notes = notes),
        )
        exercises.forEachIndexed { exerciseIndex, (exerciseId, sets) ->
            val workoutExerciseId = workoutDao.insertWorkoutExercise(
                WorkoutExerciseEntity(workoutSessionId = sessionId, exerciseId = exerciseId, orderIndex = exerciseIndex),
            )
            sets.forEachIndexed { setIndex, (weight, reps) ->
                workoutDao.insertSetEntry(
                    SetEntryEntity(workoutExerciseId = workoutExerciseId, setNumber = setIndex + 1, weight = weight, reps = reps, completedAtEpochMillis = finishedAt),
                )
            }
        }
    }

    private fun isEmulator(): Boolean = Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("google_sdk", ignoreCase = true) ||
        Build.MODEL.contains("Emulator", ignoreCase = true) ||
        Build.HARDWARE.contains("ranchu", ignoreCase = true)

    private fun isDebugBuild(): Boolean =
        context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    private companion object {
        const val MinuteMillis = 60_000L
        const val DayMillis = 24 * 60 * MinuteMillis
    }
}
