package com.griffith.androidproject_3144757.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {

    var volume by rememberSaveable {
        mutableFloatStateOf(50f)
    }

    var notificationsEnabled by rememberSaveable {
        mutableStateOf(true)
    }

    var selectedLanguage by rememberSaveable {
        mutableStateOf("English")
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    val languages = listOf(
        "English",
        "German",
        "Romanian"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        HorizontalDivider()

        Text(
            text = "Volume: ${volume.toInt()}%",
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
                text = "Notifications",
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
            text = "Language",
            fontSize = 18.sp
        )

        Box {
            OutlinedButton(
                onClick = { expanded = true }
            ) {
                Text(selectedLanguage)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language) },
                        onClick = {
                            selectedLanguage = language
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
