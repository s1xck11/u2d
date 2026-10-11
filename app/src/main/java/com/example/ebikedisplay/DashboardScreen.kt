package com.example.ebikedisplay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            .background(Color(0xFF0D0D0D))
    ) {
        // Синий диагональный луч (как на референсе)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A2C4A).copy(alpha = 0.7f),
                        Color(0xFF0D0D0D).copy(alpha = 0f)
                    ),
                    start = Offset(0f, size.height * 0.1f),
                    end = Offset(size.width * 0.7f, size.height * 0.6f)
                )
            )
        }

        // ВЕРХНЯЯ ПАНЕЛЬ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левые иконки статуса (фара, мигалка, телефон)
            Text(text = "💡", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "⚡", fontSize = 18.sp, color = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "📱", fontSize = 18.sp, color = Color(0xFF888888))

            Spacer(modifier = Modifier.weight(1f))

            // Центр — режим
            Text(
                text = when (state.mode) {
                    "P" -> "停车"
                    "Eco" -> "经济"
                    else -> "运动"
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Правые иконки статуса
            Text(text = "🔋", fontSize = 18.sp, color = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "📶", fontSize = 18.sp, color = Color(0xFF4CAF50))
        }

        // ЛЕВАЯ КОЛОНКА со значениями
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .width(200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DataRow(label = "单次里程", value = "%.1f km".format(state.range * 0.01f))
            DataRow(label = "单次电耗", value = "%.1f wh".format(state.consumption * 20f))
            DataRow(label = "平均电耗", value = "%.1f wh/km".format(state.consumption))
            DataRow(label = "总里程", value = "${state.range * 40} km")
        }

        // ЦЕНТР — скутер
        Image(
            painter = painterResource(id = R.drawable.scooter),
            contentDescription = "Scooter",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.6f)
                .aspectRatio(0.7f),
            contentScale = ContentScale.Fit
        )

        // СПРАВА В ЦЕНТРЕ — большая буква P
        Text(
            text = state.mode.first().toString(),
            color = Color(0xFF3A5A8C).copy(alpha = 0.7f),
            fontSize = 140.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 60.dp)
        )

        // НИЖНЯЯ ПАНЕЛЬ
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Левая часть — Range и %
            Column {
                Text(
                    text = "距离 (EST) 剩余里程",
                    color = Color(0xFF888888),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "Range",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${state.range} km",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${state.battery}%",
                        color = Color(0xFF4CAF50),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Правая часть — температура, дата, время
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "🌡", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${state.temperature}°C",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "10/11",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "18:35",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = Color(0xFF888888),
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}