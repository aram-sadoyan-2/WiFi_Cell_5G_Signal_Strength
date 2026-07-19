package com.algorithm.wificellgsignalstrength.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.R
import com.algorithm.wificellgsignalstrength.ui.model.SignalQuality
import com.algorithm.wificellgsignalstrength.ui.theme.MutedText

@Composable
internal fun QualityRow(
    quality: SignalQuality,
    compact: Boolean = false
) {
    val textSize = if (compact) 11.sp else 14.sp
    val chipPadH = if (compact) 8.dp else 10.dp
    val chipPadV = if (compact) 3.dp else 4.dp
    val normalColor = MutedText

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        QualityItem(
            text = stringResource(R.string.quality_poor),
            selected = quality == SignalQuality.POOR,
            selectedColor = Color(0xFFF08E8E),
            textSize = textSize,
            chipPadH = chipPadH,
            chipPadV = chipPadV,
            normalColor = normalColor
        )

        Text(
            text = stringResource(R.string.middle_dot),
            color = normalColor,
            fontSize = textSize,
            modifier = Modifier.padding(horizontal = if (compact) 2.dp else 4.dp)
        )

        QualityItem(
            text = stringResource(R.string.quality_good),
            selected = quality == SignalQuality.GOOD || quality == SignalQuality.OK_ORANGE,
            selectedColor = if (quality == SignalQuality.OK_ORANGE) {
                Color(0xFFF3C15A)
            } else {
                Color(0xFFF0D93A)
            },
            textSize = textSize,
            chipPadH = chipPadH,
            chipPadV = chipPadV,
            normalColor = normalColor
        )

        Text(
            text = stringResource(R.string.middle_dot),
            color = normalColor,
            fontSize = textSize,
            modifier = Modifier.padding(horizontal = if (compact) 2.dp else 4.dp)
        )

        QualityItem(
            text = stringResource(R.string.quality_excellent),
            selected = quality == SignalQuality.EXCELLENT,
            selectedColor = Color(0xFF8EF15A),
            textSize = textSize,
            chipPadH = chipPadH,
            chipPadV = chipPadV,
            normalColor = normalColor
        )
    }
}

@Composable
private fun QualityItem(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    textSize: TextUnit,
    chipPadH: Dp,
    chipPadV: Dp,
    normalColor: Color
) {
    if (selected) {
        Box(
            modifier = Modifier
                .background(selectedColor, RoundedCornerShape(0.dp))
                .padding(horizontal = chipPadH, vertical = chipPadV),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.Black,
                fontSize = textSize,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Text(
            text = text,
            color = normalColor,
            fontSize = textSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}
