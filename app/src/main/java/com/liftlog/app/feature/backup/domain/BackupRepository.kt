package com.liftlog.app.feature.backup.domain

import android.net.Uri
import javax.inject.Inject

interface BackupRepository {
    suspend fun exportTo(destination: Uri): BackupSummary
    suspend fun importFrom(source: Uri): BackupSummary
}

data class BackupSummary(
    val exercises: Int,
    val workouts: Int,
    val sets: Int,
)

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(destination: Uri): BackupSummary = repository.exportTo(destination)
}

class ImportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(source: Uri): BackupSummary = repository.importFrom(source)
}
