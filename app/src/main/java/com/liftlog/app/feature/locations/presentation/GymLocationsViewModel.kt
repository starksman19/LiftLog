package com.liftlog.app.feature.locations.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liftlog.app.feature.locations.domain.GymLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GymLocationsViewModel @Inject constructor(
    private val repository: GymLocationRepository,
) : ViewModel() {
    val locations: StateFlow<List<String>> = repository.observeLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) = viewModelScope.launch { repository.add(name) }

    fun rename(oldName: String, newName: String) = viewModelScope.launch {
        if (newName.isNotBlank()) repository.rename(oldName, newName.trim())
    }

    fun delete(name: String) = viewModelScope.launch { repository.delete(name) }
}
