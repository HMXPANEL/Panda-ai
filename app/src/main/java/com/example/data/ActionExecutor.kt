package com.example.data

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.content.ClipboardManager
import android.content.ClipData
import android.content.IntentFilter
import android.media.AudioManager
import android.view.KeyEvent
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothManager
import android.hardware.camera2.CameraManager
import android.speech.tts.TextToSpeech
import com.example.data.MediaProjectionHelper
import com.example.ui.PandaAccessibilityService
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class ActionExecutor(private val context: Context) {

    private val svc: PandaAccessibilityService?
        get() = PandaAccessibilityService.instance

    // ─── TTS Cache ───
    private val ttsInstances = ConcurrentHashMap<Context, TextToSpeech>()
    private var isFlashlightOn = false

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun execute(action: DeviceAction, dangerousConfirmed: Boolean = false): ActionResult {
        if (action.isDangerous() && !dangerousConfirmed) {
            return ActionResult.Error("Action requires user confirmation: ${action.description()}")
        }
        return try {
            when (action) {
                // ── UI Interaction ──
                is DeviceAction.ClickText -> {
                    val s = svc
                    if (s != null && s.clickText(action.text))
                        ActionResult.Success("Clicked \"${action.text}\"")
                    else ActionResult.Error("Could not find/click \"${action.text}\"")
                }
                is DeviceAction.ClickDesc -> {
                    val s = svc
                    if (s != null && s.clickDesc(action.desc))
                        ActionResult.Success("Clicked element \"${action.desc}\"")
                    else ActionResult.Error("Could not find/click \"${action.desc}\"")
                }
                is DeviceAction.ClickId -> {
                    val s = svc
                    if (s != null && s.clickId(action.id))
                        ActionResult.Success("Clicked element id: ${action.id}")
                    else ActionResult.Error("Could not find element id: ${action.id}")
                }
                is DeviceAction.ClickAt -> {
                    val s = svc
                    if (s != null && s.clickAt(action.x, action.y))
                        ActionResult.Success("Clicked at (${action.x}, ${action.y})")
                    else ActionResult.Error("Could not click at (${action.x}, ${action.y})")
                }
                is DeviceAction.LongPressText -> {
                    val s = svc
                    if (s != null && s.longClickText(action.text))
                        ActionResult.Success("Long pressed \"${action.text}\"")
                    else ActionResult.Error("Could not long press \"${action.text}\"")
                }
                is DeviceAction.TypeText -> {
                    val s = svc
                    if (s == null) return ActionResult.Error("Accessibility service not connected")
                    val success = if (action.intoHint != null) s.typeTextIntoField(action.text, action.intoHint)
                    else s.typeText(action.text)
                    if (success) ActionResult.Success("Typed into field")
                    else ActionResult.Error("No editable field focused")
                }

                // ── Navigation ──
                is DeviceAction.GoBack -> if (svc?.performGlobalBack() == true) ActionResult.Success("Went back") else ActionResult.Error("Could not go back")
                is DeviceAction.GoHome -> if (svc?.performGlobalHome() == true) ActionResult.Success("Went home") else ActionResult.Error("Could not go home")
                is DeviceAction.GoRecents -> if (svc?.performGlobalRecents() == true) ActionResult.Success("Opened recents") else ActionResult.Error("Could not open recents")
                is DeviceAction.ScrollDown -> if (svc?.performGlobalScrollForward() == true) ActionResult.Success("Scrolled down") else ActionResult.Error("Could not scroll down")
                is DeviceAction.ScrollUp -> if (svc?.performGlobalScrollBackward() == true) ActionResult.Success("Scrolled up") else ActionResult.Error("Could not scroll up")
                is DeviceAction.ScrollToText -> {
                    val s = svc
                    if (s != null && s.scrollToText(action.text)) ActionResult.Success("Found \"${action.text}\" after scrolling")
                    else ActionResult.Error("Could not find \"${action.text}\"")
                }

                // ── Gestures ──
                is DeviceAction.SwipeUp -> if (svc?.swipeUp() == true) ActionResult.Success("Swiped up") else ActionResult.Error("Swipe up failed")
                is DeviceAction.SwipeDown -> if (svc?.swipeDown() == true) ActionResult.Success("Swiped down") else ActionResult.Error("Swipe down failed")
                is DeviceAction.SwipeLeft -> if (svc?.swipeLeft() == true) ActionResult.Success("Swiped left") else ActionResult.Error("Swipe left failed")
                is DeviceAction.SwipeRight -> if (svc?.swipeRight() == true) ActionResult.Success("Swiped right") else ActionResult.Error("Swipe right failed")
                is DeviceAction.Swipe -> if (svc?.swipe(action.fromX, action.fromY, action.toX, action.toY) == true) ActionResult.Success("Swiped") else ActionResult.Error("Swipe failed")
                is DeviceAction.Wait -> {
                    Thread.sleep(action.ms)
                    ActionResult.Success("Waited ${action.ms}ms")
                }

                // ── Apps ──
                is DeviceAction.OpenApp -> {
                    val intent = context.packageManager.getLaunchIntentForPackage(action.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        ActionResult.Success("Opened ${action.packageName}")
                    } else {
                        ActionResult.Error("App not installed: ${action.packageName}")
                    }
                }
                is DeviceAction.OpenUrl -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action.url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    ActionResult.Success("Opened URL")
                }
                is DeviceAction.UninstallApp -> {
                    val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${action.packageName}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult.Success("Uninstalling ${action.packageName}")
                }

                // ── Communication ──
                is DeviceAction.MakeCall -> {
                    if (!checkPermission(Manifest.permission.CALL_PHONE))
                        return ActionResult.Error("Call requires CALL_PHONE permission")
                    try {
                        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${action.phoneNumber}")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        ActionResult.Success("Calling ${action.phoneNumber}")
                    } catch (e: SecurityException) {
                        ActionResult.Error("Call permission denied")
                    }
                }
                is DeviceAction.SendSms -> {
                    if (!checkPermission(Manifest.permission.SEND_SMS))
                        return ActionResult.Error("SMS requires SEND_SMS permission")
                    try {
                        val smsManager = context.getSystemService(android.telephony.SmsManager::class.java)
                        smsManager.sendTextMessage(action.phoneNumber, null, action.message, null, null)
                        ActionResult.Success("SMS sent to ${action.phoneNumber}")
                    } catch (e: Exception) {
                        ActionResult.Error("SMS failed: ${e.message}")
                    }
                }
                is DeviceAction.CallContact -> {
                    if (!checkPermission(Manifest.permission.CALL_PHONE))
                        return ActionResult.Error("Call requires CALL_PHONE permission")
                    val number = lookupContactNumber(action.name)
                    if (number != null) {
                        try {
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                            ActionResult.Success("Calling ${action.name} at $number")
                        } catch (e: SecurityException) {
                            ActionResult.Error("Call permission denied")
                        }
                    } else {
                        ActionResult.Error("Contact \"${action.name}\" not found")
                    }
                }
                is DeviceAction.SmsContact -> {
                    if (!checkPermission(Manifest.permission.SEND_SMS))
                        return ActionResult.Error("SMS requires SEND_SMS permission")
                    val number = lookupContactNumber(action.name)
                    if (number != null) {
                        try {
                            val smsManager = context.getSystemService(android.telephony.SmsManager::class.java)
                            smsManager.sendTextMessage(number, null, action.message, null, null)
                            ActionResult.Success("SMS sent to ${action.name} at $number")
                        } catch (e: Exception) {
                            ActionResult.Error("SMS failed: ${e.message}")
                        }
                    } else {
                        ActionResult.Error("Contact \"${action.name}\" not found")
                    }
                }
                is DeviceAction.SearchContact -> {
                    if (!checkPermission(Manifest.permission.READ_CONTACTS))
                        return ActionResult.Error("Contact search requires READ_CONTACTS permission")
                    val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
                    val selectionArgs = arrayOf("%${action.name}%")
                    val results = mutableListOf<String>()
                    context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val name = cursor.getString(0)
                            val number = cursor.getString(1)
                            results.add("$name: $number")
                        }
                    }
                    if (results.isNotEmpty()) {
                        ActionResult.Success("Contacts found: ${results.joinToString("; ")}")
                    } else {
                        ActionResult.Error("No contacts found for \"${action.name}\"")
                    }
                }

                // ── Device Controls ──
                is DeviceAction.FlashlightToggle -> {
                    if (!checkPermission(Manifest.permission.CAMERA))
                        return ActionResult.Error("Flashlight requires CAMERA permission")
                    val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val cameraId = cm.cameraIdList[0]
                    isFlashlightOn = !isFlashlightOn
                    cm.setTorchMode(cameraId, isFlashlightOn)
                    ActionResult.Success("Flashlight ${if (isFlashlightOn) "ON" else "OFF"}")
                }
                is DeviceAction.SetWifi -> {
                    if (!checkPermission(Manifest.permission.CHANGE_WIFI_STATE))
                        return ActionResult.Error("WiFi toggle requires CHANGE_WIFI_STATE permission")
                    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val result = wm.setWifiEnabled(action.enable)
                    if (result) ActionResult.Success("WiFi ${if (action.enable) "enabled" else "disabled"}")
                    else ActionResult.Error("WiFi toggle failed")
                }
                is DeviceAction.SetBluetooth -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkPermission(Manifest.permission.BLUETOOTH_CONNECT))
                        return ActionResult.Error("Bluetooth toggle requires BLUETOOTH_CONNECT permission")
                    val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                    val adapter = bm.adapter
                    if (adapter == null) return ActionResult.Error("Bluetooth not supported")
                    val result = if (action.enable) adapter.enable() else adapter.disable()
                    if (result) ActionResult.Success("Bluetooth ${if (action.enable) "enabled" else "disabled"}")
                    else ActionResult.Error("Bluetooth toggle failed")
                }
                is DeviceAction.MaximizeVolume -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
                    ActionResult.Success("Volume maximized")
                }
                is DeviceAction.SetVolume -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val clamped = action.level.coerceIn(0, max)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
                    ActionResult.Success("Volume set to $clamped")
                }
                is DeviceAction.ToggleMediaPlayback -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
                    am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
                    ActionResult.Success("Toggled media playback")
                }
                is DeviceAction.Vibrate -> {
                    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else { v.vibrate(500) }
                    ActionResult.Success("Vibrated")
                }
                is DeviceAction.ReadBattery -> {
                    val status = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { context.registerReceiver(null, it) }
                    val level = status?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                    val scale = status?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                    val pct = if (scale > 0) level * 100 / scale else -1
                    ActionResult.Success("Battery: $pct%")
                }
                is DeviceAction.CopyClipboard -> {
                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cb.setPrimaryClip(ClipData.newPlainText("panda_copied", action.text))
                    ActionResult.Success("Copied to clipboard")
                }

                // ── Productivity ──
                is DeviceAction.SetAlarm -> {
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, action.hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, action.minute)
                        putExtra(AlarmClock.EXTRA_MESSAGE, action.label)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult.Success("Alarm set for ${action.hour}:${action.minute}: ${action.label}")
                }
                is DeviceAction.AddCalendarEvent -> {
                    if (!checkPermission(Manifest.permission.WRITE_CALENDAR))
                        return ActionResult.Error("Calendar event requires WRITE_CALENDAR permission")
                    val values = ContentValues().apply {
                        put(CalendarContract.Events.DTSTART, action.startTime)
                        put(CalendarContract.Events.DTEND, action.endTime)
                        put(CalendarContract.Events.TITLE, action.title)
                        put(CalendarContract.Events.DESCRIPTION, action.description)
                        put(CalendarContract.Events.CALENDAR_ID, 1)
                        put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
                    }
                    context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                    ActionResult.Success("Event added: ${action.title}")
                }
                is DeviceAction.OpenMaps -> {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(action.location)}")
                    val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        ActionResult.Success("Opened maps: ${action.location}")
                    } else {
                        ActionResult.Error("Google Maps not installed")
                    }
                }

                // ── Speech ──
                is DeviceAction.SpeakText -> {
                    val appCtx = context.applicationContext
                    val tts = ttsInstances.computeIfAbsent(appCtx) { ctx ->
                        var ref: TextToSpeech? = null
                        ref = TextToSpeech(ctx) { status ->
                            if (status == TextToSpeech.SUCCESS) ref?.setLanguage(Locale.US)
                        }
                        ref!!
                    }
                    if (tts.isLanguageAvailable(Locale.US) >= TextToSpeech.LANG_AVAILABLE) {
                        tts.speak(action.text, TextToSpeech.QUEUE_FLUSH, null, "panda_${System.currentTimeMillis()}")
                    }
                    ActionResult.Success("Speaking: ${action.text}")
                }

                // ── Screen Content ──
                is DeviceAction.GetScreenContent -> {
                    val content = svc?.getScreenContent() ?: "Accessibility service not connected"
                    ActionResult.ScreenContent(content)
                }

                // ── Get / Save Memories ──
                is DeviceAction.GetMemories -> {
                    // This is handled by the ViewModel directly, but return a placeholder
                    ActionResult.Success("memories_will_be_injected")
                }
                is DeviceAction.SaveMemory -> {
                    ActionResult.Success("memory_saved_${System.currentTimeMillis()}")
                }

                // ── Notifications ──
                is DeviceAction.ReadNotifications -> {
                    val notifications = com.example.ui.PandaNotificationListenerService.currentNotifications
                    if (notifications.isEmpty()) {
                        ActionResult.Success("No notifications")
                    } else {
                        val text = notifications.joinToString("\n") { "[${it.packageName}] ${it.title}: ${it.text}" }
                        ActionResult.Success("Notifications:\n$text")
                    }
                }

                // ── Screenshot ──
                is DeviceAction.TakeScreenshot -> {
                    if (!MediaProjectionHelper.isAvailable())
                        return ActionResult.Error("Screen capture permission not granted. Please grant it first.")
                    val bytes = MediaProjectionHelper.captureScreenshotAsBytes()
                    if (bytes != null) {
                        val fileName = "screenshot_${System.currentTimeMillis()}.jpg"
                        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { it.write(bytes) }
                        ActionResult.Success("Screenshot saved as $fileName")
                    } else {
                        ActionResult.Error("Failed to capture screenshot")
                    }
                }

                // ── Files ──
                is DeviceAction.ReadFile -> {
                    try {
                        val inputStream = context.openFileInput(action.path)
                        val text = inputStream.bufferedReader().readText()
                        inputStream.close()
                        ActionResult.Success("File content: $text")
                    } catch (e: Exception) {
                        ActionResult.Error("Could not read file: ${e.message}")
                    }
                }
                is DeviceAction.WriteFile -> {
                    try {
                        context.openFileOutput(action.path, Context.MODE_PRIVATE).use {
                            it.write(action.content.toByteArray())
                        }
                        ActionResult.Success("Written to ${action.path}")
                    } catch (e: Exception) {
                        ActionResult.Error("Could not write file: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            ActionResult.Error("Action failed: ${e.message}")
        }
    }

    private fun lookupContactNumber(name: String): String? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$name%")
        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getString(0)
        }
        return null
    }
}
