package com.griffith.androidproject_3144757.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.griffith.androidproject_3144757.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class PlantLocation(val name: String, val latitude: Double, val longitude: Double)

private suspend fun searchPlaces(query: String): List<PlantLocation> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(query, "UTF-8")
    val connection = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=10&language=en&format=json").openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        if (connection.responseCode != 200) throw java.io.IOException("Place search failed")
        val results = JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).optJSONArray("results")
        if (results == null) emptyList() else (0 until results.length()).map { index ->
            val item = results.getJSONObject(index)
            val label = listOf(item.optString("name"), item.optString("admin1"), item.optString("country"))
                .filter { it.isNotBlank() }.distinct().joinToString(", ")
            PlantLocation(label, item.getDouble("latitude"), item.getDouble("longitude"))
        }
    } finally {
        connection.disconnect()
    }
}

@Composable
fun PlantLocationDialog(onSelected: (PlantLocation) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<PlantLocation>>(emptyList()) }
    var searched by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.plant_location_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.plant_location_description))
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text(stringResource(R.string.plant_location_search)) }, singleLine = true)
                Button(enabled = query.trim().length >= 2 && !loading, onClick = {
                    val search = query.trim()
                    loading = true
                    failed = false
                    scope.launch {
                        try {
                            results = searchPlaces(search)
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            failed = true
                            results = emptyList()
                        } finally {
                            loading = false
                            searched = true
                        }
                    }
                }) { Text(stringResource(R.string.plant_location_search)) }
                if (loading) CircularProgressIndicator()
                else if (failed) Text(stringResource(R.string.plant_location_error))
                else if (searched && results.isEmpty()) Text(stringResource(R.string.plant_location_empty))
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(results) { place ->
                        TextButton(onClick = { onSelected(place) }, modifier = Modifier.fillMaxWidth()) { Text(place.name) }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
