package com.wikapo.schedulewidget

import android.content.Context
import android.util.Log
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

class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}

class ScheduleWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ScheduleContent()
        }
    }
}
//TODO Dzień tygodnia na 3+ szerokość
//TODO Krótka nazwa przedmiotu i godzina na szerokości 2
//TODO Przsuwając aplikację można otworzyć ustawienia
//TODO Przytrzymując widgeta pojawai się FAB do ustawień widgeta

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

fun getExampleSchedule(): List<Lesson> {
    Log.d("Example", "Loading example schedule")
    val schedule: MutableList<Lesson> = ArrayList()
    for (i in 0..10) {
        schedule += Lesson(name = "Lekcja $i", place = "Sala ${i + 100}")
    }
    return schedule
}

@Composable
fun ScheduleContent(doFetchSchedule: Boolean = true) {
    val schedule = remember { mutableStateListOf<Lesson>() }
    val date = remember { mutableStateOf(LocalDate.now()) }
    if (!doFetchSchedule)
        schedule.addAll(getExampleSchedule())

    LaunchedEffect(date.value) {
        schedule.clear()
        if (doFetchSchedule)
            schedule.addAll(fetchSchedule(date.value))
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