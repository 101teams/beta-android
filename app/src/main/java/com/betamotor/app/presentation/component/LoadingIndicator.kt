package com.betamotor.app.presentation.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.betamotor.app.theme.Black

@Composable
fun LoadingIndicator(size: Dp = 18.dp) {
    CircularProgressIndicator(
        color = Black,
        backgroundColor = Black.copy(alpha = 0.3f),
        strokeCap = StrokeCap.Round,
        strokeWidth = 2.dp,
        modifier = Modifier
            .width(size)
            .height(size)
    )
}