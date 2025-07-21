package com.betamotor.app.presentation.component

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.betamotor.app.R
import com.betamotor.app.theme.GrayDark
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.White
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaveTrackingMotorcycleDialog(
    openDialog: MutableState<Boolean>,
    csvData: MutableState<MutableList<String>>,
    onSave: (filename: String) -> Unit,
) {
    val context = LocalContext.current
    val filename = remember { mutableStateOf("") }

    Dialog(onDismissRequest = {
        openDialog.value = false
        onSave(filename.value)
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
                    text = stringResource(R.string.track_record_complete),
                    modifier = Modifier,
                    style = MaterialTheme.typography.h4,
                    fontSize = 24.sp,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.file_name), style = TextStyle(fontSize = 16.sp, color = White), modifier = Modifier.width(90.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Input(placeholder = "", binding = filename, inputTextStyle = TextStyle(color = White))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Green, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 16.dp, horizontal = 10.dp)
                    .clickable {
                        if (filename.value.isEmpty()) {
                            Toast.makeText(context, context.getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
                            return@clickable
                        }

                        generateCSVTrackingMotorcycle(context, csvData, filename.value, openDialog)
                    },
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.button, color = White, fontSize = 20.sp)
                }
            }
        }
    }
}

fun generateCSVTrackingMotorcycle(context: Context, csvData: MutableState<MutableList<String>>, filename: String, openDialog: MutableState<Boolean>,) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault())
    val time = dateFormat.format(Date())

    val fileName = "${filename}_$time.csv"
    Log.d("generated filename", fileName)
    val csvHeader = "Time,Speed,Altitude,RPM,Latitude,Longitude"

    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsDir, fileName)

    try {
        val writer = FileWriter(file)
        writer.append(csvHeader).append("\n")
        csvData.value.forEach {
            Log.d("TrackingValue", it)
            writer.append(it).append("\n")
        }
        writer.flush()
        writer.close()
        println("CSV berhasil dibuat di: ${file.absolutePath}")

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.ms-excel"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_file)))
    } catch (e: IOException) {
        e.printStackTrace()
    }
}