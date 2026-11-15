package com.orion.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray

private val Bg = Color(0xFF070B0D)
private val Panel = Color(0xFF151F21)
private val Accent = Color(0xFF39D353)
private val TextMain = Color(0xFFEAF2F1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OrionApp() }
    }
}

@Composable
fun OrionApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("orion_local", 0) }
    var draft by remember { mutableStateOf("") }
    var memory by remember { mutableStateOf(prefs.getString("memory", "") ?: "") }
    var messages by remember {
        mutableStateOf(runCatching {
            val array = JSONArray(prefs.getString("messages", "[]"))
            List(array.length()) { i -> array.getString(i) }
        }.getOrDefault(emptyList()))
    }
    fun saveMessages(list: List<String>) {
        messages = list
        val array = JSONArray()
        list.forEach { array.put(it) }
        prefs.edit().putString("messages", array.toString()).apply()
    }
    fun reply(input: String): String {
        val q = input.trim().lowercase()
        return when {
            q.contains("ساعت") -> "زمان دستگاه: " + java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            q.contains("تاریخ") -> "تاریخ دستگاه: " + java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date())
            q.startsWith("یادداشت ") -> {
                val note = input.substringAfter("یادداشت ").trim()
                memory = memory + (if (memory.isBlank()) "" else "\n") + note
                prefs.edit().putString("memory", memory).apply()
                "یادداشت روی همین گوشی ذخیره شد."
            }
            q.contains("حافظه") || q.contains("یادداشت") -> if (memory.isBlank()) "هنوز یادداشتی ذخیره نشده است." else "یادداشت‌های ذخیره‌شده:\n" + memory
            else -> "من ORION هستم. در این نسخه، فرمان‌های پایه به‌صورت آفلاین اجرا می‌شوند. مدل زبانی در مرحله بعد اضافه خواهد شد."
        }
    }
    MaterialTheme {
        Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().navigationBarsPadding().padding(18.dp)) {
            Text("ORION", color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("دستیار شخصی آفلاین | نسخه ۰٫۱", color = TextMain, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (messages.isEmpty()) item { Text("سامانه آماده است. ساعت، تاریخ یا یادداشت را امتحان کن.", color = TextMain) }
                    items(messages) { line -> Text(line, color = if (line.startsWith("شما:")) Accent else TextMain, fontSize = 15.sp) }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = draft, onValueChange = { draft = it },
                    modifier = Modifier.weight(1f).background(Panel, RoundedCornerShape(16.dp)).padding(15.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextMain, fontSize = 16.sp),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) Text("پیام یا فرمان خود را بنویس…", color = Color.Gray)
                        inner()
                    }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val input = draft.trim()
                    if (input.isNotEmpty()) {
                        saveMessages(messages + listOf("شما: " + input, "ORION: " + reply(input)))
                        draft = ""
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Accent)) { Text("ارسال", color = Color.Black) }
            }
            Spacer(Modifier.height(8.dp))
            Text("حافظه محلی: " + if (memory.isBlank()) "خالی" else "دارای یادداشت", color = Color.LightGray, fontSize = 12.sp)
        }
    }
}
