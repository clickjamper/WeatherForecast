package com.example.weatherforecast

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherforecast.R.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.math.round

class MainActivity : AppCompatActivity() {

    private var locationHelper: GPSHelper? = null

    private var locationCallback: LocationCallback? = null

    private lateinit var city: TextView
    private lateinit var weather: TextView
    private lateinit var temp: TextView
    private lateinit var filltemp: TextView
    private lateinit var pressure: TextView
    private lateinit var humidity: TextView
    private lateinit var speed: TextView
    private var itemLayout: RelativeLayout? = null

    private var flag = false
    private var check = true
    private var save = ""

    private var shimmerLayout: ShimmerFrameLayout? = null
    private var list: ArrayList<String> = ArrayList()

    var lat = "0"
    var lon = "0"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        itemLayout = findViewById(id.items)

        shimmerLayout = findViewById(id.shimmerFrameLayout)
        shimmerLayout?.startShimmer()

        locationHelper = GPSHelper(this)

        city = findViewById(id.city)
        weather = findViewById(id.weather)
        temp = findViewById(id.temp)
        filltemp = findViewById(id.fill_temp)
        pressure = findViewById(id.pressure)
        humidity = findViewById(id.humidity)
        speed = findViewById(id.speed)

        val sharedPreferences = getSharedPreferences("city_save", MODE_PRIVATE)
        flag = sharedPreferences.getBoolean("flag", flag)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE
                ), 2
            )
            if (flag) {
                load()
            }
        } else {
            if (flag) {
                load()
            }
            else {
                GlobalScope.launch {

                    withContext(Dispatchers.Main) {
                        shimmerLayout?.startShimmer()
                        delay(800)
                        shimmerLayout?.stopShimmer()
                        locationHelper!!.getLocation {
                            list = it
                            request(list[0], list[1])
                        }
                    }
                }
            }
        }

        locationStart()
    }

    override fun onResume() {
        super.onResume()
        locationHelper?.startLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        locationHelper?.stopLocationUpdates(locationCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.city_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            id.action_list -> {
                startActivityForResult(Intent(this, CityList::class.java), 1)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun locationStart() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationHelper?.locationRequest == null) {
                    lat = "0"
                    lon = "0"
                    return
                }

                val location = locationResult?.lastLocation

                if (check) {
                    lat = location?.latitude.toString()
                    lon = location?.longitude.toString()
                    request(lat, lon)
                }

                if (locationResult != null && check) {
                    lat = location?.latitude.toString()
                    lon = location?.longitude.toString()
                    request(lat, lon)
                }
            }
        }
    }

    private fun save() {
        val sharedPreferences = getSharedPreferences("city_save", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        flag = true
        editor.putString("save", save)
        editor.putBoolean("flag", flag)
        editor.apply()
    }

    private fun load() {
        val sharedPreferences = getSharedPreferences("city_save", MODE_PRIVATE)
        save = sharedPreferences.getString("save", "").toString()
        flag = sharedPreferences.getBoolean("flag", flag)
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                shimmerLayout?.startShimmer()
                delay(800)
                shimmerLayout?.stopShimmer()
                request(save, "")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray) {
        when (requestCode) {
            2 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (check) {
                        locationHelper!!.getLocation {
                            list = it
                            request(list[0], list[1])
                        }
                    }
                } else {
                    if (!flag) {
                        startActivityForResult(Intent(this, CityList::class.java), 1)
                    }
                }
            }
            else -> {
                startActivityForResult(Intent(this, CityList::class.java), 1)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }

        val result = data.getStringExtra("city").toString()
        check = false
        GlobalScope.launch {

            withContext(Dispatchers.Main) {
                shimmerLayout?.startShimmer()
                city.text = ""
                weather.text = ""
                temp.text = ""
                filltemp.text = ""
                pressure.text = ""
                humidity.text = ""
                speed.text = ""
                delay(800)
                shimmerLayout?.stopShimmer()
                if (result == "Ваше местоположение") {
                    locationHelper?.startLocationUpdates(locationCallback)

                    check = true
                    locationHelper!!.getLocation {
                        list = it
                        request(list[0], list[1])
                    }
                } else {
                    locationHelper?.stopLocationUpdates(locationCallback)

                    request(result, "")
                }
            }
        }
    }

    private fun request(fdata: String, sdata: String){

        val url = if (sdata == "") {
            "https://api.openweathermap.org/data/2.5/weather?q=$fdata&lang=ru&units=metric&appid=e1648c9142dcfa64499525bb88bf03ed"
        } else {
            "https://api.openweathermap.org/data/2.5/weather?lat=$fdata&lon=$sdata&lang=ru&units=metric&appid=e1648c9142dcfa64499525bb88bf03ed"
        }

        url.httpGet().responseString { _, _, result ->
            when(result) {
                is Result.Success -> {
                    jsonPars(result.get())
                }
                else -> {}
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun jsonPars(data: String) {

        val json = JSONObject(data)
        locationHelper?.stopLocationUpdates(locationCallback)

        city.text = json.optString("name", "")
        save = json.optString("name", "")

        save()

        for (x in 0 until json.getJSONArray("weather").length()) {
            val jsonObject = json.getJSONArray("weather").getJSONObject(x)
            weather.text = jsonObject.optString("description", "")
        }

        temp.text = json.getJSONObject("main").optInt("temp", 0).toString() + "°C"
        filltemp.text = "Ощущается как " + json.getJSONObject("main").optInt("feels_like", 0).toString() + "°C"
        pressure.text = round(json.getJSONObject("main").optInt("pressure") / 1.333).toInt().toString()
        humidity.text = json.getJSONObject("main").optInt("humidity").toString() + "%"

        val wind = json.getJSONObject("wind")

        speed.text = wind.optInt("speed").toString() + " м/с"
    }
}