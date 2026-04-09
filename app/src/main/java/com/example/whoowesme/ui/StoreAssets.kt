package com.example.whoowesme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * GOOGLE PLAY STORE FEATURE GRAPHIC GENERATOR
 * Do not call this function in your actual app code.
 * This is designed strictly to be viewed in the Android Studio Preview pane
 * so you can right-click and save it as a perfectly sized 1024x500 banner!
 */
@Preview(widthDp = 1024, heightDp = 500, showBackground = true)
@Composable
fun StoreFeatureGraphic() {
    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F172A), // Dark Slate
            Color(0xFF1E293B)  // Lighter Slate
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Decorative background subtle circles
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = (-150).dp, y = (-150).dp)
                .clip(CircleShape)
                .background(Color(0xFF0D9488).copy(alpha = 0.08f)) // Emerald tint
        )
        Box(
            modifier = Modifier
                .size(700.dp)
                .offset(x = 600.dp, y = 100.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B).copy(alpha = 0.04f)) // Gold tint
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Content (Left Side)
            Column(
                modifier = Modifier.weight(1.1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Who Owes Me",
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Track debts offline.\nSettle up securely.",
                    color = Color(0xFF94A3B8), // Slate 400
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 44.sp
                )
                Spacer(modifier = Modifier.height(48.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981), // Emerald 500
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "100% On-Device Privacy",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Graphic Content (Right Side)
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // Mock Card 1 (Back/Blurred)
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(180.dp)
                        .offset(x = 40.dp, y = (-70).dp)
                        .rotate(6f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Column {
                            Box(modifier = Modifier.width(140.dp).height(24.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)))
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(modifier = Modifier.width(90.dp).height(16.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)))
                        }
                    }
                }

                // Mock Card 2 (Front/Focus)
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(200.dp)
                        .offset(x = (-20).dp, y = 30.dp)
                        .rotate(-4f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E)), // Teal 700
                    elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Sarah Owes You", color = Color.White.copy(alpha = 0.8f), fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$145.00", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                        }
                    }
                }
            }
        }
    }
}
