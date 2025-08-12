package com.betamotor.app.presentation.component

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.betamotor.app.R
import com.betamotor.app.presentation.viewmodel.MotorcycleViewModel
import com.betamotor.app.theme.Gray
import com.betamotor.app.theme.GrayDark
import com.betamotor.app.theme.Green
import com.betamotor.app.theme.PlaceholderGray
import com.betamotor.app.theme.White

data class BookmarkMotorcycleFormData(
    val vin: String,
    val name: String,
    val typeID: String
)

@Composable
fun BookmarkMotorcycleDialog(
    openDialog: MutableState<Boolean>,
    vin: String,
    onBookmark: (data: BookmarkMotorcycleFormData) -> Unit,
) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val typeId = remember { mutableStateOf("") }
    val typeIdDisplay = remember { mutableStateOf("") }
    val vinMut = remember { mutableStateOf(vin) }
    val viewModel = hiltViewModel<MotorcycleViewModel>()

    var expanded by remember { mutableStateOf(false) }
    val isLoading = viewModel.isLoading.collectAsState()
    val motorcycleTypes = viewModel.motorcycleTypes.collectAsState()

    Dialog(onDismissRequest = {
        openDialog.value = false
    }) {
        Card(
            shape = RoundedCornerShape(10.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .background(GrayDark)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.bookmark),
                    modifier = Modifier,
                    style = MaterialTheme.typography.h4,
                    fontSize = 24.sp,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.vin), style = TextStyle(fontSize = 16.sp, color = White), modifier = Modifier.width(90.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Input(placeholder = "", binding = vinMut, inputTextStyle = TextStyle(color = White), disabled = true)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.name), style = TextStyle(fontSize = 16.sp, color = White), modifier = Modifier.width(90.dp))
                    Spacer(modifier = Modifier.width(24.dp))
                    Input(placeholder = "", binding = name, inputTextStyle = TextStyle(color = White))
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isLoading.value) {
                    LoadingIndicator()
                } else {

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.motorcycle_model), style = TextStyle(fontSize = 16.sp, color = White), modifier = Modifier.width(90.dp))
                        Spacer(modifier = Modifier.width(24.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, PlaceholderGray, RoundedCornerShape(14.dp))
                                    .clickable { expanded = true }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = typeIdDisplay.value,
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
                                motorcycleTypes.value.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            typeId.value = it?.id.toString()
                                            typeIdDisplay.value = it?.name ?: "-"
                                            expanded = false
                                        }
                                    ) {
                                        Text(
                                            text = it?.name ?: "-",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Green, shape = RoundedCornerShape(8.dp))
                    .padding(vertical = 16.dp, horizontal = 10.dp)
                    .clickable {
                        if (name.value.isEmpty() || typeId.value.isEmpty()) {
                            Toast.makeText(context, context.getString(R.string.please_fill_all_fields), Toast.LENGTH_SHORT).show()
                            return@clickable
                        }

                        onBookmark(BookmarkMotorcycleFormData(vin, name.value, typeId.value))
                    },
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.button, color = White, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun BookmarkMotorcycleDialogPreview(vin: String) {
    val openDialog = remember { mutableStateOf(false) }
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { openDialog.value = true }) {
                Text(text = stringResource(R.string.show_dialog))
            }

            BookmarkMotorcycleDialog(
                openDialog = openDialog, onBookmark = {},
                vin = vin,
            )
        }
    }
}