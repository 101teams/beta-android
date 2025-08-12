package com.betamotor.app.presentation.screen

import android.Manifest
import android.os.Build
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
import androidx.compose.material.Divider
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
import com.betamotor.app.data.api.motorcycle.BookmarkMotorcycleCreateRequest
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.bluetooth.BluetoothDevice
import com.betamotor.app.findActivity
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.component.BookmarkMotorcycleDialog
import com.betamotor.app.presentation.component.CheckPermission
import com.betamotor.app.presentation.component.DialogDelete
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
fun TrackingListScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val logger = LocalLogging(context)
    val viewModel = hiltViewModel<MotorcycleViewModel>()
    val isSecondLoading = viewModel.isSecondLoading.collectAsState()
    val isLoading = viewModel.isThirdLoading.collectAsState()
    val motorcycleAccessories = viewModel.motorcycleAccessories.collectAsState()
    val bookmarkMotorcycles = viewModel.bookmarkMotorcycles.collectAsState()
    val prefManager = PrefManager(context)
    val vinInput = remember { mutableStateOf("") }
    val bookmarked = remember { mutableStateOf(false) }
    val deleteBookmarkID = remember { mutableStateOf(0) }

    val showBookmarkMotorcycleDialog = remember {
        mutableStateOf(false)
    }

    val showDeleteBookmarkDialog = remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getBookmarkMotorcycle()
        viewModel.getMotorcycleTypes()
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
                horizontalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = stringResource(R.string.tracking),
                    modifier = Modifier,
                    style = MaterialTheme.typography.h4,
                    fontSize = 20.sp,
                )
            }

            Button(onClick = {
                navController.popBackStack()
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

                Text(
                    stringResource(R.string.search),
                    style = MaterialTheme.typography.button.copy(textAlign = TextAlign.Center),
                    modifier = Modifier
                        .clickable {
                            scope.launch {
                                viewModel.getMotorcycleAccessories(vinInput.value)
                            }
                        }
                        .background(color = Green, shape = RoundedCornerShape(8.dp))
                        .padding(vertical = 18.dp, horizontal = 22.dp)
                )
            }


            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (isLoading.value) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        LoadingIndicator()
                        Spacer(modifier = Modifier.height(26.dp))
                    }
                } else {
                    if (motorcycleAccessories.value != null) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF353535))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .fillMaxWidth()
                                .clickable {
                                    prefManager.setSelectedMotorcycleId(vinInput.value)
                                    prefManager.setSelectedMotorcycleName(motorcycleAccessories.value?.modelDescription ?: "-")
                                    navController.navigate(Screen.DetailDevice.route)
                                }
                        ) {
                            Column {
                                Text(motorcycleAccessories.value!!.modelDescription ?: "-", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = White),)
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(motorcycleAccessories.value!!.serialID ?: "-", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White),)
                                    IconButton(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(24.dp),
                                        onClick = {
                                            showBookmarkMotorcycleDialog.value = true
                                        }) {
                                        Icon(
                                            painter = painterResource(id = if (bookmarked.value) {R.drawable.ic_bookmark_on_white} else {R.drawable.ic_bookmark_off_white}),
                                            contentDescription = stringResource(R.string.setting),
                                            tint = White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(stringResource(R.string.bookmark), style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = White),)
                Divider(
                    color = Color.White,
                    thickness = 1.dp
                )

                if (isSecondLoading.value) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        LoadingIndicator()
                    }
                } else {
                    if (bookmarkMotorcycles.value.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(bookmarkMotorcycles.value) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF353535))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            prefManager.setSelectedMotorcycleId(vinInput.value)
                                            prefManager.setSelectedMotorcycleName(motorcycleAccessories.value?.modelDescription ?: "-")
                                            navController.navigate(Screen.DetailDevice.route)
                                        }
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ){
                                            Text(it?.motorcycleName ?: "-", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = White),)
                                            IconButton(
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .height(24.dp),
                                                onClick = {
                                                    deleteBookmarkID.value = it?.id ?: 0
                                                    showDeleteBookmarkDialog.value = true
                                                }) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_trashcan_white),
                                                    contentDescription = stringResource(R.string.setting),
                                                    tint = White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(it?.vin ?: "-", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White),)
                                        Text(it?.motorcycleType?.name ?: "-", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White),)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBookmarkMotorcycleDialog.value) {
            BookmarkMotorcycleDialog(openDialog = showBookmarkMotorcycleDialog, vin = vinInput.value, onBookmark = {
                showBookmarkMotorcycleDialog.value = false

                scope.launch {
                    val success = viewModel.saveBookmarksMotorcycle(
                        BookmarkMotorcycleCreateRequest(
                            vin = it.vin,
                            motorcycleName = it.name,
                            motorcycleTypeId = it.typeID,
                        )
                    )

                    if (success) {
                        viewModel.getBookmarkMotorcycle()
                    }
                }
            })
        }

        if (showDeleteBookmarkDialog.value) {
            DialogDelete(
                onDismiss = {
                    showDeleteBookmarkDialog.value = false
                },
                onClickCancel = {
                    showDeleteBookmarkDialog.value = false
                },
                onClickContinue = {
                    showDeleteBookmarkDialog.value = false
                    scope.launch {
                        val success = viewModel.deleteMotorcycle(deleteBookmarkID.value)

                        if (success) {
                            viewModel.getBookmarkMotorcycle()
                        }
                    }
                }
            )
        }
    }
}
