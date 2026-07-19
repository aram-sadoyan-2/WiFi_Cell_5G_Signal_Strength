package com.algorithm.wificellgsignalstrength.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun WidgetSettingsScreen(
    onAddWidgetClick: () -> Unit,
    onUpdateWidgetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Widgets",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF111111)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Speed Test Widget",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111111)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Add the top-right speed panel as a home screen widget and update its values from the app.",
                    color = Color(0xFF60656D)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onAddWidgetClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add widget to home screen")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onUpdateWidgetClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update widget values")
                }
            }
        }
    }
}
