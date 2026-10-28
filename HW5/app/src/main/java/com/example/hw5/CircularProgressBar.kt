package com.example.hw5

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class CircularProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var maxProgress: Int = 100
        set(value) {
            field = value
            invalidate()
        }

    var progress: Int = 0
        set(value) {
            field = value.coerceIn(0, maxProgress)
            invalidate()
        }

    var centerText: String? = null
        set(value) {
            field = value
            invalidate()
        }

    private var trackColor: Int = Color.parseColor("#E0E0E0")
    private var progressColor: Int = Color.parseColor("#3F51B5")
    private var textColor: Int = Color.parseColor("#212121")

    private var strokeWidthPx: Float = 32f

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        color = trackColor
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        color = progressColor
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val oval = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        strokeWidthPx = 12f * density
        trackPaint.strokeWidth = strokeWidthPx
        progressPaint.strokeWidth = strokeWidthPx

        val size = min(w, h)
        val padding = (strokeWidthPx / 2f) + 16f
        val left = ((w - size) / 2f) + padding
        val top = ((h - size) / 2f) + padding
        val right = ((w + size) / 2f) - padding
        val bottom = ((h + size) / 2f) - padding
        oval.set(left, top, right, bottom)

        textPaint.textSize = size * 0.18f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw background circle track
        canvas.drawOval(oval, trackPaint)

        // 2. Draw progress arc starting from top (-90 degrees)
        val sweepAngle = (progress.toFloat() / maxProgress.toFloat()) * 360f
        if (sweepAngle > 0) {
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)
        }

        // 3. Draw center text
        val textToDraw = centerText ?: "$progress%"
        val fontMetrics = textPaint.fontMetrics
        val baseline = oval.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(textToDraw, oval.centerX(), baseline, textPaint)
    }
}
