package com.example.ebikedisplay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    val speedFontSize = (100 + (state.speed / 80f) * 40).sp

    val speedColor = when {
        state.speed < 25f -> Color(0xFF4CAF50)
        state.speed < 40f -> Color(0xFFFFD54F)
        state.speed < 60f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080C))
    ) {
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
            speedMode = state.speedMode,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // BLE-ИНДИКАТОР
        BleStatusPill(
            status = state.bleStatus,
            connected = state.bleConnected,
            onClick = onConnectClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 28.dp)
        )

        // ЛЕВАЯ КОЛОНКА
        LeftColumn(
            state = state,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 80.dp, bottom = 100.dp)
        )

        // СКУТЕР
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

        // БУКВА P С ТЕНЬЮ
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 20.dp, y = (-10).dp)
        ) {
            Text(
                text = "P",
                color = Color.Black.copy(alpha = pAlpha * 0.5f),
                fontSize = 140.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = 6.dp, y = 6.dp)
            )
            Text(
                text = "P",
                color = Color(0xFF2E4874).copy(alpha = pAlpha),
                fontSize = 140.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // СКОРОСТЬ
        if (speedAlpha > 0.01f) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(speedAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.speed.toInt().toString(),
                    color = speedColor,
                    fontSize = speedFontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "km/h",
                    color = Color(0xFF00E5FF),
                    fontSize = 24.sp
                )
            }
        }

        // ЛЕВЫЙ ПОВОРОТНИК
        TurnSignalArrow(
            active = state.leftTurn,
            pointsRight = false,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )

        // ПРАВЫЙ ПОВОРОТНИК
        TurnSignalArrow(
            active = state.rightTurn,
            pointsRight = true,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )

        // НИЖНЯЯ ПАНЕЛЬ
        BottomBar(
            state = state,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
fun TurnSignalArrow(
    active: Boolean,
    pointsRight: Boolean,
    modifier: Modifier = Modifier
) {
    // Мигание: 500 мс вкл, 500 мс выкл
    val infiniteTransition = rememberInfiniteTransition(label = "turnBlink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    val alpha = if (active) blinkAlpha else 0f
    val color = Color(0xFFFF9800)

    Canvas(
        modifier = modifier
            .size(80.dp)
            .alpha(alpha)
    ) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            if (pointsRight) {
                moveTo(w * 0.2f, h * 0.2f)
                lineTo(w * 0.8f, h * 0.5f)
                lineTo(w * 0.2f, h * 0.8f)
            } else {
                moveTo(w * 0.8f, h * 0.2f)
                lineTo(w * 0.2f, h * 0.5f)
                lineTo(w * 0.8f, h * 0.8f)
            }
            close()
        }

        drawPath(path = path, color = color)
    }
}

@Composable
fun BleStatusPill(
    status: String,
    connected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (connected) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (connected) Icons.Filled.BluetoothConnected else Icons.Filled.BluetoothDisabled

    Row(
        modifier = modifier
            .background(Color(0xFF1A1A1A).copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = status,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TopBar(speedMode: String, modifier: Modifier = Modifier) {
    val (modeText, modeColor) = when (speedMode) {
        "1" -> "Eco" to Color(0xFF4CAF50)
        "2" -> "City" to Color(0xFFFFC107)
        "3" -> "Sport" to Color(0xFFFF5722)
        else -> speedMode to Color.White
    }

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
            text = modeText,
            color = modeColor,
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
        DataRow(Icons.Filled.Straighten, "ПРОБЕГ ЗА ПОЕЗДКУ", "0,0 km")
        DataRow(Icons.Filled.Bolt, "РАСХОД ЗА ПОЕЗДКУ", "0,0 wh")
        DataRow(Icons.Filled.ShowChart, "СРЕДНИЙ РАСХОД", "%.1f wh/km".format(state.consumption))
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