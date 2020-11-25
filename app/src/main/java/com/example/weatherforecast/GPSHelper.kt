package com.example.weatherforecast

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

@SuppressLint("MissingPermission")
class GPSHelper(private val context: Context) {
    private var locationProvider: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    private var list: ArrayList<String> = ArrayList()


    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    fun getLocation(complete: (ArrayList<String>) -> Unit) {
        locationProvider = LocationServices.getFusedLocationProviderClient(context)

        locationProvider?.lastLocation?.addOnSuccessListener { location ->
            if (location != null) {
               val lat = location.latitude.toString()
              val lon = location.longitude.toString()
                list.add(lat)
                list.add(lon)
                complete(list)
            }
        }

        createLocationRequest()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(context as Activity, 500)
                } catch (sendEx: IntentSender.SendIntentException) {
                    sendEx.printStackTrace()
                }
            }
        }
    }

    fun startLocationUpdates(locationCallback: LocationCallback?) {
        locationProvider?.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopLocationUpdates(locationCallback: LocationCallback?) {
        locationProvider?.removeLocationUpdates(locationCallback)
    }
}