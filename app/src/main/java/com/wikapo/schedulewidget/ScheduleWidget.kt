package com.wikapo.schedulewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import kotlinx.coroutines.time.delay
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}

class ScheduleWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ScheduleContent()
        }
    }

    private fun getErrorIntent(context: Context, throwable: Throwable): PendingIntent {
        val intent = Intent(context, ScheduleWidget::class.java)
        intent.setAction("widgetError")
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        val rv = RemoteViews(context.packageName, R.layout.error_layout)
        rv.setTextViewText(
            R.id.error_text_view,
            "Error was thrown. \nThis is a custom view \nError Message: `${throwable.message}`"
        )
        rv.setOnClickPendingIntent(R.id.error_icon, getErrorIntent(context, throwable))
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, rv)
    }
}
//TODO Dzień tygodnia na 3+ szerokość
//TODO Krótka nazwa przedmiotu i godzina na szerokości 2
//TODO Przsuwając aplikację można otworzyć ustawienia
//TODO Przytrzymując widgeta pojawai się FAB do ustawień widgeta
//TODO Prgogress bar obecnie trwających zajęć

@Composable
fun ScheduleContent(doFetchSchedule: Boolean = true) {
    val schedule = remember { mutableStateListOf<Lesson>() }
    val date = remember { mutableStateOf(LocalDate.now()) }
    val scheduleInstance = ScheduleRequester()
    if (!doFetchSchedule)
        schedule.addAll(scheduleInstance.getExampleSchedule(10))


    LaunchedEffect(date.value) {
        schedule.clear()
        if (doFetchSchedule) {
                try {
                    schedule.addAll(scheduleInstance.fetchSchedule(date.value))
                    delay(Duration.ofMillis(200))
                } catch (e: Exception) {
                    Log.d("ERROR", e.toString())
                    schedule.add(Lesson(subjectId = "ERR", name = e.toString()))
                }
        }
        Log.d("CHECK UPDATE", schedule.toString())
    }

    Log.d("CHECK2elel", schedule.toString())
    Column(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.widgetBackground),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(vertical = 5.dp)
        ) {
            Box(
                modifier = GlanceModifier
                    .background(GlanceTheme.colors.primary)
                    .cornerRadius(14.dp)
                    .clickable { date.value = date.value.minusDays(1) }
            ) {
                Image(
                    modifier = GlanceModifier.padding(6.dp),
                    provider = ImageProvider(R.drawable.arrow_back),
                    contentDescription = "<=",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.inverseOnSurface)
                )
            }
            Text(
                text = "${date.value.format(DateTimeFormatter.ISO_DATE)}",
                style = TextStyle(color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.padding(6.dp).clickable { date.value = LocalDate.now() }
            )
            Box(
                modifier = GlanceModifier
                    .background(GlanceTheme.colors.primary)
                    .cornerRadius(15.dp)
                    .clickable { date.value = date.value.plusDays(1) }) {
                Image(
                    modifier = GlanceModifier.padding(6.dp),
                    provider = ImageProvider(R.drawable.arrow_forward),
                    contentDescription = "=>",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.inverseOnSurface)
                )
            }
        }
        if (schedule.isNotEmpty())
            LazyColumn(
                modifier = GlanceModifier
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxSize()
            ) {
                items(schedule.size) { index ->
                    val lesson = schedule[index]
                    Column {
                        if (lesson.subjectId == "ERR")
                            Text(
                                text = lesson.name,
                                style = TextStyle(
                                    color = GlanceTheme.colors.onError
                                ),
                                modifier = GlanceModifier
                                    .background(GlanceTheme.colors.error)
                                    .fillMaxWidth()
                                    .padding(7.5.dp)
                                    .cornerRadius(12.5.dp)
                            )
                        else
                            Column(
                                modifier = GlanceModifier
                                    .padding(10.dp, 5.dp)
                                    .background(if (index % 2 == 1) GlanceTheme.colors.secondaryContainer else GlanceTheme.colors.tertiaryContainer)
                                    .cornerRadius(12.5.dp)
                                    .height(50.dp)
                                    .clickable(onClick = actionStartActivity<MainActivity>())
                            ) {
                                Text(
                                    text = "[${lesson.kind}]\t\t${lesson.name}",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = GlanceTheme.colors.onSurface
                                    )
                                )
                                Row(modifier = GlanceModifier.fillMaxWidth()) {
                                    Text(
                                        text = "${lesson.startHour}:00 - ${lesson.endHour}:00",
                                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                                    )
                                    Text(
                                        modifier = GlanceModifier.fillMaxWidth(),
                                        text = lesson.place,
                                        style = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End,
                                            color = GlanceTheme.colors.onSurface
                                        )
                                    )
                                }
                            }
                        Spacer(GlanceModifier.height(5.dp))
                    }
                }
            }
        else
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BRAK ZAJĘĆ",
                    style = TextStyle(textAlign = TextAlign.Center)
                )
            }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(300, 400)
@Preview(200, 400)
@Preview(160, 160)
@Composable
private fun ScheduleContentPreview() {
    ScheduleContent(false)
}