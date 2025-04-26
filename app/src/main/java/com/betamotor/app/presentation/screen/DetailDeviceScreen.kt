package com.betamotor.app.presentation.screen

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.betamotor.app.BuildConfig
import com.betamotor.app.R
import com.betamotor.app.data.constants
import com.betamotor.app.presentation.component.BackInvokeHandler
import com.betamotor.app.presentation.component.ExportDialog
import com.betamotor.app.presentation.component.observeLifecycle
import com.betamotor.app.presentation.viewmodel.BluetoothViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.GrayDark
import com.betamotor.app.theme.GrayLight
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.RobotoCondensed
import com.betamotor.app.theme.White
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.MQTTHelper
import com.betamotor.app.utils.PrefManager
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DetailDeviceScreen(
    navController: NavController
) {
    val viewModel = hiltViewModel<BluetoothViewModel>()
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val prefManager = PrefManager(context)

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var lifecycleEvent by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    viewModel.observeLifecycle(lifecycle = lifecycleOwner.lifecycle)

    DisposableEffect(lifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            lifecycleEvent = event
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    LaunchedEffect(lifecycleEvent) {
        when (lifecycleEvent) {
            Lifecycle.Event.ON_DESTROY -> {//before was on pause
                viewModel.disconnectDevice()
                navController.popBackStack()
            }
            else -> {}
        }
    }

    val tabs = listOf("Info", "Data", "Tune", "Diag")
    val icons = listOf(
        painterResource(id = R.drawable.ic_tab2),
        painterResource(id = R.drawable.ic_tab1),
        painterResource(id = R.drawable.ic_tab3),
        painterResource(id = R.drawable.ic_tab4),
    )

    val coroutineScope = rememberCoroutineScope()
    val tab = remember { mutableStateOf(0) }
    val showDialogExport = remember { mutableStateOf(false) }
    val csvData: MutableState<MutableList<String>> = remember { mutableStateOf(mutableListOf()) }

    val isStreaming = remember { mutableStateOf(false) }

    BackInvokeHandler {
        viewModel.disconnectDevice()
        navController.popBackStack()
    }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Gray,
                        Black,
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(R.drawable.img_betamotor),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }

            Scaffold(
                backgroundColor = Color.Transparent,
                bottomBar = {
                    // TabRow at the bottom
                    TabRow(
                        selectedTabIndex = tab.value,
                        indicator = { tabPositions ->
//                            TabRowDefaults.Indicator(
//                                Modifier.pagerTabIndicatorOffset(tab, tabPositions)
//                            )
                        },
                        backgroundColor = Gray,
                        contentColor = White,
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = tab.value == index
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Green else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null // Removes ripple effect
                                    ) {
                                        if (index != 0) {
                                            isStreaming.value = false
                                        }
                                        tab.value = index
                                    }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = icons[index],
                                        contentDescription = title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(title, style = MaterialTheme.typography.button,)
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier.padding(paddingValues),
                ) {
                    when (tab.value) {
                        0 -> page2(prefManager, context)
                        1 -> page1(navController, isStreaming, showDialogExport, csvData, prefManager, context)
                        2 -> page3(prefManager, context)
                        3 -> page4(prefManager, context, navController)
                    }
                }
            }
        }
    }

    if (showDialogExport.value && csvData.value.isNotEmpty()) {
        ExportDialog(openDialog = showDialogExport, context = context, csvData = csvData) {
            showDialogExport.value = false
            csvData.value = mutableListOf()
        }
    }
}

