package com.griffith.androidproject_3144757

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.griffith.androidproject_3144757.ui.theme.AndroidProject3144757Theme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource

import com.griffith.androidproject_3144757.pages.PlantScreen
import com.griffith.androidproject_3144757.pages.PhotoJournalScreen
import com.griffith.androidproject_3144757.pages.SettingsScreen
import com.griffith.androidproject_3144757.pages.ShopScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidProject3144757Theme {
                MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen() {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    val titles = listOf("Plant", "Shop", "Photo Journal", "Settings")
    val icons = listOf(
        R.drawable.plant,
        R.drawable.shop,
        R.drawable.photo,
        R.drawable.settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                3 -> SettingsScreen()
            }

        }
    }
}