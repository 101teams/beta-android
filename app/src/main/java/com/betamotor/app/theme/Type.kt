package com.betamotor.app.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.betamotor.app.R

val RobotoCondensed = FontFamily(
    Font(R.font.roboto_condensed_regular),
    Font(R.font.roboto_condensed_medium, FontWeight.Medium),
    Font(R.font.roboto_condensed_semibold, FontWeight.SemiBold)
)

// Set of Material typography styles to start with
val Typography = Typography(
    h1 = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.SemiBold,
        fontSize = 96.sp,
        color = White,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    h4 = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        color = White,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    ),
    subtitle1 = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = White
    ),
    body1 = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = White
    ),
    body2 = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = White
    ),
    caption = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = White
    ),
    button = TextStyle(
        fontFamily = RobotoCondensed,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = White
    )
)