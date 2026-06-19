package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.telephony.SmsManager
import android.media.AudioManager
import android.view.KeyEvent
import android.widget.Toast
import java.util.Calendar
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import android.content.ClipboardManager
import android.content.ClipData
import android.content.IntentFilter
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object SystemIntegrations {

    private val ttsInstances = ConcurrentHashMap<Context, TextToSpeech>()
    private var isFlashlightOn = false

    // 1. Set Alarm in Background (No UI opened if possible, some OEM clock apps still open but this is the closest intent)
    fun setAlarmBackground(context: Context, hour: Int, minute: Int, message: String) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            Toast.makeText(context, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "No Alarm app found", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Make Direct Phone Call (Requires CALL_PHONE permission)
    fun makeDirectCall(context: Context, phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: SecurityException) {
            Toast.makeText(context, "Permission Denied for Call", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Send SMS Background (Requires SEND_SMS permission)
    fun sendSmsBackground(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(context, "SMS sent silently!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 4. Toggle Media Play/Pause Silently (Controls background music)
    fun toggleMediaPlayback(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        audioManager.dispatchMediaKeyEvent(eventDown)
        audioManager.dispatchMediaKeyEvent(eventUp)
        Toast.makeText(context, "Media Play/Pause toggled", Toast.LENGTH_SHORT).show()
    }

    // 5. Add Calendar Event completely in background silently using ContentResolver
    fun addCalendarEventSilent(context: Context, title: String, description: String, startMillis: Long, endMillis: Long) {
        try {
            val values = android.content.ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            Toast.makeText(context, "Silently added event!", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            // Need WRITE_CALENDAR permission
            Toast.makeText(context, "Need Calendar Permissions!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to add event: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMapsSilent(context: Context, locationString: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(locationString)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            Toast.makeText(context, "Google Maps not installed", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Advanced Autonomous Device Features ---

    fun toggleFlashlight(context: Context) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0] // usually 0 is rear camera
            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, isFlashlightOn)
            val stateStr = if (isFlashlightOn) "ON" else "OFF"
            Toast.makeText(context, "Flashlight $stateStr", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Flashlight error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun speakText(context: Context, text: String) {
        val appContext = context.applicationContext
        val tts = ttsInstances.computeIfAbsent(appContext) { ctx ->
            TextToSpeech(ctx) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    this.language = Locale.US
                }
            }
        }
        
        if (tts.isLanguageAvailable(Locale.US) >= TextToSpeech.LANG_AVAILABLE) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "panda_tts_${System.currentTimeMillis()}")
        }
        Toast.makeText(context, "Speaking: $text", Toast.LENGTH_SHORT).show()
    }

    fun shutdownTTS(context: Context) {
        val appContext = context.applicationContext
        ttsInstances.remove(appContext)?.let { tts ->
            tts.stop()
            tts.shutdown()
        }
    }

    fun maximizeVolume(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
            Toast.makeText(context, "Volume Maximized!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to set volume", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    fun vibrateDevice(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(500)
            }
            Toast.makeText(context, "Device Vibrated", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Vibration failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun readBatteryLevel(context: Context) {
        try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = level * 100 / scale.toFloat()
            Toast.makeText(context, "Battery Level: ${batteryPct.toInt()}%", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not read battery", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Clipboard failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleWifi(context: Context, enable: Boolean): Boolean {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val result = wifiManager.setWifiEnabled(enable)
            Toast.makeText(context, "Wi-Fi ${if (enable) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            return result
        } catch (e: Exception) {
            Toast.makeText(context, "Wi-Fi toggle failed: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun isWifiEnabled(context: Context): Boolean {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        } catch (e: Exception) {
            return false
        }
    }

    fun toggleBluetooth(context: Context, enable: Boolean): Boolean {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter == null) {
                Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
                return false
            }
            val result = if (enable) bluetoothAdapter.enable() else bluetoothAdapter.disable()
            Toast.makeText(context, "Bluetooth ${if (enable) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
            return result
        } catch (e: Exception) {
            Toast.makeText(context, "Bluetooth toggle failed: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return bluetoothAdapter?.isEnabled == true
        } catch (e: Exception) {
            return false
        }
    }
}
