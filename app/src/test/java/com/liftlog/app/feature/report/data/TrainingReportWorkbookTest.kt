package com.liftlog.app.feature.report.data

import com.liftlog.app.core.model.AppLanguage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingReportWorkbookTest {
    @Test
    fun `creates an Excel workbook with summary workouts exercises and sets sheets`() {
        val output = ByteArrayOutputStream()

        TrainingReportWorkbook.write(sampleReport(), AppLanguage.Polish, output)

        val entries = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            generateSequence { zip.nextEntry }.forEach { entry ->
                entries[entry.name] = zip.readBytes().decodeToString()
            }
        }

        assertTrue(entries.containsKey("xl/worksheets/sheet1.xml"))
        assertTrue(entries.containsKey("xl/worksheets/sheet2.xml"))
        assertTrue(entries.containsKey("xl/worksheets/sheet3.xml"))
        assertTrue(entries.containsKey("xl/worksheets/sheet4.xml"))
        assertTrue(entries.getValue("xl/worksheets/sheet1.xml").contains("Raport treningowy LiftLog"))
        assertTrue(entries.getValue("xl/workbook.xml").contains("Podsumowanie"))
    }

    private fun sampleReport() = TrainingReportData(
        startDate = LocalDate.of(2026, 7, 1),
        endDate = LocalDate.of(2026, 7, 31),
        generatedAtEpochMillis = 0,
        workouts = listOf(
            TrainingReportWorkout(
                id = 1,
                startedAtEpochMillis = 0,
                finishedAtEpochMillis = 1_000,
                gymLocation = "Main gym",
                notes = "Felt strong",
                exercises = listOf(
                    TrainingReportExercise(
                        id = 1,
                        exerciseId = 5,
                        name = "Bench Press",
                        category = "FreeWeights",
                        primaryMuscle = "Chest",
                        equipment = "Barbell",
                        notes = null,
                        sets = listOf(TrainingReportSet(1, 80.0, 8, 1_000)),
                    ),
                ),
            ),
        ),
    )
}
