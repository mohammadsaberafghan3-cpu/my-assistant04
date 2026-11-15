package com.orion.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Night = Color(0xFF080D19)
private val Panel = Color(0xFF121D31)
private val Mint = Color(0xFF49DDB0)
private val TextMain = Color(0xFFEAF1FF)

data class ChatLine(val user: Boolean, val text: String)

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
    var input by remember { mutableStateOf("") }
    var history by remember {
        mutableStateOf(runCatching {
            val arr = JSONArray(prefs.getString("history", "[]"))
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i); ChatLine(o.getBoolean("user"), o.getString("text"))
            }
        }.getOrDefault(emptyList()))
    }
    var tab by remember { mutableStateOf("chat") }
    var memory by remember { mutableStateOf(prefs.getString("memory", "") ?: "") }
    var note by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(prefs.getString("tasks", "") ?: "") }

    fun saveHistory(lines: List<ChatLine>) {
        val arr = JSONArray()
        lines.takeLast(100).forEach { arr.put(JSONObject().put("user", it.user).put("text", it.text)) }
        prefs.edit().putString("history", arr.toString()).apply()
    }
    fun send() {
        val q = input.trim()
        if (q.isEmpty()) return
        val normalized = q.replace('۰','0').replace('۱','1').replace('۲','2').replace('۳','3')
            .replace('۴','4').replace('۵','5').replace('۶','6').replace('۷','7').replace('۸','8').replace('۹','9')
        val answer = when {
            q.contains("سلام") || q.contains("درود") ->
                "درود. من ORION هستم. هسته محلی فعال است؛ هنوز مدل زبانی عمومی به این نسخه متصل نشده."
            q.contains("ساعت") -> "زمان دستگاه: " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            q.contains("تاریخ") -> "تاریخ دستگاه: " + SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())
            q.startsWith("یادداشت:") -> {
                note = q.removePrefix("یادداشت:").trim()
                prefs.edit().putString("note", note).apply()
                "یادداشت روی همین دستگاه ذخیره شد."
            }
            q.startsWith("وظیفه:") -> {
                tasks = (tasks + "\n" + q.removePrefix("وظیفه:").trim()).trim()
                prefs.edit().putString("tasks", tasks).apply()
                "وظیفه در فهرست محلی ثبت شد."
            }
            q == "کمک" -> "فرمان‌های موجود: ساعت، تاریخ، یادداشت: متن، وظیفه: متن، کمک. این نسخه هسته اولیه است، نه مدل هوش مصنوعی عمومی."
            else -> "این درخواست در نسخه فعلی قابل تحلیل عمومی نیست. می‌توانی از فرمان‌های محلی استفاده کنی. اتصال مدل هوش مصنوعی در مرحله بعد اضافه می‌شود."
        }
        history = (history + ChatLine(true, q) + ChatLine(false, answer)).takeLast(100)
        saveHistory(history)
        input = ""
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Night, surface = Panel, primary = Mint, onBackground = TextMain, onSurface = TextMain
    )) {
        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
            Column(
                Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF172744), Night, Night))).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(16.dp), color = Mint.copy(alpha = .18f)) {
                        Text("✧", Modifier.padding(horizontal = 15.dp, vertical = 7.dp), color = Mint, fontSize = 28.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ORION", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Text("دستیار شخصی · هسته محلی نسخه ۰٫۱", color = Mint, fontSize = 11.sp)
                    }
                    Text("● آفلاین", color = Mint, fontSize = 12.sp)
                }
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("chat" to "گفت‌وگو", "memory" to "حافظه", "tasks" to "وظایف").forEach { (id, label) ->
                        FilterChip(selected = tab == id, onClick = { tab = id }, label = { Text(label) })
                    }
                }
                Spacer(Modifier.height(12.dp))
                when (tab) {
                    "chat" -> {
                        Text("اتاق فرمان", color = Mint, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("فرمان‌های ساده به‌صورت محلی اجرا می‌شوند.", color = Color.LightGray, fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(history) { line ->
                                Surface(
                                    color = if (line.user) Color(0xFF263B62) else Color(0xFF17332F),
                                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth(.94f).align(if (line.user) Alignment.Start else Alignment.End)
                                ) { Text(line.text, Modifier.padding(12.dp), color = TextMain, fontSize = 14.sp) }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                                placeholder = { Text("فرمان خود را بنویسید...") }, singleLine = false,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { send() })
                            )
                            IconButton(onClick = { send() }) { Icon(Icons.Default.Send, contentDescription = "ارسال", tint = Mint) }
                        }
                        Text("نمونه: کمک | ساعت | تاریخ | یادداشت: متن | وظیفه: متن", color = Color.Gray, fontSize = 10.sp)
                    }
                    "memory" -> {
                        Text("حافظه محلی", color = Mint, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = memory, onValueChange = {
                            memory = it; prefs.edit().putString("memory", it).apply()
                        }, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("اطلاعاتی که می‌خواهید روی گوشی بماند") })
                        Text("اطلاعات این بخش در حافظه داخلی برنامه ذخیره می‌شود.", color = Color.LightGray, fontSize = 12.sp)
                    }
                    else -> {
                        Text("وظایف محلی", color = Mint, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = tasks, onValueChange = {
                            tasks = it; prefs.edit().putString("tasks", it).apply()
                        }, modifier = Modifier.fillMaxWidth().weight(1f), label = { Text("هر وظیفه را در یک خط بنویسید") })
                        Text("ویرایش و ذخیره فقط روی همین گوشی انجام می‌شود.", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
