package com.wikapo.schedulewidget

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wikapo.schedulewidget.ui.theme.ScheduleWidgetTheme
import kotlinx.coroutines.time.delay
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val date: LocalDate? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable("date", LocalDate::class.java)
        } else {
            intent.extras?.getSerializable("date") as LocalDate?
        }
        enableEdgeToEdge()
        setContent {
            ScheduleWidgetTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { ScheduleTitle() }
                ) { paddingValues ->
                    if (date == null)
                        Schedule(LocalDate.now(), paddingValues)
                    else
                        Schedule(date, paddingValues)
                }
            }
        }
    }
}

//TODO Ustawienia
// Przezroczystość
// Pokaż weekend
// TODO Ładniejsze wyświetlanie daty
// TODO Dodanie wyświetlania dni tygodnia

@Composable
fun ScheduleTitle() {
    Row(
        modifier = Modifier
            .padding(top = 30.dp)
            .background(color = MaterialTheme.colorScheme.outlineVariant)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Plan zajęć stworzony przez ",
            fontWeight = FontWeight(400),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "wiKapo",
            fontWeight = FontWeight(700),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun Schedule(
    startingDate: LocalDate,
    paddingValues: PaddingValues = PaddingValues(),
    doFetchSchedule: Boolean = true
) {
    val schedule = remember { mutableStateListOf<Lesson>() }
    val loading = remember { mutableStateOf(false) }
    val date = remember { mutableStateOf(startingDate) }
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (!loading.value) date.value = date.value.minusDays(1) },
                colors = IconButtonDefaults.filledIconButtonColors(),
                modifier = Modifier.width(100.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = "previous"
                )
            }
            TextButton(
                onClick = { if (!loading.value) date.value = LocalDate.now() },
            ) {
                Text(
                    text = date.value.format(DateTimeFormatter.ISO_DATE),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            }
            IconButton(
                onClick = { if (!loading.value) date.value = date.value.plusDays(1) },
                colors = IconButtonDefaults.filledIconButtonColors(),
                modifier = Modifier.width(100.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_forward),
                    contentDescription = "next"
                )
            }
        }
        if (schedule.isNotEmpty())
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(schedule.size) { index ->
                    val lesson = schedule[index]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (lesson.subjectId) {
                            "ERR" -> Text(
                                text = lesson.name,
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.error)
                                    .fillMaxWidth()
                                    .padding(7.5.dp)
                            )

                            "TIMEOUT" -> {
                                Text(
                                    text = lesson.name,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .fillMaxWidth()
                                        .padding(7.5.dp)
                                )
                            }

                            else -> Box(
                                modifier = Modifier
                                    .background(
                                        color = if (index % 2 == 1) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = MaterialTheme.shapes.large
                                    )
                            ) {
                                Column(modifier = Modifier.padding(10.dp, 5.dp)) {
                                    Text(
                                        text = "[${lesson.kind}]\t\t${lesson.name}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lesson.teacher,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${lesson.startHour}:00 - ${lesson.endHour}:00",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = lesson.place,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (schedule.isNotEmpty() && (schedule[0].subjectId == "ERR" || schedule[0].subjectId == "TIMEOUT")) {
                    item {
                        IconButton(
                            onClick = { if (!loading.value) update.intValue += 1 },
                            modifier = Modifier
                                .background(
                                    color = if (!loading.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    shape = MaterialTheme.shapes.large
                                )
                                .size(50.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.refresh),
                                contentDescription = "refresh"
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        else
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (loading.value)
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                else
                    Text(
                        text = "BRAK ZAJĘĆ",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface

                    )
            }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleTitlePreview() {
    ScheduleWidgetTheme {
        ScheduleTitle()
    }
}

@Preview(showBackground = true)
@Composable
fun SchedulePreview() {
    ScheduleWidgetTheme {
        Schedule(LocalDate.now(), doFetchSchedule = false)
    }
}