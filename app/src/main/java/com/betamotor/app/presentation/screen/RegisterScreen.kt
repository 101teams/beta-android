package com.betamotor.app.presentation.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import com.betamotor.app.data.api.register.RegisterRequest
import com.betamotor.app.data.constants.PRIVACY_POLICY_URL
import com.betamotor.app.presentation.component.CustomCheckbox
import com.betamotor.app.presentation.component.CustomTopBar
import com.betamotor.app.presentation.component.Input
import com.betamotor.app.presentation.component.PasswordInput
import com.betamotor.app.presentation.viewmodel.AuthViewModel
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.GrayLight
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.RobotoCondensed
import com.betamotor.app.theme.White
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<AuthViewModel>()
    val scope = rememberCoroutineScope()

//    val window = (context as Activity).window
//    val view = LocalView.current
//    Helper().setNotifBarColor(view, window, White.toArgb(),true)

    val name = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val cpassword = remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(false) }
    val showDialogSuccessRegister = remember { mutableStateOf(false) }

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
        CustomTopBar(title = stringResource(id = R.string.register), navController)

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Input(
                modifier = Modifier,
                field = null,
                placeholder = stringResource(id = R.string.name),
                binding = name,
                disabled = false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
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

            Spacer(modifier = Modifier.height(12.dp))
            Input(
                modifier = Modifier,
                field = null,
                placeholder = stringResource(id = R.string.handphone_number),
                binding = phone,
                disabled = false,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
            )

            if (phone.value.isNotEmpty() &&
                !android.util.Patterns.PHONE.matcher(phone.value).matches()) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.phone_must_valid),
                    style = TextStyle(
                        fontFamily = RobotoCondensed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PasswordInput(
                modifier = Modifier,
                placeholder = stringResource(id = R.string.password),
                binding = password,
                imeAction = ImeAction.Next,
            )

            if (password.value.isNotEmpty() &&
                password.value.length < 12) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.password_must_12_char),
                    style = TextStyle(
                        fontFamily = RobotoCondensed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                    ),
                )
            }else if (password.value.isNotEmpty() && (!password.value.any { !it.isLetterOrDigit() } || !password.value.any { it.isDigit() } || !password.value.any { it.isUpperCase() })) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.password_must_contain_special_char_and_number),
                    style = TextStyle(
                        fontFamily = RobotoCondensed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PasswordInput(
                modifier = Modifier,
                placeholder = stringResource(id = R.string.confirm_password),
                binding = cpassword,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {

                    }
                )
            )

            if (cpassword.value.isNotEmpty() &&
                cpassword.value != password.value) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.password_must_match),
                    style = TextStyle(
                        fontFamily = RobotoCondensed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                    ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                CustomCheckbox(stringResource(id = R.string.i_have_read), checked = checked.value) {
                    checked.value = it
                }

                TextButton(
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    onClick = {
                        val browserIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                        context.startActivity(browserIntent)
                    },
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.privacy_policy),
                        style = TextStyle(
                            fontFamily = RobotoCondensed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = White,
                        ),
                    )
                }
            }

            Button(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .height(62.dp)
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    if (
                        name.value.isNotEmpty() &&
                        password.value.isNotEmpty() &&
                        phone.value.isNotEmpty() &&
                        email.value.isNotEmpty() &&
                        checked.value &&
                        password.value.length >= 12 &&
                        password.value == cpassword.value &&
                        android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches() &&
                        password.value.any { !it.isLetterOrDigit() } &&
                        password.value.any { it.isDigit() } &&
                        password.value.any { it.isUpperCase() }
                    ) {
                        scope.launch {
                            viewModel.loading.value = true
                            val successRegist = viewModel.register(
                                RegisterRequest(
                                    fullName = name.value,
                                    phone = phone.value,
                                    email = email.value,
                                    password = password.value,
                                    passwordConfirmation = cpassword.value,
                                )
                            )

                            if (successRegist == null) {
                                navController.navigateUp()
                                Toast.makeText(context, context.getString(R.string.account_registration_success), Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, successRegist, Toast.LENGTH_LONG).show()
                            }
                            viewModel.loading.value = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = if (
                        name.value.isNotEmpty() &&
                        password.value.isNotEmpty() &&
                        phone.value.isNotEmpty() &&
                        email.value.isNotEmpty() &&
                        checked.value &&
                        password.value.length >= 12 &&
                        password.value == cpassword.value &&
                        android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches() &&
                        password.value.any { !it.isLetterOrDigit() } &&
                        password.value.any { it.isDigit() } &&
                        password.value.any { it.isUpperCase() }
                    ) Green else GrayLight
                ),
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
                    Text(stringResource(id = R.string.submit), style = MaterialTheme.typography.button, fontSize = 18.sp)
                }
            }

            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.already_have_an_account),
                    style = MaterialTheme.typography.body1,
                )

                TextButton(
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    onClick = {
                        navController.navigateUp()
                    },
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.login_here),
                        style = TextStyle(
                            fontFamily = RobotoCondensed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = White,
                        ),
                    )
                }
            }
        }
    }
}
