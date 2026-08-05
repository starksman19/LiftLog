package com.liftlog.app.feature.report.domain

import android.net.Uri
import com.liftlog.app.core.model.AppLanguage
import java.time.LocalDate
import javax.inject.Inject

class ExportTrainingReportUseCase @Inject constructor(
    private val repository: TrainingReportRepository,
) {
    suspend operator fun invoke(
        destination: Uri,
        startDate: LocalDate,
        endDate: LocalDate,
        language: AppLanguage,
    ): TrainingReportSummary {
        require(!endDate.isBefore(startDate)) { "End date cannot be earlier than start date." }
        return repository.exportTo(destination, startDate, endDate, language)
    }
}
