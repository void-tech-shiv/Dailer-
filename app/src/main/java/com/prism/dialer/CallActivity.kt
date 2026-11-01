package com.prism.dialer

import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        current = this
        setContent { PrismCallScreen() }
    }

    override fun onResume() {
        super.onResume()
        current = this
    }

    override fun onPause() {
        if (current == this) current = null
        super.onPause()
    }

    companion object {
        @Volatile private var current: CallActivity? = null
        fun refreshFromService() {
            current?.runOnUiThread { current?.recreate() }
        }
    }
}

@Composable
private fun PrismCallScreen() {
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(false) }
    val call = PrismInCallService.activeCall
    val incoming = call?.state == Call.STATE_RINGING
    val caller = call?.details?.handle?.schemeSpecificPart ?: "Unknown caller"
    val background = Brush.verticalGradient(
        listOf(Color(0xFF11182B), Color(0xFF080A12), Color(0xFF201333))
    )

    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF75DFFF))) {
        Box(Modifier.fillMaxSize().background(background).padding(26.dp)) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("PRISM", color = Color(0xFF75DFFF), fontSize = 17.sp, fontWeight = FontWeight.Black, letterSpacing = 5.sp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(112.dp).background(Color.White.copy(alpha = .09f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = .2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(caller.take(1).uppercase(), color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Light)
                    }
                    Spacer(Modifier.height(22.dp))
                    Text(caller, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (incoming) "Incoming call" else "Call in progress", color = Color(0xFF9CAAC2), fontSize = 13.sp)
                }

                if (incoming) {
                    Row(horizontalArrangement = Arrangement.spacedBy(54.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledIconButton(
                                onClick = { PrismInCallService.reject() },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE94D65)),
                                modifier = Modifier.size(70.dp)
                            ) { Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White) }
                            Text("Decline", color = Color.White, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FilledIconButton(
                                onClick = { PrismInCallService.answer() },
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF39C98A)),
                                modifier = Modifier.size(70.dp)
                            ) { Icon(Icons.Default.Phone, contentDescription = "Answer", tint = Color.White) }
                            Text("Answer", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        FilledTonalIconButton(
                            onClick = { muted = !muted; PrismInCallService.setMuted(muted) },
                            modifier = Modifier.size(58.dp)
                        ) { Icon(if (muted) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = "Mute") }
                        FilledTonalIconButton(
                            onClick = { speaker = !speaker; PrismInCallService.setSpeaker(speaker) },
                            modifier = Modifier.size(58.dp)
                        ) { Icon(Icons.Default.VolumeUp, contentDescription = "Speaker") }
                    }
                    FilledIconButton(
                        onClick = { PrismInCallService.disconnect() },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE94D65)),
                        modifier = Modifier.size(76.dp)
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End call", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    }
}
