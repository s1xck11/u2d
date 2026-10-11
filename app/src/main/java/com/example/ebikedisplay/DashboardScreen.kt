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

private const val PARKING_THRESHOLD = 2f

@Composable
fun DashboardScreen(
    state: DashboardState,
    onConnectClick: () -> Unit
) {
    val isParked = state.speed < PARKING_THRESHOLD

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
        TopBar(
            mode = state.mode,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // ЛЕВАЯ КОЛОНКА (между TopBar и BottomBar)
        LeftColumn(
            state = state,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 80.dp, bottom = 100.dp)
        )

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

        // БУКВА P (исчезает при движении) — ближе к скутеру
        Text(
            text = state.mode.first().toString(),
            color = Color(0xFF2E4874).copy(alpha = pAlpha),
            fontSize = 140.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 20.dp, y = (-10).dp)
        )

        // СПИДОМЕТР (появляется при движении)
        if (speedAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(0.85f)
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

        // НИЖНЯЯ ПАНЕЛЬ (только Range, %, температура, дата, время)
        BottomBar(
            state = state,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun TopBar(mode: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
fun LeftColumn(state: DashboardState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(start = 28.dp)
            .width(200.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Только 3 строки — без дубля "Общий пробег" с нижней панелью
        DataRow(
            Icons.Filled.Straighten,
            "ПРОБЕГ ЗА ПОЕЗДКУ",
            "0,0 km"
        )
        DataRow(
            Icons.Filled.Bolt,
            "РАСХОД ЗА ПОЕЗДКУ",
            "0,0 wh"
        )
        DataRow(
            Icons.Filled.ShowChart,
            "СРЕДНИЙ РАСХОД",
            "%.1f wh/km".format(state.consumption)
        )
    }
}

@Composable
fun BottomBar(state: DashboardState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Слева — Range и %
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Range", color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "${state.range} km",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "${state.battery}%",
                color = Color(0xFF4CAF50),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Справа — температура, дата, время
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

        drawArc(
            color = Color(0xFF1A1A22),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        val activeColor = when {
            progress < 30f / 80f -> Color.White
            progress < 45f / 80f -> Color(0xFFFFD54F)
            progress < 60f / 80f -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }

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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}