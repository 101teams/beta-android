package com.betamotor.app.presentation.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.betamotor.app.theme.GrayDark
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.White

data class MotorcycleFormData(
    val name: String,
    val deviceId: String,
    val password: String
)

@Composable
fun SaveMotorcycleDialog(
    openDialog: MutableState<Boolean>,
    onSave: (data: MotorcycleFormData) -> Unit,
) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val deviceId = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Dialog(onDismissRequest = {
        openDialog.value = false
    }) {
        Card(
            shape = RoundedCornerShape(10.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .background(GrayDark)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Successfully Connected",
                    modifier = Modifier,
                    style = MaterialTheme.typography.h4,
                    fontSize = 24.sp,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Name", style = TextStyle(fontSize = 16.sp, color = White), modifier = Modifier.width(90.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Input(placeholder = "", binding = name, inputTextStyle = TextStyle(color = White))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("ID", style = TextStyle(fontSize = 16.sp, color = White), modifier = Modifier.width(90.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Input(placeholder = "", binding = deviceId, inputTextStyle = TextStyle(color = White))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Password", style = TextStyle(fontSize = 16.sp, color = White), modifier = Modifier.width(90.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Input(placeholder = "", binding = password, inputTextStyle = TextStyle(color = White))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Green, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 16.dp, horizontal = 10.dp)
                    .clickable {
                        if (name.value.isEmpty() || deviceId.value.isEmpty() || password.value.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@clickable
                        }

                        onSave(MotorcycleFormData(name.value, deviceId.value, password.value))
                    },
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text("Save", style = MaterialTheme.typography.button, color = White, fontSize = 20.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun SaveMotorcycleDialogPreview() {
    val openDialog = remember { mutableStateOf(false) }
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { openDialog.value = true }) {
                Text(text = "Show Dialog")
            }

            SaveMotorcycleDialog(openDialog = openDialog, onSave = {})
        }
    }
}