package com.griffith.androidproject_3144757.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.griffith.androidproject_3144757.R

@Composable
fun PlantScreen(plantState: PlantState?, sunsCount: Int = 50) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.plant_image_3),
            contentDescription = stringResource(R.string.plant_picture),
            modifier = Modifier.fillMaxSize().background(Color(0xFF87CEFA)),
            contentScale = ContentScale.Fit
        )
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Color(0xFFEDF5E6)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(stringResource(R.string.plant_care), style = MaterialTheme.typography.titleSmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = sunsCount.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.sun),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(stringResource(if (expanded) R.string.plant_collapse else R.string.plant_expand))
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            contentDescription = null
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
fun PlantWeatherTitle(weather: Weather?, loading: Boolean, useCurrentLocation: Boolean) {
    val locationName = stringResource(if (useCurrentLocation) R.string.weather_current_location else R.string.weather_location)
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
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlantStat(
                label = stringResource(R.string.plant_moisture),
                value = plantState?.soilMoisturePercent,
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            PlantStat(
                label = stringResource(R.string.plant_health),
                value = plantState?.healthPercent,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlantStat(
                label = stringResource(R.string.plant_happiness),
                value = plantState?.happinessPercent,
                color = Color(0xFFD16937),
                modifier = Modifier.weight(1f)
            )
            PlantStat(
                label = stringResource(R.string.plant_growth),
                value = plantState?.growthProgressPercent,
                color = Color(0xFF795548),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlantStat(label: String, value: Float?, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFEDF5E6),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = if (value == null) stringResource(R.string.plant_stat_loading)
                    else stringResource(R.string.plant_stat_percent, value),
                style = MaterialTheme.typography.labelLarge
            )
            LinearProgressIndicator(
                progress = { (value ?: 0f) / 100f },
                color = color,
                modifier = Modifier.fillMaxWidth().height(4.dp)
            )
        }
    }
}
