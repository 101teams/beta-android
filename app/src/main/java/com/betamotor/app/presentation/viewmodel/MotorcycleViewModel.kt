package com.betamotor.app.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.betamotor.app.data.api.motorcycle.MotorcyclesItem
import com.betamotor.app.service.MotorcycleService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MotorcycleViewModel @Inject constructor(
    private val motorcycleService: MotorcycleService
): ViewModel() {
    private val _motorcycles = MutableStateFlow<List<MotorcyclesItem?>>(emptyList())
    val motorcycles: StateFlow<List<MotorcyclesItem?>> = _motorcycles

    val isLoading = mutableStateOf(false)

    suspend fun getMotorcycles(): Pair<List<MotorcyclesItem?>, String> {
        val result = motorcycleService.listMotorcycle()
        var motorcycleData: List<MotorcyclesItem?> = arrayListOf()

        if (result.first != null) {
            motorcycleData = result.first?.data?.motorcycles!!
        }

        _motorcycles.value = motorcycleData
        return Pair(_motorcycles.value, result.second)
    }

}