@Composable
fun page1(navController: NavController, isStreaming: MutableState<Boolean>, showDialogExport: MutableState<Boolean>, csvData: MutableState<MutableList<String>>, prefManager: PrefManager, context: Context) {
    val isRecording = remember { mutableStateOf(false) }
    val rpm = remember { mutableStateOf("-") }
    val gasPosition = remember { mutableStateOf("-") }
    val actuatedSpark = remember { mutableStateOf("-") }
    val engineCoolant = remember { mutableStateOf("-") }
    val airTemp = remember { mutableStateOf("-") }
    val atmospherePressure = remember { mutableStateOf("-") }
    val operatingHours = remember { mutableStateOf("-") }
    val batteryVoltage = remember { mutableStateOf("-") }

    val btViewModel = hiltViewModel<BluetoothViewModel>()
    val scope = rememberCoroutineScope()

    var lifecycleEvent by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }

    LaunchedEffect(lifecycleEvent) {
        when (lifecycleEvent) {
            Lifecycle.Event.ON_PAUSE -> {
                csvData.value = mutableListOf()
                isStreaming.value = false
            }
            else -> {}
        }
    }

    fun streamData(){
        Log.d("call steam", isStreaming.value.toString())
        if (isStreaming.value) {

            val data = ByteArray(5)
            data[0] = ((0x0101 shr 8) and 0xFF)
            data[1] = 0x0101 and 0xFF
            data[2] = 0x02
            // get data RPM
            data[3] = ((constants.RLI_ENGINE_SPEED shr 8) and 0xFF).toByte()
            data[4] = (constants.RLI_ENGINE_SPEED and 0xFF).toByte()
            btViewModel.sendCommandByteDES(data)
        }
    }

    fun saveCsvData(type: String, value: String){
        Log.d(type, value)
        if(isRecording.value) {
            Log.d("write", value)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val time = dateFormat.format(Date())

            val tmpCSVData = csvData.value
            tmpCSVData.add("$time,$type,$value")

            csvData.value = tmpCSVData
        }
    }

    fun extractResData(fullData: ByteArray): Int {
        return when {
            fullData.size > 7 -> ((fullData[6].toUByte().toInt() shl 8) or fullData[7].toUByte().toInt()) and 0xFFFF
            fullData.size > 6 -> fullData[6].toUByte().toInt()
            else -> 0
        }
    }

    fun extractSignedResData(fullData: ByteArray): Int {
        return when {
            fullData.size > 7 -> convertSignedTwosComplement(((fullData[6].toInt() shl 8) or fullData[7].toInt()) and 0xFFFF, 16)
            fullData.size > 6 -> convertSignedTwosComplement(fullData[6].toInt(), 8)
            else -> 0
        }
    }

    fun nextCommand(nextRliId: Int, data: ByteArray) {
        data[3] = ((nextRliId shr 8) and 0xFF).toByte()
        data[4] = (nextRliId and 0xFF).toByte()

        if (isStreaming.value) {
            Handler(Looper.getMainLooper()).postDelayed({
                btViewModel.sendCommandByteDES(data)
            }, 50)
        }
    }

    fun onDESDataReceived(rliID: Byte, fullData: ByteArray) {
        LocalLogging().writeLog(context, "DES data received: rliID = $rliID; ${fullData.joinToString(", ")}")

        val data = ByteArray(5)
        data[0] = ((0x0101 shr 8) and 0xFF).toByte()
        data[1] = (0x0101 and 0xFF).toByte()
        data[2] = 0x02

        scope.launch {
            try {
                if (fullData[1].toInt() == 0x0101 and 0xFF) {
                    when (rliID) {
                        constants.RLI_ENGINE_SPEED.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_ENGINE_SPEED received")
                            val resData = extractResData(fullData)
                            LocalLogging().writeLog(context, "RLI_ENGINE_SPEED data: $resData")
                            rpm.value = resData.toString()
                            saveCsvData("RPM", resData.toString())

                            nextCommand(constants.RLI_GAS_POSITION, data)
                        }
                        constants.RLI_GAS_POSITION.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_GAS_POSITION received")
                            val resData = extractResData(fullData)
                            LocalLogging().writeLog(context, "RLI_GAS_POSITION data: ${resData / 16}")
                            gasPosition.value = (resData / 16).toString()
                            saveCsvData("THROTTLE", (resData / 16).toString())

                            nextCommand(constants.RLI_ACTUATED_SPARK, data)
                        }
                        constants.RLI_ACTUATED_SPARK.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_ACTUATED_SPARK received")
                            val resData = extractSignedResData(fullData)
                            LocalLogging().writeLog(context, "RLI_ACTUATED_SPARK data: ${resData / 16}")
                            actuatedSpark.value = (resData / 16).toString()
                            saveCsvData("SPARK ADV", (resData / 16).toString())

                            nextCommand(constants.RLI_COOLANT_TEMP, data)
                        }
                        constants.RLI_COOLANT_TEMP.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_COOLANT_TEMP received")
                            val resData = extractSignedResData(fullData)
                            LocalLogging().writeLog(context, "RLI_COOLANT_TEMP data: ${resData / 16}")
                            engineCoolant.value = (resData / 16).toString()
                            saveCsvData("ENGINE TEMP", (resData / 16).toString())

                            nextCommand(constants.RLI_AIR_TEMP, data)
                        }
                        constants.RLI_AIR_TEMP.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_AIR_TEMP received")
                            val resData = extractSignedResData(fullData)
                            LocalLogging().writeLog(context, "RLI_AIR_TEMP data: ${resData / 16}")
                            airTemp.value = (resData / 16).toString()
                            saveCsvData("AIR TEMP", (resData / 16).toString())

                            nextCommand(constants.RLI_ATMOSPHERE_PRESSURE, data)
                        }
                        constants.RLI_ATMOSPHERE_PRESSURE.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_ATMOSPHERE_PRESSURE received")
                            val resData = extractResData(fullData)
                            LocalLogging().writeLog(context, "RLI_ATMOSPHERE_PRESSURE data: $resData")
                            atmospherePressure.value = resData.toString()
                            saveCsvData("ATM PRESSURE", resData.toString())

                            nextCommand(constants.RLI_OPERATING_HOURS, data)
                        }
                        constants.RLI_OPERATING_HOURS.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_OPERATING_HOURS received")
                            val resData = extractResData(fullData)
                            LocalLogging().writeLog(context, "RLI_OPERATING_HOURS data: ${resData / 8}")
                            operatingHours.value = (resData / 8).toString()
                            saveCsvData("OP. TIME", (resData / 8).toString())

                            nextCommand(constants.RLI_BATTERY_VOLTAGE, data)
                        }
                        constants.RLI_BATTERY_VOLTAGE.toByte() -> {
                            LocalLogging().writeLog(context, "RLI_BATTERY_VOLTAGE received")
                            val resData = extractResData(fullData)
                            LocalLogging().writeLog(context, "RLI_BATTERY_VOLTAGE data: ${resData / 16}")

                            batteryVoltage.value = (resData / 16).toString()
                            saveCsvData("BATTERY VOLTAGE", (resData / 16).toString())

                            val jsonPayload = """
                            {
                              "vin": "${prefManager.getMotorcycleVIN()}",
                              "rpm": ${rpm.value},
                              "throttle": ${gasPosition.value},
                              "sparkAdv": ${actuatedSpark.value},
                              "engineTemp": ${engineCoolant.value},
                              "airTemp": ${airTemp.value},
                              "atmPressure": ${atmospherePressure.value},
                              "opTime": ${operatingHours.value},
                              "batteryVoltage": ${batteryVoltage.value}
                            }
                        """.trimIndent()

                            MQTTHelper(context).publishMessage("Beta/${prefManager.getSelectedMotorcycleId()}/enginedata", jsonPayload)

                            streamData()
                        }
                        else -> {
                            LocalLogging().writeLog(context, "Unknown data received. RliId: $rliID; data: $fullData")
                        }
                    }
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                LocalLogging().writeLog(context, "Error: ${e.message}")
            } catch (e: Exception) {
                LocalLogging().writeLog(context, "Error: ${e.message}")
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (!BuildConfig.DEBUG) {
            return@LaunchedEffect
        }

        val fullData = byteArrayOf(
            0x01, 0x01, 0x00, 0x02, 0x00, 0x01, 0x66
        )
        val rliID = fullData[5]
        onDESDataReceived(rliID, fullData)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 26.dp, start = 26.dp, end = 26.dp)
            .background(color = Color.Transparent),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GrayDark)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("ENGINE DATA", style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailDataItem(title = "RPM", value = rpm.value, suffix = "rpm")
                    DetailDataItem(title = "THROTTLE", value = gasPosition.value, suffix = "%")
                    DetailDataItem(title = "SPARK ADV", value = actuatedSpark.value, suffix = "°")
                    DetailDataItem(title = "ENGINE TEMP.", value = engineCoolant.value, suffix = "°C")
                    DetailDataItem(title = "AIR TEMP.", value = airTemp.value, suffix = "°C")
                    DetailDataItem(title = "ATM. PRESSURE", value = atmospherePressure.value, suffix = "Mbar")
                    DetailDataItem(title = "OP. TIME", value = operatingHours.value, suffix = "h")
                    DetailDataItem(title = "BATTERY VOLTAGE", value = batteryVoltage.value, suffix = "V")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .height(62.dp)
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    prefManager.setSelectedMotorcycleId("")
                    btViewModel.disconnectDevice()
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent,),
                border = BorderStroke(1.dp, Color.Red),
            ) {
                Text("Close Connection", style = MaterialTheme.typography.button, fontSize = 18.sp, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Gray)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isStreaming.value = !isStreaming.value

                            btViewModel.setOnReadDataDESCalback(onDataReceived = {rliID, fullData ->
                                onDESDataReceived(rliID, fullData)
                            })

                            if (isStreaming.value) {
                                streamData()
                            }
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                bitmap = ImageBitmap.imageResource( if (isStreaming.value) R.drawable.ic_stop_red else R.drawable.ic_play_white),
                                contentDescription = "",
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(52.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (isStreaming.value) "Streaming" else "Start Stream", style = MaterialTheme.typography.button)
                        }
                    }

                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isStreaming.value) {
                                Toast.makeText(context, "Please start streaming first", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            isRecording.value = !isRecording.value

                            if (!isRecording.value) {
                                showDialogExport.value = true
                            }
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                bitmap = ImageBitmap.imageResource( if (isRecording.value) R.drawable.ic_stop_record else R.drawable.ic_start_record),
                                contentDescription = "",
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(52.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (isRecording.value) "Recording" else "Start Record", style = MaterialTheme.typography.button)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun convertSignedTwosComplement(value: Int, bitSize: Int): Int {
    val maxValue = (1 shl (bitSize - 1))
    return if (value >= maxValue) {
        value - (1 shl bitSize)
    } else {
        value
    }
}

fun convertVINData(fullData: ByteArray): String {
    var value = ""

    val length = fullData[3].toInt() - 2

    for(i in 6..6+(length-1)) {
        value += fullData[i].toInt().toChar()
    }

    return value
}

fun getVINData(btViewModel: BluetoothViewModel, rliId: Int) {
    val data = ByteArray(5)
    data[0] = ((0x0301 shr 8) and 0xFF)
    data[1] = 0x0301 and 0xFF
    data[2] = 0x02

    data[3] = ((rliId shr 8) and 0xFF).toByte()
    data[4] = (rliId and 0xFF).toByte()

    Handler(Looper.getMainLooper()).postDelayed({
        btViewModel.sendCommandByteDES(data)
    },50)
}

fun getTuneData(btViewModel: BluetoothViewModel, rliId: Int, isRead: Boolean, writeData: Int?) {
    val data = ByteArray(if(isRead) 5 else 7)
    if (isRead) {
        data[0] = ((0x0101 shr 8) and 0xFF) //read 0x0101 write 0x0102
        data[1] = 0x0101 and 0xFF //read 0x0101 write 0x0102
    } else {
        data[0] = ((0x0102 shr 8) and 0xFF)
        data[1] = 0x0102 and 0xFF
    }
    data[2] = 0x02

    data[3] = ((rliId shr 8) and 0xFF).toByte()
    data[4] = (rliId and 0xFF).toByte()

    if (!isRead && writeData != null) {
        data[5] = ((writeData shr 8) and 0xFF).toByte()
        data[6] = (writeData and 0xFF).toByte()
    }

    Handler(Looper.getMainLooper()).postDelayed({
        btViewModel.sendCommandByteDES(data)
    },50)
}

@Composable
fun page2(prefManager: PrefManager, context: Context) {

    val btViewModel = hiltViewModel<BluetoothViewModel>()
    val vin = remember { mutableStateOf("-") }
    val ecuDRW = remember { mutableStateOf("-") }
    val ecuHW = remember { mutableStateOf("-") }
    val ecuSW = remember { mutableStateOf("-") }
    val calibration = remember { mutableStateOf("-") }
    val homologation = remember { mutableStateOf("-") }

    LaunchedEffect(Unit) {
        btViewModel.setOnReadDataDESCalback(onDataReceived = { rliID, fullData ->
            if (fullData[1].toInt() == 0x0301 and 0xFF) {
                when (rliID) {
                    constants.ECU_VIN.toByte() -> {
                        vin.value = convertVINData(fullData)
                        prefManager.setMotorcycleVIN(vin.value)

                        getVINData(btViewModel, constants.ECU_DRAWING_NUMBER)
                    }
                    constants.ECU_DRAWING_NUMBER.toByte() -> {
                        ecuDRW.value = convertVINData(fullData)

                        getVINData(btViewModel, constants.ECU_HW_NUMBER)
                    }
                    constants.ECU_HW_NUMBER.toByte() -> {
                        ecuHW.value = convertVINData(fullData)

                        getVINData(btViewModel, constants.ECU_SW_NUMBER)
                    }
                    constants.ECU_SW_NUMBER.toByte() -> {
                        ecuSW.value = convertVINData(fullData)

                        getVINData(btViewModel, constants.ECU_SW_VERSION)
                    }
                    constants.ECU_SW_VERSION.toByte() -> {
                        calibration.value = convertVINData(fullData)

                        getVINData(btViewModel, constants.ECU_HOMOLOGATION)
                    }
                    constants.ECU_HOMOLOGATION.toByte() -> {
                        homologation.value = convertVINData(fullData)
                        val jsonPayload = """
                            {
                              "vin": "${vin.value}",
                              "ecuDrw": "${ecuDRW.value}",
                              "ecuHw": "${ecuHW.value}",
                              "ecuSw": "${ecuSW.value}",
                              "calibration": "${calibration.value}",
                              "homolCode": "${homologation.value}"
                            }
                        """.trimIndent()
                        MQTTHelper(context).publishMessage("Beta/${prefManager.getSelectedMotorcycleId()}/engineinfo", jsonPayload)
                    }
                }
            }
        })

        getVINData(btViewModel, constants.ECU_VIN)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 26.dp)
            .background(color = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GrayDark)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("ENGINE INFO", style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(24.dp))
                DetailDataItem(title = "VIN", value = vin.value, suffix = "")
                DetailDataItem(title = "ECU DRW", value = ecuDRW.value, suffix = "")
                DetailDataItem(title = "ECU HW", value = ecuHW.value, suffix = "")
                DetailDataItem(title = "ECU SW.", value = ecuSW.value, suffix = "")
                DetailDataItem(title = "CALIBRATION", value = calibration.value, suffix = "")
                DetailDataItem(title = "HOMOL. CODE", value = homologation.value, suffix = "")
            }
        }
    }
}

