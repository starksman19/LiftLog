package com.liftlog.app.feature.report.domain

import android.net.Uri
import com.liftlog.app.core.model.AppLanguage
import java.time.LocalDate

interface TrainingReportRepository {
    suspend fun exportTo(
        destination: Uri,
        startDate: LocalDate,
        endDate: LocalDate,
        language: AppLanguage,
    ): TrainingReportSummary
}

data class TrainingReportSummary(
    val workouts: Int,
    val exercises: Int,
    val sets: Int,
)
