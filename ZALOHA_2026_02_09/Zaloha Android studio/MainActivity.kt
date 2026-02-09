package com.example.vrturist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.vrturist.ui.theme.VRTuristTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.Locale

// --- UI DÁTOVÉ TRIEDY ---
data class UiRouteStat(val name: String, val steps: Int)
data class UiDailyStat(val date: String, val steps: Int)
data class UiAchievement(val threshold: Int, val title: String, val description: String = "")
data class UiRouteMetaInfo(val totalSteps: Int, val totalDistKm: Float, val totalPositions: Int)

// 🔥 ÚDAJE O DĹŽKE TRÁS
val ROUTE_METADATA_UI = mapOf(
    "bulikova_lada116" to UiRouteMetaInfo(3800, 2.71f, 380),
    "bulikova_eurovea" to UiRouteMetaInfo(5200, 3.65f, 520)
)

val STEP_MILESTONES_UI = listOf(
    UiAchievement(100, "Prvý krok", "Tvoja cesta začína."),
    UiAchievement(1000, "Rozcvička", "Prvých 1000 krokov."),
    UiAchievement(5000, "Turista", "Slušná prechádzka."),
    UiAchievement(10000, "Denný cieľ", "Splnený štandard."),
    UiAchievement(25000, "Víkendový bojovník", "Už to myslíš vážne."),
    UiAchievement(50000, "Maratónec", "Nohy z ocele."),
    UiAchievement(100000, "Stovka", "100 000 krokov!"),
    UiAchievement(1000000, "Milionár", "Milión krokov. Legenda.")
)

val CALORIE_MILESTONES_UI = listOf(
    UiAchievement(1000, "Prvé kalórie", "Spálené prvé kalórie."),
    UiAchievement(2500, "Hamburger", "Spálil si jeden burger."),
    UiAchievement(5000, "Pizza Večer", "Zaslúžená pizza."),
    UiAchievement(10000, "Jeden Kilogram", "Cca 1kg tuku dole."),
    UiAchievement(25000, "Nedeľný Obed", "Veľká porcia spálená."),
    UiAchievement(50000, "Fitness Začiatočník", "Si vo forme.")
)

