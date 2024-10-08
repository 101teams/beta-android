package com.betamotor.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
fun SystemBroadcastReceiver(
    action: String,
    onEvent: (intent: Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnEvent by rememberUpdatedState(onEvent)

    DisposableEffect(context, action) {
        val filter = IntentFilter(action)
        val broadcast = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnEvent(intent)
            }
        }

        context.registerReceiver(broadcast, filter)

        onDispose {
            context.unregisterReceiver(broadcast)
        }
    }
}