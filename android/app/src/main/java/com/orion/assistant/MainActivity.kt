package com.orion.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF070B10)
private val Panel = Color(0xFF111C27)
private val Panel2 = Color(0xFF172638)
private val Green = Color(0xFF39E58C)
private val Cyan = Color(0xFF36C9E8)
private val MainText = Color(0xFFEAF4F8)
private val Muted = Color(0xFF91A5B4)

data class OrionAppEntry(val label: String, val packageName: String)
data class OrionContact(val name: String, val phone: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OrionCommandCenter() }
    }
}

@Composable
private fun OrionMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.34f
        val nodes = listOf(
            androidx.compose.ui.geometry.Offset(cx - r * 0.82f, cy - r * 0.18f),
            androidx.compose.ui.geometry.Offset(cx - r * 0.18f, cy - r * 0.82f),
            androidx.compose.ui.geometry.Offset(cx + r * 0.62f, cy - r * 0.56f),
            androidx.compose.ui.geometry.Offset(cx + r * 0.82f, cy + r * 0.35f),
            androidx.compose.ui.geometry.Offset(cx + r * 0.08f, cy + r * 0.86f),
            androidx.compose.ui.geometry.Offset(cx - r * 0.72f, cy + r * 0.48f)
        )
        val edges = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 0, 0 to 3, 1 to 4)
        edges.forEach { (a, b) ->
            drawLine(Cyan.copy(alpha = 0.8f), nodes[a], nodes[b], strokeWidth = 2.2f, cap = StrokeCap.Round)
        }
        drawCircle(Green.copy(alpha = 0.12f), radius = r * 0.9f, center = androidx.compose.ui.geometry.Offset(cx, cy))
        drawCircle(Green, radius = r * 0.25f, center = androidx.compose.ui.geometry.Offset(cx, cy))
        nodes.forEachIndexed { index, point ->
            drawCircle(if (index % 2 == 0) Green else Cyan, radius = r * 0.09f, center = point)
        }
        drawCircle(Cyan.copy(alpha = 0.9f), radius = r * 0.42f, center = androidx.compose.ui.geometry.Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))
    }
}

@Composable
private fun OrionCommandCenter() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tab by remember { mutableStateOf("خانه") }
    var apps by remember { mutableStateOf(emptyList<OrionAppEntry>()) }
    var contacts by remember { mutableStateOf(emptyList<OrionContact>()) }
    var contactStatus by remember { mutableStateOf("برای مشاهده مخاطبین، مجوز لازم است.") }
    var messageNumber by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }

    fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = context.packageManager.queryIntentActivities(intent, 0)
            .map { OrionAppEntry(it.loadLabel(context.packageManager).toString(), it.activityInfo.packageName) }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun loadContacts() {
        val result = mutableListOf<OrionContact>()
        runCatching {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIndex) ?: "بدون نام"
                    val number = it.getString(phoneIndex) ?: ""
                    if (number.isNotBlank()) result.add(OrionContact(name, number))
                }
            }
        }
        contacts = result.distinctBy { it.name + it.phone }
        contactStatus = if (contacts.isEmpty()) "مخاطبی با شماره تلفن پیدا نشد." else "${contacts.size} شماره از دفترچه مخاطبین خوانده شد."
    }

    val contactsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) loadContacts() else contactStatus = "مجوز مخاطبین داده نشد؛ اطلاعاتی خوانده نشد."
    }

    LaunchedEffect(Unit) { loadApps() }

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OrionMark(Modifier.size(66.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("ORION", color = Green, fontSize = 29.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("مرکز فرماندهی هوشمند", color = Muted, fontSize = 13.sp)
            }
            Surface(color = Color(0xFF12352D), shape = RoundedCornerShape(50)) {
                Text("هسته فعال", color = Green, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("خانه", "برنامه‌ها", "مخاطبین", "پیام").forEach { item ->
                FilterChip(selected = tab == item, onClick = { tab = item }, label = { Text(item) })
            }
        }
        Spacer(Modifier.height(12.dp))
        when (tab) {
            "برنامه‌ها" -> {
                Text("برنامه‌های نصب‌شده", color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("برنامه‌ها را از اینجا باز کن؛ ORION به داده‌های خصوصی آن‌ها دسترسی ندارد.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                BasicTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).padding(14.dp), textStyle = androidx.compose.ui.text.TextStyle(color = MainText), decorationBox = { inner -> if (search.isEmpty()) Text("جستجوی برنامه…", color = Muted); inner() })
                LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(apps.filter { it.label.contains(search, true) }) { app ->
                        Surface(color = Panel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable {
                            val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (launch != null) context.startActivity(launch)
                        }) {
                            Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("◈", color = Cyan, fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(app.label, color = MainText, modifier = Modifier.weight(1f))
                                Text("بازکردن", color = Green, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            "مخاطبین" -> {
                Text("دفترچه مخاطبین", color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(contactStatus, color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Button(onClick = {
                    if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) loadContacts()
                    else contactsPermission.launch(Manifest.permission.READ_CONTACTS)
                }, colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("درخواست دسترسی / بارگذاری", color = Bg) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    items(contacts) { contact ->
                        Surface(color = Panel, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().clickable { messageNumber = contact.phone; tab = "پیام" }) {
                            Column(Modifier.padding(12.dp)) {
                                Text(contact.name, color = MainText, fontWeight = FontWeight.SemiBold)
                                Text(contact.phone, color = Cyan, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            "پیام" -> {
                Text("مرکز پیام", color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("پیامک در برنامه پیام‌رسان پیش‌فرض باز می‌شود؛ ارسال نهایی با تأیید شماست.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(value = messageNumber, onValueChange = { messageNumber = it }, label = { Text("شماره مخاطب") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = messageBody, onValueChange = { messageBody = it }, label = { Text("متن پیام") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Button(onClick = {
                    if (messageNumber.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(messageNumber)}")).putExtra("sms_body", messageBody)
                        runCatching { context.startActivity(intent) }
                    }
                }, enabled = messageNumber.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("بازکردن پیام‌رسان", color = Bg) }
                Spacer(Modifier.height(20.dp))
                Text("تماس تلفنی", color = MainText, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("شماره برای تماس") }, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = {
                    if (phoneNumber.isNotBlank()) runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))) }
                }, enabled = phoneNumber.isNotBlank()) { Text("بازکردن شماره‌گیر") }
            }
            else -> {
                Text("وضعیت سامانه", color = MainText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                StatusTile("هویت بصری", "هسته هوشمند + صورت فلکی ORION", Green)
                StatusTile("برنامه‌های گوشی", "${apps.size} برنامه قابل اجرا شناسایی شد", Cyan)
                StatusTile("مخاطبین", "فقط پس از اجازه شما خوانده می‌شوند", Green)
                StatusTile("پیامک و تماس", "با تأیید نهایی شما در برنامه‌های سیستم", Cyan)
                Spacer(Modifier.height(10.dp))
                Text("فرمان‌های پایه و حافظه محلی نسخه قبلی در حال حفظ و توسعه هستند. اتصال مدل زبانی و عامل چندمرحله‌ای هنوز تکمیل نشده است.", color = Muted, fontSize = 12.sp, lineHeight = 19.sp)
            }
        }
    }
}

@Composable
private fun StatusTile(title: String, detail: String, accent: Color) {
    Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(accent, RoundedCornerShape(50)))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, color = MainText, fontWeight = FontWeight.SemiBold)
                Text(detail, color = Muted, fontSize = 12.sp)
            }
        }
    }
}
