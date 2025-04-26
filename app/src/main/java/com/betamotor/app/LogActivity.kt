package com.betamotor.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.betamotor.app.utils.LocalLogging

class LogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LogScreen()
        }
    }
}

@Composable
fun LogScreen() {
    val logger = LocalLogging(LocalContext.current)
    val logContent = logger.readLog()

    val logData = remember { mutableStateOf(logContent) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())  // Scrollable jika log panjang
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                text = "Log File",
                style = TextStyle(fontSize = 24.sp),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            for (data in logData.value) {
                Row {
                    Text(
                        text = data.first ?: "",
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = data.second ?: "",
                        style = TextStyle(fontSize = 16.sp),
                        modifier = Modifier,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = {
                logger.clearLog()
                logData.value = mutableListOf()
            }
        ) {
            Text("Clear Log")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLogScreen() {
    LogScreen()
}
