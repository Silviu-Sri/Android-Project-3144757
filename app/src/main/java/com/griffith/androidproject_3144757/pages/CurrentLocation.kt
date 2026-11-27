package com.griffith.androidproject_3144757.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import android.location.LocationManager
import androidx.core.content.ContextCompat
import android.location.LocationListener
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@Suppress("DEPRECATION")
suspend fun currentLocationName(context: Context, location: Location, locale: Locale): String? =
    withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val address = Geocoder(context, locale)
                .getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            listOfNotNull(address?.locality, address?.subAdminArea, address?.adminArea, address?.countryName)
                .firstOrNull { it.isNotBlank() }
        } catch (_: java.io.IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

suspend fun currentWeatherLocation(context: Context): Location? {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED
    ) return null

    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val provider = when {
        fineGranted && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> return null
    }

    return withTimeoutOrNull(15_000L) {
        suspendCancellableCoroutine { continuation ->
            val requestTimeNanos = SystemClock.elapsedRealtimeNanos()
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (location.elapsedRealtimeNanos >= requestTimeNanos && continuation.isActive) {
                        manager.removeUpdates(this)
                        continuation.resume(location)
                    }
                }

                @Deprecated("Deprecated in Android")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) = Unit
            }
            try {
                manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            } catch (_: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
            } catch (_: IllegalArgumentException) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }
}
