package com.griffith.androidproject_3144757.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

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
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            try {
                LocationManagerCompat.getCurrentLocation(
                    manager,
                    provider,
                    signal,
                    ContextCompat.getMainExecutor(context)
                ) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            } catch (_: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
            } catch (_: IllegalArgumentException) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }
}
