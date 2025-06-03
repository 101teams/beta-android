package com.betamotor.app.presentation.component

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun ExportDialog(
    openDialog: MutableState<Boolean>,
    context: Context,
    csvData: MutableState<MutableList<String>>,
    onDone: () -> Unit
) {
    Dialog(onDismissRequest = {
        openDialog.value = false
        onDone()
    }) {
        Card(
            //shape = MaterialTheme.shapes.medium,
            shape = RoundedCornerShape(18.dp),
            // modifier = modifier.size(280.dp, 240.dp)
            modifier = Modifier.padding(10.dp,5.dp,10.dp,10.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .background(GrayDark)
                    .padding(12.dp, 20.dp, 12.dp, 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                Text(
                    text = stringResource(R.string.save_records),
                    modifier = Modifier,
                    style = MaterialTheme.typography.h4,
                    fontSize = 24.sp,
                    color = Color.White,
                )

                Button(
                    modifier = Modifier.padding(top = 12.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Green),
                    onClick = {
                        generateCSV(context, csvData)
                    },
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp,),
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.button,
                            color = White,
                        )
                    }
                }
            }
        }
    }
}

fun generateCSV(context: Context, csvData: MutableState<MutableList<String>>) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault())
    val time = dateFormat.format(Date())

    val fileName = "beta_engine_data_$time.csv"
    Log.d("generated filename", fileName)
    val csvHeader = "Time,Type,Value"

    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsDir, fileName)

    try {
        val writer = FileWriter(file)
        writer.append(csvHeader).append("\n")
        csvData.value.forEach { writer.append(it).append("\n") }
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