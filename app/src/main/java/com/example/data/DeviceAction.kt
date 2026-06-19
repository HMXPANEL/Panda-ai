package com.example.data

sealed class DeviceAction {

    // ─── UI Interaction ───
    data class ClickText(val text: String) : DeviceAction()
    data class ClickDesc(val desc: String) : DeviceAction()
    data class ClickId(val id: String) : DeviceAction()
    data class ClickAt(val x: Int, val y: Int) : DeviceAction()
    data class LongPressText(val text: String) : DeviceAction()
    data class TypeText(val text: String, val intoHint: String? = null) : DeviceAction()
    data class Wait(val ms: Long) : DeviceAction()

    // ─── Navigation ───
    object GoBack : DeviceAction()
    object GoHome : DeviceAction()
    object GoRecents : DeviceAction()
    object ScrollDown : DeviceAction()
    object ScrollUp : DeviceAction()
    data class ScrollToText(val text: String) : DeviceAction()

    // ─── Gestures ───
    data class Swipe(val fromX: Float, val fromY: Float, val toX: Float, val toY: Float) : DeviceAction()
    object SwipeUp : DeviceAction()
    object SwipeDown : DeviceAction()
    object SwipeLeft : DeviceAction()
    object SwipeRight : DeviceAction()

    // ─── Apps ───
    data class OpenApp(val packageName: String) : DeviceAction()
    data class OpenUrl(val url: String) : DeviceAction()
    data class UninstallApp(val packageName: String) : DeviceAction()

    // ─── Communication ───
    data class MakeCall(val phoneNumber: String) : DeviceAction()
    data class SendSms(val phoneNumber: String, val message: String) : DeviceAction()
    data class CallContact(val name: String) : DeviceAction()
    data class SmsContact(val name: String, val message: String) : DeviceAction()

    // ─── Device Controls ───
    object FlashlightToggle : DeviceAction()
    data class SetWifi(val enable: Boolean) : DeviceAction()
    data class SetBluetooth(val enable: Boolean) : DeviceAction()
    object MaximizeVolume : DeviceAction()
    data class SetVolume(val level: Int) : DeviceAction()
    object ToggleMediaPlayback : DeviceAction()
    object Vibrate : DeviceAction()
    object ReadBattery : DeviceAction()
    data class CopyClipboard(val text: String) : DeviceAction()

    // ─── Productivity ───
    data class SetAlarm(val hour: Int, val minute: Int, val label: String) : DeviceAction()
    data class AddCalendarEvent(val title: String, val description: String, val startTime: Long, val endTime: Long) : DeviceAction()
    data class OpenMaps(val location: String) : DeviceAction()

    // ─── Speech ───
    data class SpeakText(val text: String) : DeviceAction()

    // ─── Screen / File ───
    object TakeScreenshot : DeviceAction()
    object GetScreenContent : DeviceAction()
    object ReadNotifications : DeviceAction()
    data class ReadFile(val path: String) : DeviceAction()
    data class WriteFile(val path: String, val content: String) : DeviceAction()

    // ─── Contacts ───
    data class SearchContact(val name: String) : DeviceAction()

    // ─── Memory ───
    object GetMemories : DeviceAction()
    data class SaveMemory(val content: String, val category: String) : DeviceAction()

    fun isDangerous(): Boolean = when (this) {
        is MakeCall, is SendSms, is CallContact, is SmsContact -> true
        is UninstallApp, is WriteFile, is SetAlarm -> true
        is AddCalendarEvent -> true
        else -> false
    }

    fun description(): String = when (this) {
        is ClickText -> "Click \"$text\""
        is ClickDesc -> "Click element \"$desc\""
        is ClickId -> "Click element with id $id"
        is ClickAt -> "Click at ($x, $y)"
        is LongPressText -> "Long press \"$text\""
        is TypeText -> "Type \"$text\" into field"
        is Wait -> "Wait ${ms}ms"
        is GoBack -> "Go back"
        is GoHome -> "Go home"
        is GoRecents -> "Open recents"
        is ScrollDown -> "Scroll down"
        is ScrollUp -> "Scroll up"
        is ScrollToText -> "Scroll to \"$text\""
        is Swipe -> "Swipe from ($fromX,$fromY) to ($toX,$toY)"
        is SwipeUp -> "Swipe up"
        is SwipeDown -> "Swipe down"
        is SwipeLeft -> "Swipe left"
        is SwipeRight -> "Swipe right"
        is OpenApp -> "Open app $packageName"
        is OpenUrl -> "Open URL $url"
        is UninstallApp -> "Uninstall app $packageName"
        is MakeCall -> "Call $phoneNumber"
        is SendSms -> "SMS $phoneNumber: $message"
        is CallContact -> "Call contact $name"
        is SmsContact -> "SMS contact $name: $message"
        is FlashlightToggle -> "Toggle flashlight"
        is SetWifi -> "${if (enable) "Enable" else "Disable"} WiFi"
        is SetBluetooth -> "${if (enable) "Enable" else "Disable"} Bluetooth"
        is MaximizeVolume -> "Maximize volume"
        is SetVolume -> "Set volume to $level"
        is ToggleMediaPlayback -> "Toggle media playback"
        is Vibrate -> "Vibrate device"
        is ReadBattery -> "Read battery level"
        is CopyClipboard -> "Copy to clipboard: $text"
        is SetAlarm -> "Set alarm for $hour:$minute: $label"
        is AddCalendarEvent -> "Add event: $title"
        is OpenMaps -> "Open maps: $location"
        is SpeakText -> "Speak: $text"
        is TakeScreenshot -> "Take screenshot"
        is GetScreenContent -> "Read screen content"
        is ReadNotifications -> "Read notifications"
        is ReadFile -> "Read file $path"
        is WriteFile -> "Write file $path"
        is SearchContact -> "Search contact $name"
        is GetMemories -> "Get memories"
        is SaveMemory -> "Save memory: $content"
    }

}

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Error(val message: String) : ActionResult()
    data class ScreenContent(val content: String) : ActionResult()
}
