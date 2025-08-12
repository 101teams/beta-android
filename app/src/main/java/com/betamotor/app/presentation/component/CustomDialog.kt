package com.betamotor.app.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.betamotor.app.R
import com.betamotor.app.theme.DefaultRed
import com.betamotor.app.theme.GrayDark
import com.betamotor.app.theme.White

@Composable
fun DialogDelete(
    onDismiss: () -> Unit,
    onClickContinue: () -> Unit,
    onClickCancel: () -> Unit,
){
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = Modifier
                .background(
                    GrayDark,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(18.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_alert_red),
                    contentDescription = "Center Image",
                    modifier = Modifier
                        .height(36.dp)
                        .width(36.dp),
                    contentScale = ContentScale.Fit,
                )

                Text(
                    modifier = Modifier.padding(top = 12.dp),
                    text = stringResource(id = R.string.are_you_sure),
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ){
                    Button(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .height(62.dp)
                            .weight(1f)
                            .padding(top = 12.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            onClickCancel()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = White,),
                        border = BorderStroke(1.dp, DefaultRed),
                    ) {
                        Text(stringResource(id = R.string.back), style = MaterialTheme.typography.button, fontSize = 18.sp, color = DefaultRed)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .height(62.dp)
                            .weight(1f)
                            .padding(top = 12.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                            onClickContinue()
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = DefaultRed,),
                    ) {
                        Text(stringResource(id = R.string.delete), style = MaterialTheme.typography.button, fontSize = 18.sp, color = White)
                    }
                }
            }
        }
    }
}