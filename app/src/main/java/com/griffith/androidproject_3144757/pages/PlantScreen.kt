package com.griffith.androidproject_3144757.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
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
