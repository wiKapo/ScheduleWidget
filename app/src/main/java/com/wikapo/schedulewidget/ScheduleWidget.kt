package com.wikapo.schedulewidget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection


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


suspend fun fetchSchedule(date: LocalDate): String {
    return get("https://eti.thefen.me/schedule/${date.format(DateTimeFormatter.ISO_DATE)}")
}

suspend fun get(url: String): String = withContext(Dispatchers.IO) {
    Log.d("Request", "sending for [$url]")
    with(URL(url).openConnection() as HttpsURLConnection) {
        var result = "Testing"

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
    val schedule = remember { mutableStateOf("") }
    LaunchedEffect(scope) {
        schedule.value = fetchSchedule(date)
    }

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
                    scope.launch { schedule.value = fetchSchedule(date) }
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
                    scope.launch { schedule.value = fetchSchedule(date) }
                }
            )
        }
        Text(schedule.value, modifier = GlanceModifier.padding(24.dp))
        Button(
            text = "Reset",
            onClick = {}
        )
    }
}