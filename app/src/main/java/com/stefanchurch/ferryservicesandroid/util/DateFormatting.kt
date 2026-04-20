package com.stefanchurch.ferryservicesandroid.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val appZone: ZoneId = ZoneId.systemDefault()
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
private val longDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

fun parseInstant(value: String?): Instant? = runCatching { value?.let(Instant::parse) }.getOrNull()

fun formatTime(value: String?): String {
    val instant = parseInstant(value) ?: return ""
    return timeFormatter.format(instant.atZone(appZone))
}

fun formatDate(value: LocalDate): String = longDateFormatter.format(value)
