package com.liftlog.app.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.ExerciseSearchEntity
import com.liftlog.app.core.database.entity.GymLocationEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutTemplatePlanEntity
import com.liftlog.app.core.database.entity.toSearchEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot

@Dao
interface BackupDao {
    @Query("SELECT * FROM exercises ORDER BY id")
    suspend fun getExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM workout_sessions ORDER BY id")
    suspend fun getWorkoutSessions(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_exercises ORDER BY id")
    suspend fun getWorkoutExercises(): List<WorkoutExerciseEntity>

    @Query("SELECT * FROM set_entries ORDER BY id")
    suspend fun getSetEntries(): List<SetEntryEntity>

    @Query("SELECT * FROM workout_plans ORDER BY id")
    suspend fun getWorkoutPlans(): List<WorkoutPlanEntity>

    @Query("SELECT * FROM workout_template_plans ORDER BY templateId, planId")
    suspend fun getWorkoutTemplatePlans(): List<WorkoutTemplatePlanEntity>

    @Query("SELECT * FROM workout_templates ORDER BY id")
    suspend fun getWorkoutTemplates(): List<WorkoutTemplateEntity>

    @Query("SELECT * FROM workout_template_exercises ORDER BY id")
    suspend fun getWorkoutTemplateExercises(): List<WorkoutTemplateExerciseEntity>

    @Query("SELECT * FROM gym_locations ORDER BY id")
    suspend fun getGymLocations(): List<GymLocationEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercises(entities: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(entity: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExerciseSearch(entities: List<ExerciseSearchEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutSessions(entities: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutExercises(entities: List<WorkoutExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSetEntries(entities: List<SetEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutPlans(entities: List<WorkoutPlanEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutPlan(plan: WorkoutPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkoutTemplatePlans(entities: List<WorkoutTemplatePlanEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutTemplateExercises(entities: List<WorkoutTemplateExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGymLocations(entities: List<GymLocationEntity>)

    @Query("DELETE FROM set_entries")
    suspend fun clearSetEntries()

    @Query("DELETE FROM workout_exercises")
    suspend fun clearWorkoutExercises()

    @Query("DELETE FROM workout_sessions")
    suspend fun clearWorkoutSessions()

    @Query("DELETE FROM exercise_search")
    suspend fun clearExerciseSearch()

    @Query("DELETE FROM exercise_search WHERE rowid = :exerciseId")
    suspend fun deleteExerciseSearch(exerciseId: Long)

    @Query("DELETE FROM exercises")
    suspend fun clearExercises()

    @Query("DELETE FROM workout_templates")
    suspend fun clearWorkoutTemplates()

    @Query("DELETE FROM workout_plans")
    suspend fun clearWorkoutPlans()

    @Query("DELETE FROM workout_template_plans")
    suspend fun clearWorkoutTemplatePlans()

    @Query("DELETE FROM workout_template_exercises WHERE templateId = :templateId")
    suspend fun clearWorkoutTemplateExercises(templateId: Long)

    @Query("DELETE FROM gym_locations")
    suspend fun clearGymLocations()

    @Query("SELECT id FROM workout_plans WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findPlanId(name: String): Long?

    @Query("DELETE FROM workout_template_plans WHERE templateId = :templateId")
    suspend fun clearTemplatePlanLinks(templateId: Long)

    @Query("SELECT id FROM workout_templates WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findTemplateId(name: String): Long?

    @Query(
        """
        SELECT id
        FROM exercises
        WHERE name = :name COLLATE NOCASE
          AND primaryMuscle = :primaryMuscle COLLATE NOCASE
          AND equipment = :equipment COLLATE NOCASE
        LIMIT 1
        """,
    )
    suspend fun findExerciseId(name: String, primaryMuscle: String, equipment: String): Long?

    @Query(
        """
        UPDATE exercises
        SET name = :name,
            primaryMuscle = :primaryMuscle,
            equipment = :equipment,
            category = :category,
            gymLocation = :gymLocation,
            youTubeUrl = :youTubeUrl,
            imageUri = :imageUri,
            isCustom = :isCustom,
            createdAtEpochMillis = :createdAtEpochMillis
        WHERE id = :exerciseId
        """,
    )
    suspend fun updateExercise(
        exerciseId: Long,
        name: String,
        primaryMuscle: String,
        equipment: String,
        category: String,
        gymLocation: String?,
        youTubeUrl: String?,
        imageUri: String?,
        isCustom: Boolean,
        createdAtEpochMillis: Long,
    )

    @Transaction
    suspend fun snapshot(): DatabaseSnapshot = DatabaseSnapshot(
        exercises = getExercises(),
        workoutSessions = getWorkoutSessions(),
        workoutExercises = getWorkoutExercises(),
        setEntries = getSetEntries(),
        workoutPlans = getWorkoutPlans(),
        workoutTemplatePlans = getWorkoutTemplatePlans(),
        workoutTemplates = getWorkoutTemplates(),
        workoutTemplateExercises = getWorkoutTemplateExercises(),
        gymLocations = getGymLocations(),
    )

    @Transaction
    suspend fun replaceAll(snapshot: DatabaseSnapshot) {
        clearSetEntries()
        clearWorkoutExercises()
        clearWorkoutSessions()
        clearWorkoutTemplatePlans()
        clearWorkoutTemplates()
        clearWorkoutPlans()
        clearGymLocations()
        clearExerciseSearch()
        clearExercises()

        if (snapshot.exercises.isNotEmpty()) {
            insertExercises(snapshot.exercises)
            insertExerciseSearch(snapshot.exercises.map(ExerciseEntity::toSearchEntity))
        }
        if (snapshot.workoutSessions.isNotEmpty()) insertWorkoutSessions(snapshot.workoutSessions)
        if (snapshot.workoutExercises.isNotEmpty()) insertWorkoutExercises(snapshot.workoutExercises)
        if (snapshot.setEntries.isNotEmpty()) insertSetEntries(snapshot.setEntries)
        if (snapshot.workoutPlans.isNotEmpty()) insertWorkoutPlans(snapshot.workoutPlans)
        if (snapshot.workoutTemplates.isNotEmpty()) insertWorkoutTemplates(snapshot)
        if (snapshot.workoutTemplatePlans.isNotEmpty()) insertWorkoutTemplatePlans(snapshot.workoutTemplatePlans)
        val locations = snapshot.locationsForImport()
        if (locations.isNotEmpty()) insertGymLocations(locations)
    }

    @Transaction
    suspend fun mergeExercisesAndReplaceWorkouts(
        snapshot: DatabaseSnapshot,
        replaceWorkoutData: Boolean,
    ) {
        val locations = snapshot.locationsForImport()
        if (locations.isNotEmpty()) insertGymLocations(locations.map { it.copy(id = 0) })
        val importedToLocalExerciseIds = mutableMapOf<Long, Long>()
        for (exercise in snapshot.exercises) {
            val existingId = findExerciseId(exercise.name, exercise.primaryMuscle, exercise.equipment)
            val localId = existingId ?: insertExercise(exercise.copy(id = 0))
            if (existingId != null) {
                updateExercise(
                    exerciseId = existingId,
                    name = exercise.name,
                    primaryMuscle = exercise.primaryMuscle,
                    equipment = exercise.equipment,
                    category = exercise.category,
                    gymLocation = exercise.gymLocation,
                    youTubeUrl = exercise.youTubeUrl,
                    imageUri = exercise.imageUri,
                    isCustom = exercise.isCustom,
                    createdAtEpochMillis = exercise.createdAtEpochMillis,
                )
            }
            deleteExerciseSearch(localId)
            insertExerciseSearch(listOf(exercise.copy(id = localId).toSearchEntity()))
            importedToLocalExerciseIds[exercise.id] = localId
        }

        val importedToLocalPlanIds = importPlans(snapshot)
        val importedToLocalTemplateIds = if (snapshot.workoutTemplates.isNotEmpty()) {
            insertWorkoutTemplates(snapshot, importedToLocalExerciseIds)
        } else emptyMap()
        importTemplatePlans(snapshot, importedToLocalTemplateIds, importedToLocalPlanIds)

        if (!replaceWorkoutData) return

        clearSetEntries()
        clearWorkoutExercises()
        clearWorkoutSessions()
        if (snapshot.workoutSessions.isNotEmpty()) insertWorkoutSessions(snapshot.workoutSessions)
        if (snapshot.workoutExercises.isNotEmpty()) {
            insertWorkoutExercises(
                snapshot.workoutExercises.map { workoutExercise ->
                    workoutExercise.copy(
                        exerciseId = checkNotNull(importedToLocalExerciseIds[workoutExercise.exerciseId]),
                    )
                },
            )
        }
        if (snapshot.setEntries.isNotEmpty()) insertSetEntries(snapshot.setEntries)
    }

    private suspend fun insertWorkoutTemplates(
        snapshot: DatabaseSnapshot,
        importedToLocalExerciseIds: Map<Long, Long> = snapshot.exercises.associate { it.id to it.id },
    ): Map<Long, Long> {
        val importedTemplateExercises = snapshot.workoutTemplateExercises.groupBy { it.templateId }
        val importedToLocalTemplateIds = mutableMapOf<Long, Long>()
        for (template in snapshot.workoutTemplates) {
            val localTemplateId = findTemplateId(template.name)
                ?: insertWorkoutTemplate(template.copy(id = 0, planId = null))
            if (findTemplateId(template.name) != null) {
                clearWorkoutTemplateExercises(localTemplateId)
                clearTemplatePlanLinks(localTemplateId)
            }
            val exercises = importedTemplateExercises[template.id].orEmpty().map { templateExercise ->
                templateExercise.copy(
                    id = 0,
                    templateId = localTemplateId,
                    exerciseId = checkNotNull(importedToLocalExerciseIds[templateExercise.exerciseId]),
                )
            }
            if (exercises.isNotEmpty()) insertWorkoutTemplateExercises(exercises)
            importedToLocalTemplateIds[template.id] = localTemplateId
        }
        return importedToLocalTemplateIds
    }

    private suspend fun importPlans(snapshot: DatabaseSnapshot): Map<Long, Long> {
        val importedToLocalIds = mutableMapOf<Long, Long>()
        for (plan in snapshot.workoutPlans) {
            val localId = findPlanId(plan.name) ?: insertWorkoutPlan(plan.copy(id = 0))
            importedToLocalIds[plan.id] = localId
        }
        return importedToLocalIds
    }

    private suspend fun importTemplatePlans(
        snapshot: DatabaseSnapshot,
        importedToLocalTemplateIds: Map<Long, Long>,
        importedToLocalPlanIds: Map<Long, Long>,
    ) {
        val links = snapshot.workoutTemplatePlans.mapNotNull { link ->
            val templateId = importedToLocalTemplateIds[link.templateId]
            val planId = importedToLocalPlanIds[link.planId]
            if (templateId == null || planId == null) null else WorkoutTemplatePlanEntity(templateId, planId)
        }
        if (links.isNotEmpty()) insertWorkoutTemplatePlans(links)
    }

    private fun DatabaseSnapshot.locationsForImport(): List<GymLocationEntity> = buildList {
        addAll(gymLocations)
        exercises.mapNotNullTo(this) { exercise ->
            exercise.gymLocation?.trim()?.takeIf { it.isNotEmpty() }?.let { name ->
                GymLocationEntity(name = name, createdAtEpochMillis = 0)
            }
        }
        workoutSessions.mapNotNullTo(this) { session ->
            session.gymLocation?.trim()?.takeIf { it.isNotEmpty() }?.let { name ->
                GymLocationEntity(name = name, createdAtEpochMillis = 0)
            }
        }
    }
}
