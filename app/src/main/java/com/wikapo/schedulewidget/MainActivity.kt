package com.wikapo.schedulewidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.wikapo.schedulewidget.ui.theme.ScheduleWidgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScheduleWidgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Schedule Widget by wiKapo",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

//val message = remember { mutableStateOf("") }

/*TextField(
value = message.value,
placeholder = {Text("da")},

)*/

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