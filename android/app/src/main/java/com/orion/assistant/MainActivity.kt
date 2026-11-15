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
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val Bg = Color(0xFF050B13)
private val Panel = Color(0xFF0B1B2A)
private val Panel2 = Color(0xFF10283A)
private val Green = Color(0xFF20E6A0)
private val Cyan = Color(0xFF25C9F2)
private val MainText = Color(0xFFEAF7FF)
private val Muted = Color(0xFF8CA9BC)
private val Glow = Brush.linearGradient(listOf(Color(0xFF0C3545), Color(0xFF09202D), Color(0xFF0A1725)))

data class OrionAppEntry(val label: String, val packageName: String)
data class OrionContact(val name: String, val phone: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Green, background = Bg, surface = Panel)) { OrionCommandCenter() } }
    }
}

@Composable
private fun OrionMark(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "orion")
    val rotation by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(12000, easing = LinearEasing)), label = "rotation")
    Canvas(modifier) {
        val c = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        val r = size.minDimension * .34f
        drawCircle(Green.copy(alpha = .09f), r * 1.55f, c)
        drawCircle(Cyan.copy(alpha = .65f), r * 1.18f, c, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
        drawCircle(Green.copy(alpha = .9f), r, c, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
        val rad = Math.toRadians(rotation.toDouble())
        val p = androidx.compose.ui.geometry.Offset(c.x + kotlin.math.cos(rad).toFloat() * r * 1.3f, c.y + kotlin.math.sin(rad).toFloat() * r * .48f)
        drawOval(Cyan.copy(alpha = .85f), topLeft = androidx.compose.ui.geometry.Offset(c.x-r*1.45f,c.y-r*.45f), size = androidx.compose.ui.geometry.Size(r*2.9f,r*.9f), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
        drawCircle(Cyan, r*.12f, p)
        drawCircle(Green, r*.28f, c)
        drawCircle(Color.White.copy(alpha=.8f), r*.08f, c)
    }
}

private val menuItems = listOf(
    "خانه" to Icons.Default.Home, "برنامه‌ها" to Icons.Default.GridView,
    "مخاطبین" to Icons.Default.People, "پیام‌ها" to Icons.Default.Chat,
    "وظایف" to Icons.Default.TaskAlt, "حافظه و یادگیری" to Icons.Default.Storage,
    "امنیت و مجوزها" to Icons.Default.Shield, "تنظیمات" to Icons.Default.Settings,
    "پروفایل کاربر" to Icons.Default.AccountCircle, "درباره 04" to Icons.Default.Info
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrionCommandCenter() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("خانه") }
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
            .distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
    }
    fun loadContacts() {
        val result = mutableListOf<OrionContact>()
        runCatching {
            val cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
            cursor?.use { c ->
                val ni = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val pi = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (c.moveToNext()) { val n=c.getString(ni) ?: "بدون نام"; val p=c.getString(pi) ?: ""; if(p.isNotBlank()) result.add(OrionContact(n,p)) }
            }
        }
        contacts = result.distinctBy { it.name + it.phone }
        contactStatus = if (contacts.isEmpty()) "مخاطبی با شماره تلفن پیدا نشد." else "${contacts.size} شماره از دفترچه مخاطبین خوانده شد."
    }
    val contactsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) loadContacts() else contactStatus = "مجوز مخاطبین داده نشد."
    }
    LaunchedEffect(Unit) { loadApps() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF081522), drawerContentColor = MainText) {
                Column(Modifier.fillMaxWidth().background(Glow).padding(20.dp)) {
                    OrionMark(Modifier.size(76.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("04", color = Green, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("نسخه 0.3 • مرکز فرماندهی", color = Muted, fontSize = 11.sp)
                    Spacer(Modifier.height(10.dp))
                    Surface(color = Color(0xFF10372F), shape = CircleShape) { Text("●  آماده به کار", color = Green, fontSize = 11.sp, modifier = Modifier.padding(horizontal=12.dp, vertical=6.dp)) }
                }
                Spacer(Modifier.height(8.dp))
                menuItems.forEach { (label, icon) ->
                    NavigationDrawerItem(
                        label = { Text(label, fontSize = 14.sp) }, icon = { Icon(icon, null, tint = if(page==label) Green else Muted) },
                        selected = page == label, onClick = { page=label; scope.launch { drawerState.close() } },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = Color(0xFF123A43), selectedTextColor = Green),
                        modifier = Modifier.padding(horizontal=10.dp, vertical=2.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text("04 • Android Intelligent Agent", color=Muted, fontSize=10.sp, modifier=Modifier.padding(18.dp))
            }
        }
    ) {
        Scaffold(containerColor = Bg, topBar = {
            TopAppBar(
                title = { Column { Text("04", color=Green, fontWeight=FontWeight.Black, letterSpacing=2.sp); Text(page, color=Muted, fontSize=11.sp) } },
                navigationIcon = { IconButton(onClick={scope.launch { drawerState.open() }}) { Icon(Icons.Default.Menu, "منو", tint=MainText) } },
                actions = { Surface(color=Color(0xFF10372F), shape=CircleShape, modifier=Modifier.padding(end=14.dp)) { Text("●", color=Green, modifier=Modifier.padding(9.dp)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor=Bg)
            )
        }, bottomBar = {
            NavigationBar(containerColor=Color(0xFF081522), contentColor=MainText) {
                listOf("خانه" to Icons.Default.Home, "برنامه‌ها" to Icons.Default.GridView, "وظایف" to Icons.Default.TaskAlt, "مخاطبین" to Icons.Default.People, "بیشتر" to Icons.Default.MoreHoriz).forEach { (label,icon) ->
                    NavigationBarItem(selected=page==label || (label=="بیشتر" && page !in listOf("خانه","برنامه‌ها","وظایف","مخاطبین")), onClick={
                        if(label=="بیشتر") scope.launch { drawerState.open() } else page=label
                    }, icon={Icon(icon,null)}, label={Text(label,fontSize=10.sp)}, colors=NavigationBarItemDefaults.colors(selectedIconColor=Green, selectedTextColor=Green, indicatorColor=Color(0xFF123A43), unselectedIconColor=Muted, unselectedTextColor=Muted))
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=16.dp).padding(top=8.dp)) {
                when(page) {
                    "خانه" -> {
                        Column(Modifier.fillMaxWidth().background(Glow, RoundedCornerShape(24.dp)).padding(18.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                            OrionMark(Modifier.size(164.dp))
                            Text("سلام، من 04 هستم", color=MainText, fontSize=19.sp, fontWeight=FontWeight.Bold)
                            Text("مرکز هوشمند کنترل و اجرای وظایف گوشی", color=Muted, fontSize=12.sp)
                            Spacer(Modifier.height(14.dp))
                            Surface(color=Color(0xFF092D32), shape=CircleShape, modifier=Modifier.fillMaxWidth()) {
                                Row(Modifier.padding(12.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.Center) {
                                    Icon(Icons.Default.Mic, null, tint=Green); Spacer(Modifier.width(8.dp)); Text("برای شروع، فرمان خود را وارد کنید", color=Green, fontSize=12.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("دسترسی سریع", color=MainText, fontWeight=FontWeight.Bold, fontSize=16.sp)
                        Spacer(Modifier.height(8.dp))
                        val shortcuts=listOf("برنامه‌ها" to Icons.Default.GridView,"مخاطبین" to Icons.Default.People,"پیام‌ها" to Icons.Default.Chat,"تنظیمات" to Icons.Default.Settings)
                        shortcuts.chunked(2).forEach { pair -> Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) { pair.forEach { (label,icon) ->
                            Surface(color=Panel, shape=RoundedCornerShape(18.dp), modifier=Modifier.weight(1f).padding(bottom=9.dp).clickable { page=label }) {
                                Column(Modifier.padding(16.dp)) { Icon(icon,null,tint=if(label=="تنظیمات") Cyan else Green); Spacer(Modifier.height(8.dp)); Text(label,color=MainText,fontWeight=FontWeight.SemiBold); Text("ورود به بخش",color=Muted,fontSize=10.sp) }
                            }
                        } } }
                        StatusTile("برنامه‌های شناسایی‌شده", "${apps.size} برنامه قابل اجرا", Cyan)
                        StatusTile("عامل هوشمند", "کنترل داخل برنامه‌ها هنوز در دست توسعه است", Green)
                    }
                    "برنامه‌ها" -> {
                        PageTitle("برنامه‌های نصب‌شده","برنامه را جست‌وجو و باز کنید")
                        SearchBox(search,{search=it},"جستجوی برنامه...")
                        LazyColumn(verticalArrangement=Arrangement.spacedBy(7.dp)) {
                            items(apps.filter { it.label.contains(search,true) }) { app ->
                                Surface(color=Panel,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().clickable {
                                    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let { context.startActivity(it) }
                                }) { Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Default.Android,null,tint=Cyan); Spacer(Modifier.width(12.dp)); Text(app.label,color=MainText,modifier=Modifier.weight(1f)); Text("بازکردن",color=Green,fontSize=12.sp) } }
                            }
                        }
                    }
                    "مخاطبین" -> {
                        PageTitle("دفترچه مخاطبین",contactStatus)
                        Button(onClick={ if(context.checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED) loadContacts() else contactsPermission.launch(Manifest.permission.READ_CONTACTS) },colors=ButtonDefaults.buttonColors(containerColor=Green)) { Text("درخواست مجوز و بارگذاری",color=Bg) }
                        LazyColumn(verticalArrangement=Arrangement.spacedBy(7.dp)) { items(contacts) { c ->
                            Surface(color=Panel,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().clickable { messageNumber=c.phone; page="پیام‌ها" }) {
                                Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Default.AccountCircle,null,tint=Cyan); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)){Text(c.name,color=MainText);Text(c.phone,color=Muted,fontSize=12.sp)}; Icon(Icons.Default.Call,null,tint=Green) }
                            }
                        } }
                    }
                    "پیام‌ها" -> {
                        PageTitle("مرکز پیام","ارسال نهایی از برنامه پیام‌رسان و با تأیید شما انجام می‌شود")
                        OutlinedTextField(messageNumber,{messageNumber=it},label={Text("شماره مخاطب")},modifier=Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(messageBody,{messageBody=it},label={Text("متن پیام")},modifier=Modifier.fillMaxWidth(),minLines=3)
                        Button(onClick={ if(messageNumber.isNotBlank()) runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:${Uri.encode(messageNumber)}")).putExtra("sms_body",messageBody)) } },enabled=messageNumber.isNotBlank(),colors=ButtonDefaults.buttonColors(containerColor=Green)) { Text("بازکردن پیام‌رسان",color=Bg) }
                        Spacer(Modifier.height(18.dp)); PageTitle("تماس تلفنی","شماره را وارد کنید تا شماره‌گیر باز شود")
                        OutlinedTextField(phoneNumber,{phoneNumber=it},label={Text("شماره تلفن")},modifier=Modifier.fillMaxWidth())
                        OutlinedButton(onClick={if(phoneNumber.isNotBlank()) runCatching { context.startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:${Uri.encode(phoneNumber)}"))) }},enabled=phoneNumber.isNotBlank()) { Text("بازکردن شماره‌گیر") }
                    }
                    "وظایف" -> { PageTitle("وظایف و عملیات","مرکز پیگیری اجرای فرمان‌ها"); StatusTile("کنترل رابط برنامه‌ها","نیازمند پیاده‌سازی سرویس Accessibility",Cyan); StatusTile("برنامه‌ریز هوشمند","هنوز به موتور هوش مصنوعی متصل نشده است",Green); StatusTile("تأیید عملیات حساس","ارسال، حذف و پرداخت باید با اجازه شما باشد",Cyan) }
                    "حافظه و یادگیری" -> { PageTitle("حافظه و یادگیری","مدیریت داده‌های محلی 04"); StatusTile("حافظه محلی","ذخیره پایدار و مدیریت حافظه هنوز پیاده‌سازی نشده است",Cyan) }
                    "امنیت و مجوزها" -> { PageTitle("امنیت و مجوزها","مجوزها باید آگاهانه و توسط شما فعال شوند"); StatusTile("مخاطبین","مجوز فقط هنگام درخواست شما گرفته می‌شود",Green); StatusTile("دسترسی‌پذیری اندروید","سرویس کنترل صفحه هنوز اضافه نشده است",Cyan); StatusTile("توقف اضطراری","در نسخه عامل اجرایی اضافه خواهد شد",Green) }
                    "تنظیمات" -> { PageTitle("تنظیمات 04","تنظیمات ظاهری و رفتاری"); StatusTile("زبان","فارسی • رابط راست‌چین",Green); StatusTile("حالت عملکرد","هدف: آفلاین‌محور با امکانات آنلاین اختیاری",Cyan); StatusTile("سطح استقلال","سطح متعادل؛ تأیید عملیات حساس",Green) }
                    "پروفایل کاربر" -> { PageTitle("پروفایل و مشخصات کاربر","اطلاعات نمایشی حساب محلی"); StatusTile("نام نمایشی","کاربر 04",Cyan); StatusTile("سطح دسترسی","قابل تنظیم پس از تکمیل سامانه مجوزها",Green) }
                    "درباره 04" -> { PageTitle("درباره 04","Android Intelligent Agent"); StatusTile("نسخه رابط","0.3.0 • بازطراحی رابط فرماندهی",Green); StatusTile("وضعیت توسعه","رابط کاربری بازطراحی شده؛ عامل اجرایی هنوز کامل نیست",Cyan) }
                    else -> PageTitle(page,"این بخش در حال آماده‌سازی است")
                }
            }
        }
    }
}

@Composable
private fun PageTitle(title:String, detail:String) {
    Column(Modifier.padding(bottom=12.dp)) { Text(title,color=MainText,fontSize=20.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(detail,color=Muted,fontSize=12.sp) }
}
@Composable
private fun SearchBox(value:String,onChange:(String)->Unit,hint:String) {
    Row(Modifier.fillMaxWidth().background(Panel,RoundedCornerShape(14.dp)).padding(13.dp),verticalAlignment=Alignment.CenterVertically) {
        Icon(Icons.Default.Search,null,tint=Muted); Spacer(Modifier.width(8.dp))
        BasicTextField(value,onChange,modifier=Modifier.weight(1f),textStyle=androidx.compose.ui.text.TextStyle(color=MainText,fontSize=14.sp),decorationBox={inner->if(value.isEmpty()) Text(hint,color=Muted);inner()})
    }
}
@Composable
private fun StatusTile(title:String,detail:String,accent:Color) {
    Surface(color=Panel,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().padding(bottom=8.dp)) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(accent,CircleShape)); Spacer(Modifier.width(12.dp))
            Column { Text(title,color=MainText,fontWeight=FontWeight.SemiBold); Text(detail,color=Muted,fontSize=12.sp,lineHeight=17.sp) }
        }
    }
}
