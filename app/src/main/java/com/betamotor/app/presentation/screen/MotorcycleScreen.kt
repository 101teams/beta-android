package com.betamotor.app.presentation.screen

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.betamotor.app.R
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.component.Input
import com.betamotor.app.presentation.component.PasswordInput
import com.betamotor.app.presentation.component.PermissionNeededDialog
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.DarkBlue
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.RobotoCondensed
import com.betamotor.app.theme.White

@Composable
fun MotorcycleScreen(
    navController: NavController
) {
    val itemsList = (1..50).map { "Item $it" }

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
            modifier = Modifier.fillMaxSize().padding(all = 24.dp),
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
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 columns
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top= 24.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(itemsList) { item ->
                    GridItem(text = item)
                }
            }
        }
    }
}

@Composable
fun GridItem(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = White,
                fontWeight = FontWeight.Medium,
            ),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(){
                Image(
                    painter = rememberAsyncImagePainter("https://picsum.photos/300/300"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(Green)
                        .fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier.padding(all=8.dp,),
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