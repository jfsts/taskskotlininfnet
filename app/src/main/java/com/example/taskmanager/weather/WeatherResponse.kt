package com.example.taskmanager.weather

data class WeatherResponse(
    val name: String,
    val main: MainData,
    val weather: List<WeatherData>
)

data class MainData(
    val temp: Float,
    val feels_like: Float,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Int,
    val humidity: Int
)

data class WeatherData(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) 