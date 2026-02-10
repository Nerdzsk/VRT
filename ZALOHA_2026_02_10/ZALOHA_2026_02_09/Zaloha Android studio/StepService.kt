package com.example.vrturist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepService : Service(), SensorEventListener {

    companion object {
        const val TAG = "VR_SERVICE"
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val ACTION_SET_ROUTE = "ACTION_SET_ROUTE"
        const val EXTRA_ROUTE_ID = "routeId"

        private const val PREF_ACTIVE_ROUTE = "activeRouteId"
        private const val PREF_LAST_SENSOR_VAL = "last_sensor_val"
        private const val PREF_IS_RECORDING = "is_recording"

        // Konštanty pre výpočty
        private const val STEPS_PER_MOVE = 10
        private const val KM_PER_STEP = 0.00075
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var prefs: SharedPreferences
    private val db = FirebaseDatabase.getInstance().reference

    private var isRecording: Boolean = false
    private var lastHardwareSensorValue: Int = -1
    private var activeRouteId: String = "bulikova_lada116"

    // Premenné pre kontrolu dokončenia trasy
    private var isRouteCompleted = false
    private var completionListener: ValueEventListener? = null
    private var completionRef: DatabaseReference? = null

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("VRTourist", Context.MODE_PRIVATE)

        activeRouteId = prefs.getString(PREF_ACTIVE_ROUTE, "bulikova_lada116") ?: "bulikova_lada116"
        isRecording = prefs.getBoolean(PREF_IS_RECORDING, false)
        lastHardwareSensorValue = prefs.getInt(PREF_LAST_SENSOR_VAL, -1)

        Log.d(TAG, "🟢 SERVICE START: Trasa=$activeRouteId, Nahrávanie=$isRecording")

        startForegroundServiceNotification()
        setupSensors()

        // Spustíme sledovanie, či je trasa dokončená
        setupCompletionListener()
    }

    // Funkcia na sledovanie stavu 'isCompleted' vo Firebase
    private fun setupCompletionListener() {
        // Najprv odstránime starý listener, ak existuje (pri zmene trasy)
        if (completionRef != null && completionListener != null) {
            completionRef?.removeEventListener(completionListener!!)
        }

        completionRef = db.child("routes").child(activeRouteId).child("isCompleted")
        completionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isRouteCompleted = snapshot.getValue(Boolean::class.java) ?: false
                if (isRouteCompleted) {
                    Log.d(TAG, "🏁 TRASA DOKONČENÁ: Kroky sa prestávajú započítavať pre $activeRouteId")
                    // Aktualizujeme notifikáciu, aby user videl, že je koniec
                    startForegroundServiceNotification()
                } else {
                    Log.d(TAG, "👟 TRASA AKTÍVNA: Pokračujem v počítaní.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase Error: ${error.message}")
            }
        }
        completionRef?.addValueEventListener(completionListener!!)
    }

    private fun startForegroundServiceNotification() {
        val channelId = "step_service_channel"
        // Bezpečnejší spôsob získania Notification Managera pre rôzne verzie Androidu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "VR Turista", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val statusText = if (isRouteCompleted) "Trasa Dokončená! 🎉"
        else if (isRecording) "Nahrávam: $activeRouteId"
        else "Pauza"

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("VR Turista")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher) // Použil som kratší zápis R.mipmap
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        startForeground(1, notification)
    }

    private fun setupSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Senzor aktivovaný.")
        } else {
            Log.e(TAG, "❌ CHYBA: Žiadny senzor krokov!")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                Log.d(TAG, "▶️ PRIKAZ: Štart")
                isRecording = true
                prefs.edit().putBoolean(PREF_IS_RECORDING, true).apply()
                startForegroundServiceNotification()
            }
            ACTION_STOP_TRACKING -> {
                Log.d(TAG, "⏸️ PRIKAZ: Stop")
                isRecording = false
                prefs.edit().putBoolean(PREF_IS_RECORDING, false).apply()
                startForegroundServiceNotification()
            }
            ACTION_SET_ROUTE -> {
                val newRoute = intent.getStringExtra(EXTRA_ROUTE_ID)
                if (newRoute != null && newRoute != activeRouteId) {
                    Log.d(TAG, "🔀 ZMENA TRASY: $activeRouteId -> $newRoute")
                    isRecording = false
                    prefs.edit().putBoolean(PREF_IS_RECORDING, false).apply()

                    activeRouteId = newRoute
                    prefs.edit().putString(PREF_ACTIVE_ROUTE, newRoute).apply()

                    // Prehodíme listener na novú trasu
                    setupCompletionListener()
                    startForegroundServiceNotification()
                }
            }
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // 🛑 STOPKA: Ak je trasa dokončená, ignorujeme senzor
        if (isRouteCompleted) {
            return
        }

        val currentValue = event.values[0].toInt()
        val sensorType = event.sensor.type
        var delta = 0

        if (sensorType == Sensor.TYPE_STEP_DETECTOR) {
            delta = 1
        } else if (sensorType == Sensor.TYPE_STEP_COUNTER) {
            if (lastHardwareSensorValue == -1) {
                lastHardwareSensorValue = currentValue
                prefs.edit().putInt(PREF_LAST_SENSOR_VAL, currentValue).apply()
                return
            }
            delta = currentValue - lastHardwareSensorValue
            lastHardwareSensorValue = currentValue
            prefs.edit().putInt(PREF_LAST_SENSOR_VAL, currentValue).apply()
        }

        if (delta < 0) delta = 0

        if (delta > 0 && isRecording) {
            Log.d(TAG, "👣 KROK (Delta: $delta).")

            // 1. Aktualizácia TRASY
            updateRouteStats(delta)

            // 2. Aktualizácia DENNEJ ŠTATISTIKY
            updateDailyStats(delta)
        }
    }

    private fun updateRouteStats(stepsToAdd: Int) {
        val routeRef = db.child("routes").child(activeRouteId)

        routeRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentSteps = currentData.child("steps").getValue(Int::class.java) ?: 0
                val newSteps = currentSteps + stepsToAdd

                val newPosition = (newSteps / STEPS_PER_MOVE) + 1
                val newDistance = newSteps * KM_PER_STEP

                currentData.child("steps").value = newSteps
                currentData.child("position").value = newPosition
                currentData.child("traveledDistance").value = newDistance

                return Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
        })
    }

    private fun updateDailyStats(stepsToAdd: Int) {
        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyRef = db.child("dailyStats").child(todayDate).child("steps")

        dailyRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentSteps = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentSteps + stepsToAdd
                return Transaction.success(currentData)
            }
            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {}
        })
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        // Odstránenie listenera pri vypnutí
        if (completionRef != null && completionListener != null) {
            completionRef?.removeEventListener(completionListener!!)
        }
    }
}