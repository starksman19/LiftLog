package com.liftlog.app.core.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.appcompat.app.AppCompatDelegate
import com.liftlog.app.core.model.AppLanguage

@Composable
fun t(english: String, polish: String = PolishText[english] ?: english): String =
    if (AppLanguageState.current == AppLanguage.Polish) polish else english

fun localizedNow(english: String, polish: String): String =
    if (AppLanguageState.current == AppLanguage.Polish) polish else english

object AppLanguageState {
    var current by mutableStateOf(readCurrentLanguage())
        private set

    fun set(language: AppLanguage) {
        current = language
    }

    fun synchronize() {
        current = readCurrentLanguage()
    }

    private fun readCurrentLanguage(): AppLanguage =
        if (AppCompatDelegate.getApplicationLocales().get(0)?.language == "pl") AppLanguage.Polish else AppLanguage.English
}

private val PolishText = mapOf(
    "Settings" to "Ustawienia",
    "Language" to "Język",
    "English" to "Angielski",
    "Polish" to "Polski",
    "Units" to "Jednostki",
    "Kilograms" to "Kilogramy",
    "Pounds" to "Funty",
    "Default rest" to "Domyślny odpoczynek",
    "seconds" to "sekund",
    "Data transfer" to "Przenoszenie danych",
    "Export backup" to "Eksportuj kopię zapasową",
    "Import backup" to "Importuj kopię zapasową",
    "Cancel" to "Anuluj",
    "Save" to "Zapisz",
    "Delete" to "Usuń",
    "Close" to "Zamknij",
    "Add" to "Dodaj",
    "Start" to "Rozpocznij",
    "Add exercise" to "Dodaj ćwiczenie",
    "Delete exercise" to "Usuń ćwiczenie",
    "Delete location" to "Usuń lokalizację",
    "Edit" to "Edytuj",
    "Remove" to "Usuń",
    "Back" to "Wstecz",
    "Exercises" to "Ćwiczenia",
    "Workout" to "Trening",
    "Progress" to "Postępy",
    "Locations" to "Lokalizacje",
    "History" to "Historia",
    "Search exercises" to "Szukaj ćwiczeń",
    "Search available exercises" to "Szukaj dostępnych ćwiczeń",
    "Add exercises" to "Dodaj ćwiczenia",
    "Create new exercise" to "Utwórz nowe ćwiczenie",
    "Start workout" to "Rozpocznij trening",
    "Active Workout" to "Aktywny trening",
    "Manage templates" to "Zarządzaj szablonami",
    "Templates" to "Szablony",
    "New exercise" to "Nowe ćwiczenie",
    "Edit exercise" to "Edytuj ćwiczenie",
    "Exercise name" to "Nazwa ćwiczenia",
    "Primary muscle" to "Główna partia mięśniowa",
    "Equipment" to "Sprzęt",
    "Exercise type" to "Rodzaj ćwiczenia",
    "Free weights" to "Wolne ciężary",
    "Machine" to "Maszyna",
    "Timed" to "Na czas",
    "Location" to "Lokalizacja",
    "No location" to "Bez lokalizacji",
    "Add location" to "Dodaj lokalizację",
    "New location" to "Nowa lokalizacja",
    "Location name" to "Nazwa lokalizacji",
    "Rename location" to "Zmień nazwę lokalizacji",
    "YouTube link (optional)" to "Link do YouTube (opcjonalnie)",
    "Add photo" to "Dodaj zdjęcie",
    "Remove photo" to "Usuń zdjęcie",
    "Photo selected" to "Zdjęcie wybrane",
    "Workout details" to "Szczegóły treningu",
    "Workout notes" to "Notatki treningu",
    "Exercise notes" to "Notatki ćwiczenia",
    "Notes" to "Notatki",
    "Set" to "Seria",
    "sets" to "serii",
    "No sets yet" to "Brak zapisanych serii",
    "Add set" to "Dodaj serię",
    "Edit set" to "Edytuj serię",
    "Weight (kg)" to "Ciężar (kg)",
    "Reps" to "Powtórzenia",
    "Time (seconds)" to "Czas (sekundy)",
    "Time (s)" to "Czas (s)",
    "Delete set" to "Usuń serię",
    "Workout history" to "Historia treningów",
    "Workouts" to "Treningi",
    "Exercises in workouts" to "Ćwiczenia w treningach",
    "Sets" to "Serie",
    "Workout templates" to "Szablony treningów",
    "Gym locations" to "Lokalizacje siłowni",
    "The file contains:" to "Plik zawiera:",
    "Choose file" to "Wybierz plik",
    "Import selected data" to "Importuj wybrane dane",
    "No completed workouts match these filters." to "Brak ukończonych treningów spełniających te filtry.",
)
