package com.liftlog.app.core.util

private const val ImageSeparator = "\n--LIFTLOG-IMAGE--\n"

fun String?.toExerciseImageUris(): List<String> = this
    ?.split(ImageSeparator)
    ?.map(String::trim)
    ?.filter(String::isNotEmpty)
    .orEmpty()

fun List<String>.toExerciseImageStorageValue(): String? = asSequence()
    .map(String::trim)
    .filter(String::isNotEmpty)
    .joinToString(ImageSeparator)
    .takeIf(String::isNotEmpty)
