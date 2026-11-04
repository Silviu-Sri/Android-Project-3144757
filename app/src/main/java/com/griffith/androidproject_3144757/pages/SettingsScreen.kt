package com.griffith.androidproject_3144757.pages

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
fun SettingsScreen(onLanguageChange: (String) -> Unit) {

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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineMedium
        )

        HorizontalDivider()

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
