package com.betamotor.app.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.betamotor.app.R
import com.betamotor.app.data.bluetooth.BluetoothDevice
import com.betamotor.app.theme.GrayLight
import com.betamotor.app.theme.White

@Composable
fun NetworkItem(
    device: BluetoothDevice,
    isLoading: Boolean,
    onClick: (BluetoothDevice) -> Unit,
    iconType: IconType = IconType.ARROW,
    clickMode: ClickMode = ClickMode.ROW,
    secondRow: SecondRow = SecondRow.MAC_ADDRESS
) {
    val modifierClickable = Modifier.clickable { onClick(device) }
    val rowModifier = if (clickMode == ClickMode.ROW) modifierClickable else Modifier
    val rightIconModifier = if (clickMode == ClickMode.ELEMENT) modifierClickable else Modifier

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .then(rowModifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(device.identity ?: "(${stringResource(id = R.string.no_name)})")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (secondRow == SecondRow.MAC_ADDRESS) device.macAddress else device.name,
                    style = MaterialTheme.typography.caption,
                    color = GrayLight
                )
            }

            if (isLoading) {
                Row(
                    modifier = Modifier
                        .padding(end = 4.dp)
                ) {
                    LoadingIndicator(size = 16.dp)
                }
            } else {
                when(iconType) {
                    IconType.DELETE -> {
                        Image(
                            bitmap = ImageBitmap.imageResource(R.drawable.img_betamotor_vertical),
                            contentDescription = "delete item",
                            modifier = Modifier
                                .size(16.dp)
                                .then(rightIconModifier)
                        )
                    }

                    else -> {
                        Image(
                            bitmap = ImageBitmap.imageResource(R.drawable.ic_right_arrow_white),
                            contentDescription = "arrow to right",
                            modifier = Modifier
                                .size(12.dp)
                                .then(rightIconModifier)
                        )
                    }
                }
            }
        }

        Divider(
            modifier = Modifier
                .padding(top = 8.dp)
                .background(color = White)
        )
    }
}

enum class SecondRow {
    MAC_ADDRESS,
    NAME
}

enum class ClickMode {
    ROW,
    ELEMENT
}

enum class IconType {
    DELETE,
    ARROW
}
