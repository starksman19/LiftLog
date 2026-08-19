package com.liftlog.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
        )
    }

    val weightUnit: Flow<WeightUnit> = settings.map { it.weightUnit }

    suspend fun snapshot(): AppSettings = settings.first()

    suspend fun restore(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.WeightUnit] = settings.weightUnit.name
            preferences[Keys.DefaultRestSeconds] = settings.defaultRestSeconds.coerceIn(0, 600)
            preferences[Keys.RestTimerMode] = settings.restTimerMode.name
            preferences[Keys.RestTimerEnabled] = settings.restTimerMode != RestTimerMode.Off
            preferences[Keys.RestTimerOffsetSeconds] = settings.restTimerOffsetSeconds.coerceIn(0, 600)
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
        val StarterExercisesSeeded: Preferences.Key<Boolean> = booleanPreferencesKey("starter_exercises_seeded")
    }
}
