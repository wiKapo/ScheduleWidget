package com.wikapo.schedulewidget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Calendar
import javax.net.ssl.HttpsURLConnection


class ScheduleWidgetReciever : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}

val calendar = Calendar.getInstance()

class ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        fetchSchedule()

        update(context, id)

        provideContent {
            ScheduleContent(context, id)
        }
    }
}

var result = "LOL"

suspend fun fetchSchedule(date: String = "") {
    get("https://eti.thefen.me/schedule/$date")
}

suspend fun get(url: String) = withContext(Dispatchers.IO) {
    with(URL(url).openConnection() as HttpsURLConnection) {

        inputStream.bufferedReader().use {
            it.lines().forEach { line ->
                Log.d("InputStream", line)
                result += line + "\n"
            }
        }
    }
}

//Log.d("calendar","${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DATE)})"
@Composable
fun ScheduleContent(context: Context, id: GlanceId) {
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
                    calendar.roll(Calendar.DATE, false)
//                    ScheduleWidget().update(context, id)
                }
            )
            Text(
                text = "${calendar.get(Calendar.DATE)}.${calendar.get(Calendar.MONTH) + 1}." +
                        "${calendar.get(Calendar.YEAR)}",
                modifier = GlanceModifier.padding(6.dp)
            )
            Button(
                text = "=>",
                onClick = { calendar.roll(Calendar.DATE, false) }
            )
        }
        Text(result, modifier = GlanceModifier.padding(24.dp))
        Button(
            text = "Reset",
            onClick = {}
        )
    }
}