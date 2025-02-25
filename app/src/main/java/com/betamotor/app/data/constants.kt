package com.betamotor.app.data

object constants {
    const val v1 = "/api/v1"
//    const val RLI_ENGINE_SPEED = 0x30
//    const val RLI_GAS_POSITION = 0x37
//    const val RLI_ACTUATED_SPARK = 0x58
//    const val RLI_COOLANT_TEMP = 0x32
//    const val RLI_AIR_TEMP = 0x35
//    const val RLI_ATMOSPHERE_PRESSURE = 0x55
//    const val RLI_OPERATING_HOURS = 0x04
    const val RLI_ENGINE_SPEED = 0x0001
    const val RLI_GAS_POSITION = 0x0002
    const val RLI_ACTUATED_SPARK = 0x0003
    const val RLI_COOLANT_TEMP = 0x0004
    const val RLI_AIR_TEMP = 0x0005
    const val RLI_ATMOSPHERE_PRESSURE = 0x0006
    const val RLI_OPERATING_HOURS = 0x0007
    const val RLI_BATTERY_VOLTAGE = 0x0008

    const val ECU_VIN = 0x0001
    const val ECU_DRAWING_NUMBER = 0x0002
    const val ECU_HW_NUMBER = 0x0003
    const val ECU_SW_NUMBER = 0x0004
    const val ECU_SW_VERSION = 0x0005
    const val ECU_HOMOLOGATION = 0x0006

    const val TUNE_OFFSET = 0x0017
    const val TUNE_MAX = 0x0018
    const val TUNE_MIN = 0x0019
}