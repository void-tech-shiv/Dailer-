package com.prism.dialer

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Person(val name: String, val number: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PrismApp() }
    }
}

@Composable
private fun PrismApp() {
    val context = LocalContext.current
    var number by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var contacts by remember { mutableStateOf<List<Person>>(emptyList()) }
    var permission by remember {
        mutableStateOf(
            android.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var accent by remember { mutableIntStateOf(0) }
    var isDefault by remember { mutableStateOf(isDefaultDialer(context)) }
    val accents = listOf(
        Color(0xFF65D6FF), Color(0xFFB58CFF), Color(0xFFFF83C8),
        Color(0xFF83E6B0), Color(0xFFFFC56D)
    )
    val glow by animateColorAsState(accents[accent], label = "accent")
    val backdrop = Brush.verticalGradient(
        listOf(Color(0xFF101323), Color(0xFF080A12), Color(0xFF17112B))
    )

    val contactsPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permission = granted
        if (granted) contacts = loadContacts(context)
    }
    val dialerRole = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isDefault = isDefaultDialer(context)
    }

    LaunchedEffect(Unit) {
        if (permission) contacts = loadContacts(context)
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = glow)) {
        Box(
            Modifier.fillMaxSize().background(backdrop)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "PRISM", color = Color.White, fontSize = 25.sp,
                            fontWeight = FontWeight.Black, letterSpacing = 4.sp
                        )
                        Text(
                            "NORD 3 5G  •  ANDROID 16",
                            color = Color(0xFF9AA3BC), fontSize = 9.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                    IconButton(onClick = { accent = (accent + 1) % accents.size }) {
                        Icon(Icons.Default.Palette, contentDescription = "Change theme", tint = glow)
                    }
                }

                if (!isDefault) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth()
                            .background(glow.copy(alpha = .10f), RoundedCornerShape(16.dp))
                            .border(1.dp, glow.copy(alpha = .30f), RoundedCornerShape(16.dp))
                            .clickable { requestDialerRole(context, dialerRole::launch) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Make Prism your default dialer", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Android will ask you to confirm", color = Color(0xFFADB7CD), fontSize = 10.sp)
                        }
                        Text("SET UP  ›", color = glow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth()
                        .background(Color.White.copy(alpha = .06f), RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = .12f), RoundedCornerShape(24.dp))
                        .padding(5.dp)
                ) {
                    listOf("Keypad", "Contacts").forEachIndexed { index, label ->
                        Box(
                            Modifier.weight(1f)
                                .background(
                                    if (tab == index) glow.copy(alpha = .18f) else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { tab = index }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (tab == index) glow else Color(0xFF9AA3BC), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                if (tab == 0) {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (number.isEmpty()) "Enter number" else number,
                                color = Color.White, fontSize = 27.sp,
                                fontWeight = FontWeight.Light, textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text("Tap a number to begin", color = Color(0xFF818BA5), fontSize = 11.sp)
                        }

                        val keys = listOf(
                            "1" to "", "2" to "ABC", "3" to "DEF",
                            "4" to "GHI", "5" to "JKL", "6" to "MNO",
                            "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
                            "*" to "", "0" to "+", "#" to ""
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            keys.chunked(3).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    row.forEach { (digit, letters) ->
                                        Box(
                                            Modifier.size(74.dp)
                                                .background(Color.White.copy(alpha = .075f), CircleShape)
                                                .border(1.dp, Color.White.copy(alpha = .13f), CircleShape)
                                                .clickable { number += digit },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(digit, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Medium)
                                                if (letters.isNotEmpty()) Text(letters, color = glow.copy(alpha = .8f), fontSize = 8.sp, letterSpacing = 1.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.size(52.dp))
                            Box(
                                Modifier.size(64.dp)
                                    .background(Brush.linearGradient(listOf(glow, Color(0xFF5675FF))), CircleShape)
                                    .clickable {
                                        if (number.isNotBlank()) {
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF07101A), modifier = Modifier.size(27.dp))
                            }
                            IconButton(onClick = { if (number.isNotEmpty()) number = number.dropLast(1) }) {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color(0xFFB5BED4))
                            }
                        }
                    }
                } else if (!permission) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Contacts, tint = glow, modifier = Modifier.size(42.dp), contentDescription = null)
                        Spacer(Modifier.height(12.dp))
                        Text("Allow contacts access to see your people", color = Color.White, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { contactsPermission.launch(Manifest.permission.READ_CONTACTS) }) {
                            Text("Allow contacts")
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(contacts) { person ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(Color.White.copy(alpha = .07f), RoundedCornerShape(18.dp))
                                    .clickable { number = person.number; tab = 0 }
                                    .padding(15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(42.dp).background(glow.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
                                    Text(person.name.take(1).uppercase(), color = glow, fontWeight = FontWeight.Bold)
                                }
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text(person.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(person.number, color = Color(0xFF9AA3BC), fontSize = 12.sp)
                                }
                                Icon(Icons.Default.Call, tint = glow, contentDescription = "Select contact")
                            }
                        }
                    }
                }

                Text(
                    "GLASS UI  •  CUSTOM ACCENTS  •  PRISM",
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = Color(0xFF737E99), fontSize = 8.sp,
                    letterSpacing = 1.sp, textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun isDefaultDialer(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val manager = context.getSystemService(RoleManager::class.java)
    return manager?.isRoleHeld(RoleManager.ROLE_DIALER) == true
}

private fun requestDialerRole(context: Context, launch: (Intent) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val manager = context.getSystemService(RoleManager::class.java)
        if (manager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true) {
            launch(manager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
        }
    }
}

private fun loadContacts(context: Context): List<Person> {
    val result = mutableListOf<Person>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )
    cursor?.use {
        val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        while (it.moveToNext()) {
            val name = it.getString(nameCol) ?: "Unknown"
            val phone = it.getString(numberCol) ?: ""
            result.add(Person(name, phone))
        }
    }
    return result.distinctBy { it.number }
}
