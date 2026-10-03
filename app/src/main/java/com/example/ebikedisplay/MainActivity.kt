package com.example.ebikedisplay

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.min

// ТЕСТОВЫЕ ДАННЫЕ — потом заменим на реальные из BLE
private val testSpeed = 45f
private val testVoltage = 58.4f
private val testBattery = 78
private val testRange = 54
private val testTemp = 32
private val testPower = 850
private val testConsumption = 18.5f
private val testMode = "Sport"
private val testError = 0

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme {
                DashboardScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ЛЕВАЯ ЧАСТЬ: круглый спидометр
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                SpeedometerGauge(speed = testSpeed, maxSpeed = 80f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = testSpeed.toInt().toString(),
                        color = Color.White,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "km/h",
                        color = Color(0xFF00E5FF),
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // ПРАВАЯ ЧАСТЬ
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            // Режим и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val modeColor = when (testMode) {
                    "P" -> Color(0xFFFFC107)
                    "Eco" -> Color(0xFF4CAF50)
                    else -> Color(0xFFFF5722)
                }
                StatusPill("MODE: $testMode", modeColor, Modifier.weight(1f))

                val errColor = if (testError == 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                val errText = if (testError == 0) "OK" else "ERR $testError"
                StatusPill(errText, errColor, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Плитки данных
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoTile("VOLT", "%.1fV".format(testVoltage), Modifier.weight(1f))
                InfoTile("BAT", "$testBattery%", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoTile("RANGE", "$testRange km", Modifier.weight(1f))
                InfoTile("TEMP", "$testTemp°C", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoTile("POWER", "$testPower W", Modifier.weight(1f))
                InfoTile("Wh/km", "%.1f".format(testConsumption), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SpeedometerGauge(speed: Float, maxSpeed: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 16.dp.toPx()
        val diameter = min(size.width, size.height) - stroke
        val radius = diameter / 2
        val center = Offset(size.width / 2, size.height / 2)
        val startAngle = 135f
        val totalSweep = 270f
        val progress = (speed / maxSpeed).coerceIn(0f, 1f)
        val sweep = totalSweep * progress

        // Фоновая дуга (серая)
        drawArc(
            color = Color(0xFF2A2A2A),
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(diameter, diameter),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // Активная дуга
        val activeColor = when {
            progress < 0.5f -> Color(0xFF4CAF50)
            progress < 0.8f -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
        drawArc(
            color = activeColor,
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
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = Color(0xFF888888), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}