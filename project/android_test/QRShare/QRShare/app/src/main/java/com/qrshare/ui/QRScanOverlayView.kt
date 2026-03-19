package com.qrshare.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Custom overlay view for the QR scanner screen.
 * Draws a semi-transparent dark background with a clear rounded-rect
 * scan window in the centre, plus corner bracket decorations.
 */
class QRScanOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AA000000")
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.FILL
    }

    private val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A73E8")
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val scanRect = RectF()
    private val cornerLength = 48f
    private val cornerRadius = 16f

    init {
        // Required for PorterDuff CLEAR mode to work
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val side = minOf(w, h) * 0.68f
        val left = (w - side) / 2f
        val top = (h - side) / 2.2f
        scanRect.set(left, top, left + side, top + side)

        // Dark overlay covering the whole screen
        canvas.drawRect(0f, 0f, w, h, overlayPaint)

        // Punch a clear rounded window
        canvas.drawRoundRect(scanRect, cornerRadius, cornerRadius, clearPaint)

        // Draw blue corner brackets
        val l = scanRect.left
        val t = scanRect.top
        val r = scanRect.right
        val b = scanRect.bottom
        val cl = cornerLength

        // Top-left
        canvas.drawLine(l, t + cl, l, t + cornerRadius, bracketPaint)
        canvas.drawLine(l + cornerRadius, t, l + cl, t, bracketPaint)
        // Top-right
        canvas.drawLine(r - cl, t, r - cornerRadius, t, bracketPaint)
        canvas.drawLine(r, t + cornerRadius, r, t + cl, bracketPaint)
        // Bottom-left
        canvas.drawLine(l, b - cl, l, b - cornerRadius, bracketPaint)
        canvas.drawLine(l + cornerRadius, b, l + cl, b, bracketPaint)
        // Bottom-right
        canvas.drawLine(r - cl, b, r - cornerRadius, b, bracketPaint)
        canvas.drawLine(r, b - cl, r, b - cornerRadius, bracketPaint)
    }
}
