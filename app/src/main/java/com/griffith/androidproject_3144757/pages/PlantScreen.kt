package com.griffith.androidproject_3144757.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
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
fun PlantScreen() {
    Image(
        painter = painterResource(id = R.drawable.plant_image_3),
        contentDescription = stringResource(R.string.plant_picture),
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun PlantWeatherTitle(weather: Weather?, loading: Boolean) {
    val currentWeather = weather
    val heading = if (!loading && currentWeather != null) {
        stringResource(R.string.weather_location) + " · " +
            stringResource(R.string.weather_temperature, currentWeather.temperatureCelsius)
    } else {
        stringResource(R.string.weather_location)
    }
    val conditions = when {
        loading -> stringResource(R.string.weather_loading)
        currentWeather != null ->
            stringResource(if (currentWeather.isDay) R.string.weather_day else R.string.weather_night) +
                " · " + stringResource(currentWeather.condition.label)
        else -> stringResource(R.string.weather_unavailable)
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
        PlantStat(
            label = stringResource(R.string.plant_growth),
            value = plantState?.growthProgressPercent,
            color = Color(0xFF795548),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlantStat(label: String, value: Float?, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
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
