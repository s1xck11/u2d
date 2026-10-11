package com.example.ebikedisplay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    state: DashboardState,
    onConnectClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080C))
    ) {
        // Синий диагональный луч из верхнего левого угла
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1B3358).copy(alpha = 0.85f),
                        Color(0xFF0A1424).copy(alpha = 0.3f),
                        Color(0x00000000)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width * 0.75f, size.height * 1.0f)
                )
            )
        }

        // ВЕРХНЯЯ ПАНЕЛЬ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lightbulb,
                contentDescription = null,
                tint = Color(0xFF4A78C0),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = Color(0xFF4A78C0),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Icon(
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = Color(0xFF4A78C0),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = when (state.mode) {
                    "P" -> "P"
                    "Eco" -> "ECO"
                    else -> "SPORT"
                },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Filled.BatteryFull,
                contentDescription = null,
                tint = Color(0xFF4A78C0),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Icon(
                imageVector = Icons.Filled.SignalCellularAlt,
                contentDescription = null,
                tint = Color(0xFF4A78C0),
                modifier = Modifier.size(22.dp)
            )
        }

        // ЛЕВАЯ КОЛОНКА
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp)
                .width(220.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DataRow(
                icon = Icons.Filled.Straighten,
                label = "ПРОБЕГ ЗА ПОЕЗДКУ",
                value = "0,0 km"
            )
            DataRow(
                icon = Icons.Filled.Bolt,
                label = "РАСХОД ЗА ПОЕЗДКУ",
                value = "0,0 wh"
            )
            DataRow(
                icon = Icons.Filled.ShowChart,
                label = "СРЕДНИЙ РАСХОД",
                value = "0,0 wh/km"
            )
            DataRow(
                icon = Icons.Filled.Route,
                label = "ОБЩИЙ ПРОБЕГ",
                value = "0 km"
            )
        }

        // ЦЕНТР — СКУТЕР (большой)
        Image(
            painter = painterResource(id = R.drawable.scooter),
            contentDescription = "Scooter",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-40).dp)
                .fillMaxHeight(0.85f)
                .aspectRatio(0.8f),
            contentScale = ContentScale.Fit
        )

        // БОЛЬШАЯ БУКВА P (за скутером, правее центра)
        Text(
            text = state.mode.first().toString(),
            color = Color(0xFF2E4874).copy(alpha = 0.85f),
            fontSize = 180.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 180.dp, y = (-10).dp)
        )

        // НИЖНЯЯ ПАНЕЛЬ
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Navigation,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ЗАПАС ХОДА (ОЦЕНКА)",
                        color = Color(0xFF888888),
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Range",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "0 km",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "0%",
                        color = Color(0xFF4CAF50),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.Bottom) {
                Icon(
                    imageVector = Icons.Filled.Thermostat,
                    contentDescription = null,
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "0°C",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "11.10",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "18:35",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun DataRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4A78C0),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = Color(0xFF6A6A6A),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}