package com.liftlog.app.feature.report.data

import com.liftlog.app.core.model.AppLanguage
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal data class TrainingReportData(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val generatedAtEpochMillis: Long,
    val workouts: List<TrainingReportWorkout>,
)

internal data class TrainingReportWorkout(
    val id: Long,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long,
    val gymLocation: String?,
    val notes: String?,
    val exercises: List<TrainingReportExercise>,
)

internal data class TrainingReportExercise(
    val id: Long,
    val exerciseId: Long,
    val name: String,
    val category: String,
    val primaryMuscle: String,
    val equipment: String,
    val notes: String?,
    val sets: List<TrainingReportSet>,
)

internal data class TrainingReportSet(
    val number: Int,
    val weight: Double,
    val reps: Int,
    val completedAtEpochMillis: Long,
)

internal object TrainingReportWorkbook {
    fun write(report: TrainingReportData, language: AppLanguage, output: OutputStream) {
        val labels = ReportLabels(language)
        ZipOutputStream(output.buffered()).use { zip ->
            zip.writeEntry("[Content_Types].xml", contentTypes())
            zip.writeEntry("_rels/.rels", rootRelationships())
            zip.writeEntry("xl/workbook.xml", workbook(labels))
            zip.writeEntry("xl/_rels/workbook.xml.rels", workbookRelationships())
            zip.writeEntry("xl/styles.xml", styles())
            zip.writeEntry("xl/worksheets/sheet1.xml", summarySheet(report, labels))
            zip.writeEntry("xl/worksheets/sheet2.xml", workoutsSheet(report, labels))
            zip.writeEntry("xl/worksheets/sheet3.xml", exercisesSheet(report, labels))
            zip.writeEntry("xl/worksheets/sheet4.xml", setsSheet(report, labels))
        }
    }

    private fun summarySheet(report: TrainingReportData, labels: ReportLabels): String {
        val allExercises = report.workouts.flatMap { it.exercises }
        val allSets = allExercises.flatMap { it.sets }
        val totalVolume = allSets.sumOf { it.weight * it.reps }
        val averageVolume = if (report.workouts.isEmpty()) 0.0 else totalVolume / report.workouts.size
        val bestSet = allExercises.flatMap { exercise ->
            exercise.sets.map { set -> exercise.name to set }
        }.maxByOrNull { (_, set) -> set.weight }

        return sheetXml(
            rows = listOf(
                listOf(Cell.Text(labels.reportTitle, style = 1)),
                listOf(Cell.Text(labels.period), Cell.Text("${report.startDate} - ${report.endDate}")),
                listOf(Cell.Text(labels.generated), Cell.Text(report.generatedAtEpochMillis.dateTimeText())),
                emptyList(),
                listOf(Cell.Text(labels.workouts, style = 2), Cell.Text(report.workouts.size.toString())),
                listOf(Cell.Text(labels.exerciseEntries, style = 2), Cell.Text(allExercises.size.toString())),
                listOf(Cell.Text(labels.uniqueExercises, style = 2), Cell.Text(allExercises.map { it.exerciseId }.distinct().size.toString())),
                listOf(Cell.Text(labels.sets, style = 2), Cell.Text(allSets.size.toString())),
                listOf(Cell.Text(labels.totalVolume, style = 2), Cell.Number(totalVolume)),
                listOf(Cell.Text(labels.averageWorkoutVolume, style = 2), Cell.Number(averageVolume)),
                listOf(
                    Cell.Text(labels.bestWeight, style = 2),
                    Cell.Text(bestSet?.let { (name, set) -> "$name: ${set.weight.numberText()} kg x ${set.reps}" }.orEmpty()),
                ),
            ),
            widths = listOf(30.0, 38.0),
            mergeTitleToColumn = 2,
        )
    }

    private fun workoutsSheet(report: TrainingReportData, labels: ReportLabels): String {
        val rows = buildList {
            add(labels.workoutHeaders.map { Cell.Text(it, style = 2) })
            report.workouts.forEach { workout ->
                val sets = workout.exercises.flatMap { it.sets }
                add(
                    listOf(
                        Cell.Text(workout.finishedAtEpochMillis.dateText()),
                        Cell.Text(workout.startedAtEpochMillis.dateTimeText()),
                        Cell.Text(workout.finishedAtEpochMillis.dateTimeText()),
                        Cell.Text(workout.gymLocation.orEmpty()),
                        Cell.Number(workout.exercises.size.toDouble()),
                        Cell.Number(sets.size.toDouble()),
                        Cell.Number(sets.sumOf { it.weight * it.reps }),
                        Cell.Text(workout.notes.orEmpty()),
                    ),
                )
            }
        }
        return sheetXml(rows, listOf(15.0, 20.0, 20.0, 22.0, 12.0, 10.0, 16.0, 44.0), autoFilter = "A1:H${rows.size}")
    }

