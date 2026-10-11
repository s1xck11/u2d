package com.example.ebikedisplay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

// Скорость, при которой P превращается в спидометр
private const val PARKING_THRESHOLD = 2f

@Composable
fun DashboardScreen(
    state: DashboardState,
    onConnectClick: () -> Unit
) {
    val isParked = state.speed < PARKING_THRESHOLD

    // Плавная анимация прозрачности скутера и P
    val scooterAlpha by animateFloatAsState(
        targetValue = if (isParked) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "scooterAlpha"
    )
    val pAlpha by animateFloatAsState(
        targetValue = if (isParked) 0.85f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "pAlpha"
    )
    val speedAlpha by animateFloatAsState(
        targetValue = if (isParked) 0f else 1f,
        animationSpec = tween(durationMillis = 800),
        label = "speedAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080C))
    ) {
        // Синий диагональный луч
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
        TopBar(mode = state.mode)

        // ЛЕВАЯ КОЛОНКА
        LeftColumn(state = state)

        // СКУТЕР (исчезает при движении)
        Image(
            painter = painterResource(id = R.drawable.scooter),
            contentDescription = "Scooter",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-60).dp)
                .fillMaxHeight(0.85f)
                .aspectRatio(0.8f)
                .alpha(scooterAlpha),
            contentScale = ContentScale.Fit
        )

        // БУКВА P (исчезает при движении)
        Text(
            text = state.mode.first().toString(),
            color = Color(0xFF2E4874).copy(alpha = pAlpha),
            fontSize = 180.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 60.dp, y = (-10).dp)
        )

        // СПИДОМЕТР (появляется при движении)
        if (speedAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(0.9f)
                    .aspectRatio(1f)
                    .alpha(speedAlpha),
                contentAlignment = Alignment.Center
            ) {
                SpeedometerGauge(speed = state.speed, maxSpeed = 80f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.speed.toInt().toString(),
                        color = Color.White,
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "km/h",
                        color = Color(0xFF00E5FF),
                        fontSize = 22.sp
                    )
                }
            }
        }

        // НИЖНЯЯ ПАНЕЛЬ
        BottomBar(state = state)
    }
}

@Composable
fun TopBar(mode: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Lightbulb, null, tint = Color(0xFF4A78C0), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Icon(Icons.Filled.Bolt, null, tint = Color(0xFF4A78C0), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Icon(Icons.Filled.PhoneAndroid, null, tint = Color(0xFF4A78C0), modifier = Modifier.size(22.dp))

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = when (mode) {
                "P" -> "P"
                "Eco" -> "ECO"
                else -> "SPORT"
            },
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(Icons.Filled.BatteryFull, null, tint = Color(0xFF4A78C0), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Icon(Icons.Filled.SignalCellularAlt, null, tint = Color(0xFF4A78C0), modifier = Modifier.size(22.dp))
    }
}

@Composable
fun LeftColumn(state: DashboardState) {
    Column(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 28.dp)
            .width(220.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DataRow(Icons.Filled.Straighten, "ПРОБЕГ ЗА ПОЕЗДКУ", "0,0 km")
        DataRow(Icons.Filled.Bolt, "РАСХОД ЗА ПОЕЗДКУ", "0,0 wh")
        DataRow(Icons.Filled.ShowChart, "СРЕДНИЙ РАСХОД", "%.1f wh/km".format(state.consumption))
        DataRow(Icons.Filled.Route, "ОБЩИЙ ПРОБЕГ", "${state.range * 40} km")
    }
}

@Composable
fun BottomBar(state: DashboardState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 18.dp)
            .align(Alignment.BottomStart),
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Navigation, null, tint = Color(0xFF888888), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("ЗАПАС ХОДА (ОЦЕНКА)", color = Color(0xFF888888), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Range", color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("${state.range} km", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(16.dp))
                Text("${state.battery}%", color = Color(0xFF4CAF50), fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.Bottom) {
            Icon(Icons.Filled.Thermostat, null, tint = Color(0xFF888888), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("${state.temperature}°C", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Text("11.10", color = Color.White, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(20.dp))
            Text("18:35", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun SpeedometerGauge(speed: Float, maxSpeed: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 20.dp.toPx()
        val diameter = min(size.width, size.height) - stroke
        val radius = diameter / 2
        val center = Offset(size.width / 2, size.height / 2)
        val startAngle = 135f
        val totalSweep = 270f
        val progress = (speed / maxSpeed).coerceIn(0f, 1f)
        val sweep = totalSweep * progress

        // Фоновая дуга
        drawArc(
            color = Color(0xFF1A1A22),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Цвет активной дуги: белый → жёлтый → красный
        val activeColor = when {
            progress < 30f / 80f -> Color.White                          // до 30 км/ч
            progress < 45f / 80f -> Color(0xFFFFD54F)                    // 30–45 км/ч (жёлтый)
            progress < 60f / 80f -> Color(0xFFFF9800)                    // 45–60 км/ч (оранжевый)
            else -> Color(0xFFF44336)                                    // 60+ км/ч (красный)
        }

        // Градиент от белого к текущему цвету — для плавности
        drawArc(
            brush = Brush.sweepGradient(
                0f to Color.White,
                progress to activeColor,
                1f to activeColor
            ),
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
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