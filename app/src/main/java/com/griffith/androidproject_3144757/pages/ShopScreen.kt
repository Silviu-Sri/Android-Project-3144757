package com.griffith.androidproject_3144757.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.griffith.androidproject_3144757.R

@Composable
fun ShopScreen(
    sunsCount: Int = 50,
    inventory: Map<String, Int> = emptyMap(),
    onBuyItem: ((itemId: String, cost: Int) -> Unit)? = null
) {

    val shopItems = listOf(
        Triple("trowel", R.drawable.tool_01_trowel, 20),
        Triple("scissors", R.drawable.tool_02_scissors, 10),
        Triple("watering_can", R.drawable.tool_03_watering_can, 35),
        Triple("speaker", R.drawable.tool_04_speaker, 50)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.coin_balance, sunsCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painter = painterResource(id = R.drawable.leaf),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shopItems) { (itemId, image, price) ->
                val canAfford = sunsCount >= price
                Column(
                    modifier = Modifier.clickable(enabled = canAfford && onBuyItem != null) {
                        onBuyItem?.invoke(itemId, price)
                    },
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = image),
                        contentDescription = stringResource(R.string.tool),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.CenterHorizontally)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f).align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.shop_owned, inventory[itemId] ?: 0),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.coin_price, price),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.leaf),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
