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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.betamotor.app.R
import com.betamotor.app.data.api.auth.AuthRequest
import com.betamotor.app.navigation.Screen
import com.betamotor.app.presentation.component.Input
import com.betamotor.app.presentation.component.PasswordInput
import com.betamotor.app.presentation.component.PermissionNeededDialog
import com.betamotor.app.presentation.viewmodel.AuthViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.DarkBlue
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.RobotoCondensed
import com.betamotor.app.theme.White
import com.betamotor.app.utils.PrefManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val prefManager = PrefManager(context)

    val email = remember { mutableStateOf(prefManager.getRememberMe()) }
    val password = remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val viewModel = hiltViewModel<AuthViewModel>()
    val focusManager = LocalFocusManager.current

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }

    var isRememberMeChecked by remember { mutableStateOf(prefManager.getRememberMe() != "") }

    fun login() {
        val request = AuthRequest(
            email = email.value ?: "",
            password = password.value ?: ""
        )

        if (request.email.isBlank()) {
            showToast(getString(context, R.string.email_cannot_empty))
            return
        }

        if (request.password.isBlank()) {
            showToast(getString(context, R.string.password_cannot_empty))
            return
        }

        if (request.password.length < 12) {
            showToast(getString(context, R.string.password_must_12_char))
            return
        }

        focusManager.clearFocus()

        if (isRememberMeChecked) {
            prefManager.setRememberMe(email.value)
        } else {
            prefManager.setRememberMe("")
        }

        scope.launch {
            viewModel.loading.value = true
            val error = viewModel.login(request)

            if (error == null) {
                viewModel.resetToken()

                navController.navigate(Screen.MyMotorcycle.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            } else {
                showToast(error, Toast.LENGTH_LONG)
            }
            viewModel.loading.value = false
        }
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
                    contentDescription = stringResource(R.string.betamotor_app_logo),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            isRememberMeChecked = !isRememberMeChecked
                        }
                        .padding(0.dp)
                ) {
                    Checkbox(
                        modifier = Modifier
                            .padding(0.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = Green,
                        ),
                        checked = isRememberMeChecked,
                        onCheckedChange = {
                            isRememberMeChecked = it
                        }
                    )
                    Text(
                        text = stringResource(id = R.string.remember_me),
                        style = MaterialTheme.typography.body1,
                    )
                }

                TextButton(
                    modifier = Modifier,
                    onClick = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.forgot_password),
                        style = MaterialTheme.typography.body1,
                    )
                }
            }

            Button(
                modifier = Modifier.padding(top = 42.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Green),
                onClick = {
                    login()
                },
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp,),
                ) {
                    if (viewModel.loading.value) {
                        CircularProgressIndicator(
                            color = White,
                            strokeCap = StrokeCap.Round,
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .width(18.dp)
                                .height(18.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.sign_in),
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.button,
                            color = White,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.doesnt_have_account),
                    style = TextStyle(
                        color = White,
                        fontSize = 14.sp,
                        fontFamily = RobotoCondensed,
                    ),
                )

                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(
                        stringResource(R.string.register_here),
                        style = TextStyle(
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = RobotoCondensed,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
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
        }
    }
}
