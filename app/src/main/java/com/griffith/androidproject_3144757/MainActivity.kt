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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLanguageChange: (String) -> Unit) {
    var selectedTab by rememberSaveable {
        mutableIntStateOf(0)
    }

    val context = LocalContext.current
    val dirtBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.bottom_nav_dirt)
    }
    val dirtImageBitmap = remember(dirtBitmap) {
        dirtBitmap.asImageBitmap()
    }

    val skyBitmaps = remember {
        listOf(
            BitmapFactory.decodeResource(context.resources, R.drawable.sky_1),
            BitmapFactory.decodeResource(context.resources, R.drawable.sky_2),
            BitmapFactory.decodeResource(context.resources, R.drawable.sky_3)
        ).map { it.asImageBitmap() }
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
                    title = { },
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
                0 -> PlantScreen()
                1 -> ShopScreen()
                2 -> PhotoJournalScreen()
                3 -> SettingsScreen(onLanguageChange)
            }
        }
    }
}