val DAILY_MILESTONES_UI = listOf(
    UiAchievement(10000, "Dnešný Štandard", "10k denne."),
    UiAchievement(20000, "Dvojitá Dávka", "20k denne."),
    UiAchievement(30000, "Výletník", "30k denne.")
)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private val db by lazy { Firebase.database.reference }
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tts = TextToSpeech(this, this)

        setContent {
            VRTuristTheme {
                RequestPermissionsAndStartApp(
                    db = db,
                    onCommand = { action, routeId -> sendCommandToService(action, routeId) },
                    onSpeak = { text -> speakOut(text) }
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale("sk", "SK"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Slovenčina nie je podporovaná!")
            } else {
                isTtsReady = true
            }
        } else {
            Log.e("TTS", "Inicializácia zlyhala!")
        }
    }

    private fun speakOut(text: String) {
        if (isTtsReady) {
            tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    private fun sendCommandToService(action: String, routeId: String? = null) {
        try {
            val intent = Intent(this, StepService::class.java).apply {
                this.action = action
                if (routeId != null) putExtra(StepService.EXTRA_ROUTE_ID, routeId)
            }
            ContextCompat.startForegroundService(this, intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Chyba služby: ${e.message}")
        }
    }
}

@Composable
fun RequestPermissionsAndStartApp(
    db: com.google.firebase.database.DatabaseReference,
    onCommand: (String, String?) -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionsToRequest = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            permissionsGranted = perms.values.all { it }
            if (!permissionsGranted) {
                Toast.makeText(context, "Sú potrebné povolenia!", Toast.LENGTH_LONG).show()
            } else {
                try {
                    val intent = Intent(context, StepService::class.java)
                    ContextCompat.startForegroundService(context, intent)
                } catch (e: Exception) {
                    Log.e("Perms", "Služba neštartuje: ${e.message}")
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            permissionsGranted = true
            try {
                val intent = Intent(context, StepService::class.java)
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }

    if (permissionsGranted) {
        AppNavigation(db, onCommand, onSpeak)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Čakám na povolenia...", fontWeight = FontWeight.Bold)
        }
    }
}

enum class Screen { HOME, LEVELS, HISTORY, ACHIEVEMENTS, PROFILE }

@Composable
fun AppNavigation(
    db: com.google.firebase.database.DatabaseReference,
    onCommand: (String, String?) -> Unit,
    onSpeak: (String) -> Unit
) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var currentRouteId by rememberSaveable { mutableStateOf("bulikova_lada116") }

    var steps by remember { mutableStateOf(0) }
    var position by remember { mutableStateOf(1) }
    var traveledDistance by remember { mutableStateOf(0.0f) }

    LaunchedEffect(currentRouteId) {
        val routeRef = db.child("routes").child(currentRouteId)
        onCommand(StepService.ACTION_SET_ROUTE, currentRouteId)
        db.child("currentRouteId").setValue(currentRouteId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                steps = snapshot.child("steps").getValue(Int::class.java) ?: 0
                position = snapshot.child("position").getValue(Int::class.java) ?: 1
                val distVal = snapshot.child("traveledDistance").getValue(Double::class.java)
                traveledDistance = distVal?.toFloat() ?: 0.0f
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        routeRef.addValueEventListener(listener)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Default.Home, "Domov") }, label = { Text("Domov") }, selected = currentScreen == Screen.HOME, onClick = { currentScreen = Screen.HOME })
                NavigationBarItem(icon = { Icon(Icons.AutoMirrored.Filled.List, "Úrovne") }, label = { Text("Úrovne") }, selected = currentScreen == Screen.LEVELS, onClick = { currentScreen = Screen.LEVELS })
                NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "História") }, label = { Text("História") }, selected = currentScreen == Screen.HISTORY, onClick = { currentScreen = Screen.HISTORY })
                NavigationBarItem(icon = { Icon(Icons.Default.Star, "Úspechy") }, label = { Text("Úspechy") }, selected = currentScreen == Screen.ACHIEVEMENTS, onClick = { currentScreen = Screen.ACHIEVEMENTS })
                NavigationBarItem(icon = { Icon(Icons.Default.Person, "Profil") }, label = { Text("Profil") }, selected = currentScreen == Screen.PROFILE, onClick = { currentScreen = Screen.PROFILE })
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(currentRouteId, { currentRouteId = it }, steps, position, traveledDistance, onCommand, db, onSpeak)
                Screen.LEVELS -> LevelsScreen(db)
                Screen.HISTORY -> HistoryScreen(db)
                Screen.ACHIEVEMENTS -> AchievementsScreen(db)
                Screen.PROFILE -> ProfileScreen(db)
            }
        }
    }
}

