package com.liftlog.app.feature.backup.data

import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.GymLocationEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateEntity
import com.liftlog.app.core.database.entity.WorkoutTemplateExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutPlanEntity
import com.liftlog.app.core.database.entity.WorkoutTemplatePlanEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.ExerciseCategory
import com.liftlog.app.core.model.WeightUnit
import com.liftlog.app.feature.backup.domain.BackupSection
import com.liftlog.app.feature.backup.domain.BackupSelection
import org.json.JSONArray
import org.json.JSONObject

internal object BackupJsonCodec {
    private const val FormatVersion = 7

    fun encode(backup: LiftLogBackup): String = JSONObject().apply {
        put("formatVersion", FormatVersion)
        put("exportedAtEpochMillis", backup.exportedAtEpochMillis)
        put("sections", backup.selection.toJson())
        backup.settings?.let { settings ->
            put("settings", JSONObject().apply {
                put("weightUnit", settings.weightUnit.name)
                put("defaultRestSeconds", settings.defaultRestSeconds)
                put("restTimerEnabled", settings.restTimerEnabled)
                put("restTimerOffsetSeconds", settings.restTimerOffsetSeconds)
            })
        }
        put("database", JSONObject().apply {
            if (backup.selection.locations) put("gymLocations", backup.snapshot.gymLocations.toJsonArray { location ->
                JSONObject().apply {
                    put("id", location.id)
                    put("name", location.name)
                    put("createdAtEpochMillis", location.createdAtEpochMillis)
                }
            })
            if (backup.selection.exercises) put("exercises", backup.snapshot.exercises.toJsonArray { exercise ->
                JSONObject().apply {
                    put("id", exercise.id)
                    put("name", exercise.name)
                    put("primaryMuscle", exercise.primaryMuscle)
                    put("equipment", exercise.equipment)
                    put("category", exercise.category)
                    putNullable("gymLocation", exercise.gymLocation)
                    putNullable("youTubeUrl", exercise.youTubeUrl)
                    putNullable("imageUri", exercise.imageUri)
                    put("isCustom", exercise.isCustom)
                    put("createdAtEpochMillis", exercise.createdAtEpochMillis)
                }
            })
            if (backup.selection.workoutSessions) put("workoutSessions", backup.snapshot.workoutSessions.toJsonArray { session ->
                JSONObject().apply {
                    put("id", session.id)
                    put("startedAtEpochMillis", session.startedAtEpochMillis)
                    putNullable("finishedAtEpochMillis", session.finishedAtEpochMillis)
                    putNullable("gymLocation", session.gymLocation)
                    putNullable("notes", session.notes)
                }
            })
            if (backup.selection.workoutExercises) put("workoutExercises", backup.snapshot.workoutExercises.toJsonArray { exercise ->
                JSONObject().apply {
                    put("id", exercise.id)
                    put("workoutSessionId", exercise.workoutSessionId)
                    put("exerciseId", exercise.exerciseId)
                    put("orderIndex", exercise.orderIndex)
                    putNullable("notes", exercise.notes)
                }
            })
            if (backup.selection.setEntries) put("setEntries", backup.snapshot.setEntries.toJsonArray { set ->
                JSONObject().apply {
                    put("id", set.id)
                    put("workoutExerciseId", set.workoutExerciseId)
                    put("setNumber", set.setNumber)
                    put("weight", set.weight)
                    put("reps", set.reps)
                    putNullable("rpe", set.rpe)
                    putNullable("restSeconds", set.restSeconds)
                    putNullable("notes", set.notes)
                    put("completedAtEpochMillis", set.completedAtEpochMillis)
                }
            })
            if (backup.selection.workoutTemplates) put("workoutPlans", backup.snapshot.workoutPlans.toJsonArray { plan ->
                JSONObject().apply {
                    put("id", plan.id)
                    put("name", plan.name)
                    put("createdAtEpochMillis", plan.createdAtEpochMillis)
                }
            })
            if (backup.selection.workoutTemplates) put("workoutTemplates", backup.snapshot.workoutTemplates.toJsonArray { template ->
                JSONObject().apply {
                    put("id", template.id)
                    put("name", template.name)
                    put("createdAtEpochMillis", template.createdAtEpochMillis)
                }
            })
            if (backup.selection.workoutTemplates) put("workoutTemplatePlans", backup.snapshot.workoutTemplatePlans.toJsonArray { link ->
                JSONObject().apply {
                    put("templateId", link.templateId)
                    put("planId", link.planId)
                }
            })
            if (backup.selection.workoutTemplates) put("workoutTemplateExercises", backup.snapshot.workoutTemplateExercises.toJsonArray { templateExercise ->
                JSONObject().apply {
                    put("id", templateExercise.id)
                    put("templateId", templateExercise.templateId)
                    put("exerciseId", templateExercise.exerciseId)
                    put("orderIndex", templateExercise.orderIndex)
                }
            })
        })
    }.toString(2)

