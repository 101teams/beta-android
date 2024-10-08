package com.betamotor.app.presentation.component

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.betamotor.app.R
import com.betamotor.app.theme.DarkBlue

@Composable
fun PermissionNeededDialog(
    onDone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDone() },
        confirmButton = {
            TextButton(onClick = { onDone() }) {
                Text(stringResource(R.string.done), color = DarkBlue)
            }
        }, title = {
            Text(stringResource(id = R.string.bt_location_request))
        }, text = {
            Text(stringResource(id = R.string.bt_location_request_usage))
        })
}