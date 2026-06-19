package com.example.ui

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.WindowManager
import kotlinx.coroutines.delay

class PandaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "PandaAccessibilityService"
        var instance: PandaAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (instance === this) instance = null
        return super.onUnbind(intent)
    }

    // ─── Screen Dimensions ───

    fun getScreenWidth(): Int {
        val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return 1080
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    fun getScreenHeight(): Int {
        val wm = getSystemService(WINDOW_SERVICE) as? WindowManager ?: return 1920
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

    // ─── Retry-Aware Element Finding ───

    fun findNodeByText(text: String, retryMs: Long = 0): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val found = findNodeRecursive(root) { node ->
            node.text?.toString()?.contains(text, ignoreCase = true) == true
        }
        if (found != null) return found
        if (retryMs > 0) {
            try { Thread.sleep(retryMs) } catch (_: InterruptedException) {}
            val root2 = rootInActiveWindow ?: return null
            return findNodeRecursive(root2) { node ->
                node.text?.toString()?.contains(text, ignoreCase = true) == true
            }
        }
        return null
    }

    fun findNodeByContentDesc(desc: String, retryMs: Long = 0): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val found = findNodeRecursive(root) { node ->
            node.contentDescription?.toString()?.contains(desc, ignoreCase = true) == true
        }
        if (found != null) return found
        if (retryMs > 0) {
            try { Thread.sleep(retryMs) } catch (_: InterruptedException) {}
            val root2 = rootInActiveWindow ?: return null
            return findNodeRecursive(root2) { node ->
                node.contentDescription?.toString()?.contains(desc, ignoreCase = true) == true
            }
        }
        return null
    }

    fun findNodeByViewId(id: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        return findNodeRecursive(root) { node ->
            node.viewIdResourceName?.contains(id, ignoreCase = true) == true
        }
    }

    fun findAllNodesByText(text: String): List<AccessibilityNodeInfo> {
        val root = rootInActiveWindow ?: return emptyList()
        val results = mutableListOf<AccessibilityNodeInfo>()
        collectAllMatching(root) { node ->
            node.text?.toString()?.contains(text, ignoreCase = true) == true
        }.let { results.addAll(it) }
        return results
    }

    fun findDeepestNodeAt(x: Int, y: Int): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val candidates = mutableListOf<Pair<AccessibilityNodeInfo, Int>>()
        fun walk(node: AccessibilityNodeInfo, depth: Int) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            if (bounds.contains(x, y)) {
                candidates.add(node to depth)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    walk(child, depth + 1)
                }
            }
        }
        walk(root, 0)
        return candidates.maxByOrNull { it.second }?.first
    }

    // ─── Click Actions ───

    fun clickText(text: String, retryMs: Long = 1000): Boolean {
        val node = findNodeByText(text, retryMs) ?: return false
        return clickNode(node)
    }

    fun clickDesc(desc: String, retryMs: Long = 1000): Boolean {
        val node = findNodeByContentDesc(desc, retryMs) ?: return false
        return clickNode(node)
    }

    fun clickId(id: String): Boolean {
        val node = findNodeByViewId(id) ?: return false
        return clickNode(node)
    }

    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        node.recycle()
        return success
    }

    fun clickAt(x: Int, y: Int): Boolean {
        val node = findDeepestNodeAt(x, y)
        if (node != null) {
            val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            node.recycle()
            if (success) return true
        }
        // Fallback: dispatch tap gesture
        return tap(x.toFloat(), y.toFloat())
    }

    fun longClickText(text: String, retryMs: Long = 1000): Boolean {
        val node = findNodeByText(text, retryMs) ?: return false
        val success = node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        node.recycle()
        if (success) return true
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        node.recycle()
        return longPressAt(bounds.centerX(), bounds.centerY())
    }

    fun longPressAt(x: Int, y: Int): Boolean {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 800)
        val builder = GestureDescription.Builder().addStroke(stroke)
        return dispatchGesture(builder.build(), null, null)
    }

    fun tap(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val builder = GestureDescription.Builder().addStroke(stroke)
        return dispatchGesture(builder.build(), null, null)
    }

    // ─── Type Text ───

    fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focusedNode = findNodeRecursive(root) { it.isFocused }
        if (focusedNode != null) {
            val args = Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            val success = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            focusedNode.recycle()
            if (success) return true
            // fallback: append text character by character
            args.clear()
            args.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                (focusedNode.text?.toString() ?: "") + text
            )
            val success2 = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            focusedNode.recycle()
            return success2
        }
        return false
    }

    fun typeTextIntoField(text: String, fieldHint: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val candidates = mutableListOf<AccessibilityNodeInfo>()

        fun collectEditable(node: AccessibilityNodeInfo) {
            if (node.className?.toString()?.contains("EditText", ignoreCase = true) == true) {
                candidates.add(node)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child -> collectEditable(child) }
            }
        }
        collectEditable(root)

        val target = candidates.firstOrNull { candidate ->
            candidate.text?.toString()?.contains(fieldHint, ignoreCase = true) == true ||
            candidate.contentDescription?.toString()?.contains(fieldHint, ignoreCase = true) == true
        } ?: candidates.firstOrNull()

        val success = if (target != null) {
            target.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        } else false

        candidates.forEach { it.recycle() }
        return success
    }

    // ─── Swipe Gestures ───

    fun swipeUp(): Boolean {
        val w = getScreenWidth()
        val h = getScreenHeight()
        return swipe(w / 2f, h * 0.7f, w / 2f, h * 0.3f)
    }

    fun swipeDown(): Boolean {
        val w = getScreenWidth()
        val h = getScreenHeight()
        return swipe(w / 2f, h * 0.3f, w / 2f, h * 0.7f)
    }

    fun swipeLeft(): Boolean {
        val w = getScreenWidth()
        val h = getScreenHeight()
        return swipe(w * 0.8f, h / 2f, w * 0.2f, h / 2f)
    }

    fun swipeRight(): Boolean {
        val w = getScreenWidth()
        val h = getScreenHeight()
        return swipe(w * 0.2f, h / 2f, w * 0.8f, h / 2f)
    }

    fun swipe(fromX: Float, fromY: Float, toX: Float, toY: Float): Boolean {
        val path = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 400)
        val builder = GestureDescription.Builder().addStroke(stroke)
        return dispatchGesture(builder.build(), null, null)
    }

    // ─── Screen Content Dump (with bounds and icons) ───

    fun getScreenContent(): String {
        val root = rootInActiveWindow ?: return "Screen content unavailable"
        val sb = StringBuilder()
        sb.appendLine("--- SCREEN CONTENT ---")
        dumpNodeTree(root, sb, 0)
        root.recycle()
        return sb.toString()
    }

    private fun dumpNodeTree(node: AccessibilityNodeInfo, sb: StringBuilder, depth: Int) {
        if (depth > 12) return
        val indent = "  ".repeat(depth)
        val text = node.text?.toString()?.take(200) ?: ""
        val desc = node.contentDescription?.toString()?.take(200) ?: ""
        val cls = node.className?.toString()?.substringAfterLast('.') ?: ""
        val viewId = node.viewIdResourceName ?: ""
        val bounds = Rect().also { node.getBoundsInScreen(it) }
        val isEditable = cls.contains("EditText", ignoreCase = true)
        val isClickable = node.isClickable || cls.contains("Button", ignoreCase = true) || cls.contains("ImageButton", ignoreCase = true)

        // Only emit if there's useful info
        if (text.isNotEmpty() || desc.isNotEmpty() || viewId.isNotEmpty() || isClickable || isEditable) {
            sb.append(indent)
            when {
                isEditable -> sb.append("[INPUT]")
                isClickable && text.isEmpty() && desc.isEmpty() -> sb.append("[ICON]")
                isClickable -> sb.append("[BTN]")
                else -> sb.append("[TEXT]")
            }
            if (text.isNotEmpty()) sb.append(" \"$text\"")
            if (desc.isNotEmpty()) sb.append(" desc=\"$desc\"")
            sb.append(" bounds=[${bounds.left},${bounds.top},${bounds.right},${bounds.bottom}]")
            if (viewId.isNotEmpty()) {
                val shortId = viewId.substringAfterLast('/')
                sb.append(" id=\"$shortId\"")
            }
            sb.appendLine()
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                dumpNodeTree(child, sb, depth + 1)
                child.recycle()
            }
        }
    }

    // ─── Scroll Into View ───

    fun scrollToText(text: String, maxScrolls: Int = 8): Boolean {
        // First check if already visible
        val root = rootInActiveWindow ?: return false
        var found = findNodeRecursive(root) { it.text?.toString()?.contains(text, ignoreCase = true) == true }
        if (found != null) return true

        for (i in 0 until maxScrolls) {
            try { Thread.sleep(600) } catch (_: InterruptedException) {}
            performGlobalScrollForward()
            try { Thread.sleep(800) } catch (_: InterruptedException) {}
            val newRoot = rootInActiveWindow ?: continue
            found = findNodeRecursive(newRoot) {
                it.text?.toString()?.contains(text, ignoreCase = true) == true
            }
            if (found != null) return true
        }
        return false
    }

    // ─── Utilities ───

    private fun collectAllMatching(node: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        if (predicate(node)) results.add(node)
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                results.addAll(collectAllMatching(child, predicate))
            }
        }
        return results
    }

    private fun findNodeRecursive(node: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        if (predicate(node)) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeRecursive(child, predicate)
            if (result != null) {
                child.recycle()
                return result
            }
            child.recycle()
        }
        return null
    }

    // ─── Global Actions ───

    fun performGlobalBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performGlobalHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performGlobalRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun performGlobalNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    fun performGlobalScrollForward(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollable = findNodeRecursive(root) { it.isScrollable }
        return scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
    }
    fun performGlobalScrollBackward(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollable = findNodeRecursive(root) { it.isScrollable }
        return scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
    }
}
