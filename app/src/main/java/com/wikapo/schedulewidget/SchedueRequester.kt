package com.wikapo.schedulewidget

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
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
//        throw IllegalAccessException("adhbahd hjdsfj hsdvfjvdsjhvk jcvsjgw yet ri 7 364723gyu qggfkyg4f yuwegu jhhahdjahdjhadsjhdhajhsd")
        val result =
            get("https://eti.thefen.me/schedule/${date.format(DateTimeFormatter.ISO_DATE)}")
        return Json.decodeFromString<List<Lesson>>(result)
    }

    suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        Log.d("Request", "sending for [$url]")
        with(URL(url).openConnection() as HttpsURLConnection) {
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