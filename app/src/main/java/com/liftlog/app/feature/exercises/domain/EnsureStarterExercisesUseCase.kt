package com.liftlog.app.feature.exercises.domain

import com.liftlog.app.core.datastore.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class EnsureStarterExercisesUseCase @Inject constructor(
    private val repository: ExerciseRepository,
    private val settingsRepository: SettingsRepository,
) {
    private val seedMutex = Mutex()

    suspend operator fun invoke() {
        seedMutex.withLock {
            if (settingsRepository.areStarterExercisesSeeded()) return

            repository.ensureStarterExercises()
            settingsRepository.markStarterExercisesSeeded()
        }
    }
}
