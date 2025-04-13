package com.betamotor.app.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.betamotor.app.theme.White

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomCheckbox(
    label: String? = null,
    checked: Boolean,
    onToggle: (checked: Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
            Checkbox(
                checked,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(checkedColor = Color.Transparent),
                modifier = Modifier.scale(0.6f))
        }

        if (label != null) {
            Text(
                label,
                color = White,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}