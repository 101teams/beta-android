package com.betamotor.app.presentation.screen

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.betamotor.app.R
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.bluetooth.BluetoothDevice
import com.betamotor.app.findActivity
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.component.CheckPermission
import com.betamotor.app.presentation.component.Input
import com.betamotor.app.presentation.viewmodel.AuthViewModel
import com.betamotor.app.presentation.viewmodel.BluetoothViewModel
import com.betamotor.app.presentation.viewmodel.MotorcycleViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.DefaultRed
import com.betamotor.app.theme.Gray
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
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyMotorcycleScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val logger = LocalLogging(context)
    val authViewModel = hiltViewModel<AuthViewModel>()
    val viewModel = hiltViewModel<MotorcycleViewModel>()
    val bluetoothViewModel = hiltViewModel<BluetoothViewModel>()
    val isLoading = viewModel.isLoading.collectAsState()
    val myMotorcycles = viewModel.motorcycles.collectAsState()
    val prefManager = PrefManager(context)
    val isConnecting = remember { mutableStateOf(false) }
    val selectedDevice = remember { mutableStateOf<MotorcycleItem?>(null) }
    val btState by bluetoothViewModel.state.collectAsState()
    val vinInput = remember { mutableStateOf("") }

    var checkedPermission by remember { mutableStateOf(false) }
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) listOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
    ) else listOf(
        Manifest.permission.BLUETOOTH,
    )
    val bluetoothPermissionState = rememberMultiplePermissionsState(permissions = permissions)
    var showPermissionDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun isPermissionGranted(): Boolean {
        val denied = bluetoothPermissionState.permissions.filter {
            !it.status.isGranted
        }

        return denied.isEmpty()
    }

    LaunchedEffect(key1 = Unit) {
        prefManager.clearSelectedMotorcycleId()
        viewModel.getMotorcycles()

        if (viewModel.error.value == "401") {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkedPermission) {
        CheckPermission(onPermissionGranted = {
            checkedPermission = true
        }, onPermissionDenied = {
            checkedPermission = true
            showPermissionDialog = true
        })
    }

    LaunchedEffect(btState.isConnectionAuthorized) {
        // currently connecting to device
        if (!btState.isConnectionAuthorized) {
            return@LaunchedEffect
        }

        if (selectedDevice.value?.deviceId == null) {
            return@LaunchedEffect
        }

        if (selectedDevice.value?.macAddress == null)
        {
            return@LaunchedEffect
        }

        prefManager.setSelectedMotorcycleId(selectedDevice.value!!.deviceId)
        prefManager.setMacAddress(selectedDevice.value!!.macAddress)
        prefManager.setSelectedMotorcycleName(selectedDevice.value!!.name)

        MQTTHelper(context).publishMessage("BetaDebug", JSONObject(
            mapOf(
                "deviceId" to selectedDevice.value!!.deviceId,
                "macAddress" to selectedDevice.value!!.macAddress,
                "action" to "moving to detailScreen from myMotorcycle's isConnectionAuthorized = true",
            )
        ).toString())
        navController.navigate(Screen.DetailDevice.route)
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
        ) {
            Row(
                modifier = Modifier
                    .background(color = Gray)
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = stringResource(R.string.my_garage),
                    modifier = Modifier,
                    style = MaterialTheme.typography.h4,
                    fontSize = 20.sp,
                )

                IconButton(onClick = {
                    navController.navigate(Screen.Setting.route)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.cog),
                        contentDescription = stringResource(R.string.setting),
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.select_your_registered_motorcycle),
                    style = MaterialTheme.typography.subtitle1
                )

                Spacer(modifier = Modifier.width(12.dp))

                if (isLoading.value) { LoadingIndicator() }
                else Text(
                    stringResource(R.string.add_new),
                    style = MaterialTheme.typography.button.copy(textAlign = TextAlign.Center),
                    modifier = Modifier
                        .clickable {
                            logger.writeLog("Add New Motorcycle Clicked")
                            navController.navigate(Screen.MotorcycleTypes.route)
                        }
                        .background(color = Green, shape = RoundedCornerShape(4.dp))
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                )
            }

            Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Input(
                    modifier = Modifier,
                    field = null,
                    placeholder = stringResource(R.string.vin),
                    binding = vinInput,
                    disabled = false,
                    fillMaxWidth = false,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    inputTextStyle = TextStyle(
                        fontFamily = RobotoCondensed,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = White,
                    ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = stringResource(R.string.search),
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (isLoading.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp)
                ) {
                    CircularProgressIndicator(
                        color = White,
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    items(myMotorcycles.value) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF353535))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .fillMaxWidth()
                                .clickable {
                                    if (!isPermissionGranted()) {
                                        bluetoothPermissionState.launchMultiplePermissionRequest()
                                        return@clickable
                                    }

                                    isConnecting.value = true
                                    selectedDevice.value = it
                                    bluetoothViewModel.connectDevice(
                                        BluetoothDevice(
                                            "",
                                            it.macAddress,
                                            it.name,
                                        ),
                                        "",
                                        callback = { success, message ->
                                            if (success) {
                                                return@connectDevice
                                            } else {
//                                              DEBUG ONLY
//                                                scope.launch {
//                                                    prefManager.setSelectedMotorcycleId(selectedDevice.value!!.deviceId)
//                                                    prefManager.setMacAddress(selectedDevice.value!!.macAddress)
//                                                    navController.navigate(Screen.DetailDevice.route)
//                                                }
//                                                return@connectDevice
//                                              END OF DEBUG ONLY

                                                context
                                                    .findActivity()
                                                    ?.runOnUiThread {
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                message,
                                                                Toast.LENGTH_LONG
                                                            )
                                                            .show()
                                                    }
                                            }

                                            isConnecting.value = false
                                            selectedDevice.value = null
                                        },
                                        onDataReceived = {}
                                    )
                                }
                        ) {
                            Column {
                                Text(it.name, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = White),)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(it.motorcycleType?.name ?: "-", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White),)
                            }

                            if (isConnecting.value && selectedDevice.value?.macAddress == it.macAddress) {
                                LoadingIndicator()
                            } else {
                                Text(it.deviceId, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White),)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.0f))

            Button(onClick = {
                navController.navigate(Screen.TrackingList.route)
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 0.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Green),) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        bitmap = ImageBitmap.imageResource(R.drawable.ic_map_white),
                        contentDescription = stringResource(R.string.tracking),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.tracking),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = White,
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = Modifier
                    )
                }
            }

            Button(onClick = {
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp), colors = ButtonDefaults.buttonColors(backgroundColor = DefaultRed),) {
                Text(
                    text = stringResource(R.string.logout),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = White,
                        fontWeight = FontWeight.Medium,
                    ),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