@Composable
fun HomeScreen(
    currentRouteId: String,
    onRouteChanged: (String) -> Unit,
    steps: Int,
    position: Int,
    distance: Float,
    onCommand: (String, String?) -> Unit,
    db: com.google.firebase.database.DatabaseReference,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val scrollState = rememberScrollState()
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var currentPoi by remember { mutableStateOf<PointOfInterest?>(null) }
    var showPoiListDialog by remember { mutableStateOf(false) }
    var showGoalsListDialog by remember { mutableStateOf(false) }

    // 🔥 NOVÉ: Stav pre dokončenie trasy
    var isRouteCompleted by remember { mutableStateOf(false) }

    var totalGlobalSteps by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        db.child("routes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var sum = 0
                for (route in snapshot.children) sum += route.child("steps").getValue(Int::class.java) ?: 0
                totalGlobalSteps = sum
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 🔥 NOVÉ: Sledovanie, či je trasa dokončená (isCompleted) z Firebase
    LaunchedEffect(currentRouteId) {
        db.child("routes").child(currentRouteId).child("isCompleted").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isRouteCompleted = snapshot.getValue(Boolean::class.java) ?: false
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val routeMeta = ROUTE_METADATA_UI[currentRouteId] ?: UiRouteMetaInfo(10000, 5.0f, 1000)

    // Logika prepočtu pozície
    LaunchedEffect(steps, position) {
        // Ak ešte nie sme v cieli, prepočítavame
        if (!isRouteCompleted && steps > 100 && position <= 1) {
            val correctPosition = (steps / 10) + 1
            val correctDistance = steps * 0.00075
            db.child("routes").child(currentRouteId).child("position").setValue(correctPosition)
            db.child("routes").child(currentRouteId).child("traveledDistance").setValue(correctDistance)
        }

        // 🔥 NOVÉ: Kontrola, či sme dorazili do cieľa
        if (!isRouteCompleted && position >= routeMeta.totalPositions && steps > 100) {
            // 1. Zamknúť trasu
            db.child("routes").child(currentRouteId).child("isCompleted").setValue(true)

            // 2. Vypočítať XP (Base + bonus)
            val baseXp = 500 // Základ za dokončenie
            val bonusXp = (steps / 1000) * 10
            val totalEarnedXp = baseXp + bonusXp

            // 3. Zapísať do histórie (Completed Routes)
            val historyData = mapOf(
                "routeId" to currentRouteId,
                "date" to ServerValue.TIMESTAMP,
                "steps" to steps,
                "distanceKm" to distance,
                "xpEarned" to totalEarnedXp
            )
            db.child("userProfile").child("completedRoutes").push().setValue(historyData)

            // 4. Pridať XP užívateľovi
            db.child("userProfile").child("totalXp").get().addOnSuccessListener { snap ->
                val currentXp = snap.getValue(Int::class.java) ?: 0
                db.child("userProfile").child("totalXp").setValue(currentXp + totalEarnedXp)
            }

            // 5. Zastaviť nahrávanie
            onCommand(StepService.ACTION_STOP_TRACKING, null)
            isRecording = false

            Toast.makeText(context, "GRATULUJEME! Trasa dokončená! (+$totalEarnedXp XP)", Toast.LENGTH_LONG).show()
        }
    }

    var maxTriggeredSteps by remember { mutableStateOf(0) }
    var isMaxStepsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(currentRouteId) {
        isRecording = false
        isMaxStepsLoaded = false
        db.child("routes").child(currentRouteId).child("maxTriggeredSteps").get().addOnSuccessListener {
            maxTriggeredSteps = it.getValue(Int::class.java) ?: 0
            isMaxStepsLoaded = true
        }
    }

    LaunchedEffect(steps, currentRouteId, isMaxStepsLoaded) {
        if (isMaxStepsLoaded && !isRouteCompleted) {
            val pois = ROUTE_POIS[currentRouteId] ?: emptyList()
            val poi = pois.findLast { steps >= it.requiredSteps && it.requiredSteps > maxTriggeredSteps }

            if (poi != null) {
                currentPoi = poi
                maxTriggeredSteps = poi.requiredSteps
                db.child("routes").child(currentRouteId).child("maxTriggeredSteps").setValue(poi.requiredSteps)

                val xpReward = LevelSystem.calculatePoiXpReward(poi.requiredSteps)

                if (xpReward > 0) {
                    Toast.makeText(context, "Získal si $xpReward XP!", Toast.LENGTH_LONG).show()
                    db.child("userProfile").child("totalXp").get().addOnSuccessListener { snap ->
                        val currentXp = snap.getValue(Int::class.java) ?: 0
                        val newXp = currentXp + xpReward
                        db.child("userProfile").child("totalXp").setValue(newXp)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("VR Turista", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Vyber trasu:", fontSize = 14.sp, color = Color.Gray)
        RouteDropdown(selected = currentRouteId, onSelect = { onRouteChanged(it) })

        val description = ROUTE_DESCRIPTIONS[currentRouteId] ?: ""
        if (description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = description, modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontStyle = FontStyle.Italic, color = Color(0xFF0277BD), textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 NOVÉ: Ak je trasa hotová, ukážeme Gratuláciu namiesto štatistík
        if (isRouteCompleted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), // Svetlozelená
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Text("GRATULUJEME!", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trasa úspešne dokončená.", fontSize = 16.sp)
                    Text("XP a štatistiky boli uložené.", fontSize = 14.sp, color = Color.Gray)
                }
            }
        } else {
            // Klasická karta so štatistikami
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Aktuálny stav", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(12.dp))
                    StatLine("Kroky:", "$steps / ${routeMeta.totalSteps}")
                    StatLine("Pozícia:", "$position / ${routeMeta.totalPositions}")
                    StatLine("Vzdialenosť:", "%.2f km / %.2f km".format(distance, routeMeta.totalDistKm))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        NextGoalCard(totalGlobalSteps, onShowAll = { showGoalsListDialog = true })

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { showPoiListDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1976D2))
        ) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Itinerár trasy (Body záujmu)")
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 NOVÉ: Tlačidlo ŠTART zobrazujeme len ak trasa NIE JE hotová
        if (!isRouteCompleted) {
            Button(
                onClick = {
                    if (isRecording) { onCommand(StepService.ACTION_STOP_TRACKING, null); isRecording = false }
                    else { onCommand(StepService.ACTION_START_TRACKING, null); isRecording = true }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color(0xFFD32F2F) else Color(0xFF388E3C)),
                modifier = Modifier.size(width = 200.dp, height = 80.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text(if (isRecording) "STOP" else "ŠTART", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

            Text(if (isRecording) "🔴 Nahrávam" else "⚪ Pauza", color = if(isRecording) Color.Red else Color.Gray, modifier = Modifier.padding(top=8.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 🔥 NOVÉ: Reset musí odomknúť trasu (isCompleted = false)
        Button(
            onClick = { if (!isRecording) {
                db.child("routes").child(currentRouteId).child("steps").setValue(0)
                db.child("routes").child(currentRouteId).child("position").setValue(1)
                db.child("routes").child(currentRouteId).child("traveledDistance").setValue(0.0)
                db.child("routes").child(currentRouteId).child("maxTriggeredSteps").setValue(0)

                // Odomknutie trasy
                db.child("routes").child(currentRouteId).child("isCompleted").setValue(false)

                maxTriggeredSteps = 0
            }},
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            enabled = !isRecording,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Resetovať trasu")
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showPoiListDialog) {
        val routePois = ROUTE_POIS[currentRouteId] ?: emptyList()
        AlertDialog(
            onDismissRequest = { showPoiListDialog = false },
            title = { Text("Itinerár trasy", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(routePois) { poi ->
                        val isUnlocked = steps >= poi.requiredSteps
                        val xpReward = LevelSystem.calculatePoiXpReward(poi.requiredSteps)
                        PoiListItem(poi, isUnlocked, xpReward, steps, onSpeak)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPoiListDialog = false }) { Text("Zavrieť") } }
        )
    }

    if (showGoalsListDialog) {
        AlertDialog(
            onDismissRequest = { showGoalsListDialog = false },
            title = { Text("Všetky ciele", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    item { Text("🏆 KROKOVÉ CIELE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), modifier = Modifier.padding(vertical = 8.dp)) }
                    items(STEP_MILESTONES_UI) { ach ->
                        val isUnlocked = totalGlobalSteps >= ach.threshold
                        AchievementDialogItem(ach, isUnlocked)
                    }
                    item { Text("🔥 SPÁLENÉ KALÓRIE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
                    items(CALORIE_MILESTONES_UI) { ach ->
                        val totalCalories = (totalGlobalSteps * 0.04 * 1.0).toInt()
                        val isUnlocked = totalCalories >= ach.threshold
                        AchievementDialogItem(ach, isUnlocked)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showGoalsListDialog = false }) { Text("Zavrieť") } }
        )
    }

    if (currentPoi != null) {
        val xpReward = LevelSystem.calculatePoiXpReward(currentPoi!!.requiredSteps)
        AlertDialog(
            onDismissRequest = { currentPoi = null },
            icon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF1976D2)) },
            title = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(currentPoi!!.title, fontWeight = FontWeight.Bold); if (xpReward > 0) Text("+$xpReward XP", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 14.sp) } },
            text = { Column { Text("Našiel si zaujímavé miesto.", fontSize = 14.sp, color = Color.Gray); Spacer(modifier = Modifier.height(8.dp)); Text(currentPoi!!.text, fontSize = 16.sp, textAlign = TextAlign.Center) } },
            confirmButton = { Button(onClick = { onSpeak(currentPoi!!.text) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Prehrať hlasom") } },
            dismissButton = { TextButton(onClick = { currentPoi = null }) { Text("Zavrieť") } }
        )
    }
}

@Composable
fun LevelsScreen(db: com.google.firebase.database.DatabaseReference) {
    var totalXp by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { db.child("userProfile").child("totalXp").addValueEventListener(object : ValueEventListener { override fun onDataChange(snapshot: DataSnapshot) { totalXp = snapshot.getValue(Int::class.java) ?: 0 }; override fun onCancelled(error: DatabaseError) {} }) }
    val currentLevel = LevelSystem.getLevelFromXp(totalXp); val ranks = LevelSystem.UNIQUE_LEVELS
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp)); Text("Prehľad Úrovní", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.align(Alignment.CenterHorizontally)); Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) { items(ranks) { levelDef -> LevelRankItem(levelDef, currentLevel >= levelDef.level, currentLevel == levelDef.level) } }
    }
}

@Composable
fun HistoryScreen(db: com.google.firebase.database.DatabaseReference) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE) }
    val weight = prefs.getFloat("userWeight", 70f)

    var totalAllSteps by remember { mutableStateOf(0) }
    var dailyStatsList by remember { mutableStateOf(emptyList<UiDailyStat>()) }
    var routeStatsList by remember { mutableStateOf(emptyList<UiRouteStat>()) }

    LaunchedEffect(Unit) {
        db.child("routes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var sum = 0
                val tempList = mutableListOf<UiRouteStat>()
                for (routeSnapshot in snapshot.children) {
                    val steps = routeSnapshot.child("steps").getValue(Int::class.java) ?: 0
                    sum += steps
                    if (steps > 0) {
                        val niceName = ROUTE_NAMES[routeSnapshot.key] ?: routeSnapshot.key ?: "Trasa"
                        tempList.add(UiRouteStat(niceName, steps))
                    }
                }
                totalAllSteps = sum
                routeStatsList = tempList
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        db.child("dailyStats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<UiDailyStat>()
                for (daySnapshot in snapshot.children) {
                    val date = daySnapshot.key ?: "Neznámy"
                    val steps = daySnapshot.child("steps").getValue(Int::class.java) ?: 0
                    tempList.add(UiDailyStat(date, steps))
                }
                dailyStatsList = tempList.sortedByDescending { it.date }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val totalCalories = (totalAllSteps * 0.04 * (weight / 70.0)).toInt()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Moja Aktivita", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
            Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KROKY", fontSize = 12.sp, color = Color.Gray)
                    Text("$totalAllSteps", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KCAL", fontSize = 12.sp, color = Color.Gray)
                    Text("$totalCalories", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            item {
                Text("Denná aktivita:", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
            }
            items(dailyStatsList) { day -> DailyStatItem(day, weight) }

            item {
                Text("História trás:", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
            }
            items(routeStatsList) { route -> RouteStatItem(route, weight) }
        }
    }
}

@Composable
fun AchievementsScreen(db: com.google.firebase.database.DatabaseReference) {
    val context = LocalContext.current; val prefs = remember { context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE) }
    val weight = prefs.getFloat("userWeight", 70f); var totalAllSteps by remember { mutableStateOf(0) }; var maxDailySteps by remember { mutableStateOf(0) }
    var selectedAchievement by remember { mutableStateOf<UiAchievement?>(null) }; var selectedCategoryType by remember { mutableStateOf("") }
    val totalCalories = (totalAllSteps * 0.04 * (weight / 70.0)).toInt()
    LaunchedEffect(Unit) { db.child("routes").addValueEventListener(object : ValueEventListener { override fun onDataChange(snapshot: DataSnapshot) { var sum = 0; for (route in snapshot.children) sum += route.child("steps").getValue<Int>() ?: 0; totalAllSteps = sum }; override fun onCancelled(error: DatabaseError) {} }); db.child("dailyStats").addValueEventListener(object : ValueEventListener { override fun onDataChange(snapshot: DataSnapshot) { var max = 0; for (day in snapshot.children) { val s = day.child("steps").getValue<Int>() ?: 0; if (s > max) max = s }; maxDailySteps = max }; override fun onCancelled(error: DatabaseError) {} }) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp)); Text("Sieň slávy", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), modifier = Modifier.align(Alignment.CenterHorizontally)); Spacer(modifier = Modifier.height(10.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) { Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("KROKY", fontSize = 12.sp, color = Color.Gray); Text(formatSteps(totalAllSteps), fontWeight = FontWeight.Bold, fontSize = 20.sp) }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("KCAL", fontSize = 12.sp, color = Color.Gray); Text(formatSteps(totalCalories), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFD32F2F)) } } }
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = { GridItemSpan(3) }) { Text("🏆 Celkové kroky", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), modifier = Modifier.padding(vertical = 8.dp)) }
            items(STEP_MILESTONES_UI) { achievement -> AchievementGridItem(achievement, totalAllSteps >= achievement.threshold, Icons.Default.Star) { selectedAchievement = achievement; selectedCategoryType = "TOTAL_STEPS" } }
            item(span = { GridItemSpan(3) }) { Text("🔥 Spálené kalórie", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
            items(CALORIE_MILESTONES_UI) { achievement -> AchievementGridItem(achievement, totalCalories >= achievement.threshold, Icons.Default.Favorite) { selectedAchievement = achievement; selectedCategoryType = "TOTAL_CALORIES" } }
            item(span = { GridItemSpan(3) }) { Text("📅 Denný rekord", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) }
            items(DAILY_MILESTONES_UI) { achievement -> AchievementGridItem(achievement, maxDailySteps >= achievement.threshold, Icons.Default.DateRange) { selectedAchievement = achievement; selectedCategoryType = "DAILY_STEPS" } }
        }
    }
    if (selectedAchievement != null) {
        val ach = selectedAchievement!!; val currentValue = when(selectedCategoryType) { "TOTAL_CALORIES" -> totalCalories; "DAILY_STEPS" -> maxDailySteps; else -> totalAllSteps }; val unit = if (selectedCategoryType == "TOTAL_CALORIES") "kcal" else "kr."; val isUnlocked = currentValue >= ach.threshold; val progress = if (isUnlocked) 1.0f else (currentValue.toFloat() / ach.threshold.toFloat())
        AlertDialog(onDismissRequest = { selectedAchievement = null }, icon = { Icon(imageVector = if (isUnlocked) Icons.Default.Star else Icons.Default.Lock, contentDescription = null, tint = if (isUnlocked) Color(0xFFFFA000) else Color.Gray, modifier = Modifier.size(40.dp)) }, title = { Text(ach.title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }, text = { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(ach.description, textAlign = TextAlign.Center); Spacer(modifier = Modifier.height(16.dp)); LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = if (isUnlocked) Color(0xFF4CAF50) else Color(0xFF1976D2)); Spacer(modifier = Modifier.height(8.dp)); Text(if (isUnlocked) "SPLNENÉ! 🎉" else "Aktuálne: ${formatSteps(currentValue)} / ${formatSteps(ach.threshold)} $unit", fontWeight = FontWeight.Bold) } }, confirmButton = { TextButton(onClick = { selectedAchievement = null }) { Text("Zavrieť") } })
    }
}

@Composable
fun ProfileScreen(db: com.google.firebase.database.DatabaseReference) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("UserProfile", Context.MODE_PRIVATE) }

    var userName by remember { mutableStateOf(prefs.getString("userName", "Turista") ?: "Turista") }
    var weightInput by remember { mutableStateOf(prefs.getFloat("userWeight", 70f).toString()) }
    var heightInput by remember { mutableStateOf(prefs.getFloat("userHeight", 175f).toString()) }

    val weight = weightInput.toFloatOrNull() ?: 70f

    var totalXp by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        db.child("userProfile").child("weight").setValue(weight)
        db.child("userProfile").child("totalXp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalXp = snapshot.getValue(Int::class.java) ?: 0
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    val currentLevel = LevelSystem.getLevelFromXp(totalXp)
    val rankTitle = LevelSystem.getRankTitle(currentLevel)
    val progress = LevelSystem.getProgressToNextLevel(totalXp)
    val nextLevelXp = LevelSystem.getXpRequiredForLevel(currentLevel + 1)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Nastavenia Profilu", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(10.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Level $currentLevel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                Text(rankTitle, fontSize = 16.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp), color = Color(0xFF43A047), trackColor = Color(0xFFC8E6C9))
                Spacer(modifier = Modifier.height(4.dp))
                Text("$totalXp / $nextLevelXp XP", fontSize = 12.sp, color = Color(0xFF1B5E20))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = {
                    weightInput = it
                    it.toFloatOrNull()?.let { w ->
                        prefs.edit().putFloat("userWeight", w).apply()
                        db.child("userProfile").child("weight").setValue(w)
                    }
                },
                label = { Text("Váha (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = heightInput,
                onValueChange = {
                    heightInput = it
                    it.toFloatOrNull()?.let { h ->
                        prefs.edit().putFloat("userHeight", h).apply()
                    }
                },
                label = { Text("Výška (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = userName,
            onValueChange = {
                userName = it
                prefs.edit().putString("userName", it).apply()
            },
            label = { Text("Prezývka") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                db.child("userProfile").child("totalXp").setValue(0)
                Toast.makeText(context, "Level bol resetovaný (0 XP)", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Text("Resetovať Level (XP)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.DarkGray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color.Black)
    }
}

@Composable
fun PoiListItem(poi: PointOfInterest, isUnlocked: Boolean, xpReward: Int, currentSteps: Int, onSpeak: (String) -> Unit) {
    val bgColor = if (isUnlocked) Color(0xFFF1F8E9) else Color(0xFFF5F5F5)
    val textColor = if (isUnlocked) Color(0xFF1B5E20) else Color(0xFF455A64)
    val borderColor = if (isUnlocked) Color(0xFF81C784) else Color.LightGray

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, borderColor, RoundedCornerShape(8.dp)), colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).background(if (isUnlocked) Color(0xFF4CAF50) else Color.LightGray, CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(poi.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                if (isUnlocked) { Text(poi.text, fontSize = 12.sp, color = Color(0xFF333333), maxLines = 2, lineHeight = 14.sp) } else {
                    val remaining = poi.requiredSteps - currentSteps
                    Text("Cieľ: ${poi.requiredSteps} kr. (Chýba: $remaining)", fontSize = 11.sp, color = Color(0xFF616161), fontStyle = FontStyle.Italic)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (xpReward > 0) { Surface(color = if (isUnlocked) Color(0xFFFFF3E0) else Color(0xFFEEEEEE), shape = RoundedCornerShape(4.dp)) { Text("+$xpReward XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isUnlocked) Color(0xFFE65100) else Color(0xFF616161), modifier = Modifier.padding(4.dp)) } }
                if (isUnlocked) { Spacer(modifier = Modifier.height(4.dp)); IconButton(onClick = { onSpeak(poi.text) }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF2E7D32)) } }
            }
        }
    }
}

@Composable
fun NextGoalCard(totalSteps: Int, onShowAll: () -> Unit = {}) {
    val nextMilestone = STEP_MILESTONES_UI.firstOrNull { it.threshold > totalSteps }
    if (nextMilestone != null) {
        val progress = totalSteps.toFloat() / nextMilestone.threshold.toFloat()
        val remaining = nextMilestone.threshold - totalSteps
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFA000)); Spacer(modifier = Modifier.width(8.dp)); Text("Najbližší cieľ:", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0)) }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = nextMilestone.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0D47A1), modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Color(0xFF4CAF50), trackColor = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("$totalSteps / ${nextMilestone.threshold}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black); Text("(-$remaining)", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onShowAll, modifier = Modifier.fillMaxWidth().height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1565C0)), border = BorderStroke(1.dp, Color(0xFFBBDEFB))) { Text("Zobraziť všetky ciele", fontSize = 12.sp) }
            }
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700))) { Text("🎉 Všetky ciele splnené!", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color.Black) }
    }
}

