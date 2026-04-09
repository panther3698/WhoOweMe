package com.example.whoowesme.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whoowesme.R

@Preview(widthDp = 1024, heightDp = 500)
@Composable
fun PlayStoreFeatureGraphic() {
    // Premium Emerald Gradient Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00332A), Color(0xFF004D40))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-Resolution Icon Glow
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(160.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name with Premium Typography
            Text(
                text = "Who Owes Me",
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2).sp
            )

            // Value Proposition
            Text(
                text = "Premium Debt Management • Offline • Secure",
                color = Color(0xFFB0BEC5),
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }
        
        // Subtle Decorative Accent (Bottom Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .size(300.dp)
                .background(Color(0x0AFFFFFF), CircleShape)
        )
    }
}

@Preview(widthDp = 512, heightDp = 512)
@Composable
fun PlayStoreAppIcon() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF004D40)), // Our Premium Emerald
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(380.dp), // Properly scaled for 512px
            contentScale = ContentScale.Fit
        )
    }
}
