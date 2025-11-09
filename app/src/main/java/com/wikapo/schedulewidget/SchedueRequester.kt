package com.wikapo.schedulewidget

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import java.net.SocketTimeoutException
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Lesson(
    @JsonNames("subject_id") val subjectId: String = "",
    val name: String = "",
    val kind: Char = '#',
    val teacher: String = "",
    val place: String = "",
    @JsonNames("start_hour") val startHour: Int = 0,
    @JsonNames("end_hour") val endHour: Int = 0,
)

class ScheduleRequester() {
    suspend fun fetchSchedule(date: LocalDate): List<Lesson> {
        try {
            return Json.decodeFromString<List<Lesson>>(
                get(
                    "https://eti.thefen.me/schedule/${date.format(DateTimeFormatter.ISO_DATE)}",
                    5000
                )
            )
        } catch (_: SocketTimeoutException) {
            val schedule: MutableList<Lesson> = ArrayList()
            schedule.add(
                Lesson(
                    subjectId = "TIMEOUT",
                    name = "Nie udało się nawiązać połączenia z serwerem. Spróbuj ponownie później."
                )
            )
            return schedule
        } catch (e: Exception) {
            val schedule: MutableList<Lesson> = ArrayList()
            schedule.add(Lesson(subjectId = "ERR", name = e.toString()))
            return schedule
        }
    }

    suspend fun get(url: String, timeout: Int): String = withContext(Dispatchers.IO) {
        Log.d("Request", "sending for [$url]")
        val connection = URL(url).openConnection()
        connection.connectTimeout = timeout
        with(connection as HttpsURLConnection) {
            var result = ""

            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    Log.d("InputStream", line)
                    result += line + "\n"
                }
            }
            return@withContext result
        }
    }

    fun getExampleSchedule(amount: Int): List<Lesson> {
        Log.d("Example", "Loading example schedule")
        val schedule: MutableList<Lesson> = ArrayList()
        for (i in 0..amount) {
            schedule += Lesson(name = "Lekcja $i", place = "Sala ${i + 100}")
        }
        return schedule
    }
}