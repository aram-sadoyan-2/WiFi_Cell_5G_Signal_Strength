package com.algorithm.wificell5gsignalstrength.widget

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.algorithm.wificell5gsignalstrength.MainActivity
import com.algorithm.wificell5gsignalstrength.R
import java.util.Locale

class SimInfoWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val simInfo = getSimInfo(context)

        val cardBg = ColorProvider(Color(ContextCompat.getColor(context, R.color.widget_card_bg)))
        val whiteText = ColorProvider(Color(ContextCompat.getColor(context, R.color.widget_text_primary)))
        val subText = ColorProvider(Color(ContextCompat.getColor(context, R.color.widget_text_secondary)))

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(cardBg)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_sim_card),
                            contentDescription = "SIM info",
                            modifier = GlanceModifier.size(24.dp)
                        )

                        Spacer(modifier = GlanceModifier.width(10.dp))

                        Column {
                            Text(
                                text = "SIM Info",
                                style = TextStyle(
                                    color = whiteText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = GlanceModifier.height(2.dp))

                            Text(
                                text = simInfo.carrier,
                                style = TextStyle(
                                    color = subText,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    SimRow("Status", simInfo.state, subText, whiteText)
                    Spacer(modifier = GlanceModifier.height(6.dp))

                    SimRow("Network", simInfo.networkType, subText, whiteText)
                    Spacer(modifier = GlanceModifier.height(6.dp))

                    SimRow("Roaming", simInfo.roaming, subText, whiteText)
                    Spacer(modifier = GlanceModifier.height(6.dp))

                    SimRow("Country", simInfo.countryIso, subText, whiteText)
                }
            }
        }
    }
}

class SimInfoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SimInfoWidget()
}

private data class SimInfoUi(
    val carrier: String,
    val state: String,
    val networkType: String,
    val roaming: String,
    val countryIso: String
)

private fun getSimInfo(context: Context): SimInfoUi {
    val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val hasPhonePermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

    val carrierName = getCarrierName(context, telephonyManager, hasPhonePermission)
    val simState = simStateToText(telephonyManager.simState)
    val networkType = networkTypeToText(telephonyManager.dataNetworkType)
    val roaming = if (telephonyManager.isNetworkRoaming) "On" else "Off"
    val countryIso = telephonyManager.simCountryIso
        ?.takeIf { it.isNotBlank() }
        ?.uppercase(Locale.getDefault())
        ?: "--"

    return SimInfoUi(
        carrier = carrierName,
        state = simState,
        networkType = networkType,
        roaming = roaming,
        countryIso = countryIso
    )
}

private fun getCarrierName(
    context: Context,
    telephonyManager: TelephonyManager,
    hasPhonePermission: Boolean
): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && hasPhonePermission) {
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        val activeSubs = try {
            subscriptionManager.activeSubscriptionInfoList
        } catch (_: SecurityException) {
            null
        }

        val firstCarrier = activeSubs
            ?.firstOrNull()
            ?.carrierName
            ?.toString()
            ?.takeIf { it.isNotBlank() }

        if (!firstCarrier.isNullOrBlank()) return firstCarrier
    }

    return telephonyManager.simOperatorName.takeIf { it.isNotBlank() } ?: "No SIM"
}

private fun simStateToText(state: Int): String {
    return when (state) {
        TelephonyManager.SIM_STATE_READY -> "Ready"
        TelephonyManager.SIM_STATE_ABSENT -> "Absent"
        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN required"
        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK required"
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Locked"
        TelephonyManager.SIM_STATE_NOT_READY -> "Not ready"
        TelephonyManager.SIM_STATE_PERM_DISABLED -> "Disabled"
        TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "I/O error"
        TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Restricted"
        else -> "Unknown"
    }
}

private fun networkTypeToText(type: Int): String {
    return when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_NR -> "5G"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO A"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO B"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
        TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
        TelephonyManager.NETWORK_TYPE_UNKNOWN -> "--"
        else -> "--"
    }
}

@Composable
@GlanceComposable
private fun SimRow(
    label: String,
    value: String,
    labelColor: ColorProvider,
    valueColor: ColorProvider
) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = TextStyle(
                color = labelColor,
                fontSize = 12.sp
            )
        )

        Text(
            text = value,
            style = TextStyle(
                color = valueColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}