@Composable
fun page3(prefManager: PrefManager, context: Context) {

    val btViewModel = hiltViewModel<BluetoothViewModel>()
    val adjustmentValue = remember { mutableStateOf(0) }
    val tuneOffset = remember { mutableStateOf(0) }
    val tuneMin = remember { mutableStateOf(0) }
    val tuneMax = remember { mutableStateOf(0) }

    val isApply = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        btViewModel.setOnReadDataDESCalback(onDataReceived = { rliID, fullData ->
            when (rliID) {
                constants.TUNE_OFFSET.toByte() -> {
                    if (isApply.value) {
                        isApply.value = false
                        getTuneData(btViewModel, constants.TUNE_OFFSET, true, null)
                    } else {
                        val resData = convertSignedTwosComplement(((fullData[6].toUByte().toInt() shl 8) or fullData[7].toUByte().toInt()) and 0xFFFF, 16)
                        tuneOffset.value = 1900 + resData
                        adjustmentValue.value = resData

                        val jsonPayload = """
                            {
                              "vin": "${prefManager.getMotorcycleVIN()}",
                              "idleTarget": ${tuneOffset.value},
                              "adjustment": ${adjustmentValue.value}
                            }
                        """.trimIndent()

                        MQTTHelper(context).publishMessage("Beta/${prefManager.getSelectedMotorcycleId()}/idleadjustment", jsonPayload)

                        getTuneData(btViewModel, constants.TUNE_MIN, true, null)
                    }
                }
                constants.TUNE_MIN.toByte() -> {
                    val resData = convertSignedTwosComplement(((fullData[6].toUByte().toInt() shl 8) or fullData[7].toUByte().toInt()) and 0xFFFF, 16)
                    tuneMin.value = resData

                    getTuneData(btViewModel, constants.TUNE_MAX, true, null)
                    Log.d("TUNEMIN", tuneMin.value.toString())
                }
                constants.TUNE_MAX.toByte() -> {
                    val resData = convertSignedTwosComplement(((fullData[6].toUByte().toInt() shl 8) or fullData[7].toUByte().toInt()) and 0xFFFF, 16)
                    tuneMax.value = resData
                    Log.d("TUNEMAX", tuneMax.value.toString())
                }
            }
        })

        getTuneData(btViewModel, constants.TUNE_OFFSET, true, null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 26.dp)
            .background(color = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GrayDark)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("IDLE ADJUSTMENT", style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(24.dp))
                DetailDataItem(title = "IDLE TARGET", value = (tuneOffset.value).toString(), suffix = "rpm")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GrayDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .width(40.dp)
                        .height(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (tuneMin.value < adjustmentValue.value) {
                            adjustmentValue.value -= 50
                        } else {
                            adjustmentValue.value = tuneMin.value
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                ) {
                    Text("-", style = MaterialTheme.typography.button, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            Color.White,
                            shape = RoundedCornerShape(4.dp)
                        ) // White background with rounded corners
                        .padding(0.dp) // No internal padding
                ) {
                    BasicTextField(
                        value = (if (adjustmentValue.value > 0) "+" else "") + adjustmentValue.value.toString(),
                        onValueChange = {},
                        modifier = Modifier
                            .padding(8.dp),
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = RobotoCondensed,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                        ),
                        enabled = false,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .width(40.dp)
                        .height(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (tuneMax.value > adjustmentValue.value) {
                            adjustmentValue.value += 50
                        } else {
                            adjustmentValue.value = tuneMax.value
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Green),
                ) {
                    Text("+", style = MaterialTheme.typography.button, fontSize = 24.sp)
                }
            }
        }

        Button(
            modifier = Modifier
                .padding(top = 42.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Green),
            onClick = {
                getTuneData(btViewModel, constants.TUNE_OFFSET, false, adjustmentValue.value)
                isApply.value = true
            },
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp,),
            ) {
                Text(
                    text = "Apply",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.button,
                )
            }
        }
    }
}