    fun decode(source: String): LiftLogBackup {
        val root = JSONObject(source)
        val formatVersion = root.optInt("formatVersion", -1)
        require(formatVersion in 1..FormatVersion) {
            "This LiftLog backup format is not supported."
        }
        val selection = if (formatVersion == 1) {
            BackupSelection.Everything.copy(locations = false, workoutTemplates = false)
        } else {
            root.getJSONObject("sections").toSelection(formatVersion)
        }
        require(selection.hasAnySelection()) { "The backup does not contain any selected data." }
        val settings = if (selection.settings) root.getJSONObject("settings").let { settingsJson ->
            AppSettings(
                weightUnit = settingsJson.getString("weightUnit").toWeightUnit(),
                defaultRestSeconds = settingsJson.getInt("defaultRestSeconds").coerceIn(0, 600),
                restTimerEnabled = settingsJson.optBoolean("restTimerEnabled", true),
                restTimerOffsetSeconds = settingsJson.optInt("restTimerOffsetSeconds", 0).coerceIn(0, 600),
            )
        } else {
            null
        }
        val database = root.getJSONObject("database")
        val snapshot = DatabaseSnapshot(
            gymLocations = database.arrayFor("gymLocations", selection.locations).mapJson { item ->
                GymLocationEntity(
                    id = item.positiveLong("id"),
                    name = item.nonBlankString("name"),
                    createdAtEpochMillis = item.getLong("createdAtEpochMillis"),
                )
            },
            exercises = database.arrayFor("exercises", selection.exercises).mapJson { item ->
                ExerciseEntity(
                    id = item.positiveLong("id"),
                    name = item.nonBlankString("name"),
                    primaryMuscle = item.optionalString("primaryMuscle").orEmpty(),
                    equipment = item.optionalString("equipment").orEmpty(),
                    category = item.optionalString("category")
                        ?: if (item.optionalString("equipment").equals("Machine", ignoreCase = true)) {
                            ExerciseCategory.Machine.name
                        } else {
                            ExerciseCategory.FreeWeights.name
                        },
                    gymLocation = item.optionalString("gymLocation"),
                    youTubeUrl = item.optionalString("youTubeUrl"),
                    imageUri = item.optionalString("imageUri"),
                    isCustom = item.getBoolean("isCustom"),
                    createdAtEpochMillis = item.getLong("createdAtEpochMillis"),
                )
            },
            workoutSessions = database.arrayFor("workoutSessions", selection.workoutSessions).mapJson { item ->
                WorkoutSessionEntity(
                    id = item.positiveLong("id"),
                    startedAtEpochMillis = item.getLong("startedAtEpochMillis"),
                    finishedAtEpochMillis = item.optionalLong("finishedAtEpochMillis"),
                    gymLocation = item.optionalString("gymLocation"),
                    notes = item.optionalString("notes"),
                )
            },
            workoutExercises = database.arrayFor("workoutExercises", selection.workoutExercises).mapJson { item ->
                WorkoutExerciseEntity(
                    id = item.positiveLong("id"),
                    workoutSessionId = item.positiveLong("workoutSessionId"),
                    exerciseId = item.positiveLong("exerciseId"),
                    orderIndex = item.getInt("orderIndex").also { require(it >= 0) },
                    notes = item.optionalString("notes"),
                )
            },
            setEntries = database.arrayFor("setEntries", selection.setEntries).mapJson { item ->
                SetEntryEntity(
                    id = item.positiveLong("id"),
                    workoutExerciseId = item.positiveLong("workoutExerciseId"),
                    setNumber = item.getInt("setNumber").also { require(it > 0) },
                    weight = item.getDouble("weight").also { require(it >= 0) },
                    reps = item.getInt("reps").also { require(it > 0) },
                    rpe = item.optionalDouble("rpe"),
                    restSeconds = item.optionalInt("restSeconds"),
                    notes = item.optionalString("notes"),
                    completedAtEpochMillis = item.getLong("completedAtEpochMillis"),
                )
            },
            workoutPlans = database.arrayFor("workoutPlans", selection.workoutTemplates && formatVersion >= 5).mapJson { item ->
                WorkoutPlanEntity(
                    id = item.positiveLong("id"),
                    name = item.nonBlankString("name"),
                    createdAtEpochMillis = item.getLong("createdAtEpochMillis"),
                )
            },
            workoutTemplates = database.arrayFor("workoutTemplates", selection.workoutTemplates).mapJson { item ->
                WorkoutTemplateEntity(
                    id = item.positiveLong("id"),
                    name = item.nonBlankString("name"),
                    createdAtEpochMillis = item.getLong("createdAtEpochMillis"),
                    planId = if (formatVersion >= 5) item.optionalLong("planId") else null,
                )
            },
            workoutTemplateExercises = database.arrayFor("workoutTemplateExercises", selection.workoutTemplates).mapJson { item ->
                WorkoutTemplateExerciseEntity(
                    id = item.positiveLong("id"),
                    templateId = item.positiveLong("templateId"),
                    exerciseId = item.positiveLong("exerciseId"),
                    orderIndex = item.getInt("orderIndex").also { require(it >= 0) },
                )
            },
            workoutTemplatePlans = if (formatVersion >= 6) {
                database.arrayFor("workoutTemplatePlans", selection.workoutTemplates).mapJson { item ->
                    WorkoutTemplatePlanEntity(
                        templateId = item.positiveLong("templateId"),
                        planId = item.positiveLong("planId"),
                    )
                }
            } else {
                database.arrayFor("workoutTemplates", selection.workoutTemplates).mapJson { item ->
                    item.optionalLong("planId")?.let { planId ->
                        WorkoutTemplatePlanEntity(item.positiveLong("id"), planId)
                    }
                }.filterNotNull()
            },
        )
        snapshot.validateRelations()
        return LiftLogBackup(
            exportedAtEpochMillis = root.getLong("exportedAtEpochMillis"),
            settings = settings,
            snapshot = snapshot,
            selection = selection,
        )
    }

