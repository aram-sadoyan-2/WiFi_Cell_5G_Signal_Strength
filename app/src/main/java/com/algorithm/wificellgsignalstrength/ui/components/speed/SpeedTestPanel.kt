package com.algorithm.wificellgsignalstrength.ui.components.speed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.algorithm.wificellgsignalstrength.ui.model.SpeedCircleState
import com.algorithm.wificellgsignalstrength.ui.theme.MutedText

@Composable
internal fun SpeedTestPanel(
    state: SpeedCircleState,
    onGoClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    if (state is SpeedCircleState.FinalResult) {
        SpeedTestResultPanel(
            state = state,
            onCloseClick = onCloseClick,
            modifier = modifier,
            compact = compact
        )
        return
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.speedtest),
            color = MutedText,
            fontSize = if (compact) 15.sp else 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        SpeedCircle(
            state = state,
            compact = compact,
            onGoClick = onGoClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
    }
}
