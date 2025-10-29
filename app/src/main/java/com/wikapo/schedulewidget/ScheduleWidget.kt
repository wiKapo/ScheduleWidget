package com.wikapo.schedulewidget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection

@Serializable
data class Lesson(
    val subject_id: String = "",
    val name: String = "",
    val kind: Char = '#',
    val teacher: String = "",
    val place: String = "",
    val start_hour: Int = 0,
    val end_hour: Int = 0,
)


class ScheduleWidgetReciever : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}

var date: LocalDate = LocalDate.now()

class ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ScheduleContent(context, id)
        }
    }
}


suspend fun fetchSchedule(date: LocalDate): List<Lesson> {
    val result = get("https://eti.thefen.me/schedule/${date.format(DateTimeFormatter.ISO_DATE)}")

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

@Composable
fun ScheduleContent(context: Context, id: GlanceId) {
    val scope = rememberCoroutineScope()
    val schedule = remember { mutableListOf(Lesson()) }
    schedule.removeIf { it -> it.name.isEmpty() }

    LaunchedEffect(scope) {
        updateAppWidgetState(context, id) { //TODO look into the states
            schedule.addAll(fetchSchedule(date))
            Log.d("CHECK LAUNCH", schedule.toString())
        }
    }

    Log.d("CHECK2elel", schedule.toString())
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                text = "<=",
                onClick = {
                    date = date.minusDays(1)
                    scope.launch {
                        updateAppWidgetState(context, id) {
                            schedule.removeIf { true }
                            schedule.addAll(fetchSchedule(date))
                            Log.d("CHECK", schedule.toString())
                        }
                    }
                }
            )
            Text(
                text = "${date.format(DateTimeFormatter.ISO_DATE)}",
                modifier = GlanceModifier.padding(6.dp)
            )
            Button(
                text = "=>",
                onClick = {
                    date = date.plusDays(1)
                    scope.launch {
                        updateAppWidgetState(context, id) {
                            schedule.removeIf { true }
                            schedule.addAll(fetchSchedule(date))
                            Log.d("CHECK", schedule.toString())
                        }
                    }
                }
            )
        }
        Text(text = schedule.toString())
        LazyColumn(
            modifier = GlanceModifier.background(Color(64, 64, 64, 64)).cornerRadius(25.dp)
        ) {
            if (schedule.isNotEmpty())
                items(schedule.size) { index ->
                    val lesson = schedule[index]
                    Column(modifier = GlanceModifier.padding(5.dp)) {
                        Text(
                            text = "[${lesson.kind}] ${lesson.name}",
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                        Text(text = lesson.place, style = TextStyle(fontWeight = FontWeight.Medium))
                        Text(text = lesson.teacher)
                        Text(text = "${lesson.start_hour}:00\n${lesson.end_hour}:00")//TODO maybe change to LocalTime??
                    }
                }
            else
                item {
                    Text("NIC NIE MA", modifier = GlanceModifier.padding(24.dp))
                }
        }
        Button(
            text = "Reset",
            onClick = {}
        )
    }
}