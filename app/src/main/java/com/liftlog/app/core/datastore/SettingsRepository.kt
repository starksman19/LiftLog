package com.liftlog.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liftlog.app.core.model.AppSettings
import com.liftlog.app.core.model.RestTimerMode
import com.liftlog.app.core.model.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            weightUnit = when (preferences[Keys.WeightUnit]) {
                WeightUnit.Pounds.name -> WeightUnit.Pounds
                else -> WeightUnit.Kilograms
            },
            defaultRestSeconds = preferences[Keys.DefaultRestSeconds] ?: 90,
            restTimerMode = preferences[Keys.RestTimerMode]
                ?.let { value -> RestTimerMode.entries.firstOrNull { it.name == value } }
                ?: if (preferences[Keys.RestTimerEnabled] ?: true) RestTimerMode.Workout else RestTimerMode.Off,
            restTimerOffsetSeconds = preferences[Keys.RestTimerOffsetSeconds] ?: 0,
            restTimerNotificationsEnabled = preferences[Keys.RestTimerNotificationsEnabled] ?: false,
            restTimerBubbleEnabled = preferences[Keys.RestTimerBubbleEnabled] ?: true,
        )
    }

    val weightUnit: Flow<WeightUnit> = settings.map { it.weightUnit }

    val restTimerVisibility: Flow<RestTimerVisibility> = context.settingsDataStore.data.map { preferences ->
        RestTimerVisibility(
            workoutHiddenForSetId = preferences[Keys.WorkoutTimerHiddenForSetId],
            exerciseHiddenForSetIds = preferences[Keys.ExerciseTimerHiddenForSetIds]
                .orEmpty()
                .mapNotNull(String::toLongOrNull)
                .toSet(),
        )
    }

    suspend fun snapshot(): AppSettings = settings.first()

    suspend fun restore(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.WeightUnit] = settings.weightUnit.name
            preferences[Keys.DefaultRestSeconds] = settings.defaultRestSeconds.coerceIn(0, 600)
            preferences[Keys.RestTimerMode] = settings.restTimerMode.name
            preferences[Keys.RestTimerEnabled] = settings.restTimerMode != RestTimerMode.Off
            preferences[Keys.RestTimerOffsetSeconds] = settings.restTimerOffsetSeconds.coerceIn(0, 600)
            preferences[Keys.RestTimerNotificationsEnabled] = settings.restTimerNotificationsEnabled
            preferences[Keys.RestTimerBubbleEnabled] = settings.restTimerBubbleEnabled
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.WeightUnit] = unit.name
        }
    }

    suspend fun setDefaultRestSeconds(seconds: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.DefaultRestSeconds] = seconds.coerceIn(0, 600)
        }
    }

    suspend fun setRestTimerMode(mode: RestTimerMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.RestTimerMode] = mode.name
            preferences[Keys.RestTimerEnabled] = mode != RestTimerMode.Off
        }
    }

    suspend fun setRestTimerOffsetSeconds(seconds: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.RestTimerOffsetSeconds] = seconds.coerceIn(0, 600)
        }
    }

    suspend fun setRestTimerNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.RestTimerNotificationsEnabled] = enabled
        }
    }

    suspend fun setRestTimerBubbleEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.RestTimerBubbleEnabled] = enabled
        }
    }

    suspend fun setWorkoutTimerVisible(visible: Boolean, latestSetId: Long) {
        context.settingsDataStore.edit { preferences ->
            if (visible) preferences.remove(Keys.WorkoutTimerHiddenForSetId)
            else preferences[Keys.WorkoutTimerHiddenForSetId] = latestSetId
        }
    }

    suspend fun setExerciseTimerVisible(visible: Boolean, latestSetId: Long) {
        context.settingsDataStore.edit { preferences ->
            val hiddenIds = preferences[Keys.ExerciseTimerHiddenForSetIds]
                .orEmpty()
                .toMutableSet()
            if (visible) hiddenIds.remove(latestSetId.toString()) else hiddenIds.add(latestSetId.toString())
            preferences[Keys.ExerciseTimerHiddenForSetIds] = hiddenIds
        }
    }

    suspend fun areStarterExercisesSeeded(): Boolean =
        context.settingsDataStore.data.first()[Keys.StarterExercisesSeeded] ?: false

    suspend fun markStarterExercisesSeeded() {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.StarterExercisesSeeded] = true
        }
    }

    private object Keys {
        val WeightUnit: Preferences.Key<String> = stringPreferencesKey("weight_unit")
        val DefaultRestSeconds: Preferences.Key<Int> = intPreferencesKey("default_rest_seconds")
        val RestTimerEnabled: Preferences.Key<Boolean> = booleanPreferencesKey("rest_timer_enabled")
        val RestTimerMode: Preferences.Key<String> = stringPreferencesKey("rest_timer_mode")
        val RestTimerOffsetSeconds: Preferences.Key<Int> = intPreferencesKey("rest_timer_offset_seconds")
        val RestTimerNotificationsEnabled: Preferences.Key<Boolean> = booleanPreferencesKey("rest_timer_notifications_enabled")
        val RestTimerBubbleEnabled: Preferences.Key<Boolean> = booleanPreferencesKey("rest_timer_bubble_enabled")
        val WorkoutTimerHiddenForSetId: Preferences.Key<Long> = longPreferencesKey("workout_timer_hidden_for_set_id")
        val ExerciseTimerHiddenForSetIds: Preferences.Key<Set<String>> = stringSetPreferencesKey("exercise_timer_hidden_for_set_ids")
        val StarterExercisesSeeded: Preferences.Key<Boolean> = booleanPreferencesKey("starter_exercises_seeded")
    }
}

data class RestTimerVisibility(
    val workoutHiddenForSetId: Long? = null,
    val exerciseHiddenForSetIds: Set<Long> = emptySet(),
)
