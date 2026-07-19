package com.algorithm.wificellgsignalstrength.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
internal fun InfoLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = stringResource(R.string.label_with_space, label),
            color = DarkText,
            fontSize = 17.sp
        )

        Text(
            text = value,
            color = DarkText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
