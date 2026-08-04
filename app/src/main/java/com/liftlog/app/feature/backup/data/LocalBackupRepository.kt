package com.liftlog.app.feature.backup.data

import android.content.Context
import android.net.Uri
import com.liftlog.app.core.database.dao.BackupDao
import com.liftlog.app.core.datastore.SettingsRepository
import com.liftlog.app.feature.backup.domain.BackupRepository
import com.liftlog.app.feature.backup.domain.BackupSummary
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupDao: BackupDao,
    private val settingsRepository: SettingsRepository,
) : BackupRepository {
    override suspend fun exportTo(destination: Uri): BackupSummary {
        val backup = LiftLogBackup(
            exportedAtEpochMillis = System.currentTimeMillis(),
            settings = settingsRepository.snapshot(),
            snapshot = backupDao.snapshot(),
        )
        context.contentResolver.openOutputStream(destination, "wt")
            ?.bufferedWriter()
            ?.use { writer -> writer.write(BackupJsonCodec.encode(backup)) }
            ?: error("Unable to open the selected file.")

        return backup.summary()
    }

    override suspend fun importFrom(source: Uri): BackupSummary {
        val backup = context.contentResolver.openInputStream(source)
            ?.bufferedReader()
            ?.use { reader -> BackupJsonCodec.decode(reader.readText()) }
            ?: error("Unable to read the selected file.")

        backupDao.replaceAll(backup.snapshot)
        settingsRepository.restore(backup.settings)
        return backup.summary()
    }

    private fun LiftLogBackup.summary() = BackupSummary(
        exercises = snapshot.exercises.size,
        workouts = snapshot.workoutSessions.size,
        sets = snapshot.setEntries.size,
    )
}