    private fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject): JSONArray = JSONArray().also { array ->
        forEach { item -> array.put(mapper(item)) }
    }

    private fun <T> JSONArray.mapJson(mapper: (JSONObject) -> T): List<T> = buildList {
        repeat(length()) { index -> add(mapper(getJSONObject(index))) }
    }

    private fun JSONObject.arrayFor(name: String, included: Boolean): JSONArray {
        return if (included) getJSONArray(name) else JSONArray()
    }

    private fun BackupSelection.toJson(): JSONObject = JSONObject().apply {
        put("settings", settings)
        put("locations", locations)
        put("exercises", exercises)
        put("workoutSessions", workoutSessions)
        put("workoutExercises", workoutExercises)
        put("setEntries", setEntries)
        put("workoutTemplates", workoutTemplates)
    }

    private fun JSONObject.toSelection(formatVersion: Int): BackupSelection = BackupSelection(
        settings = getBoolean("settings"),
        locations = if (formatVersion >= 4) getBoolean("locations") else false,
        exercises = getBoolean("exercises"),
        workoutSessions = getBoolean("workoutSessions"),
        workoutExercises = getBoolean("workoutExercises"),
        setEntries = getBoolean("setEntries"),
        workoutTemplates = if (formatVersion >= 3) getBoolean("workoutTemplates") else false,
    ).normalized()

    private fun JSONObject.putNullable(name: String, value: Any?) {
        put(name, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optionalString(name: String): String? =
        if (isNull(name)) null else getString(name)

    private fun JSONObject.optionalLong(name: String): Long? =
        if (isNull(name)) null else getLong(name)

    private fun JSONObject.optionalInt(name: String): Int? =
        if (isNull(name)) null else getInt(name)

    private fun JSONObject.optionalDouble(name: String): Double? =
        if (isNull(name)) null else getDouble(name)

    private fun JSONObject.positiveLong(name: String): Long = getLong(name).also { require(it > 0) }

    private fun JSONObject.nonBlankString(name: String): String = getString(name).trim().also { require(it.isNotEmpty()) }

    private fun String.toWeightUnit(): WeightUnit = runCatching { WeightUnit.valueOf(this) }
        .getOrElse { throw IllegalArgumentException("Unsupported weight unit.") }

    private fun DatabaseSnapshot.validateRelations() {
        val exerciseIds = exercises.map { it.id }.toSet()
        val sessionIds = workoutSessions.map { it.id }.toSet()
        val workoutExerciseIds = workoutExercises.map { it.id }.toSet()
        val templateIds = workoutTemplates.map { it.id }.toSet()
        val planIds = workoutPlans.map { it.id }.toSet()
        require(exerciseIds.size == exercises.size) { "Exercise IDs must be unique." }
        require(sessionIds.size == workoutSessions.size) { "Workout IDs must be unique." }
        require(workoutExerciseIds.size == workoutExercises.size) { "Workout exercise IDs must be unique." }
        require(workoutExercises.all { it.exerciseId in exerciseIds && it.workoutSessionId in sessionIds }) {
            "A workout exercise points to missing data."
        }
        require(setEntries.all { it.workoutExerciseId in workoutExerciseIds }) {
            "A set points to a missing workout exercise."
        }
        require(setEntries.map { it.id }.toSet().size == setEntries.size) { "Set IDs must be unique." }
        require(templateIds.size == workoutTemplates.size) { "Template IDs must be unique." }
        require(planIds.size == workoutPlans.size) { "Workout plan IDs must be unique." }
        require(workoutTemplatePlans.all { it.templateId in templateIds && it.planId in planIds }) {
            "A template plan points to missing data."
        }
        require(workoutTemplateExercises.all { it.templateId in templateIds && it.exerciseId in exerciseIds }) {
            "A template exercise points to missing data."
        }
        require(workoutTemplateExercises.map { it.id }.toSet().size == workoutTemplateExercises.size) {
            "Template exercise IDs must be unique."
        }
    }
}

internal data class LiftLogBackup(
    val exportedAtEpochMillis: Long,
    val settings: AppSettings?,
    val snapshot: DatabaseSnapshot,
    val selection: BackupSelection,
)
