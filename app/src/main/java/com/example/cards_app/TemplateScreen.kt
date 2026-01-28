package com.example.cards_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

class TemplateScreen {

    @Composable
    fun Template(
        modifier: Modifier = Modifier,
        onTemplateClick: (template: TemplateCard) -> Unit = {},
        onCustomClick: () -> Unit = {}
    ) {
        val templates = Templates.list
        LazyVerticalStaggeredGrid(
            modifier = modifier,
            columns = StaggeredGridCells.Fixed(2)
        ) {
            items(templates.size) {
                val template = templates[it]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable(
                            onClick = {
                                onTemplateClick(template)
                            }
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(template.color.toColorInt())
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .heightIn(max = 200.dp, min = 50.dp)
                    ) {
                        Image(
                            painter = painterResource(id = template.logoResId),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.7f),
                                            Color.Black.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                                .matchParentSize()
                        ) {
                            Column {
                                val blackOrWhite = Color.White
                                Text(
                                    text = template.nameOfCard,
                                    modifier = Modifier.padding(8.dp),
                                    color = blackOrWhite
                                )
                            }
                        }
                    }
                }
            }
            item(
                span = StaggeredGridItemSpan.FullLine
            ) {
                Card(
                    modifier = Modifier
                        .padding(7.dp)
                        .clickable(
                            onClick = {
                                onCustomClick()
                            }
                        )
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom card",
                            modifier = Modifier
                                .padding(8.dp)
                                .weight(1f),
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.outline_add_card_24),
                            contentDescription = "Add card",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}