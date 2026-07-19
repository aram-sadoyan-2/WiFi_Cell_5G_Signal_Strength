package com.algorithm.wificellgsignalstrength.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.R

@Composable
internal fun SpeedCenterMetric(
    label: String,
    value: String,
    unit: String,
    pingMs: Int,
    compact: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = MutedText,
            fontSize = if (compact) 10.sp else 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = DarkText,
            fontSize = if (compact) 30.sp else 42.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = unit,
            color = MutedText,
            fontSize = if (compact) 12.sp else 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.ping_ms_value, pingMs),
            color = DarkText,
            fontSize = if (compact) 13.sp else 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
