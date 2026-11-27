package com.griffith.androidproject_3144757.pages

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.griffith.androidproject_3144757.model.PlantState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.griffith.androidproject_3144757.R

@Composable
fun PlantScreen(
    plantState: PlantState?,
    sunsCount: Int = 50,
    plantImageResource: Int = R.drawable.plant_image_3,
    isDay: Boolean = true
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val bottomSkyRes = if (isDay) R.drawable.clear_sky_bottom else R.drawable.clear_sky_night_bottom
    val bottomSkyBitmap = remember(bottomSkyRes) {
        BitmapFactory.decodeResource(context.resources, bottomSkyRes)
    }
    val bottomSkyImageBitmap = remember(bottomSkyBitmap) {
        bottomSkyBitmap?.asImageBitmap()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = plantImageResource),
            contentDescription = stringResource(R.string.plant_picture),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .drawBehind {
                    val canvasHeight = size.height
                    val canvasWidth = size.width
                    if (bottomSkyImageBitmap != null && bottomSkyImageBitmap.height > 0) {
                        val scale = canvasHeight / bottomSkyImageBitmap.height
                        val scaledWidth = bottomSkyImageBitmap.width * scale

                        var currentX = 0f
                        while (currentX < canvasWidth) {
                            drawImage(
                                image = bottomSkyImageBitmap,
                                dstOffset = IntOffset(currentX.toInt(), 0),
                                dstSize = IntSize(scaledWidth.toInt(), canvasHeight.toInt())
                            )
                            currentX += scaledWidth
                        }
                    } else {
                        drawRect(color = Color(0xFFEDF5E6))
                    }
                    drawRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        size = size
                    )
                }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.plant_care),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = sunsCount.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFEE55)
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.leaf),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFEE55)
                            )
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(
                            stringResource(if (expanded) R.string.plant_collapse else R.string.plant_expand),
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                if (expanded) {
                    PlantStatusRow(plantState = plantState)
                }
            }
        }
    }
}

@Composable
fun PlantWeatherTitle(weather: Weather?, loading: Boolean, useCurrentLocation: Boolean, fixedLocationName: String, currentLocationName: String?, onRefreshLocation: () -> Unit) {
    val locationName = if (useCurrentLocation) currentLocationName ?: stringResource(R.string.weather_current_location) else fixedLocationName
    val currentWeather = weather
    val heading = if (!loading && currentWeather != null) {
        locationName + " · " +
            stringResource(R.string.weather_temperature, currentWeather.temperatureCelsius)
    } else {
        locationName
    }
    val conditions = when {
        loading -> stringResource(R.string.weather_loading)
        currentWeather != null ->
            stringResource(if (currentWeather.isDay) R.string.weather_day else R.string.weather_night) +
                " · " + stringResource(currentWeather.condition.label)
        else -> stringResource(if (useCurrentLocation) R.string.weather_location_unavailable else R.string.weather_unavailable)
    }

    Surface(
        onClick = onRefreshLocation,
        enabled = useCurrentLocation,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
            Text(
                text = heading,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = conditions,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlantStatusRow(plantState: PlantState?) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlantStat(
                label = stringResource(R.string.plant_moisture),
                value = plantState?.soilMoisturePercent,
                color = Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )
            PlantStat(
                label = stringResource(R.string.plant_health),
                value = plantState?.healthPercent,
                color = Color(0xFF81C784),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlantStat(
                label = stringResource(R.string.plant_happiness),
                value = plantState?.happinessPercent,
                color = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f)
            )
            PlantStat(
                label = stringResource(R.string.plant_growth),
                value = plantState?.growthProgressPercent,
                color = Color(0xFFA1887F),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlantStat(
    label: String,
    value: Float?,
    color: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.Black.copy(alpha = 0.35f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = if (value == null) stringResource(R.string.plant_stat_loading)
                    else stringResource(R.string.plant_stat_percent, value),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
            LinearProgressIndicator(
                progress = { (value ?: 0f) / 100f },
                color = color,
                trackColor = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth().height(4.dp)
            )
        }
    }
}
