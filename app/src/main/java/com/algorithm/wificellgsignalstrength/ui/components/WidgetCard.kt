package com.algorithm.wificellgsignalstrength.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.algorithm.wificellgsignalstrength.R

@Composable
internal fun WidgetCard(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    showManualSteps: Boolean,
    isAlreadyAdded: Boolean,
    onRemoveHelpClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Widgets,
                    contentDescription = null,
                    tint = Color(0xFF2C62F4),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = title,
                    color = Color(0xFF111111),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                color = Color(0xFF60656D),
                fontSize = 14.sp
            )

            if (!isAlreadyAdded) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C62F4)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.already_added),
                    color = Color(0xFF1E8E3E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onRemoveHelpClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAEAEA)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.how_to_remove),
                        color = Color(0xFF111111)
                    )
                }
            }

            if (showManualSteps && !isAlreadyAdded) {
                Text(
                    text = stringResource(R.string.manual_way),
                    color = Color(0xFF111111),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.add_widget_step_1),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_2),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_3),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_4),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.add_widget_step_5_drag),
                    color = Color(0xFF60656D),
                    fontSize = 14.sp
                )
            }
        }
    }
}
