package com.wikapo.schedulewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
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
import androidx.glance.layout.size
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
        intent.action = "widgetError"
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
    val loading = remember { mutableStateOf(false) }
    val date = remember { mutableStateOf(LocalDate.now()) }
    val update = remember { mutableIntStateOf(0) }
    val scheduleInstance = ScheduleRequester()
    if (!doFetchSchedule)
        schedule.addAll(scheduleInstance.getExampleSchedule(10))

    LaunchedEffect(date.value, update.intValue) {
        Log.d("UPDATE val", update.intValue.toString())
        loading.value = true
        schedule.clear()
        if (doFetchSchedule) {
            schedule.addAll(scheduleInstance.fetchSchedule(date.value))
            delay(Duration.ofMillis(200))
            loading.value = false
        }
        loading.value = false
        Log.d("CHECK UPDATE", schedule.toString())
    }

    Column(
        modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.widgetBackground),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(vertical = 4.dp)
        ) {
            Box(
                modifier = GlanceModifier
                    .background(if (!loading.value) GlanceTheme.colors.primary else GlanceTheme.colors.secondary)
                    .cornerRadius(14.dp)
                    .clickable { if (!loading.value) date.value = date.value.minusDays(1) }
            ) {
                Image(
                    modifier = GlanceModifier.padding(6.dp),
                    provider = ImageProvider(R.drawable.arrow_back),
                    contentDescription = "previous",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.inverseOnSurface)
                )
            }
            Text(
                text = "${date.value.format(DateTimeFormatter.ISO_DATE)}",
                style = TextStyle(color = GlanceTheme.colors.onSurface),
                modifier = GlanceModifier.padding(6.dp).cornerRadius(14.dp)
                    .clickable { if (!loading.value) date.value = LocalDate.now() }
            )
            Box(
                modifier = GlanceModifier
                    .background(if (!loading.value) GlanceTheme.colors.primary else GlanceTheme.colors.secondary)
                    .cornerRadius(15.dp)
                    .clickable { if (!loading.value) date.value = date.value.plusDays(1) }) {
                Image(
                    modifier = GlanceModifier.padding(6.dp),
                    provider = ImageProvider(R.drawable.arrow_forward),
                    contentDescription = "next",
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.inverseOnSurface)
                )
            }
        }
        if (schedule.isNotEmpty())
            LazyColumn(
                modifier = GlanceModifier
                    .padding(horizontal = 10.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(schedule.size) { index ->
                    val lesson = schedule[index]
                    Column(
                        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (lesson.subjectId) {
                            "ERR" -> Text(
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

                            "TIMEOUT" -> {
                                Text(
                                    text = lesson.name,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onErrorContainer
                                    ),
                                    modifier = GlanceModifier
                                        .background(GlanceTheme.colors.errorContainer)
                                        .fillMaxWidth()
                                        .padding(7.5.dp)
                                        .cornerRadius(12.5.dp)
                                )
                            }

                            else -> Column(
                                modifier = GlanceModifier
                                    .padding(10.dp, 5.dp)
                                    .background(if (index % 2 == 1) GlanceTheme.colors.secondaryContainer else GlanceTheme.colors.tertiaryContainer)
                                    .cornerRadius(12.5.dp)
                                    .clickable(
                                        onClick = actionStartActivity<MainActivity>(
                                            parameters = actionParametersOf(
                                                pairs = arrayOf(ActionParameters.Key<LocalDate>("date") to date.value)
                                            )
                                        )
                                    )
                            ) {
                                Text(
                                    text = "[${lesson.kind}]\t\t${lesson.name}",
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = GlanceTheme.colors.onSurface
                                    ),
                                    maxLines = 1
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
                        }
                    }
                }
                if (schedule[0].subjectId == "ERR" || schedule[0].subjectId == "TIMEOUT") {
                    item {
                        Box(
                            modifier = GlanceModifier
                                .background(if (!loading.value) GlanceTheme.colors.primary else GlanceTheme.colors.secondary)
                                .cornerRadius(15.dp)
                                .size(50.dp)
                                .clickable { if (!loading.value) update.intValue += 1 }) {
                            Image(
                                modifier = GlanceModifier.padding(6.dp).fillMaxSize(),
                                provider = ImageProvider(R.drawable.refresh),
                                contentDescription = "refresh",
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.inverseOnSurface)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = GlanceModifier.height(5.dp))
                }
            }
        else
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (loading.value)
                    CircularProgressIndicator(
                        modifier = GlanceModifier.size(50.dp),
                        color = GlanceTheme.colors.primary
                    )
                else
                    Text(
                        text = "BRAK ZAJĘĆ",
                        style = TextStyle(
                            textAlign = TextAlign.Center,
                            color = GlanceTheme.colors.onSurface
                        )
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