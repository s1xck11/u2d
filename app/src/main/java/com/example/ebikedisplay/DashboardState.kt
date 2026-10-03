package com.example.ebikedisplay

data class DashboardState(
    val speed: Float = 0f,
    val voltage: Float = 0f,
    val battery: Int = 0,
    val range: Int = 0,
    val temperature: Int = 0,
    val errorCode: Int = 0,
    val mode: String = "P",
    val power: Float = 0f,
    val consumption: Float = 0f,
    val batteryHealth: Int = 100,
    val bleStatus: String = "Disconnected",
    val bleConnected: Boolean = false
)