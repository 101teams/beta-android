package com.betamotor.app.presentation.screen

import android.Manifest
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.betamotor.app.R
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.bluetooth.BluetoothDevice
import com.betamotor.app.data.constants
import com.betamotor.app.findActivity
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.component.CheckPermission
import com.betamotor.app.presentation.component.Input
import com.betamotor.app.presentation.component.LocationUpdater
import com.betamotor.app.presentation.component.SaveMotorcycleDialog
import com.betamotor.app.presentation.component.SaveTrackingMotorcycleDialog
import com.betamotor.app.presentation.component.observeLifecycle
import com.betamotor.app.presentation.viewmodel.AuthViewModel
import com.betamotor.app.presentation.viewmodel.BluetoothViewModel
import com.betamotor.app.presentation.viewmodel.DetailDeviceViewModel
import com.betamotor.app.presentation.viewmodel.GoogleViewModel
import com.betamotor.app.presentation.viewmodel.MotorcycleViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.DefaultBlue
import com.betamotor.app.theme.DefaultRed
import com.betamotor.app.theme.DefaultTextBlack
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.GrayDark
import com.betamotor.app.theme.GrayLight
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.White
import com.betamotor.app.utils.LocalLogging
import com.betamotor.app.utils.MQTTHelper
import com.betamotor.app.utils.PrefManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.json.JSONObject
import com.betamotor.app.theme.RobotoCondensed
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TrackingScreen(
    navController: NavController
) {
    val googleViewModel = hiltViewModel<GoogleViewModel>()
    val btViewModel = hiltViewModel<BluetoothViewModel>()
    val detailDeviceViewModel = hiltViewModel<DetailDeviceViewModel>()
    val motorViewModel = hiltViewModel<MotorcycleViewModel>()

    val lifecycleOwner = LocalLifecycleOwner.current
    var lifecycleEvent by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    btViewModel.observeLifecycle(lifecycle = lifecycleOwner.lifecycle)

    val context = LocalContext.current
    val prefManager = PrefManager(context)

    val isRecording by detailDeviceViewModel.isRecording.collectAsState()

    val cameraPositionState = rememberCameraPositionState()
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val scope = rememberCoroutineScope()

    var altitude by remember { mutableStateOf("-") }
    var speed by remember { mutableFloatStateOf(0.0f) }

    val showSaveTrackingMotorcycleDialog = remember {
        mutableStateOf(false)
    }

    val csvData: MutableState<MutableList<String>> = remember { mutableStateOf(mutableListOf()) }

    LaunchedEffect(currentLocation) {
        if (currentLocation != null) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(currentLocation!!, 16f),
                durationMs = 1000
            )
        }
    }

    fun goBackToHome() {
        navController.popBackStack()
    }

    LaunchedEffect(lifecycleEvent) {
        when (lifecycleEvent) {
            Lifecycle.Event.ON_DESTROY -> {//before was on pause
                goBackToHome()
            }
            else -> {}
        }
    }

    fun stripToZero(raw: String): String {
        return if (raw.isBlank() || raw == "-") {
            "0"
        } else {
            raw
        }
    }

    val dataIds = listOf(
        constants.RLI_ENGINE_SPEED,
    )

    var finishedFetch = true

    fun fetchAndContinue(index: Int = 0, result: MutableMap<Int, String> = mutableMapOf(), latLng: LatLng, speed: Float) {
        if (!finishedFetch) {
            return
        }

        finishedFetch = false

        if (index == dataIds.size - 1) {
            val payload = mapOf(
                "speed" to speed.toString(),
                "altitude" to stripToZero(altitude),
                "rpm" to stripToZero(result[constants.RLI_ENGINE_SPEED] ?: "0"),
                "latitude" to latLng.latitude.toString(),
                "longitude" to latLng.longitude.toString(),
            ).mapValues { it.value.ifBlank { "null" } }

            Log.d("MQTT Payload", payload.toString())
            Log.d("Send to ","Beta/${prefManager.getSelectedMotorcycleId()}/position")

            MQTTHelper(context).publishMessage(
                "Beta/${prefManager.getSelectedMotorcycleId()}/position",
                JSONObject(payload).toString(2)
            )
            finishedFetch = true


            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            csvData.value.add("$time,$speed,$altitude,${stripToZero(result[constants.RLI_ENGINE_SPEED] ?: "0")},${latLng.latitude},${latLng.longitude}")

            return
        }

        val id = dataIds[index]
        val data = byteArrayOf(0x01, 0x01, 0x02, (id shr 8).toByte(), (id and 0xFF).toByte())
        val key = "fetch_$id"

        val handler = Handler(Looper.getMainLooper())
        var callbackCalled = false

        val callback: (Byte, ByteArray) -> Unit = { rliID, fullData ->
            if (rliID.toInt() == id) {
                callbackCalled = true
                btViewModel.removeOnDataReceivedCallback(key)

                val value = parseSensorValue(id, fullData)
                if (value != null) {
                    result[id] = value
                    updateViewModel(detailDeviceViewModel, id, value)
                }
            }
        }

        btViewModel.addOnDataReceivedCallback(key, callback)
        btViewModel.sendCommandByteDES(data)

        handler.postDelayed({
            if (!callbackCalled) {
                btViewModel.removeOnDataReceivedCallback(key)
            }
        }, 200L)
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
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(onClick = {
                    goBackToHome()
                }, modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Green),) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Image(
                            bitmap = ImageBitmap.imageResource(R.drawable.ic_arrow_back_white),
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier
                                .height(18.dp)
                                .width(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.back),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = White,
                                fontWeight = FontWeight.Medium,
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Image(
                    bitmap = ImageBitmap.imageResource(R.drawable.img_betamotor),
                    contentDescription = stringResource(R.string.betamotor_app_logo),
                    modifier = Modifier
                        .height(100.dp)
                        .weight(1f)
                )

                Spacer(modifier = Modifier.height(1.dp).weight(1f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 24.dp)
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GrayDark)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                    ) {
                        if (currentLocation != null) {
                            key(altitude) {
                                val markerLocation = remember { mutableStateOf(LatLng(currentLocation!!.latitude, currentLocation!!.longitude)) }
                                val mapInfoShown = remember { mutableStateOf(false) }

                                MarkerComposable(
                                    state = MarkerState(position = markerLocation.value),
                                    visible = mapInfoShown.value,
                                    onClick = {
                                        mapInfoShown.value = false
                                        true
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .padding(bottom = 58.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(White)
                                            .padding(12.dp)
                                            .zIndex(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(
                                                stringResource(id = R.string.motorcycle_name),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 15.sp,
                                                color = DefaultTextBlack,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                modifier = Modifier.fillMaxWidth()
                                                    .padding(bottom = 6.dp)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    "${stringResource(id = R.string.speed)}:",
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = GrayLight,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "-",
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = DefaultBlue,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    "${stringResource(id = R.string.rpm)}:",
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = GrayLight,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "-",
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = DefaultBlue,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    "${stringResource(id = R.string.altitude)}:",
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = GrayLight,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    altitude,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 14.sp,
                                                    color = DefaultBlue,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }
                                }

                                MarkerComposable(
                                    state = MarkerState(position = markerLocation.value),
                                    onClick = {
                                        mapInfoShown.value = !mapInfoShown.value
                                        true
                                    }
                                ) {
                                    IconButton(
                                        modifier = Modifier
                                            .width(58.dp),
                                        onClick = {
                                            mapInfoShown.value = !mapInfoShown.value
                                        }
                                    ) {
                                        Box(){
                                            Image(
                                                contentScale = ContentScale.Inside,
                                                painter = rememberDrawablePainter(
                                                    drawable = getDrawable(
                                                        LocalContext.current,
                                                        R.drawable.motorcycle_pin
                                                    )
                                                ),
                                                contentDescription = "",
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LocationUpdater(
                        onLocationUpdate = { location ->
                            val newLatLng = LatLng(location.latitude, location.longitude)
                            currentLocation = newLatLng

                            scope.launch {
                                speed = location.speed
                                val altitudeData = googleViewModel.getAltitude(location.latitude, location.longitude)
                                altitude = altitudeData.first?.results?.get(0)?.elevation.toString()

                                if (currentLocation != null && isRecording) {
                                    fetchAndContinue(latLng = currentLocation!!, speed = speed)
                                }
                            }
                        },
                        intervalMillis = 5000L
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Box(
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                detailDeviceViewModel.setIsRecording(!isRecording)

                                scope.launch {
                                    if(isRecording) {
                                        motorViewModel.startTracking(prefManager.getSelectedMotorcycleId())
                                    } else {
                                        if (motorViewModel.trackingTransactionID.value?.data?.transactionKey != null) {
                                            if (motorViewModel.trackingTransactionID.value != null) {
                                                motorViewModel.stopTracking(prefManager.getSelectedMotorcycleId(), motorViewModel.trackingTransactionID.value?.data?.transactionKey!!)
                                            }

                                            motorViewModel.getTrackingHistory(motorViewModel.trackingTransactionID.value?.data?.transactionKey!!)
                                        }

                                        showSaveTrackingMotorcycleDialog.value = true
                                    }
                                }
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    bitmap = ImageBitmap.imageResource( if (isRecording) R.drawable.ic_stop_record else R.drawable.ic_start_record),
                                    contentDescription = "",
                                    modifier = Modifier
                                        .height(52.dp)
                                        .width(52.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(if (isRecording) stringResource(R.string.recording) else stringResource(R.string.start_record), style = MaterialTheme.typography.button)
                            }
                        }
                    }
                }
            }
        }

        if (showSaveTrackingMotorcycleDialog.value) {
            SaveTrackingMotorcycleDialog (openDialog = showSaveTrackingMotorcycleDialog, csvData) { filename ->
                scope.launch {
                    showSaveTrackingMotorcycleDialog.value = false
                    csvData.value = mutableListOf()
                    Toast.makeText(context, filename, Toast.LENGTH_SHORT).show()
                    showSaveTrackingMotorcycleDialog.value = false
                }
            }
        }
    }
}
