package com.betamotor.app.presentation.screen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.betamotor.app.R
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.White
import com.betamotor.app.utils.LocaleHelper
import com.betamotor.app.utils.PrefManager

@Composable
fun SettingScreen(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PrefManager(context) }
    
    // Get the current selected language
    var currentLanguage by remember { 
        mutableStateOf(prefManager.getCurrentLanguage()) 
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .background(color = Gray)
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = {
                    navController.navigateUp()
                },
                modifier = Modifier
                    .width(36.dp)
                    .height(36.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(24.dp),
                    bitmap = ImageBitmap.imageResource(R.drawable.ic_arrow_back_white),
                    contentDescription = stringResource(R.string.back_button)
                )
            }

            Text(
                text = stringResource(R.string.setting),
                modifier = Modifier,
                style = MaterialTheme.typography.h4,
                fontSize = 20.sp,
            )

            Spacer(modifier = Modifier.width(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Language selection section
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.h6,
                color = White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LanguageSelector(
                currentLanguage = currentLanguage,
                onLanguageSelected = { languageCode ->
                    // Update the selected language
                    currentLanguage = languageCode
                    prefManager.setCurrentLanguage(languageCode)
                    
                    // Apply the new locale using our helper
                    LocaleHelper.setLocale(context, languageCode)
                    
                    // Restart the activity to apply changes
                    val activity = context as? Activity
                    activity?.let {
                        val intent = it.intent
                        it.finish()
                        it.startActivity(intent)
                        it.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }
            )
        }
    }
}

@Composable
fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val languageOptions = listOf(
        "system" to stringResource(R.string.system_default),
        "en" to stringResource(R.string.english),
        "fr" to stringResource(R.string.french),
        "de" to stringResource(R.string.german),
        "it" to stringResource(R.string.italian),
        "es" to stringResource(R.string.spanish)
    )
    
    val currentLanguageDisplay = languageOptions.find { it.first == currentLanguage }?.second
        ?: stringResource(R.string.system_default)

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Gray)
                .clickable { expanded = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentLanguageDisplay,
                color = White
            )
            Icon(
                painter = painterResource(id = R.drawable.dropdown),
                contentDescription = stringResource(R.string.select_language),
                tint = White
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Gray)
        ) {
            languageOptions.forEach { (code, name) ->
                DropdownMenuItem(
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    }
                ) {
                    Text(
                        text = name,
                        color = if (code == currentLanguage) Green else White
                    )
                }
            }
        }
    }
}