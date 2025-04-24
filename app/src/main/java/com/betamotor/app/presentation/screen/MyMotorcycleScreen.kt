package com.betamotor.app.presentation.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.betamotor.app.R
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.viewmodel.AuthViewModel
import com.betamotor.app.presentation.viewmodel.MotorcycleViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.DefaultRed
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.White

@Composable
fun MyMotorcycleScreen(
    navController: NavController
) {
    val authViewModel = hiltViewModel<AuthViewModel>()
    val viewModel = hiltViewModel<MotorcycleViewModel>()
    val isLoading = viewModel.isLoading.collectAsState()
    val myMotorcycles = viewModel.motorcycles.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getMotorcycles()
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
                Text(
                    text = "My Motorcycles",
                    modifier = Modifier,
                    style = MaterialTheme.typography.h4,
                    fontSize = 20.sp,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Select Your Registered Motorcycle",
                    style = MaterialTheme.typography.subtitle1
                )

                if (isLoading.value) { LoadingIndicator() }
                else Text(
                    "Add New",
                    style = MaterialTheme.typography.button,
                    modifier = Modifier
                        .clickable { navController.navigate(Screen.MotorcycleTypes.route) }
                        .background(color = Green, shape = RoundedCornerShape(4.dp))
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                )
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
                        ) {
                            Column {
                                Text(it.name, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = White),)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(it.motorcycleType?.name ?: "-", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White),)
                            }

                            Text(it.deviceId, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = White),)
                        }
                    }
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
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), colors = ButtonDefaults.buttonColors(backgroundColor = DefaultRed),) {
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
}
