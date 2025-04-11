package com.example.taskmanager.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.taskmanager.R
import com.example.taskmanager.databinding.FragmentWeatherBinding
import com.example.taskmanager.weather.RetrofitClient
import com.example.taskmanager.weather.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import com.example.taskmanager.weather.MainData
import com.example.taskmanager.weather.WeatherData

class WeatherFragment : Fragment() {
    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = "0dcb5edc30de23600d65678046297091"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            Log.d("WeatherFragment", "onViewCreated iniciado")
            
            // Inicializar o provedor de localização
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            
            // Verificar permissões e obter localização
            if (hasLocationPermission()) {
                Log.d("WeatherFragment", "Permissão de localização concedida, obtendo localização atual")
                getCurrentLocation()
            } else {
                Log.d("WeatherFragment", "Solicitando permissão de localização")
                requestLocationPermission()
            }
            
            // Adicione isso para testar a API quando o usuário clicar na mensagem de erro ou na progressbar
            binding.progressBar.setOnClickListener {
                testWeatherAPI()
            }
            
            binding.textError.setOnClickListener {
                testWeatherAPI()
            }
            
            // Você pode chamar isso automaticamente também
            testWeatherAPI()
            
        } catch (e: Exception) {
            Log.e("WeatherFragment", "Erro no onViewCreated: ${e.message}")
            e.printStackTrace()
            showError()
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    private fun getCurrentLocation() {
        try {
            Log.d("WeatherFragment", "Tentando obter localização atual")
            
            if (!hasLocationPermission()) {
                Log.d("WeatherFragment", "Sem permissão para obter localização")
                getWeatherData(-23.5505, -46.6333)
                return
            }

            // Criar solicitação de localização com prioridade alta
            val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000 // 10 segundos
                fastestInterval = 5000 // 5 segundos
            }

            // Criar callback de localização
            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        Log.d("WeatherFragment", "Nova localização obtida: ${location.latitude}, ${location.longitude}")
                        
                        // Obter nome da cidade baseado nas coordenadas
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (addresses != null && addresses.isNotEmpty()) {
                                val city = addresses[0].locality ?: addresses[0].subAdminArea ?: "Local desconhecido"
                                Log.d("WeatherFragment", "Cidade detectada: $city")
                                Toast.makeText(context, "Localização atual: $city", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("WeatherFragment", "Erro ao obter nome da cidade: ${e.message}")
                        }
                        
                        // Buscar dados do clima
                        getWeatherData(location.latitude, location.longitude)
                        
                        // Remover atualizações de localização após obter uma localização
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }

            // Registrar para atualizações de localização
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("WeatherFragment", "Solicitando atualizações de localização")
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
                
                // Como backup, tentar obter a última localização conhecida
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            Log.d("WeatherFragment", "Última localização conhecida: ${location.latitude}, ${location.longitude}")
                            getWeatherData(location.latitude, location.longitude)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("WeatherFragment", "Erro ao obter última localização: ${e.message}")
                        getWeatherData(-23.5505, -46.6333) // Localização padrão como fallback
                    }
            } else {
                Log.d("WeatherFragment", "Sem permissão, mesmo após verificação")
                getWeatherData(-23.5505, -46.6333)
            }
        } catch (e: Exception) {
            Log.e("WeatherFragment", "Erro ao obter localização: ${e.message}")
            e.printStackTrace()
            getWeatherData(-23.5505, -46.6333)
        }
    }

    private fun getMockWeatherData() {
        try {
            // Criar dados simulados para demonstração
            val mockWeather = WeatherResponse(
                name = "São Paulo",
                main = MainData(
                    temp = 23.5f,
                    feels_like = 24.0f,
                    temp_min = 22.0f,
                    temp_max = 25.0f,
                    pressure = 1012,
                    humidity = 70
                ),
                weather = listOf(
                    WeatherData(
                        id = 800,
                        main = "Clear",
                        description = "céu limpo",
                        icon = "01d"
                    )
                )
            )
            
            // Atualizar UI com dados simulados
            updateUI(mockWeather)
            
        } catch (e: Exception) {
            Log.e("WeatherFragment", "Erro ao criar dados simulados: ${e.message}")
            showError()
        }
    }

    private fun getWeatherData(lat: Double, lon: Double) {
        try {
            Log.d("WeatherFragment", "Obtendo dados do clima para: $lat, $lon")
            
            // Determinar o idioma baseado nas configurações locais
            val currentLocale = Locale.getDefault().language
            val lang = when (currentLocale) {
                "pt" -> "pt_br"
                "es" -> "es"
                else -> "en"
            }
            
            Log.d("WeatherFragment", "Usando idioma: $lang")
            
            // Fazer a chamada da API
            val call = RetrofitClient.weatherApiService.getCurrentWeather(
                lat = lat,
                lon = lon,
                appid = apiKey,
                units = "metric",
                lang = lang
            )

            call.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    try {
                        if (response.isSuccessful) {
                            val weatherData = response.body()
                            Log.d("WeatherFragment", "Resposta recebida com sucesso: ${weatherData?.name}")
                            weatherData?.let {
                                updateUI(it)
                            }
                        } else {
                            Log.e("WeatherFragment", "Erro na resposta: ${response.code()}")
                            Log.e("WeatherFragment", "Corpo do erro: ${response.errorBody()?.string()}")
                            
                            // Se falhar, como plano B, usar dados simulados
                            getMockWeatherData()
                        }
                    } catch (e: Exception) {
                        Log.e("WeatherFragment", "Erro ao processar resposta: ${e.message}")
                        e.printStackTrace()
                        
                        // Se falhar, como plano B, usar dados simulados
                        getMockWeatherData()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    val url = call.request().toString()
                    
                    Log.e("WeatherFragment", "Falha na chamada: ${t.message}")
                    Log.e("WeatherFragment", "URL completa: $url")
                    t.printStackTrace()
                    
                    // Se falhar, como plano B, usar dados simulados
                    getMockWeatherData()
                }
            })
        } catch (e: Exception) {
            Log.e("WeatherFragment", "Erro ao obter dados do clima: ${e.message}")
            e.printStackTrace()
            
            // Se falhar, como plano B, usar dados simulados
            getMockWeatherData()
        }
    }

    private fun updateUI(weather: WeatherResponse) {
        try {
            // Log detalhado da resposta
            Log.d("WeatherFragment", "Dados da API recebidos:")
            Log.d("WeatherFragment", "Cidade: ${weather.name}")
            Log.d("WeatherFragment", "Temperatura: ${weather.main.temp}°C")
            Log.d("WeatherFragment", "Clima: ${weather.weather[0].description}")
            Log.d("WeatherFragment", "Ícone: ${weather.weather[0].icon}")
            Log.d("WeatherFragment", "Sensação térmica: ${weather.main.feels_like}°C")
            Log.d("WeatherFragment", "Umidade: ${weather.main.humidity}%")
            Log.d("WeatherFragment", "Pressão: ${weather.main.pressure} hPa")
            
            _binding?.let { binding ->
                binding.textCity.text = weather.name
                binding.textTemp.text = "${weather.main.temp.toInt()}°C"
                binding.textDescription.text = weather.weather[0].description
                
                // Carregar ícone com Glide
                val iconUrl = "https://openweathermap.org/img/wn/${weather.weather[0].icon}@2x.png"
                Glide.with(this)
                    .load(iconUrl)
                    .into(binding.imageWeather)
                
                // Mostrar o layout
                binding.weatherLayout.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.textError.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("WeatherFragment", "Erro ao atualizar UI: ${e.message}")
            e.printStackTrace()
            showError()
        }
    }

    private fun showError() {
        _binding?.let {
            it.weatherLayout.visibility = View.GONE
            it.progressBar.visibility = View.GONE
            it.textError.visibility = View.VISIBLE
        }
    }

    private fun testWeatherAPI() {
        try {
            Log.d("WeatherTest", "Testando API do tempo...")
            
            // Localização de São Paulo
            val lat = -23.5505
            val lon = -46.6333
            
            // Determinar o idioma baseado nas configurações locais
            val currentLocale = Locale.getDefault().language
            val lang = when (currentLocale) {
                "pt" -> "pt_br"
                "es" -> "es"
                else -> "en"
            }
            
            // Fazer a chamada da API
            val call = RetrofitClient.weatherApiService.getCurrentWeather(
                lat = lat,
                lon = lon,
                appid = apiKey,
                units = "metric",
                lang = lang
            )

            call.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                    if (response.isSuccessful) {
                        val weatherData = response.body()
                        Log.d("WeatherTest", "Resposta recebida com sucesso!")
                        
                        // Log da resposta completa
                        Log.d("WeatherTest", "Nome da cidade: ${weatherData?.name}")
                        Log.d("WeatherTest", "Temperatura: ${weatherData?.main?.temp}°C")
                        Log.d("WeatherTest", "Sensação térmica: ${weatherData?.main?.feels_like}°C")
                        Log.d("WeatherTest", "Clima: ${weatherData?.weather?.get(0)?.description}")
                        Log.d("WeatherTest", "Ícone: ${weatherData?.weather?.get(0)?.icon}")
                        
                        // Mostrar resposta em um toast para debug
                        context?.let {
                            Toast.makeText(it, 
                                "Temperatura em ${weatherData?.name}: ${weatherData?.main?.temp}°C", 
                                Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("WeatherTest", "Erro na resposta: ${response.code()}")
                        Log.e("WeatherTest", "Corpo do erro: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("WeatherTest", "Falha na chamada: ${t.message}")
                    Log.e("WeatherTest", "URL completa: ${call.request()}")
                    t.printStackTrace()
                }
            })
        } catch (e: Exception) {
            Log.e("WeatherTest", "Erro ao testar API: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && 
                (grantResults[0] == PackageManager.PERMISSION_GRANTED || 
                 grantResults.getOrNull(1) == PackageManager.PERMISSION_GRANTED)) {
                
                Log.d("WeatherFragment", "Permissão de localização concedida pelo usuário")
                getCurrentLocation()
            } else {
                Log.d("WeatherFragment", "Permissão de localização negada, usando localização padrão")
                Toast.makeText(
                    requireContext(),
                    "Usando localização padrão pois permissão foi negada",
                    Toast.LENGTH_SHORT
                ).show()
                getWeatherData(-23.5505, -46.6333)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 