fun getDiagData(btViewModel: BluetoothViewModel) {
    val data = ByteArray(3)
    data[0] = 0x00
    data[1] = 0x01
    data[2] = 0x00

    Handler(Looper.getMainLooper()).postDelayed({
        btViewModel.sendCommandByteDES(data)
    },50)
}

fun get4BitsAsHex(byte: Byte, isHigh: Boolean): String {
    return if (isHigh) {
        // Ambil 4 bit atas dan geser ke kanan
        ((byte.toInt() shr 4) and 0xF).toString(16).uppercase()
    } else {
        // Ambil 4 bit bawah
        (byte.toInt() and 0xF).toString(16).uppercase()
    }
}

@Composable
fun page4(prefManager: PrefManager, context: Context, navController: NavController) {
    val btViewModel = hiltViewModel<BluetoothViewModel>()
    val imgEngineOn = remember { mutableStateOf(false) }

    val tvTitleData: MutableState<MutableList<Pair<String, String>>> = remember {
        mutableStateOf(mutableListOf())
    }

    fun getBit(value: Byte, position: Int): Int {
        require(position in 0..7) { "Position must be in range 0 to 7" }
        return (value.toInt() shr position) and 1
    }

    LaunchedEffect(Unit) {
        btViewModel.setOnReadDataDESCalback(onDataReceived = { rliID, fullData ->
            if (fullData[0].toInt() == 0x00 && fullData[1].toInt() == 0x01) {
                val dataLen = fullData[3]

                var firstIndex = 4

                val dataLoop = dataLen.toInt() / 10

                val tempData = mutableListOf<Pair<String,String>>()

                var mqttCodesPayload = ""

                for (x in 1..dataLoop) {
                    var title = ""
                    var value = ""

                    val byte0 = fullData[firstIndex]
                    val byte1 = fullData[firstIndex+1]
                    val byte2 = fullData[firstIndex+2]

                    //first prefix
                    title += if (getBit(byte0,7) == 1) {
                        if (getBit(byte0, 6) == 1) {
                            "U"
                        } else {
                            "B"
                        }
                    } else {
                        if (getBit(byte0, 6) == 1) {
                            "C"
                        } else {
                            "P"
                        }
                    }

                    //second prefix
                    title += if (getBit(byte0,5) == 1) {
                        if (getBit(byte0, 4) == 1) {
                            "3"
                        } else {
                            "2"
                        }
                    } else {
                        if (getBit(byte0, 4) == 1) {
                            "1"
                        } else {
                            "0"
                        }
                    }

                    title += get4BitsAsHex(byte0, false)
                    title += get4BitsAsHex(byte1, true)
                    title += get4BitsAsHex(byte1, false)

                    if (getBit(byte2, 0).toString() == "0" &&
                        getBit(byte2, 1).toString() == "0" &&
                        getBit(byte2, 2).toString() == "0" &&
                        getBit(byte2, 3).toString() == "0") {
                        value += "NO FAULT"
                    } else {
                        for (n in 7 downTo 0) {
                            value += getBit(byte2, n).toString()
                        }
                    }

                    if (mqttCodesPayload != "") {
                        mqttCodesPayload += ","
                    }

                    mqttCodesPayload += """
                            {
                              "code": "$title",
                              "binaryValue": "$value"
                            }
                        """.trimIndent()

                    tempData.add(Pair(title, value))

                    //set lamp
                    if (!imgEngineOn.value) {
                        imgEngineOn.value = getBit(byte2, 7).toString() == "1"
                    }

                    firstIndex += 10
                }

                val jsonPayload = """
                            {
                              "vin": "${prefManager.getMotorcycleVIN()}",
                              "codes": [${mqttCodesPayload}],
                              "checkEngineLight": ${imgEngineOn.value}
                            }
                        """.trimIndent()

                MQTTHelper(context).publishMessage("Beta/${prefManager.getSelectedMotorcycleId()}/enginediagnose", jsonPayload)

                tvTitleData.value = tempData
            }
        })

        getDiagData(btViewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 26.dp)
            .background(color = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GrayDark)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("ENGINE DIAGNOSE", style = MaterialTheme.typography.body1, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(24.dp))
                Image(
                    bitmap = ImageBitmap.imageResource(if (imgEngineOn.value) R.drawable.ic_engine_on else R.drawable.ic_engine_off),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    items(tvTitleData.value) {
                        DetailDataItem(title = it.first, value = it.second, suffix = "")
                    }
                }
//                DetailDataItem(title = tvTitle.value, value = tvData.value, suffix = "")
            }
        }
    }
}

@Composable
fun DetailDataItem(
    title: String,
    value: String,
    suffix: String,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.body2)
        Box(
            modifier = Modifier
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawRoundRect(
                        color = GrayLight,
                        size = Size(size.width, size.height),
                        style = Stroke(strokeWidth),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
                .background(Color.Transparent)
                .padding(vertical = 2.dp, horizontal = 6.dp,)
        ) {
            Row{
                Text(value, style = MaterialTheme.typography.body2)
                if (suffix != "") {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(suffix, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Thin)
            }
        }
    }
}