package com.liftlog.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liftlog.app.core.model.WeightUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val weightUnit: Flow<WeightUnit> = context.settingsDataStore.data.map { preferences ->
        when (preferences[Keys.WeightUnit]) {
            WeightUnit.Pounds.name -> WeightUnit.Pounds
            else -> WeightUnit.Kilograms
        }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.WeightUnit] = unit.name
        }
    }

    private object Keys {
        val WeightUnit: Preferences.Key<String> = stringPreferencesKey("weight_unit")
    }
}

