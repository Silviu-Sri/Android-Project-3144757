package com.griffith.androidproject_3144757.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.griffith.androidproject_3144757.R

@Composable
fun SettingsScreen(
    onLanguageChange: (String) -> Unit,
    useCurrentLocation: Boolean,
    onWeatherLocationChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionGranted by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    var showDenied by remember { mutableStateOf(false) }
    fun checkPermission(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) permissionGranted = checkPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        permissionGranted = checkPermission()
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_FINE_LOCATION] == true
        permissionGranted = granted
        if (granted) onWeatherLocationChange(true) else showDenied = true
    }
    fun requestLocation() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> onWeatherLocationChange(true)
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) == true ->
                showRationale = true
            else -> locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.weather_allow_location)) },
            text = { Text(stringResource(R.string.weather_location_explanation)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }) { Text(stringResource(R.string.weather_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text(stringResource(R.string.weather_cancel)) }
            }
        )
    }
    if (showDenied) {
        AlertDialog(
            onDismissRequest = { showDenied = false },
            title = { Text(stringResource(R.string.weather_allow_location)) },
            text = { Text(stringResource(R.string.weather_permission_denied)) },
            confirmButton = {
                TextButton(onClick = {
                    showDenied = false
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)))
                }) { Text(stringResource(R.string.weather_app_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showDenied = false }) { Text(stringResource(R.string.weather_cancel)) }
            }
        )
    }

    var volume by rememberSaveable {
        mutableFloatStateOf(50f)
    }

    var notificationsEnabled by rememberSaveable {
        mutableStateOf(true)
    }

    val languageCode = LocalConfiguration.current.locales[0].language

    var expanded by remember {
        mutableStateOf(false)
    }

    val languages = listOf(
        "en" to "English",
        "ro" to "Română",
        "de" to "Deutsch"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {


        Text(
            text = stringResource(R.string.volume_level, volume.toInt()),
            fontSize = 18.sp
        )

        Slider(
            value = volume,
            onValueChange = { volume = it },
            valueRange = 0f..100f
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.notifications),
                fontSize = 18.sp
            )

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                }
            )
        }

        HorizontalDivider()

        Text(text = stringResource(R.string.weather_location_setting), fontSize = 18.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.weather_use_current), modifier = Modifier.weight(1f))
            Switch(
                checked = useCurrentLocation,
                onCheckedChange = { enabled ->
                    if (enabled) requestLocation() else onWeatherLocationChange(false)
                }
            )
        }
        Text(
            text = stringResource(R.string.weather_location_explanation),
            style = MaterialTheme.typography.bodySmall
        )
        if (useCurrentLocation && !permissionGranted) {
            Text(stringResource(R.string.weather_permission_denied), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { requestLocation() }) {
                Text(stringResource(R.string.weather_allow_location))
            }
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.language),
            fontSize = 18.sp
        )

        Box {
            OutlinedButton(
                onClick = { expanded = true }
            ) {
                Text(languages.firstOrNull { it.first == languageCode }?.second ?: "English")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                languages.forEach { (code, language) ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            onLanguageChange(code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
