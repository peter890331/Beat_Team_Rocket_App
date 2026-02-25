package com.peter890331.Beat_Team_Rocket_App

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import okhttp3.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.ArrayList
import java.util.regex.Pattern
import kotlin.math.*
import kotlin.random.Random

object ServiceLauncher {
    var pendingIntent: Intent? = null
}

class MainActivity : ComponentActivity() {
    private val SCREEN_CAPTURE_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenCVLoader.initDebug()
        Handler(Looper.getMainLooper()).postDelayed({ checkPermissions() }, 1000)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = ComposeColor(0xFF00E5FF), background = ComposeColor.Black)) {
                Surface(modifier = Modifier.fillMaxSize(), color = ComposeColor.Black) { DashboardUI() }
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val isServiceEnabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC).any { it.resolveInfo.serviceInfo.packageName == packageName }
        if (!isServiceEnabled) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DashboardUI() {
        var rLimit by remember { mutableStateOf("5") }; var hLimit by remember { mutableStateOf("5") }
        var startCoord by remember { mutableStateOf("25.032966, 121.535516") }; var isRunning by remember { mutableStateOf(false) }
        val typeList = listOf("Normal", "Fire", "Water", "Grass", "Electric", "Ice", "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy")
        val context = LocalContext.current
        var selectedTypes by remember { mutableStateOf(setOf<String>()) }
        val isCoordValid = startCoord.matches(Regex("^-?\\d+\\.\\d+\\s*,\\s*-?\\d+\\.\\d+$"))
        val isLimitValid = rLimit.toIntOrNull() != null && hLimit.toIntOrNull() != null
        fun loadBitmapFromAssets(fileName: String): Bitmap? {
            return try { val istr: InputStream = context.assets.open("type/$fileName"); BitmapFactory.decodeStream(istr) } catch (e: Exception) { null }
        }
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("火箭隊助手", style = MaterialTheme.typography.headlineMedium, color = ComposeColor(0xFF00E5FF))
            Text("Beat Team Rocket App, made by Peter Yu.", style = MaterialTheme.typography.bodySmall, color = ComposeColor.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("參數設定", style = MaterialTheme.typography.titleMedium, color = ComposeColor.White, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = startCoord, onValueChange = { startCoord = it }, label = { Text("起始座標") }, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = rLimit, onValueChange = { rLimit = it }, label = { Text("雷達掃描週期") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = hLimit, onValueChange = { hLimit = it }, label = { Text("復活補血週期") }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("目標屬性", style = MaterialTheme.typography.titleMedium, color = ComposeColor.White, modifier = Modifier.fillMaxWidth())
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { selectedTypes = typeList.toSet() }, modifier = Modifier.weight(1f)) { Text("全選") }
                Button(onClick = { selectedTypes = emptySet() }, modifier = Modifier.weight(1f)) { Text("全不選") }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                typeList.chunked(6).forEach { rowTypes ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        rowTypes.forEach { name ->
                            val bitmap = loadBitmapFromAssets("$name.png")
                            FilterChip(
                                selected = name in selectedTypes,
                                onClick = { selectedTypes = if (name in selectedTypes) selectedTypes - name else selectedTypes + name },
                                label = { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { if (bitmap != null) Image(bitmap = bitmap.asImageBitmap(), contentDescription = name, modifier = Modifier.size(24.dp)) else Text(name.take(3), fontSize = 10.sp) } },
                                modifier = Modifier.width(55.dp).height(45.dp).padding(2.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ComposeColor(0xFF00E5FF).copy(alpha = 0.2f))
                            )
                        }
                    }
                }
            }
            Button(
                enabled = isCoordValid && isLimitValid && selectedTypes.isNotEmpty() && !isRunning,
                onClick = {
                    val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    ServiceLauncher.pendingIntent = Intent(this@MainActivity, AutoClickService::class.java).apply {
                        action = "START_BOT"; putExtra("START_COORD", startCoord); putExtra("RADAR_LIMIT", rLimit.toIntOrNull() ?: 5)
                        putExtra("HEAL_LIMIT", hLimit.toIntOrNull() ?: 5); putStringArrayListExtra("TYPES", ArrayList(selectedTypes.map { it.lowercase() }))
                    }
                    startActivityForResult(manager.createScreenCaptureIntent(), SCREEN_CAPTURE_CODE)
                    isRunning = true
                },
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFF00E5FF), disabledContainerColor = ComposeColor.DarkGray)
            ) { Text(if (isRunning) "運行中" else "啟動", color = if(isRunning) ComposeColor.LightGray else ComposeColor.Black) }
            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComposeColor.DarkGray.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📋 啟動前檢查清單", style = MaterialTheme.typography.titleMedium, color = ComposeColor(0xFF00E5FF))
                    Spacer(modifier = Modifier.height(8.dp))
                    val instructions = """
                        【GPS JoyStick 設定】
                            - 傳送：建議將操作桿預設為「隱藏」。
                        【Pokémon GO 準備】
                            - 孵蛋：建議暫停孵蛋。
                            - 大佬：建議解除裝備「火箭隊雷達」與「超級火箭隊雷達」。
                            - 夥伴：建議攜帶達到「給力好夥伴」等級以上的寶可夢作為夥伴。
                            - 物資：確保背包備有足夠的「厲害傷藥」與「活力碎片」，可以事先刷路線獲得。
                            - 打手：確保在火箭隊對戰中皆已預先編排好各屬性對應的「小隊」。
                        【注意事項】
                            - 提醒：此腳本執行時建議全程在旁觀看，以便隨時應對突發狀況。
                            - 雷達：此腳本的座標來源是 PokeList，有時會出現查無座標的情況。
                            - 上限：注意 Pokémon GO 中有每日打火箭隊的上限，超過可能導致軟鎖。
                    """.trimIndent()
                    Text(instructions, style = MaterialTheme.typography.bodySmall, color = ComposeColor.LightGray, lineHeight = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ComposeColor.DarkGray.copy(alpha = 0.5f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🕹️ 懸浮視窗操作介紹", style = MaterialTheme.typography.titleMedium, color = ComposeColor(0xFF00E5FF))
                    Spacer(modifier = Modifier.height(8.dp))
                    val controlInstructions = """
                        【拖曳】：拖曳最上方的圖示可上下移動懸浮視窗。
                        【開始】：點擊後正式啟動自動化掛機流程。
                        【停止】：強制停止腳本運行，並關閉懸浮視窗。
                        【計數】：捕捉計數器，依序為成功捕捉數量與總拜訪補給站數量。
                        【省電】：距離感應省電開關，開啟後遮擋手機上方感測器即可熄滅螢幕。
                        【截圖】：手動儲存當前畫面至 Pictures/PokemonGO。
                    """.trimIndent()
                    Text(controlInstructions, style = MaterialTheme.typography.bodySmall, color = ComposeColor.LightGray, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCREEN_CAPTURE_CODE && resultCode == Activity.RESULT_OK) { ServiceLauncher.pendingIntent?.apply { putExtra("RESULT_CODE", resultCode); putExtra("DATA", data); startForegroundService(this) } }
    }
}

