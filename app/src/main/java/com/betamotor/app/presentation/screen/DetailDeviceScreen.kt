package com.betamotor.app.presentation.screen

import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.betamotor.app.R
import com.betamotor.app.data.constants
import com.betamotor.app.presentation.component.BackInvokeHandler
import com.betamotor.app.presentation.component.observeLifecycle
import com.betamotor.app.presentation.viewmodel.BluetoothViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.GrayDark
import com.betamotor.app.theme.GrayLight
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.RobotoCondensed
import com.betamotor.app.theme.White
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DetailDeviceScreen(
    navController: NavController
) {
    val viewModel = hiltViewModel<BluetoothViewModel>()
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

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

    val tabs = listOf("Data", "Info", "Tune", "Diag")
    val icons = listOf(
        painterResource(id = R.drawable.ic_tab1),
        painterResource(id = R.drawable.ic_tab2),
        painterResource(id = R.drawable.ic_tab3),
        painterResource(id = R.drawable.ic_tab4),
    )

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()


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
                        selectedTabIndex = pagerState.currentPage,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                            )
                        },
                        backgroundColor = Gray,
                        contentColor = White,
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Green else Color.Transparent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null // Removes ripple effect
                                    ) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
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
                HorizontalPager(
                    count = tabs.size,
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(color = Color.Transparent)
                ) { page ->
                    when (page) {
                        0 -> page1(navController)
                        1 -> page2()
                        2 -> page3()
                        3 -> page4()
                    }
                }
            }
        }
    }
}

@Composable
fun page1(navController: NavController) {
    val isStreaming = remember { mutableStateOf(false) }
    val isRecording = remember { mutableStateOf(false) }
    val rpm = remember { mutableStateOf("-") }
    val gasPosition = remember { mutableStateOf("-") }
    val actuatedSpark = remember { mutableStateOf("-") }
    val engineCoolant = remember { mutableStateOf("-") }
    val airTemp = remember { mutableStateOf("-") }
    val atmospherePressure = remember { mutableStateOf("-") }
    val operatingHours = remember { mutableStateOf("-") }

    val btViewModel = hiltViewModel<BluetoothViewModel>()
    val scope = rememberCoroutineScope()

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

                            val data = ByteArray(5)
                            data[0] = 0x01
                            data[1] = 0x01
                            data[2] = 0x02
                            data[3] = 0x00

                            btViewModel.setOnReadDataDESCalback(onDataReceived = {rliID, fullData ->
                                scope.launch {
                                    val resData = fullData[6]
                                    when(rliID) {
                                        constants.RLI_ENGINE_SPEED -> {
                                            resData.toUInt()
                                            rpm.value = resData.toString()
                                        }
                                        constants.RLI_GAS_POSITION -> {
                                            resData.toUInt()
                                            gasPosition.value = resData.toString()
                                        }
                                        constants.RLI_ACTUATED_SPARK -> {
                                            resData.toInt()
                                            actuatedSpark.value = resData.toString()
                                        }
                                        constants.RLI_COOLANT_TEMP -> {
                                            resData.toInt()
                                            engineCoolant.value = resData.toString()
                                        }
                                        constants.RLI_AIR_TEMP -> {
                                            resData.toInt()
                                            airTemp.value = resData.toString()
                                        }
                                        constants.RLI_ATMOSPHERE_PRESSURE -> {
                                            resData.toUInt()
                                            atmospherePressure.value = resData.toString()
                                        }
                                        constants.RLI_OPERATING_HOURS -> {
                                            resData.toUInt()
                                            operatingHours.value = resData.toString()
                                        }
                                    }
                                }
                            })

                            val delayMult = 300L

                            Handler(Looper.getMainLooper()).postDelayed({
                                data[4] = constants.RLI_ENGINE_SPEED
                                btViewModel.sendCommandByteDES(data)
                            },0 * delayMult)

                            Handler(Looper.getMainLooper()).postDelayed({
                                data[4] = constants.RLI_GAS_POSITION
                                btViewModel.sendCommandByteDES(data)
                            },1 * delayMult)

                            Handler(Looper.getMainLooper()).postDelayed({
                                data[4] = constants.RLI_ACTUATED_SPARK
                                btViewModel.sendCommandByteDES(data)
                            },2 * delayMult)

                            Handler(Looper.getMainLooper()).postDelayed({
                                data[4] = constants.RLI_COOLANT_TEMP
                                btViewModel.sendCommandByteDES(data)
                            },3 * delayMult)

                            Handler(Looper.getMainLooper()).postDelayed({
                                data[4] = constants.RLI_AIR_TEMP
                                btViewModel.sendCommandByteDES(data)
                            },4 * delayMult)

                            Handler(Looper.getMainLooper()).postDelayed({
                                data[4] = constants.RLI_ATMOSPHERE_PRESSURE
                                btViewModel.sendCommandByteDES(data)
                            },5 * delayMult)

                            Handler(Looper.getMainLooper()).postDelayed({
                                data[4] = constants.RLI_OPERATING_HOURS
                                btViewModel.sendCommandByteDES(data)
                            },6 * delayMult)
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
                            isRecording.value = !isRecording.value
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

@Composable
fun page2() {
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
                DetailDataItem(title = "VIN", value = "-", suffix = "")
                DetailDataItem(title = "ECU DRW", value = "-", suffix = "")
                DetailDataItem(title = "ECU HW", value = "-", suffix = "")
                DetailDataItem(title = "ECU SW.", value = "-", suffix = "")
                DetailDataItem(title = "CALIBRATION", value = "-", suffix = "")
                DetailDataItem(title = "HOMOL. CODE", value = "-", suffix = "")
            }
        }
    }
}

@Composable
fun page3() {

    val adjustmentValue = remember { mutableStateOf(0) }

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
                DetailDataItem(title = "IDLE TARGET", value = "2350", suffix = "rpm")
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
                    onClick = { adjustmentValue.value -= 1 },
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
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .width(40.dp)
                        .height(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = { adjustmentValue.value += 1 },
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

@Composable
fun page4() {
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
                    bitmap = ImageBitmap.imageResource(R.drawable.img_engine),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                DetailDataItem(title = "P0325", value = "111000001010", suffix = "")
                DetailDataItem(title = "P0120", value = "101011100011", suffix = "")
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