package com.wikapo.schedulewidget

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wikapo.schedulewidget.ui.theme.ScheduleWidgetTheme
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            name = "Schedule Widget by wiKapo",
                            modifier = Modifier.padding(innerPadding)
                        )
                        if (date == null)
                            Schedule(LocalDate.now())
                        else
                            Schedule(date)
                    }
                }
            }
        }
    }
}

//TODO Ustawienia
// Przezroczystość
// Pokaż weekend
// TODO Pokaż plan z prowadzącymi

@Composable
fun Schedule(startingDate: LocalDate, doFetchSchedule: Boolean = true) {
    val date = remember { mutableStateOf(startingDate) }

    Column {
        Text(date.value.format(DateTimeFormatter.ISO_DATE))
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "WELCOME TO \n $name",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScheduleWidgetTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun SchedulePreview() {
    Schedule(LocalDate.now())
}