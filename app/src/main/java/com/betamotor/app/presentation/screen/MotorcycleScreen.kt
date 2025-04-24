package com.betamotor.app.presentation.screen

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.betamotor.app.R
import com.betamotor.app.data.api.motorcycle.MotorcyclesItem
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.component.PermissionNeededDialog
import com.betamotor.app.presentation.viewmodel.AuthViewModel
import com.betamotor.app.presentation.viewmodel.MotorcycleViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.DefaultRed
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.White
import com.betamotor.app.utils.PrefManager

@Composable
fun MotorcycleScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val motorcycleViewModel = hiltViewModel<MotorcycleViewModel>()
    val authViewModel = hiltViewModel<AuthViewModel>()
    var showPermissionDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        motorcycleViewModel.isLoading.value = true

        val result = motorcycleViewModel.getMotorcycles()

        if (result.second == "Unauthorized") {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        } else if (result.second.isNotBlank() && !result.second.contains("coroutine scope")) {
            Toast.makeText(context, result.second, Toast.LENGTH_LONG)
                .show()
        }

        motorcycleViewModel.isLoading.value = false
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
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.motorcycle_model),
                style = TextStyle(
                    fontSize = 20.sp,
                    color = White,
                    fontWeight = FontWeight.Medium,
                ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(id = R.string.choose_motorcycle_model),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = White,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }


            if (motorcycleViewModel.isLoading.value) {
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 columns
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(motorcycleViewModel.motorcycles.value) { item ->
                        GridItem(motorcycle = item, navController = navController, showPermissionDialog, context)
                    }
                }

                Spacer(modifier = Modifier.weight(1.0f))

                Button(onClick = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                                 }, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), colors = ButtonDefaults.buttonColors(backgroundColor = DefaultRed),) {
                    Text(
                        text = stringResource(R.string.logout),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = White,
                            fontWeight = FontWeight.Medium,
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        if (showPermissionDialog.value) {
            PermissionNeededDialog {
                showPermissionDialog.value = false
            }
        }
    }
}

@Composable
fun GridItem(motorcycle: MotorcyclesItem?, navController: NavController, showPermissionDialog: MutableState<Boolean>, context: Context) {
    val prefManager = PrefManager(context)

    val bluetoothManager by lazy { context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    val bluetoothAdapter by lazy { bluetoothManager?.adapter }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {  }

    val neededPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    fun findDevices(motorcycleCode: String) {
        val deniedPermissions = neededPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            permissionLauncher.launch(neededPermissions)
            return
        }

        if (bluetoothAdapter?.isEnabled != null && bluetoothAdapter?.isEnabled!! && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            prefManager.setMotorcycleCode(motorcycleCode)
            navController.navigate(Screen.ScanDevice.route)
            return
        }

        showPermissionDialog.value = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = motorcycle?.name ?: "",
            style = TextStyle(
                fontSize = 14.sp,
                color = White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
            minLines = 2,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column() {
                Image(
                    painter = rememberAsyncImagePainter(motorcycle?.imageUrl ?: ""),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Green)
                        .fillMaxWidth()
                        .clickable {
                            findDevices(motorcycle?.code ?: "")
                        },
                ) {
                    Text(
                        modifier = Modifier.padding(all = 8.dp,),
                        text = stringResource(R.string.choose),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = White,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}
