package com.liftlog.app.feature.backup.data

import com.liftlog.app.core.database.entity.ExerciseEntity
import com.liftlog.app.core.database.entity.SetEntryEntity
import com.liftlog.app.core.database.entity.WorkoutExerciseEntity
import com.liftlog.app.core.database.entity.WorkoutSessionEntity
import com.liftlog.app.core.database.model.DatabaseSnapshot
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.WeightUnit
import org.json.JSONArray
import org.json.JSONObject

internal object BackupJsonCodec {
    private const val FormatVersion = 1

    fun encode(backup: LiftLogBackup): String = JSONObject().apply {
        put("formatVersion", FormatVersion)
        put("exportedAtEpochMillis", backup.exportedAtEpochMillis)
        put("settings", JSONObject().apply {
            put("weightUnit", backup.settings.weightUnit.name)
            put("defaultRestSeconds", backup.settings.defaultRestSeconds)
        })
        put("database", JSONObject().apply {
            put("exercises", backup.snapshot.exercises.toJsonArray { exercise ->
                JSONObject().apply {
                    put("id", exercise.id)
                    put("name", exercise.name)
                    put("primaryMuscle", exercise.primaryMuscle)
                    put("equipment", exercise.equipment)
                    put("isCustom", exercise.isCustom)
                    put("createdAtEpochMillis", exercise.createdAtEpochMillis)
                }
            })
            put("workoutSessions", backup.snapshot.workoutSessions.toJsonArray { session ->
                JSONObject().apply {
                    put("id", session.id)
                    put("startedAtEpochMillis", session.startedAtEpochMillis)
                    putNullable("finishedAtEpochMillis", session.finishedAtEpochMillis)
                    putNullable("notes", session.notes)
                }
            })
            put("workoutExercises", backup.snapshot.workoutExercises.toJsonArray { exercise ->
                JSONObject().apply {
                    put("id", exercise.id)
                    put("workoutSessionId", exercise.workoutSessionId)
                    put("exerciseId", exercise.exerciseId)
                    put("orderIndex", exercise.orderIndex)
                    putNullable("notes", exercise.notes)
                }
            })
            put("setEntries", backup.snapshot.setEntries.toJsonArray { set ->
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
        })
    }.toString(2)

    fun decode(source: String): LiftLogBackup {
        val root = JSONObject(source)
        require(root.optInt("formatVersion", -1) == FormatVersion) {
            "This LiftLog backup format is not supported."
        }

        val settingsJson = root.getJSONObject("settings")
        val settings = AppSettings(
            weightUnit = settingsJson.getString("weightUnit").toWeightUnit(),
            defaultRestSeconds = settingsJson.getInt("defaultRestSeconds").coerceIn(0, 600),
        )
        val database = root.getJSONObject("database")
        val snapshot = DatabaseSnapshot(
            exercises = database.getJSONArray("exercises").mapJson { item ->
                ExerciseEntity(
                    id = item.positiveLong("id"),
                    name = item.nonBlankString("name"),
                    primaryMuscle = item.nonBlankString("primaryMuscle"),
                    equipment = item.nonBlankString("equipment"),
                    isCustom = item.getBoolean("isCustom"),
                    createdAtEpochMillis = item.getLong("createdAtEpochMillis"),
                )
            },
            workoutSessions = database.getJSONArray("workoutSessions").mapJson { item ->
                WorkoutSessionEntity(
                    id = item.positiveLong("id"),
                    startedAtEpochMillis = item.getLong("startedAtEpochMillis"),
                    finishedAtEpochMillis = item.optionalLong("finishedAtEpochMillis"),
                    notes = item.optionalString("notes"),
                )
            },
            workoutExercises = database.getJSONArray("workoutExercises").mapJson { item ->
                WorkoutExerciseEntity(
                    id = item.positiveLong("id"),
                    workoutSessionId = item.positiveLong("workoutSessionId"),
                    exerciseId = item.positiveLong("exerciseId"),
                    orderIndex = item.getInt("orderIndex").also { require(it >= 0) },
                    notes = item.optionalString("notes"),
                )
            },
            setEntries = database.getJSONArray("setEntries").mapJson { item ->
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
        )
        snapshot.validateRelations()
        return LiftLogBackup(
            exportedAtEpochMillis = root.getLong("exportedAtEpochMillis"),
            settings = settings,
            snapshot = snapshot,
        )
    }

    private fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject): JSONArray = JSONArray().also { array ->
        forEach { item -> array.put(mapper(item)) }
    }

    private fun <T> JSONArray.mapJson(mapper: (JSONObject) -> T): List<T> = buildList {
        repeat(length()) { index -> add(mapper(getJSONObject(index))) }
    }

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
    }
}

internal data class LiftLogBackup(
    val exportedAtEpochMillis: Long,
    val settings: AppSettings,
    val snapshot: DatabaseSnapshot,
)