class AutoClickService : AccessibilityService() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient.Builder().connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS).readTimeout(15, java.util.concurrent.TimeUnit.SECONDS).build()
    private var isBotRunning = false; private var isConfirmed = false
    private var windowManager: WindowManager? = null; private var sidePanel: LinearLayout? = null
    private var statusText: TextView? = null; private var floatStartBtn: Button? = null
    private var btnAnimator: ObjectAnimator? = null; private var projection: MediaProjection? = null
    private var imageReader: ImageReader? = null; private var virtualDisplay: VirtualDisplay? = null
    private var customToastView: LinearLayout? = null; private var customToastText: TextView? = null
    private val mainHandler = Handler(Looper.getMainLooper()); private val hideToastRunnable = Runnable { customToastView?.visibility = View.GONE }
    private val POS_POKESTOP = PointF(540f, 1420f); private val POS_BATTLE_BTN = PointF(540f, 1730f)
    private val POS_CONFIRM_BTN = PointF(540f, 2030f); private val POS_DEAD_BAG = PointF(300f, 320f)
    private val POS_HEAL_CONFIRM = PointF(540f, 1910f); private val BATTLE_POINTS = listOf(PointF(350f, 1950f), PointF(540f, 1950f), PointF(730f, 1950f))
    private val POS_MENU_BALL = PointF(540f, 2110f); private val POS_BAG_ICON = PointF(840f, 1880f)
    private var baseLat = 0.0; private var baseLng = 0.0; private var lastActionTime = System.currentTimeMillis()
    private val visitedStops = mutableSetOf<String>(); private var healCount = 0; private var totalVisited = 0; private var totalCatched = 0

    private var proximityWakeLock: PowerManager.WakeLock? = null

    override fun onServiceConnected() {
        this.serviceInfo = AccessibilityServiceInfo().apply { eventTypes = AccessibilityEvent.TYPES_ALL_MASK; feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC; flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS }
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "BTR:ProximityLock")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "START_BOT") {
            isBotRunning = true; setupForegroundNotification()
            val resCode = intent.getIntExtra("RESULT_CODE", 0); val resData = intent.getParcelableExtra<Intent>("DATA")
            if (resData != null) { projection = (getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).getMediaProjection(resCode, resData); setupImageReader() }
            showSidePanel(); initCustomToast()
            val startCoord = intent.getStringExtra("START_COORD") ?: "25.032966, 121.535516"
            Regex("-?\\d+\\.\\d+").findAll(startCoord).map { it.value.toDouble() }.toList().let { if (it.size >= 2) { baseLat = it[0]; baseLng = it[1] } }
            val types = intent.getStringArrayListExtra("TYPES") ?: arrayListOf()
            val rLimit = intent.getIntExtra("RADAR_LIMIT", 5); val hLimit = intent.getIntExtra("HEAL_LIMIT", 5)
            scope.launch {
                try {
                    updateLog("啟動"); delay(1500); updateLog("開啟 GPS JoyStick"); delay(1000)
                    packageManager.getLaunchIntentForPackage(getJoyStickPackageName())?.let { startActivity(it) }
                    delay(3500)
                    val formattedLat = (baseLat * 100000).toInt() / 100000.0; val formattedLng = (baseLng * 100000).toInt() / 100000.0
                    updateLog("傳送到座標: $formattedLat, $formattedLng"); executeTeleportSilent(baseLat, baseLng)
                    delay(10000); launchGame()
                    for (i in 15 downTo 1) { updateLog("開啟 Pokémon GO"); delay(1000) }
                    Handler(Looper.getMainLooper()).post { floatStartBtn?.isEnabled = true; floatStartBtn?.setTextColor(Color.parseColor("#00FF00")); btnAnimator = ObjectAnimator.ofFloat(floatStartBtn, "alpha", 0.4f, 1.0f).apply { duration = 800; repeatMode = ValueAnimator.REVERSE; repeatCount = ValueAnimator.INFINITE; start() } }
                    updateLog("請在確認 Pokémon GO 載入後\n將遊戲視角拉至最小\n並隱藏 GPS JoyStick\n完成按 ▶ 開始", autoHide = false)
                    while (!isConfirmed && isBotRunning) delay(500)
                    delay(1500)
                    if (isBotRunning) mainBotLoop(types, rLimit, hLimit, baseLat, baseLng)
                } catch (e: Exception) {}
            }
        }
        return START_STICKY
    }

    private fun showSidePanel() {
        Handler(Looper.getMainLooper()).post {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            sidePanel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; background = GradientDrawable().apply { setColor(Color.parseColor("#CC000000")); cornerRadius = 20f; setStroke(2, Color.CYAN) }; setPadding(15, 20, 15, 20) }
            val lp = LinearLayout.LayoutParams(70, 70).apply { setMargins(0, 5, 0, 5) }
            val dragHandle = ImageView(this).apply { layoutParams = lp; try { val istr = assets.open("my_icon/my_icon.png"); val bitmap = BitmapFactory.decodeStream( istr); setImageBitmap(bitmap); scaleType = ImageView.ScaleType.FIT_CENTER } catch (e: Exception) { setImageResource(android.R.drawable.ic_menu_mylocation) } }
            floatStartBtn = Button(this).apply { text = "▶"; textSize = 20f; setTextColor(Color.GRAY); background = null; layoutParams = lp; isEnabled = false; alpha = 0.5f; setPadding(0, 0, 0, 0); setOnClickListener { isConfirmed = true; isEnabled = false; btnAnimator?.cancel(); setTextColor(Color.DKGRAY); alpha = 0.5f; updateLog("開始") } }
            val stopBtn = Button(this).apply { text = "✖"; textSize = 16f; setTextColor(Color.RED); background = null; layoutParams = lp; setPadding(0, 0, 0, 0); setOnClickListener { shutdown() } }
            statusText = TextView(this).apply { text = "0 / 0"; textSize = 10f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 20, 0, 20) } }
            val sensorBtn = Button(this).apply { text = "\uD83C\uDF1E"; textSize = 16f; setTextColor(Color.CYAN); background = null; layoutParams = lp; setPadding(0, 0, 0, 0); setOnClickListener { toggleSensorMode(this) } }
            val screenshotLp = LinearLayout.LayoutParams(70, 70).apply { setMargins(0, 0, 0, 10) }
            val screenshotBtn = Button(this).apply { text = "📸"; textSize = 16f; setTextColor(Color.WHITE); background = null; layoutParams = screenshotLp; setPadding(0, 0, 0, 0); setOnClickListener { takeAndSaveScreenshot() } }
            sidePanel?.addView(dragHandle); sidePanel?.addView(floatStartBtn); sidePanel?.addView(stopBtn); sidePanel?.addView(statusText); sidePanel?.addView(sensorBtn); sidePanel?.addView(screenshotBtn)
            val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 20; y = 500 }
            dragHandle.setOnTouchListener { _, e -> if (e.action == MotionEvent.ACTION_MOVE) { params.y = (e.rawY - 150).toInt(); windowManager?.updateViewLayout(sidePanel, params) }; true }
            windowManager?.addView(sidePanel, params)
        }
    }

    private fun toggleSensorMode(btn: Button) {
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock?.release()
            btn.setTextColor(Color.CYAN)
            btn.text = "\uD83C\uDF1E"
            updateLog("距離感應模式: 關閉")
        } else {
            try {
                proximityWakeLock?.acquire()
                btn.setTextColor(Color.GREEN)
                btn.text = "\uD83C\uDF1D"
                updateLog("距離感應模式: 開啟")
            } catch (e: Exception) {}
        }
    }

    private fun takeAndSaveScreenshot() {
        scope.launch(Dispatchers.IO) {
            val bitmap = getScreenshot() ?: return@launch
            try {
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val dir = File(path, "PokemonGO").apply { if (!exists()) mkdirs() }
                val file = File(dir, "PGO_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                updateLog("截圖已存至 Pictures/PokemonGO")
            } catch (e: Exception) {}
        }
    }

    private fun initCustomToast() {
        Handler(Looper.getMainLooper()).post {
            customToastView = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; background = GradientDrawable().apply { setColor(Color.parseColor("#DD333333")); cornerRadius = 50f }; setPadding(40, 20, 40, 20); visibility = View.GONE }
            customToastText = TextView(this).apply { text = ""; setTextColor(Color.parseColor("#00E5FF")); textSize = 14f; gravity = Gravity.CENTER }
            customToastView?.addView(customToastText)
            val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = 150 }
            windowManager?.addView(customToastView, params)
        }
    }

    private fun updateLog(msg: String, autoHide: Boolean = true) { mainHandler.post { customToastText?.text = msg; customToastView?.visibility = View.VISIBLE; mainHandler.removeCallbacks(hideToastRunnable); if (autoHide) mainHandler.postDelayed(hideToastRunnable, 2500) } }
    private fun updateStatusUI() { Handler(Looper.getMainLooper()).post { statusText?.text = "$totalCatched / $totalVisited" } }

    private suspend fun mainBotLoop(types: List<String>, rLimit: Int, hLimit: Int, startLat: Double, startLng: Double) {
        var curRefLat = startLat; var curRefLng = startLng; var isFirstScan = true
        while (isBotRunning) {
            withContext(Dispatchers.Main) { if (isFirstScan) { updateLog("首次雷達掃描中"); isFirstScan = false } else updateLog("執行定期雷達掃描流程") }
            delay(2500)
            val allScraped = mutableListOf<Pair<Double, Double>>()
            val blacklist = listOf("arlo", "cliff", "sierra", "decoy", "giovanni")
            if (types.isEmpty()) { try { val html = withContext(Dispatchers.IO) { client.newCall(Request.Builder().url("https://moonani.com/PokeList/rocket.php").build()).execute().body?.string() ?: "" }; val rowMatcher = Pattern.compile("<tr.*?>([\\s\\S]*?)</tr>", Pattern.CASE_INSENSITIVE).matcher(html); while (rowMatcher.find()) { val rowHtml = rowMatcher.group(1)?.lowercase() ?: ""; if (blacklist.any { rowHtml.contains(it) }) continue; val coordMatcher = Pattern.compile("(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+\\.\\d+)").matcher(rowHtml); if (coordMatcher.find()) { val lat = coordMatcher.group(1)?.toDoubleOrNull(); val lng = coordMatcher.group(2)?.toDoubleOrNull(); if (lat != null && lng != null) allScraped.add(lat to lng) } } } catch (e: Exception) {} }
            else { for (t in types) { if (!isBotRunning) break; try { val url = "https://moonani.com/PokeList/rocket.php?type=$t"; val html = withContext(Dispatchers.IO) { client.newCall(Request.Builder().url(url).build()).execute().body?.string() ?: "" }; val rowMatcher = Pattern.compile("<tr.*?>([\\s\\S]*?)</tr>", Pattern.CASE_INSENSITIVE).matcher(html); while (rowMatcher.find()) { val rowHtml = rowMatcher.group(1)?.lowercase() ?: ""; if (blacklist.any { rowHtml.contains(it) }) continue; val coordMatcher = Pattern.compile("(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+\\.\\d+)").matcher(rowHtml); if (coordMatcher.find()) { val lat = coordMatcher.group(1)?.toDoubleOrNull(); val lng = coordMatcher.group(2)?.toDoubleOrNull(); if (lat != null && lng != null) allScraped.add(lat to lng) } } } catch (e: Exception) {}; delay(300) } }
            val targets = allScraped.distinctBy { "${it.first},${it.second}" }.filter { !visitedStops.contains("${it.first},${it.second}") }.toMutableList()
            if (targets.isEmpty()) { updateLog("無新目標"); delay(1500); executeTeleportSilent(startLat, startLng); delay(20000); continue }
            if (healCount >= hLimit) { runHealProcess(); healCount = 0 }
            for (i in 1..rLimit) {
                if (!isBotRunning || targets.isEmpty()) break
                val nearest = targets.minByOrNull { calculateDistance(curRefLat, curRefLng, it.first, it.second) } ?: break; targets.remove(nearest)
                val dist = calculateDistance(curRefLat, curRefLng, nearest.first, nearest.second); val waitSec = getCooldownSec(dist) - (System.currentTimeMillis() - lastActionTime) / 1000
                if (waitSec > 0) { updateLog("剩餘冷卻時間: ${waitSec.toInt()}s"); for (j in waitSec.toInt() downTo 1) { if (!isBotRunning) break; if (j % 10 == 0) updateLog("剩餘冷卻時間: ${j}s"); delay(1000) } }
                val formattedLat = (nearest.first * 100000).toInt() / 100000.0; val formattedLng = (nearest.second * 100000).toInt() / 100000.0
                updateLog("傳送到座標: $formattedLat, $formattedLng"); performLoadingJitter(nearest.first, nearest.second, 10000); executeTeleportSilent(nearest.first, nearest.second); delay(2000); visitedStops.add("${nearest.first},${nearest.second}")
                if (runRocketInteraction()) { curRefLat = nearest.first; curRefLng = nearest.second; lastActionTime = System.currentTimeMillis(); healCount++; if (healCount >= hLimit && i < rLimit) { runHealProcess(); healCount = 0 } } else updateLog("跳過目標")
                totalVisited++; updateStatusUI(); delay(3000)
            }
        }
    }

    private suspend fun backToMap() { for (i in 1..8) { val dm = resources.displayMetrics; val roi = org.opencv.core.Rect(0, (dm.heightPixels * 2 / 3), dm.widthPixels, dm.heightPixels / 3); if (findImageOnScreen("template/map.png", 0.90, roi)) { updateLog("已回到地圖"); return }; performGlobalAction(GLOBAL_ACTION_BACK); delay(1500) } }

    private suspend fun runRocketInteraction(): Boolean {
        if (findImageOnScreen("template/fast.png", 0.70)) { clickAt(PointF(540f, 1630f)); delay(2000) }
        var battleReady = false; val dm = resources.displayMetrics; val lowerRoi = org.opencv.core.Rect(0, (dm.heightPixels * 2 / 3), dm.widthPixels, dm.heightPixels / 3)
        repeat(2) { if (!battleReady && isBotRunning) { updateLog("嘗試點擊火箭隊補給站"); clickAt(PointF(POS_POKESTOP.x + Random.nextInt(-15, 15), POS_POKESTOP.y + Random.nextInt(-15, 15))); delay(2000); var foundStatus = ""; for (j in 1..10) { if (!isBotRunning) break; if (findImageOnScreen("template/battle.png", 0.70, lowerRoi)) { foundStatus = "battle"; break } else if (findImageOnScreen("template/map.png", 0.90, lowerRoi)) { foundStatus = "map"; break } else { performGlobalAction(GLOBAL_ACTION_BACK); delay(1000) } }; if (foundStatus == "battle") { clickAt(POS_BATTLE_BTN); delay(2500); clickAt(POS_CONFIRM_BTN); delay(1000); if (findImageOnScreen("template/dead.png", 0.80, lowerRoi)) { updateLog("執行臨時復活流程"); clickAt(POS_DEAD_BAG); delay(1500); val upperRoi = org.opencv.core.Rect(0, 0, dm.widthPixels, dm.heightPixels / 2); findImagePos("template/resurrect.png", 0.80, upperRoi)?.let { clickAt(PointF(it.x - 50f, it.y + 50f)); delay(1500); clickAt(POS_HEAL_CONFIRM); delay(1500); performGlobalAction(GLOBAL_ACTION_BACK); delay(1500); performGlobalAction(GLOBAL_ACTION_BACK); delay(1500) }; clickAt(POS_CONFIRM_BTN); delay(3000) }; battleReady = true } } }
        if (battleReady) { if (startCombatLoop()) { runCatchProcess(); return true } }
        return false
    }

    private suspend fun startCombatLoop(): Boolean {
        var isWin = false; updateLog("與火箭隊手下對戰中"); var stopBattle = false; val startTime = System.currentTimeMillis(); val dm = resources.displayMetrics; val lowerRoi = org.opencv.core.Rect(0, (dm.heightPixels * 2 / 3), dm.widthPixels, dm.heightPixels / 3)
        coroutineScope {
            val attackJob = launch { while (!stopBattle && isBotRunning) { clickAt(getRandomPointInCircle(PointF(540f, 1400f), 400f), 100L); delay(150L + Random.nextLong(0, 100)); BATTLE_POINTS.forEach { if (!stopBattle && isBotRunning) { clickAt(it, 50L); delay(150L + Random.nextLong(0, 100)) } } } }
            val scannerJob = launch {
                while (!stopBattle && isBotRunning) {
                    if (findImageOnScreen("template/win.png", 0.75, lowerRoi)) { isWin = true; stopBattle = true; break }
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime > 180000) { stopBattle = true; attackJob.cancel(); updateLog("強制返回"); repeat(10) { performGlobalAction(GLOBAL_ACTION_BACK); delay(1200) }; val roi = org.opencv.core.Rect(0, (dm.heightPixels * 2 / 3), dm.widthPixels, dm.heightPixels / 3); if (!findImageOnScreen("template/map.png", 0.90, roi)) { clickAt(PointF(540f, 2050f)); delay(2000); backToMap() }; break }
                    delay(5000)
                }
            }
            scannerJob.join(); attackJob.cancelAndJoin()
        }
        return isWin
    }

    private suspend fun runCatchProcess() {
        updateLog("捕捉暗影寶可夢"); val dm = resources.displayMetrics; val lowerRoi = org.opencv.core.Rect(0, (dm.heightPixels * 2 / 3), dm.widthPixels, dm.heightPixels / 3)
        while (isBotRunning) { if (findImageOnScreen("template/map.png", 0.90, lowerRoi)) break; dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(Path().apply { moveTo(540f / 1080f * dm.widthPixels, 2000f / 2158f * dm.heightPixels); lineTo(540f / 1080f * dm.widthPixels, 800f / 2158f * dm.heightPixels) }, 0, 100)).build(), null, null); delay(2500); if (!findImageOnScreen("template/win.png", 0.70, lowerRoi)) { delay(3000); if (findImageOnScreen("template/catched.png", 0.55)) { val okP = findImagePos("template/OK.png", 0.55, lowerRoi); clickAt(okP ?: PointF(540f, 1500f)); delay(3000); totalCatched++; for (i in 1..8) { if (findImageOnScreen("template/map.png", 0.90, lowerRoi)) break; performGlobalAction(GLOBAL_ACTION_BACK); delay(1000) }; break } } }
    }

    private suspend fun runHealProcess() {
        withContext(Dispatchers.Main) { updateLog("執行定期復活補血流程") }
        delay(1500); clickAt(POS_MENU_BALL); delay(1500); clickAt(POS_BAG_ICON); delay(2000); val dm = resources.displayMetrics; val upperRoi = org.opencv.core.Rect(0, 0, dm.widthPixels, dm.heightPixels / 2)
        findImagePos("template/resurrect.png", 0.75, upperRoi)?.let { clickAt(PointF(it.x - 50f, it.y + 50f)); delay(1500); clickAt(POS_HEAL_CONFIRM); delay(1500); performGlobalAction(GLOBAL_ACTION_BACK); delay(1500) }
        findImagePos("template/medicine.png", 0.75, upperRoi)?.let { clickAt(PointF(it.x - 50f, it.y + 50f)); delay(1500); clickAt(POS_HEAL_CONFIRM); delay(1500); performGlobalAction(GLOBAL_ACTION_BACK); delay(1500) }
        performGlobalAction(GLOBAL_ACTION_BACK); delay(1500)
    }

    private fun clickAt(p: PointF, duration: Long = 150L) { val dm = resources.displayMetrics; val path = Path().apply { moveTo((p.x / 1080f) * dm.widthPixels, (p.y / 2158f) * dm.heightPixels) }; dispatchGesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, duration)).build(), null, null) }
    private fun executeTeleportSilent(lat: Double, lng: Double) { val joyPkg = getJoyStickPackageName(); try { val sIntent = Intent("theappninjas.gpsjoystick.TELEPORT").apply { setPackage(joyPkg); putExtra("lat", lat.toFloat()); putExtra("lng", lng.toFloat()) }; if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(sIntent) else startService(sIntent) } catch (e: Exception) {}; Handler(Looper.getMainLooper()).postDelayed({ sendBroadcast(Intent("com.theappninjas.fakegps.TELEPORT").apply { setPackage(joyPkg); putExtra("lat", lat.toFloat()); putExtra("lng", lng.toFloat()); addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES) }) }, 500) }
    private fun findImagePos(name: String, threshold: Double, roiRect: org.opencv.core.Rect? = null): PointF? { val screen = getScreenshot() ?: return null; val template = try { BitmapFactory.decodeStream(assets.open(name)) } catch (e: Exception) { null } ?: return null; val fullMat = Mat(); val tMat = Mat(); val res = Mat(); try { Utils.bitmapToMat(screen, fullMat); Utils.bitmapToMat(template, tMat); val sMat = if (roiRect != null) { val x = max(0, roiRect.x); val y = max(0, roiRect.y); val w = min(fullMat.cols() - x, roiRect.width); val h = min(fullMat.rows() - y, roiRect.height); if (w > 0 && h > 0) Mat(fullMat, org.opencv.core.Rect(x, y, w, h)) else fullMat } else fullMat; Imgproc.matchTemplate(sMat, tMat, res, Imgproc.TM_CCOEFF_NORMED); val mm = Core.minMaxLoc(res); if (mm.maxVal >= threshold) { val offsetX = if (roiRect != null && sMat != fullMat) roiRect.x else 0; val offsetY = if (roiRect != null && sMat != fullMat) roiRect.y else 0; val finalX = mm.maxLoc.x.toFloat() + tMat.cols() / 2f + offsetX; val finalY = mm.maxLoc.y.toFloat() + tMat.rows() / 2f + offsetY; return PointF(finalX / screen.width * 1080f, finalY / screen.height * 2158f) } } finally { fullMat.release(); tMat.release(); res.release(); screen.recycle(); template.recycle() }; return null }
    private fun findImageOnScreen(name: String, threshold: Double, roiRect: org.opencv.core.Rect? = null) = findImagePos(name, threshold, roiRect) != null
    private fun shutdown() { isBotRunning = false; btnAnimator?.cancel(); if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) stopForeground(STOP_FOREGROUND_REMOVE) else stopForeground(true); if (customToastView != null) try { windowManager?.removeView(customToastView) } catch (e: Exception) {}; if (sidePanel != null) try { windowManager?.removeView(sidePanel) } catch (e: Exception) {}; if (proximityWakeLock?.isHeld == true) proximityWakeLock?.release(); stopSelf(); android.os.Process.killProcess(android.os.Process.myPid()) }
    private fun setupForegroundNotification() { val chan = NotificationChannel("BOT", "BTR", NotificationManager.IMPORTANCE_LOW); getSystemService(NotificationManager::class.java).createNotificationChannel(chan); startForeground(1, Notification.Builder(this, "BOT").setContentTitle("運行中").setSmallIcon(android.R.drawable.ic_menu_mylocation).build()) }
    private fun getJoyStickPackageName() = packageManager.queryIntentActivities(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0")), 0).find { !it.activityInfo.packageName.contains("google") }?.activityInfo?.packageName ?: "com.theappninjas.fakegpsjoystick"
    private fun getScreenshot(): Bitmap? { val image = try { imageReader?.acquireLatestImage() } catch (e: Exception) { null } ?: return null; val plane = image.planes[0]; val bitmap = Bitmap.createBitmap(image.width + (plane.rowStride - plane.pixelStride * image.width) / plane.pixelStride, image.height, Bitmap.Config.ARGB_8888); bitmap.copyPixelsFromBuffer(plane.buffer); image.close(); return bitmap }
    private fun launchGame() { packageManager.getLaunchIntentForPackage("com.nianticlabs.pokemongo")?.let { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(it) } }
    private fun setupImageReader() { val dm = resources.displayMetrics; imageReader = ImageReader.newInstance(dm.widthPixels, dm.heightPixels, PixelFormat.RGBA_8888, 2); virtualDisplay = projection?.createVirtualDisplay("Bot", dm.widthPixels, dm.heightPixels, dm.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader?.surface, null, null) }
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double { val r = 6371.0; val a = sin(Math.toRadians(lat2 - lat1) / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(Math.toRadians(lon2 - lon1) / 2).pow(2); return r * 2 * atan2(sqrt(a), sqrt(1 - a)) }
    private fun getCooldownSec(dist: Double): Long { val table = listOf(1.0 to 0.5, 2.0 to 1.0, 4.0 to 2.0, 10.0 to 8.0, 15.0 to 11.0, 20.0 to 13.0, 25.0 to 15.0, 30.0 to 18.0, 40.0 to 22.0, 45.0 to 23.0, 60.0 to 25.0, 80.0 to 27.0, 100.0 to 30.0, 250.0 to 45.0, 500.0 to 65.0, 1000.0 to 100.0, 1250.0 to 118.0); for (item in table) if (dist <= item.first) return max(0, (item.second * 60).toLong() - 10); return 7190L }
    private fun getRandomPointInCircle(center: PointF, radius: Float): PointF = PointF(center.x + Random.nextFloat() * 50, center.y + Random.nextFloat() * 50)
    private suspend fun performLoadingJitter(lat: Double, lng: Double, dur: Long) { val s = System.currentTimeMillis(); while (System.currentTimeMillis() - s < dur) { if (!isBotRunning) break; executeTeleportSilent(lat + Random.nextDouble(-0.0004, 0.0004), lng + Random.nextDouble(-0.0004, 0.0004)); delay(1500) }; executeTeleportSilent(lat, lng) }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}