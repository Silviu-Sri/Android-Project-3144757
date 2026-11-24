package com.griffith.androidproject_3144757

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import java.util.Locale
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.griffith.androidproject_3144757.pages.Weather
import com.griffith.androidproject_3144757.pages.WeatherRepository
import com.griffith.androidproject_3144757.pages.currentWeatherLocation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.griffith.androidproject_3144757.ui.theme.AndroidProject3144757Theme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.graphics.BitmapFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.griffith.androidproject_3144757.R

import com.griffith.androidproject_3144757.pages.PlantScreen
import com.griffith.androidproject_3144757.pages.PlantWeatherTitle
import com.griffith.androidproject_3144757.model.PlantState
import com.griffith.androidproject_3144757.pages.PhotoJournalScreen
import com.griffith.androidproject_3144757.pages.SettingsScreen
import com.griffith.androidproject_3144757.pages.ShopScreen
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences = getSharedPreferences("settings", MODE_PRIVATE)
            var languageTag by rememberSaveable {
                mutableStateOf(preferences.getString("language", resources.configuration.locales[0].toLanguageTag())!!)
            }
            var languageChosen by rememberSaveable { mutableStateOf(preferences.contains("language")) }
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag(languageTag))
            }
            val localizedContext = createConfigurationContext(configuration)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalActivity provides this@MainActivity,
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                AndroidProject3144757Theme {
                    if (!languageChosen) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = {},
                            title = { Text(stringResource(R.string.choose_language)) },
                            text = {
                                Column {
                                    listOf("en" to "English", "ro" to "Română", "de" to "Deutsch").forEach { (code, label) ->
                                        androidx.compose.material3.TextButton(
                                            onClick = {
                                                preferences.edit { putString("language", code) }
                                                languageTag = code
                                                languageChosen = true
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text(label) }
                                    }
                                }
                            },
                            confirmButton = {}
                        )
                    } else {
                        MainScreen(onLanguageChange = { language ->
                            preferences.edit { putString("language", language) }
                            languageTag = language
                        })
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLanguageChange: (String) -> Unit) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    val weatherPreferences = LocalContext.current.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
    var useCurrentLocation by rememberSaveable {
        mutableStateOf(weatherPreferences.getBoolean("weather_use_current_location", false))
    }
    var fixedLocationName by remember { mutableStateOf(weatherPreferences.getString("plant_location_name", null)) }
    var locationRefresh by remember { mutableIntStateOf(0) }
    var isDay by rememberSaveable { mutableStateOf(true) }
    var weather by remember { mutableStateOf<Weather?>(null) }
    var loading by remember { mutableStateOf(true) }
    var plantState by remember { mutableStateOf<PlantState?>(null) }
    val plantImageResource = R.drawable.plant_image_3
    val coroutineScope = rememberCoroutineScope()
    val currentSuns = plantState?.suns ?: 50
    val lifecycleOwner = LocalLifecycleOwner.current
    val contextForPlant = LocalContext.current.applicationContext
    val plantRepository = remember(contextForPlant) {
        com.griffith.androidproject_3144757.model.PlantRepository(contextForPlant)
    }

    if (fixedLocationName == null) {
        com.griffith.androidproject_3144757.pages.PlantLocationDialog { place ->
            weatherPreferences.edit {
                putString("plant_location_name", place.name)
                putString("plant_location_latitude", place.latitude.toString())
                putString("plant_location_longitude", place.longitude.toString())
            }
            fixedLocationName = place.name
            locationRefresh++
        }
    }

    LaunchedEffect(lifecycleOwner, useCurrentLocation, locationRefresh, fixedLocationName) {
        weather = null
        loading = true
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            plantState = plantRepository.read()
            while (true) {
                loading = true
                try {
                    val result = if (useCurrentLocation) {
                        val location = currentWeatherLocation(contextForPlant)
                            ?: throw java.io.IOException("Location unavailable")
                        WeatherRepository.fetch(location.latitude, location.longitude)
                    } else {
                        val latitude = weatherPreferences.getString("plant_location_latitude", null)?.toDoubleOrNull()
                            ?: throw java.io.IOException("Plant location not selected")
                        val longitude = weatherPreferences.getString("plant_location_longitude", null)?.toDoubleOrNull()
                            ?: throw java.io.IOException("Plant location not selected")
                        WeatherRepository.fetch(latitude, longitude)
                    }
                    weather = result
                    isDay = result.isDay
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    android.util.Log.e("Weather", "Unable to obtain location or fetch weather", error)
                    weather = null
                } finally {
                    loading = false
                }
                try {
                    plantState = plantRepository.update(weather)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: java.io.IOException) {
                    android.util.Log.e("PlantProgress", "Unable to save plant progress", error)
                }
                delay(10.minutes)
            }
        }
    }

    val context = LocalContext.current
    val dirtBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.bottom_nav_dirt)
    }
    val dirtImageBitmap = remember(dirtBitmap) {
        dirtBitmap.asImageBitmap()
    }

    val skyBitmaps = remember(isDay) {
        val skyResources = if (isDay) {
            listOf(R.drawable.clear_sky_1, R.drawable.clear_sky_2, R.drawable.clear_sky_3)
        } else {
            listOf(
                R.drawable._clear_sky_night_1,
                R.drawable._clear_sky_night_2,
                R.drawable._clear_sky_night_3
            )
        }
        skyResources.map { BitmapFactory.decodeResource(context.resources, it).asImageBitmap() }
    }

    val titles = listOf(
        stringResource(R.string.plant),
        stringResource(R.string.shop),
        stringResource(R.string.photo_journal),
        stringResource(R.string.settings)
    )
    val icons = listOf(
        R.drawable.plant,
        R.drawable.shop,
        R.drawable.photo,
        R.drawable.settings
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val canvasHeight = size.height
                        val canvasWidth = size.width
                        var currentX = 0f
                        var index = 0
                        while (currentX < canvasWidth && skyBitmaps.isNotEmpty()) {
                            val bmp = skyBitmaps[index % skyBitmaps.size]
                            if (bmp.height > 0) {
                                val scale = canvasHeight / bmp.height
                                val scaledWidth = bmp.width * scale
                                drawImage(
                                    image = bmp,
                                    dstOffset = IntOffset(currentX.toInt(), 0),
                                    dstSize = IntSize(scaledWidth.toInt(), canvasHeight.toInt())
                                )
                                currentX += scaledWidth
                            }
                            index++
                        }
                    }
            ) {
                TopAppBar(
                    title = {
                        if (selectedTab == 0) {
                            PlantWeatherTitle(weather = weather, loading = loading, useCurrentLocation = useCurrentLocation, fixedLocationName = fixedLocationName.orEmpty())
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(
                        64.dp + NavigationBarDefaults.windowInsets
                            .asPaddingValues().calculateBottomPadding()
                    )
                    .drawBehind {
                        val canvasHeight = size.height
                        val canvasWidth = size.width
                        if (dirtBitmap.height > 0) {
                            val scale = canvasHeight / dirtBitmap.height
                            val scaledWidth = dirtBitmap.width * scale

                            var currentX = 0f
                            while (currentX < canvasWidth) {
                                drawImage(
                                    image = dirtImageBitmap,
                                    dstOffset = IntOffset(currentX.toInt(), 0),
                                    dstSize = IntSize(scaledWidth.toInt(), canvasHeight.toInt())
                                )
                                currentX += scaledWidth
                            }
                        }
                        drawRect(
                            color = Color.Black.copy(alpha = 0.4f),
                            size = size
                        )
                    },
                containerColor = Color.Transparent
            ) {
                titles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        icon = {
                            Icon(
                                painter = painterResource(icons[index]),
                                contentDescription = title,
                                tint = if (selectedTab == index) Color(0xFFFFEE55) else Color.White
                            )
                        },
                        label = {
                            Text(title, color = if (selectedTab == index) Color(0xFFFFEE55) else Color.White)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> PlantScreen(plantState = plantState, sunsCount = currentSuns, plantImageResource = plantImageResource)
                1 -> ShopScreen(
                    sunsCount = currentSuns,
                    inventory = plantState?.inventory ?: emptyMap(),
                    onBuyItem = if (plantState == null) null else { itemId, cost ->
                        coroutineScope.launch {
                            try {
                                plantState = plantRepository.buyItem(itemId, cost)
                            } catch (error: java.io.IOException) {
                                android.util.Log.e("PlantProgress", "Unable to save purchase", error)
                            }
                        }
                    }
                )
                2 -> PhotoJournalScreen(plantImageResource = plantImageResource)
                3 -> SettingsScreen(
                    onLanguageChange = onLanguageChange,
                    useCurrentLocation = useCurrentLocation,
                    onWeatherLocationChange = { enabled ->
                        weatherPreferences.edit { putBoolean("weather_use_current_location", enabled) }
                        useCurrentLocation = enabled
                        locationRefresh++
                    }
                )
            }
        }
    }
}