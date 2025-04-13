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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
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
fun LoginScreen(
    navController: NavController
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

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
            modifier = Modifier.fillMaxSize().padding(all = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 64.dp),
            ) {
                Image(
                    bitmap = ImageBitmap.imageResource(R.drawable.img_betamotor_vertical),
                    contentDescription = "Betamotor App Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Input(
                modifier = Modifier,
                field = null,
                placeholder = stringResource(id = R.string.your_email),
                binding = email,
                disabled = false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                inputTextStyle = TextStyle(
                    fontFamily = RobotoCondensed,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = White,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordInput(
                modifier = Modifier,
                placeholder = stringResource(id = R.string.your_password),
                binding = password,
                imeAction = ImeAction.Done,
            )

            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
            ) {
                Text(
                    text = stringResource(id = R.string.forgot_password),
                    style = MaterialTheme.typography.body1,
                )
            }

            Button(
                modifier = Modifier.padding(top = 42.dp)
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
                        text = "Sign In",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.button,
                        color = White,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Doesn't have account?",
                    style = TextStyle(
                        color = White,
                        fontSize = 14.sp,
                        fontFamily = RobotoCondensed,
                    ),
                )

                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(
                        "Register Here",
                        style = TextStyle(
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = RobotoCondensed,
                        ),
                    )
                }
            }
        }
    }
}