@Composable
fun AchievementDialogItem(ach: UiAchievement, isUnlocked: Boolean) {
    val bgColor = if (isUnlocked) Color(0xFFFFF8E1) else Color(0xFFFAFAFA)
    val iconTint = if (isUnlocked) Color(0xFFFFC107) else Color.LightGray
    val textColor = if (isUnlocked) Color.Black else Color.Gray
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, if(isUnlocked) Color(0xFFFFE082) else Color.LightGray, RoundedCornerShape(8.dp)), colors = CardDefaults.cardColors(containerColor = bgColor)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = iconTint, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(ach.title, fontWeight = FontWeight.Bold, color = textColor)
                Text("${ach.threshold} krokov", fontSize = 12.sp, color = if(isUnlocked) Color(0xFFE65100) else Color.Gray)
            }
        }
    }
}

@Composable
fun RouteDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(ROUTE_NAMES[selected] ?: selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ROUTE_NAMES.forEach { (id, label) -> DropdownMenuItem(text = { Text(label) }, onClick = { expanded = false; onSelect(id) }) }
        }
    }
}

@Composable
fun LevelRankItem(levelDef: LevelDefinition, isUnlocked: Boolean, isCurrent: Boolean) {
    val bgColor = if (isUnlocked) Color(0xFFE8F5E9) else Color(0xFFEEEEEE); val titleColor = if (isUnlocked) Color(0xFF1B5E20) else Color.Gray
    val borderColor = if (isCurrent) Color(0xFF43A047) else Color.Transparent; val iconTint = if (isUnlocked) Color(0xFF2E7D32) else Color.Gray
    Card(modifier = Modifier.fillMaxWidth().border(if (isCurrent) 2.dp else 0.dp, borderColor, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(if (isUnlocked) 4.dp else 0.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).background(Color.White, CircleShape).border(1.dp, if(isUnlocked) Color(0xFFC8E6C9) else Color.LightGray, CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = getRankIcon(levelDef.iconName), contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp)) }
            Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(levelDef.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = titleColor); if (isCurrent) { Spacer(modifier = Modifier.width(8.dp)); Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(4.dp)) { Text("AKTUÁLNE", fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold) } } else if (!isUnlocked) { Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray) } }
            Text(levelDef.description, fontSize = 13.sp, color = if(isUnlocked) Color(0xFF333333) else Color.Gray, fontStyle = if(isUnlocked) FontStyle.Normal else FontStyle.Italic); Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Odomkne sa na Leveli ${levelDef.level}", fontSize = 12.sp, color = if(isUnlocked) Color(0xFF1B5E20) else Color.Gray, fontWeight = FontWeight.SemiBold); Spacer(modifier = Modifier.weight(1f)); val xpNeeded = LevelSystem.getXpRequiredForLevel(levelDef.level); Text("${formatSteps(xpNeeded)} XP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(isUnlocked) Color(0xFFE65100) else Color.Gray) }
        }
        }
    }
}