    private fun exercisesSheet(report: TrainingReportData, labels: ReportLabels): String {
        val rows = buildList {
            add(labels.exerciseHeaders.map { Cell.Text(it, style = 2) })
            report.workouts.forEach { workout ->
                workout.exercises.forEach { exercise ->
                    val sets = exercise.sets
                    add(
                        listOf(
                            Cell.Text(workout.finishedAtEpochMillis.dateText()),
                            Cell.Number(workout.id.toDouble()),
                            Cell.Text(exercise.name),
                            Cell.Text(exercise.category),
                            Cell.Text(exercise.primaryMuscle),
                            Cell.Text(exercise.equipment),
                            Cell.Number(sets.size.toDouble()),
                            Cell.Number(sets.sumOf { it.weight * it.reps }),
                            Cell.Number(sets.maxOfOrNull { it.weight } ?: 0.0),
                            Cell.Number(sets.maxOfOrNull { it.reps }?.toDouble() ?: 0.0),
                            Cell.Text(exercise.notes.orEmpty()),
                        ),
                    )
                }
            }
        }
        return sheetXml(rows, listOf(15.0, 12.0, 28.0, 16.0, 20.0, 20.0, 10.0, 16.0, 16.0, 14.0, 40.0), autoFilter = "A1:K${rows.size}")
    }

    private fun setsSheet(report: TrainingReportData, labels: ReportLabels): String {
        val rows = buildList {
            add(labels.setHeaders.map { Cell.Text(it, style = 2) })
            report.workouts.forEach { workout ->
                workout.exercises.forEach { exercise ->
                    exercise.sets.forEach { set ->
                        add(
                            listOf(
                                Cell.Text(workout.finishedAtEpochMillis.dateText()),
                                Cell.Number(workout.id.toDouble()),
                                Cell.Text(exercise.name),
                                Cell.Number(set.number.toDouble()),
                                Cell.Number(set.weight),
                                Cell.Number(set.reps.toDouble()),
                                Cell.Number(set.weight * set.reps),
                                Cell.Text(set.completedAtEpochMillis.dateTimeText()),
                                Cell.Text(workout.gymLocation.orEmpty()),
                                Cell.Text(workout.notes.orEmpty()),
                                Cell.Text(exercise.notes.orEmpty()),
                            ),
                        )
                    }
                }
            }
        }
        return sheetXml(rows, listOf(15.0, 12.0, 28.0, 10.0, 14.0, 12.0, 16.0, 20.0, 22.0, 38.0, 38.0), autoFilter = "A1:K${rows.size}")
    }

