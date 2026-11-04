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
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.griffith.androidproject_3144757.ui.theme.AndroidProject3144757Theme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.griffith.androidproject_3144757.R

import com.griffith.androidproject_3144757.pages.PlantScreen
import com.griffith.androidproject_3144757.pages.PhotoJournalScreen
import com.griffith.androidproject_3144757.pages.SettingsScreen
import com.griffith.androidproject_3144757.pages.ShopScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferences = getSharedPreferences("settings", MODE_PRIVATE)
            var languageTag by rememberSaveable {
                mutableStateOf(preferences.getString("language", resources.configuration.locales[0].toLanguageTag())!!)
            }
            val configuration = Configuration(LocalConfiguration.current).apply {
                setLocale(Locale.forLanguageTag(languageTag))
            }
            val localizedContext = createConfigurationContext(configuration)

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration
            ) {
                AndroidProject3144757Theme {
                    MainScreen(onLanguageChange = { language ->
                        preferences.edit().putString("language", language).apply()
                        languageTag = language
                    })
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(onLanguageChange: (String) -> Unit) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
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
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(
                    64.dp + NavigationBarDefaults.windowInsets
                        .asPaddingValues().calculateBottomPadding()
                )
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
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(title)
                        }
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
                0 -> PlantScreen()
                1 -> ShopScreen()
                2 -> PhotoJournalScreen()
                3 -> SettingsScreen(onLanguageChange)
            }
        }
    }
}