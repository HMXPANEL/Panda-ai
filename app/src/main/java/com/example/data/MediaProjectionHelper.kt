package com.example.data

import android.app.Application
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.view.WindowManager

import java.io.ByteArrayOutputStream

object MediaProjectionHelper {

    private var projection: MediaProjection? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var screenWidth = 1080
    private var screenHeight = 1920
    private var screenDensity = 320

    fun setProjection(mp: MediaProjection?) {
        cleanup()
        projection = mp
    }

    fun isAvailable(): Boolean = projection != null

    fun initScreenDimensions(app: Application) {
        val wm = app.getSystemService(WindowManager::class.java)
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
    }

    fun captureScreenshot(): Bitmap? {
        val proj = projection ?: return null

        if (handlerThread == null) {
            handlerThread = HandlerThread("screenshot_capture").apply { start() }
            handler = Handler(handlerThread!!.looper)
        }

        val imageReader = ImageReader.newInstance(
            screenWidth, screenHeight, PixelFormat.RGBA_8888, 2
        )

        virtualDisplay = proj.createVirtualDisplay(
            "screenshot_display",
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, handler
        )

        try {
            Thread.sleep(300)

            val image = imageReader.acquireLatestImage() ?: run {
                var attempts = 0
                var img = imageReader.acquireLatestImage()
                while (img == null && attempts < 10) {
                    Thread.sleep(100)
                    img = imageReader.acquireLatestImage()
                    attempts++
                }
                img
            }

            if (image == null) return null

            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth

            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride, screenHeight, Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            val result = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
            bitmap.recycle()

            image.close()

            virtualDisplay?.release()
            virtualDisplay = null
            imageReader.close()

            return result
        } catch (e: Exception) {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader.close()
            return null
        }
    }

    fun captureScreenshotAsBytes(): ByteArray? {
        val bitmap = captureScreenshot() ?: return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        bitmap.recycle()
        return stream.toByteArray()
    }

    private fun cleanup() {
        virtualDisplay?.release()
        virtualDisplay = null
        handlerThread?.quitSafely()
        handlerThread = null
        handler = null
    }
}
