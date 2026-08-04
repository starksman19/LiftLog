package com.liftlog.app.feature.backup.data

import android.content.Context
import android.net.Uri
import com.liftlog.app.core.database.dao.BackupDao
import com.liftlog.app.core.datastore.SettingsRepository
import com.liftlog.app.feature.backup.domain.BackupRepository
import com.liftlog.app.feature.backup.domain.BackupContents
import com.liftlog.app.feature.backup.domain.BackupSelection
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
    override suspend fun exportTo(destination: Uri, selection: BackupSelection): BackupSummary {
        val backup = LiftLogBackup(
            exportedAtEpochMillis = System.currentTimeMillis(),
            settings = settingsRepository.snapshot().takeIf { selection.settings },
            snapshot = backupDao.snapshot(),
            selection = selection,
        )
        context.contentResolver.openOutputStream(destination, "wt")
            ?.bufferedWriter()
            ?.use { writer -> writer.write(BackupJsonCodec.encode(backup)) }
            ?: error("Unable to open the selected file.")

        return backup.summary()
    }

    override suspend fun inspect(source: Uri): BackupContents {
        val backup = readBackup(source)
        return BackupContents(
            selection = backup.selection,
            summary = backup.summary(),
        )
    }

    override suspend fun importFrom(source: Uri): BackupSummary {
        val backup = readBackup(source)

        backupDao.mergeExercisesAndReplaceWorkouts(
            snapshot = backup.snapshot,
            replaceWorkoutData = backup.selection.hasWorkoutData(),
        )
        backup.settings?.let { settings -> settingsRepository.restore(settings) }
        return backup.summary()
    }

    private fun readBackup(source: Uri): LiftLogBackup = context.contentResolver.openInputStream(source)
        ?.bufferedReader()
        ?.use { reader -> BackupJsonCodec.decode(reader.readText()) }
        ?: error("Unable to read the selected file.")

    private fun LiftLogBackup.summary() = BackupSummary(
        exercises = snapshot.exercises.size,
        workouts = snapshot.workoutSessions.size,
        workoutExercises = snapshot.workoutExercises.size,
        sets = snapshot.setEntries.size,
    )
}
