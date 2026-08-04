package com.liftlog.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liftlog.app.core.model.AppSettings
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
        )
    }

    val weightUnit: Flow<WeightUnit> = settings.map { it.weightUnit }

    suspend fun snapshot(): AppSettings = settings.first()

    suspend fun restore(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.WeightUnit] = settings.weightUnit.name
            preferences[Keys.DefaultRestSeconds] = settings.defaultRestSeconds.coerceIn(0, 600)
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

    private object Keys {
        val WeightUnit: Preferences.Key<String> = stringPreferencesKey("weight_unit")
        val DefaultRestSeconds: Preferences.Key<Int> = intPreferencesKey("default_rest_seconds")
    }
}
