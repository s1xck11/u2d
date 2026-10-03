package com.example.ebikedisplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DashboardScreen()
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Верхняя панель со статусом
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatusPill("MODE: P", Color(0xFFFFC107))
            StatusPill("OK", Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Большая цифра скорости
        Text(
            text = "0",
            color = Color.White,
            fontSize = 120.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "km/h",
            color = Color(0xFF00E5FF),
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Плитки данных
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoTile("VOLT", "0.0V", Modifier.weight(1f))
            InfoTile("BAT", "0%", Modifier.weight(1f))
            InfoTile("RANGE", "0 km", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoTile("TEMP", "0°C", Modifier.weight(1f))
            InfoTile("POWER", "0 W", Modifier.weight(1f))
            InfoTile("Wh/km", "0.0", Modifier.weight(1f))
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 8.dp)
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