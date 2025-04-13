package com.betamotor.app.presentation.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.betamotor.app.R
import com.betamotor.app.presentation.component.CustomTopBar
import com.betamotor.app.presentation.component.Input
//import com.betamotor.app.presentation.component.TopToastDialog
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.Gray
//import com.betamotor.app.presentation.viewmodel.AuthViewModel
import com.betamotor.app.theme.GrayLight
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.RobotoCondensed
import com.betamotor.app.theme.White
//import com.betamotor.app.utils.PrefManager
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    navController: NavController
) {
    val context = LocalContext.current
//    val viewModel = hiltViewModel<AuthViewModel>()
    val scope = rememberCoroutineScope()

    val email = remember { mutableStateOf("") }
    val showToast = remember { mutableStateOf(false) }
    val postDataMessage = remember { mutableStateOf("") }

    Box {
        Column(
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
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTopBar(title = stringResource(id = R.string.password_recovery), navController)

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.forgot_password_enter_email),
                    style = MaterialTheme.typography.body1,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Input(
                    modifier = Modifier,
                    field = null,
                    placeholder = stringResource(id = R.string.email),
                    binding = email,
                    disabled = false,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                )

                if (email.value.isNotEmpty() &&
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.email_must_valid),
                        style = TextStyle(
                            fontFamily = RobotoCondensed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = White,
                        ),
                    )
                }

                Button(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .padding(top = 48.dp)
                        .height(50.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (
                            email.value.isNotEmpty() &&
                            android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
                        ) {
                            scope.launch {
//                                viewModel.loading.value = true
//                                val response = viewModel.forgotPassword(
//                                    ForgotPasswordRequest(
//                                        email = email.value
//                                    ),
//                                    PrefManager(context).getCurrentLanguage(),
//                                )
//                                if (response == null) {
//                                    postDataMessage.value = context.getString(R.string.success_reset_password)
//                                } else {
//                                    postDataMessage.value = context.getString(R.string.failed_reset_password)
//                                }
//                                showToast.value = true
//                                viewModel.loading.value = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = if (
                            email.value.isNotEmpty() &&
                            android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
                        ) Green else GrayLight
                    ),
                ) {
//                    if (viewModel.loading.value) {
//                        CircularProgressIndicator(
//                            color = White,
//                            strokeCap = StrokeCap.Round,
//                            strokeWidth = 2.dp,
//                            modifier = Modifier
//                                .width(18.dp)
//                                .height(18.dp)
//                        )
//                    } else {
                        Text(stringResource(id = R.string.submit), style = MaterialTheme.typography.button, fontSize = 18.sp)
//                    }
                }
            }
        }

//        TopToastDialog(
//            postDataMessage.value,
//            showToast.value,
//            onDismiss = {
//                showToast.value = false
//            }
//        )
    }
}
