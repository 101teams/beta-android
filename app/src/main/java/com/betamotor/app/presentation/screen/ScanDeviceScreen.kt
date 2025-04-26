package com.betamotor.app.presentation.screen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.betamotor.app.R
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.bluetooth.BluetoothDevice
import com.betamotor.app.findActivity
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.component.CheckPermission
import com.betamotor.app.presentation.component.CustomTopBar
import com.betamotor.app.presentation.component.NetworkItem
import com.betamotor.app.presentation.component.PermissionNeededDialog
import com.betamotor.app.presentation.component.SaveMotorcycleDialog
import com.betamotor.app.presentation.component.observeLifecycle
import com.betamotor.app.presentation.viewmodel.BluetoothViewModel
import com.betamotor.app.presentation.viewmodel.MotorcycleViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.White
import com.betamotor.app.utils.PrefManager
import com.betamotor.app.utils.SystemBroadcastReceiver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanDeviceScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<BluetoothViewModel>()
    val motorcycleViewModel = hiltViewModel<MotorcycleViewModel>()
    val state by viewModel.state.collectAsState()
    val motorcycleError = motorcycleViewModel.error.collectAsState()
    val prefManager = PrefManager(context)
    val scope = rememberCoroutineScope()

    val isLoading = remember { mutableStateOf(false) }
    val isConnecting = remember { mutableStateOf(false) }
    val selectedDevice: MutableState<BluetoothDevice?> = remember {
        mutableStateOf(null)
    }

    val showSaveMotorcycleDialog = remember {
        mutableStateOf(false)
    }

    val bluetoothManager by lazy { context.getSystemService(BluetoothManager::class.java) }
    val bluetoothAdapter by lazy { bluetoothManager?.adapter }
    var isBluetoothEnabled by remember { mutableStateOf(bluetoothAdapter?.isEnabled ?: false) }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var isLocationEnabled by remember { mutableStateOf(locationManager.isProviderEnabled(
        LocationManager.GPS_PROVIDER)) }

    var checkedPermission by remember { mutableStateOf(false) }
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) listOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
    ) else listOf(
        Manifest.permission.BLUETOOTH,
    )
    val bluetoothPermissionState = rememberMultiplePermissionsState(permissions = permissions)
    var showPermissionDialog by remember { mutableStateOf(false) }

    fun isPermissionGranted(): Boolean {
        val denied = bluetoothPermissionState.permissions.filter {
            !it.status.isGranted
        }

        return denied.isEmpty()
    }

    LaunchedEffect(Unit) {
        motorcycleViewModel.getMotorcycles()

        if (isPermissionGranted()) {
            viewModel.startScan()
        }
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
        if (isPermissionGranted()) {
            when (lifecycleEvent) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.startScan()
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.stopScan()
                }
                else -> {
                }
            }
        }
    }

    SystemBroadcastReceiver(action = BluetoothAdapter.ACTION_STATE_CHANGED) {
        val action = it?.action ?: return@SystemBroadcastReceiver
        if (action != BluetoothAdapter.ACTION_STATE_CHANGED) return@SystemBroadcastReceiver
        Log.d("BLE", "action state changed: ${it.data}")

        when (it.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
            BluetoothAdapter.STATE_OFF -> {
                isBluetoothEnabled = false
            }
        }
    }

    SystemBroadcastReceiver(action = LocationManager.MODE_CHANGED_ACTION) {
        val action = it?.action ?: return@SystemBroadcastReceiver
        if (action != LocationManager.MODE_CHANGED_ACTION) return@SystemBroadcastReceiver

        isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    SystemBroadcastReceiver(action = BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
        val action = it?.action ?: return@SystemBroadcastReceiver
        if (action != BluetoothAdapter.ACTION_DISCOVERY_FINISHED) return@SystemBroadcastReceiver
        Log.d("BLE", "action discovery finished")
        viewModel.stopScan()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkedPermission) {
        CheckPermission(onPermissionGranted = {
            viewModel.startScan()
            checkedPermission = true
        }, onPermissionDenied = {
            checkedPermission = true
            showPermissionDialog = true
        })
    }

    LaunchedEffect(isLocationEnabled, isBluetoothEnabled) {
        if (!isLocationEnabled || !isBluetoothEnabled) {
            viewModel.stopScan()
            showPermissionDialog = true
        }
    }

    LaunchedEffect(state.isConnectionAuthorized) {
        // currently connecting to device
        if (!state.isConnectionAuthorized) {
            return@LaunchedEffect
        }
        showSaveMotorcycleDialog.value = true
    }

    LaunchedEffect(motorcycleError.value) {
        if (motorcycleError.value != null) {
            Toast.makeText(context, motorcycleError.value, Toast.LENGTH_LONG).show()
            motorcycleViewModel.clearError()
        }
    }

    fun connect(device: BluetoothDevice) {
       viewModel.connectDevice(device, "", callback = { isSuccess, errMessage ->
           isLoading.value = false
           isConnecting.value = false

           if (isSuccess) {
               return@connectDevice
           }

           context.findActivity()?.runOnUiThread {
               Toast.makeText(context, errMessage, Toast.LENGTH_LONG).show()
           }
       }, onDataReceived = {

       })
    }

    println("devices: ${state.scannedDevices.map { it.identity }}")

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
            BluetoothDeviceList(
                navController = navController,
                pairedDevices = state.pairedDevices,
                scannedDevices = state.scannedDevices,
                isScanning = state.isScanning,
                isConnecting = isConnecting.value,
                selectedDevice = selectedDevice.value,
                onClick = {
                    selectedDevice.value = it
                    isConnecting.value = true
                    connect(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            if (showPermissionDialog) {
                PermissionNeededDialog {
                    showPermissionDialog = false
                    navController.popBackStack()
                }
            }
        }

        if (showSaveMotorcycleDialog.value) {
            SaveMotorcycleDialog(openDialog = showSaveMotorcycleDialog) { form ->
                val motorcycleTypeId = prefManager.getMotorcycleTypeId()

                if (motorcycleTypeId == null) {
                    Toast.makeText(context, "Please select motorcycle type", Toast.LENGTH_SHORT).show()

                    navController.navigate(Screen.MotorcycleTypes.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }

                    return@SaveMotorcycleDialog
                }

                scope.launch {
                    // check if device already saved
                    val savedMotorcycles = motorcycleViewModel.motorcycles.value
                    val deviceIdAlreadyUsed = savedMotorcycles.any { it.deviceId == form.deviceId }
                    val macAddressAlreadyUsed = savedMotorcycles.any { it.macAddress == selectedDevice.value?.macAddress }

                    if (deviceIdAlreadyUsed || macAddressAlreadyUsed) {
                        Toast.makeText(context, "Device already saved", Toast.LENGTH_SHORT).show()
                        prefManager.setSelectedMotorcycleId(form.deviceId)
                        navController.navigate(Screen.DetailDevice.route)
                        return@launch
                    }

                    val success = motorcycleViewModel.saveMotorcycle(
                        CreateMotorcycleRequest(
                            name = form.name,
                            deviceId = form.deviceId,
                            password = form.password,
                            macAddress = selectedDevice.value?.macAddress ?: "",
                            motorcycleTypeId = motorcycleTypeId,
                            bleGattCharRx = "-",
                            bleGattCharWx = "-"
                        )
                    )

                    if (success) {
                        prefManager.setSelectedMotorcycleId(form.deviceId)
                        navController.navigate(Screen.DetailDevice.route)
                    }

                    showSaveMotorcycleDialog.value = false
                }
            }
        }
    }
}

@Composable
fun BluetoothDeviceList(
    navController: NavController,
    pairedDevices: List<BluetoothDevice>,
    scannedDevices: List<BluetoothDevice>,
    isScanning: Boolean,
    isConnecting: Boolean,
    selectedDevice: BluetoothDevice?,
    onClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = hiltViewModel<BluetoothViewModel>()

    fun startScan() {
        viewModel.resetDevices()
        viewModel.startScan()
    }

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .background(color = Gray)
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = {
                    navController.navigateUp()
                },
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(24.dp),
                    bitmap = ImageBitmap.imageResource(R.drawable.ic_arrow_back_white),
                    contentDescription = "back button"
                )
            }

            Text(
                text = "Find Nearby Bikes",
                modifier = Modifier,
                style = MaterialTheme.typography.h4,
                fontSize = 20.sp,
            )

            Spacer(modifier = Modifier.width(24.dp))
        }

        LazyColumn(
            modifier = modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp,)
        ) {

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(id = R.string.nearby_bike),
                        style = MaterialTheme.typography.subtitle1
                    )

                    if (isScanning) { LoadingIndicator() }
                    else Text(
                        "Refresh",
                        style = MaterialTheme.typography.button,
                        modifier = Modifier
                            .clickable { startScan() }
                            .background(color = Green, shape = RoundedCornerShape(4.dp))
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
            }

            items(scannedDevices) {
                NetworkItem(
                    it,
                    isLoading = isConnecting && (it.macAddress == selectedDevice?.macAddress),
                    onClick
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator(size: Dp = 20.dp) {
    CircularProgressIndicator(
        color = White,
        backgroundColor = Black.copy(alpha = 0.3f),
        strokeCap = StrokeCap.Round,
        strokeWidth = 2.dp,
        modifier = Modifier
            .width(size)
            .height(size)
    )
}
