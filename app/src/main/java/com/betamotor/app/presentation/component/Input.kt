package com.betamotor.app.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RangeSlider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.betamotor.app.R
import com.betamotor.app.theme.Black
import com.betamotor.app.theme.GrayLight
import com.betamotor.app.theme.GrayVeryLight
import com.betamotor.app.theme.InputPlaceholderGray
import com.betamotor.app.theme.RobotoCondensed
import com.betamotor.app.theme.PlaceholderGray
import com.betamotor.app.theme.White

@Composable
fun TrailingIcon(@DrawableRes id: Int, onClick: () -> Unit, isXML: Boolean?) {
    IconButton(onClick = onClick) {
        if (isXML != null && isXML == true) {
            Icon(
                painter  = painterResource(id), // hasil konversi
                contentDescription = "My Icon"
            )
        } else {
            Image(
                bitmap = ImageBitmap.imageResource(id),
                contentDescription = "Show password icon",
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

@Composable
fun Input(
    placeholder: String,
    modifier: Modifier = Modifier,
    field: String? = null,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null,
    binding: MutableState<String>,
    disabled: Boolean = false,
    @DrawableRes trailingIcon: Int? = null,
    trailingIconXML: Boolean? = null,
    onTrailingIconClick: (() -> Unit) = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    colorConfig: TextFieldColors? = null,
    modifierParent: Modifier = Modifier,
    trailingUnit: String? = null,
    inputTextStyle: TextStyle? = null,
    fillMaxWidth: Boolean = true,
) {
    var isFocused by remember { mutableStateOf(false) }

    val trailingIc: @Composable (() -> Unit) = {
        if (trailingIcon != null) {
            TrailingIcon(trailingIcon, onTrailingIconClick, trailingIconXML)
        }
    }

    Column(
        modifier = modifierParent,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (field != null) {
            Text(field,
                fontFamily = RobotoCondensed,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Black,
            )
        }

        Row {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .then(modifier)
            ) {
                OutlinedTextField(
                    value = binding.value,
                    readOnly = disabled,
                    onValueChange = { binding.value = it },
                    placeholder = {
                        Text(
                            placeholder,
                            color = InputPlaceholderGray,
                            style = TextStyle(
                                fontFamily = RobotoCondensed,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Black,
                                textAlign = inputTextStyle?.textAlign ?: TextAlign.Start,
                            ),
                            modifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier,
                        )
                    },
                    modifier = Modifier
                        .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                        .onFocusChanged {
                            isFocused = it.isFocused
                        },
                    singleLine = true,
                    keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
                    keyboardActions = keyboardActions ?: KeyboardActions.Default,
                    visualTransformation = visualTransformation,
                    trailingIcon = if(trailingIcon != null) trailingIc else null,
                    colors = colorConfig ?: TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Black,
                        focusedBorderColor = if (disabled) PlaceholderGray else White,
                        unfocusedBorderColor = PlaceholderGray,
                        cursorColor = White,
                        disabledBorderColor = PlaceholderGray,
                        backgroundColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = inputTextStyle ?: LocalTextStyle.current.copy(),
                )
            }

            if (trailingUnit != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(trailingUnit,
                    modifier = Modifier
                        .align(Alignment.Bottom),
                    fontFamily = RobotoCondensed,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = Black,
                )
            }
        }
    }
}

@Composable
fun PasswordInput(
    modifier: Modifier = Modifier,
    field: String? = null,
    placeholder: String,
    binding: MutableState<String>,
    disabled: Boolean = false,
    imeAction: ImeAction? = null,
    keyboardActions: KeyboardActions? = null,
) {
    val showPassword = remember { mutableStateOf(false) }
    val passwordIcon = if (showPassword.value) R.drawable.eye else R.drawable.eye_invisible
    val togglePasswordVisibility = { showPassword.value = !showPassword.value }
    val visualTransformation =
        if (!showPassword.value) PasswordVisualTransformation()
        else VisualTransformation.None

    Input(
        field = field,
        placeholder = placeholder,
        binding = binding,
        disabled = disabled,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction ?: ImeAction.Default
        ),
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        trailingIcon = passwordIcon,
        trailingIconXML = true,
        onTrailingIconClick = togglePasswordVisibility,
        modifier = modifier,
        inputTextStyle = TextStyle(
            fontFamily = RobotoCondensed,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = White,
        ),
    )
}