    private fun sheetXml(
        rows: List<List<Cell>>,
        widths: List<Double>,
        autoFilter: String? = null,
        mergeTitleToColumn: Int? = null,
    ): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        append("<sheetViews><sheetView workbookViewId=\"0\"><selection activeCell=\"A1\" sqref=\"A1\"/></sheetView></sheetViews>")
        append("<sheetFormatPr defaultRowHeight=\"18\"/>")
        append("<cols>")
        widths.forEachIndexed { index, width ->
            val column = index + 1
            append("<col min=\"$column\" max=\"$column\" width=\"$width\" customWidth=\"1\"/>")
        }
        append("</cols><sheetData>")
        rows.forEachIndexed { rowIndex, cells ->
            val rowNumber = rowIndex + 1
            append("<row r=\"$rowNumber\">")
            cells.forEachIndexed { columnIndex, cell -> append(cell.xml(columnName(columnIndex + 1), rowNumber)) }
            append("</row>")
        }
        append("</sheetData>")
        mergeTitleToColumn?.let { append("<mergeCells count=\"1\"><mergeCell ref=\"A1:${columnName(it)}1\"/></mergeCells>") }
        autoFilter?.let { append("<autoFilter ref=\"$it\"/>") }
        append("</worksheet>")
    }

    private fun contentTypes(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
          <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
          <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
        </Types>
    """.trimIndent()

    private fun rootRelationships(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
        </Relationships>
    """.trimIndent()

    private fun workbook(labels: ReportLabels): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
          <sheets>
            <sheet name="${labels.summarySheet}" sheetId="1" r:id="rId1"/>
            <sheet name="${labels.workoutsSheet}" sheetId="2" r:id="rId2"/>
            <sheet name="${labels.exercisesSheet}" sheetId="3" r:id="rId3"/>
            <sheet name="${labels.setsSheet}" sheetId="4" r:id="rId4"/>
          </sheets>
        </workbook>
    """.trimIndent()

    private fun workbookRelationships(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
          <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
          <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
          <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
        </Relationships>
    """.trimIndent()

    private fun styles(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <fonts count="3">
            <font><sz val="11"/><name val="Aptos"/></font>
            <font><b/><sz val="16"/><color rgb="FFFFFFFF"/><name val="Aptos Display"/></font>
            <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Aptos"/></font>
          </fonts>
          <fills count="3">
            <fill><patternFill patternType="none"/></fill>
            <fill><patternFill patternType="gray125"/></fill>
            <fill><patternFill patternType="solid"><fgColor rgb="FF1B5E20"/><bgColor indexed="64"/></patternFill></fill>
          </fills>
          <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
          <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
          <cellXfs count="3">
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
            <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFill="1"/>
            <xf numFmtId="0" fontId="2" fillId="2" borderId="0" xfId="0" applyFill="1"/>
          </cellXfs>
        </styleSheet>
    """.trimIndent()

    private fun ZipOutputStream.writeEntry(name: String, contents: String) {
        putNextEntry(ZipEntry(name))
        write(contents.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun columnName(index: Int): String {
        var number = index
        return buildString {
            while (number > 0) {
                number -= 1
                append(('A'.code + number % 26).toChar())
                number /= 26
            }
        }.reversed()
    }

    private sealed interface Cell {
        val style: Int

        data class Text(val value: String, override val style: Int = 0) : Cell
        data class Number(val value: Double, override val style: Int = 0) : Cell

        fun xml(column: String, row: Int): String = when (this) {
            is Text -> "<c r=\"$column$row\" t=\"inlineStr\"${style.attribute()}><is><t xml:space=\"preserve\">${value.xmlEscape()}</t></is></c>"
            is Number -> "<c r=\"$column$row\"${style.attribute()}><v>${value.numberText()}</v></c>"
        }
    }

    private fun Int.attribute(): String = if (this == 0) "" else " s=\"$this\""

    private fun String.xmlEscape(): String = replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun Double.numberText(): String = String.format(Locale.US, "%.2f", this)

    private fun Long.dateText(): String = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()

    private fun Long.dateTimeText(): String = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    private class ReportLabels(language: AppLanguage) {
        private val polish = language == AppLanguage.Polish

        val reportTitle = if (polish) "Raport treningowy LiftLog" else "LiftLog training report"
        val period = if (polish) "Zakres" else "Period"
        val generated = if (polish) "Wygenerowano" else "Generated"
        val workouts = if (polish) "Treningi" else "Workouts"
        val exerciseEntries = if (polish) "Wpisy ćwiczeń" else "Exercise entries"
        val uniqueExercises = if (polish) "Unikalne ćwiczenia" else "Unique exercises"
        val sets = if (polish) "Serie" else "Sets"
        val totalVolume = if (polish) "Łączna objętość (kg)" else "Total volume (kg)"
        val averageWorkoutVolume = if (polish) "Średnia objętość treningu (kg)" else "Average workout volume (kg)"
        val bestWeight = if (polish) "Największy ciężar" else "Best weight"
        val summarySheet = if (polish) "Podsumowanie" else "Summary"
        val workoutsSheet = if (polish) "Treningi" else "Workouts"
        val exercisesSheet = if (polish) "Ćwiczenia" else "Exercises"
        val setsSheet = if (polish) "Serie" else "Sets"
        val workoutHeaders = if (polish) {
            listOf("Data", "Rozpoczęto", "Zakończono", "Lokalizacja", "Ćwiczenia", "Serie", "Objętość (kg)", "Notatki treningu")
        } else {
            listOf("Date", "Started", "Finished", "Location", "Exercises", "Sets", "Volume (kg)", "Workout notes")
        }
        val exerciseHeaders = if (polish) {
            listOf("Data", "ID treningu", "Ćwiczenie", "Rodzaj", "Partia mięśniowa", "Sprzęt", "Serie", "Objętość (kg)", "Największy ciężar (kg)", "Najwięcej powtórzeń", "Notatki ćwiczenia")
        } else {
            listOf("Date", "Workout ID", "Exercise", "Type", "Primary muscle", "Equipment", "Sets", "Volume (kg)", "Best weight (kg)", "Best reps", "Exercise notes")
        }
        val setHeaders = if (polish) {
            listOf("Data", "ID treningu", "Ćwiczenie", "Nr serii", "Ciężar (kg)", "Powtórzenia", "Objętość (kg)", "Zapisano", "Lokalizacja", "Notatki treningu", "Notatki ćwiczenia")
        } else {
            listOf("Date", "Workout ID", "Exercise", "Set no.", "Weight (kg)", "Reps", "Volume (kg)", "Recorded", "Location", "Workout notes", "Exercise notes")
        }
    }
}