@Composable
fun AchievementGridItem(achievement: UiAchievement, isUnlocked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val bgColor = if (isUnlocked) Color(0xFFFFD700) else Color(0xFFEEEEEE)
    val textColor = if (isUnlocked) Color.Black else Color.Gray.copy(alpha = 0.6f)
    val iconColor = if (isUnlocked) Color(0xFFFFA000) else Color.LightGray
    val valueColor = if (isUnlocked) Color(0xFFBF360C) else textColor
    Card(modifier = Modifier.height(140.dp).clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(if (isUnlocked) 6.dp else 0.dp)) {
        Column(modifier = Modifier.padding(8.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(40.dp).background(iconColor, CircleShape), contentAlignment = Alignment.Center) { Icon(imageVector = if (isUnlocked) icon else Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.height(8.dp)); Text(text = achievement.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor, textAlign = TextAlign.Center, lineHeight = 12.sp)
            Spacer(modifier = Modifier.height(4.dp)); Text(text = formatSteps(achievement.threshold), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = valueColor, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun DailyStatItem(day: UiDailyStat, userWeight: Float) {
    val calories = (day.steps * 0.04 * (userWeight / 70.0)).toInt()
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column { Text(day.date, fontWeight = FontWeight.Bold); Text("Cieľ: ${((day.steps.toFloat()/10000)*100).toInt()}%", fontSize = 10.sp, color = Color.Gray) }
            Row { Text("${day.steps} kr.", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C)); Spacer(modifier = Modifier.width(10.dp)); Text("$calories kcal", fontSize = 12.sp, color = Color.Gray) }
        }
    }
}

@Composable
fun RouteStatItem(route: UiRouteStat, userWeight: Float) {
    val routeCalories = (route.steps * 0.04 * (userWeight / 70.0)).toInt()
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) { Text("TRASA", fontSize = 10.sp, color = Color(0xFF90CAF9), fontWeight = FontWeight.Bold); Text(route.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) }
            Column(horizontalAlignment = Alignment.End) {
                Surface(color = Color(0xFF0D47A1), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(bottom = 4.dp)) { Text("${route.steps} kr.", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                Surface(color = Color(0xFFEF6C00), shape = RoundedCornerShape(8.dp)) { Text("$routeCalories kcal", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

fun getRankIcon(iconName: String): ImageVector { return when (iconName) { "SOFA" -> Icons.Default.Home; "WALK" -> Icons.AutoMirrored.Filled.List; "HIKE" -> Icons.Default.Place; "MAP" -> Icons.Default.LocationOn; "COMPASS" -> Icons.Default.Search; "STAR" -> Icons.Default.Star; "TROPHY" -> Icons.Default.ThumbUp; else -> Icons.Default.Star } }

fun formatSteps(steps: Int): String { return when { steps >= 1000000 -> String.format("%.1fM", steps / 1000000.0); steps >= 1000 -> "${steps / 1000}k"; else -> "$steps" } }