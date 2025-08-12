package com.betamotor.app.service

import com.betamotor.app.data.api.motorcycle.BookmarkMotorcycleCreateRequest
import com.betamotor.app.data.api.motorcycle.BookmarksItem
import com.betamotor.app.data.api.motorcycle.CreateMotorcycleRequest
import com.betamotor.app.data.api.motorcycle.HistoryMotorcycleTrackingDataItem
import com.betamotor.app.data.api.motorcycle.HistoryMotorcycleTrackingResponse
import com.betamotor.app.data.api.motorcycle.MotorcycleAccessoriesAccessoriesItem
import com.betamotor.app.data.api.motorcycle.MotorcycleAccessoriesData
import com.betamotor.app.data.api.motorcycle.MotorcycleItem
import com.betamotor.app.data.api.motorcycle.MotorcycleTypesResponse
import com.betamotor.app.data.api.motorcycle.StarMotorcycleTrackingResponse
import com.betamotor.app.data.api.motorcycle.StartMotorcycleTrackingRequest
import com.betamotor.app.data.api.motorcycle.StopMotorcycleTrackingRequest

interface MotorcycleService {
    suspend fun getMotorcycleTypes(): Pair<MotorcycleTypesResponse?, String>
    suspend fun getMotorcycles(): Pair<List<MotorcycleItem>?, String>
    suspend fun saveMotorcycle(device: CreateMotorcycleRequest): Pair<Boolean?, String>
    suspend fun removeMotorcycle(macAddress: String): Pair<Boolean?, String>
    suspend fun updateLastConnected(macAddress: String): Pair<Boolean?, String>
    suspend fun startTracking(data: StartMotorcycleTrackingRequest): Pair<StarMotorcycleTrackingResponse?, String>
    suspend fun stopTracking(data: StopMotorcycleTrackingRequest): Pair<Boolean?, String>
    suspend fun getTrackingHistory(transactionID: String): Pair<List<HistoryMotorcycleTrackingDataItem>?, String>
    suspend fun getMotorcyclesAccessories(vin: String):Pair<MotorcycleAccessoriesData?, String>
    suspend fun getBookmarksMotorcycles():Pair<List<BookmarksItem?>?, String>
    suspend fun saveBookmarksMotorcycle(data: BookmarkMotorcycleCreateRequest): Pair<Boolean?, String>
    suspend fun deleteBookmarksMotorcycle(id: Int): Pair<Boolean?, String>
}
