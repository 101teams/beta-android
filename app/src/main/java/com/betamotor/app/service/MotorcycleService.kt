package com.betamotor.app.service

import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.api.motorcycle.MotorcycleTypesResponse

interface MotorcycleService {
    suspend fun getMotorcycleTypes(): Pair<MotorcycleTypesResponse?, String>
    suspend fun getMotorcycles(): Pair<List<MotorcycleItem>?, String>
    suspend fun saveMotorcycle(device: CreateMotorcycleRequest): Pair<Boolean?, String>
    suspend fun removeMotorcycle(macAddress: String): Pair<Boolean?, String>
    suspend fun updateLastConnected(macAddress: String): Pair<Boolean?, String>
}
