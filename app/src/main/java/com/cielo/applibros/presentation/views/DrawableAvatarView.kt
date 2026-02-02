package com.cielo.applibros.presentation.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Base64
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.io.ByteArrayOutputStream

class DrawableAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val drawPath = Path()

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val canvasPaint = Paint(Paint.DITHER_FLAG)

    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null

    // 🔧 Reutiliza paints (NO en onDraw)
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5F5F5")
        style = Paint.Style.FILL
    }

    var currentColor: Int
        get() = drawPaint.color
        set(value) {
            drawPaint.color = value
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(width, height) / 2f * 0.95f

        // Fondo circular
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Guía
        drawAvatarGuide(canvas, cx, cy, radius)

        // Dibujo del usuario
        canvasBitmap?.let { canvas.drawBitmap(it, 0f, 0f, canvasPaint) }
        canvas.drawPath(drawPath, drawPaint)
    }

    private fun drawAvatarGuide(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val guidePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        val fillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Forma base del rostro (óvalo)
        val faceWidth = radius * 0.55f
        val faceHeight = radius * 0.67f
        val faceRect = RectF(
            cx - faceWidth,
            cy - faceHeight,
            cx + faceWidth,
            cy + faceHeight
        )

        // Relleno sutil del rostro
        fillPaint.color = Color.parseColor("#FFE4C4")
        fillPaint.alpha = 38 // 15% opacity
        canvas.drawOval(faceRect, fillPaint)

        // Contorno del rostro
        guidePaint.color = Color.parseColor("#D4A574")
        guidePaint.strokeWidth = 3f
        guidePaint.alpha = 102 // 40% opacity
        canvas.drawOval(faceRect, guidePaint)

        // Guía de cabello (línea superior)
        guidePaint.color = Color.parseColor("#8B7355")
        guidePaint.strokeWidth = 2f
        guidePaint.alpha = 77 // 30% opacity
        guidePaint.strokeCap = Paint.Cap.ROUND

        val hairPath = Path()
        hairPath.moveTo(cx - faceWidth * 0.8f, cy - faceHeight * 0.5f)
        hairPath.quadTo(cx, cy - faceHeight * 0.7f, cx + faceWidth * 0.8f, cy - faceHeight * 0.5f)
        canvas.drawPath(hairPath, guidePaint)

        // Ojos (posición y forma)
        guidePaint.color = Color.parseColor("#6B5D52")
        guidePaint.strokeWidth = 3f
        guidePaint.alpha = 64 // 25% opacity

        val eyeOffsetX = faceWidth * 0.35f
        val eyeY = cy - faceHeight * 0.15f
        val eyeWidth = radius * 0.1f
        val eyeHeight = radius * 0.12f

        // Ojo izquierdo
        val leftEyeRect = RectF(
            cx - eyeOffsetX - eyeWidth,
            eyeY - eyeHeight,
            cx - eyeOffsetX + eyeWidth,
            eyeY + eyeHeight
        )
        canvas.drawOval(leftEyeRect, guidePaint)

        // Pupila izquierda
        fillPaint.color = Color.parseColor("#6B5D52")
        fillPaint.alpha = 102 // 40% opacity
        canvas.drawCircle(cx - eyeOffsetX, eyeY + eyeHeight * 0.15f, radius * 0.03f, fillPaint)

        // Ojo derecho
        val rightEyeRect = RectF(
            cx + eyeOffsetX - eyeWidth,
            eyeY - eyeHeight,
            cx + eyeOffsetX + eyeWidth,
            eyeY + eyeHeight
        )
        canvas.drawOval(rightEyeRect, guidePaint)

        // Pupila derecha
        canvas.drawCircle(cx + eyeOffsetX, eyeY + eyeHeight * 0.15f, radius * 0.03f, fillPaint)

        // Cejas
        guidePaint.color = Color.parseColor("#8B7355")
        guidePaint.strokeWidth = 3f
        guidePaint.alpha = 51 // 20% opacity

        val browY = eyeY - eyeHeight * 1.5f
        val browWidth = eyeWidth * 2f

        // Ceja izquierda
        val leftBrowPath = Path()
        leftBrowPath.moveTo(cx - eyeOffsetX - browWidth * 0.5f, browY)
        leftBrowPath.quadTo(cx - eyeOffsetX, browY - radius * 0.02f, cx - eyeOffsetX + browWidth * 0.5f, browY)
        canvas.drawPath(leftBrowPath, guidePaint)

        // Ceja derecha
        val rightBrowPath = Path()
        rightBrowPath.moveTo(cx + eyeOffsetX - browWidth * 0.5f, browY)
        rightBrowPath.quadTo(cx + eyeOffsetX, browY - radius * 0.02f, cx + eyeOffsetX + browWidth * 0.5f, browY)
        canvas.drawPath(rightBrowPath, guidePaint)

        // Nariz (muy sutil)
        guidePaint.color = Color.parseColor("#D4A574")
        guidePaint.strokeWidth = 2f
        guidePaint.alpha = 51 // 20% opacity
        canvas.drawLine(cx, cy, cx, cy + faceHeight * 0.2f, guidePaint)

        // Boca (sonrisa amigable)
        guidePaint.color = Color.parseColor("#E89E9E")
        guidePaint.strokeWidth = 4f
        guidePaint.alpha = 77 // 30% opacity

        val mouthY = cy + faceHeight * 0.35f
        val mouthWidth = faceWidth * 0.5f

        val mouthPath = Path()
        mouthPath.moveTo(cx - mouthWidth, mouthY)
        mouthPath.quadTo(cx, mouthY + radius * 0.08f, cx + mouthWidth, mouthY)
        canvas.drawPath(mouthPath, guidePaint)

        // Mejillas (rubor)
        fillPaint.color = Color.parseColor("#FFB6C1")
        fillPaint.alpha = 38 // 15% opacity

        val cheekY = cy + faceHeight * 0.25f
        val cheekOffsetX = faceWidth * 0.75f
        val cheekWidth = radius * 0.1f
        val cheekHeight = radius * 0.07f

        val leftCheekRect = RectF(
            cx - cheekOffsetX - cheekWidth,
            cheekY - cheekHeight,
            cx - cheekOffsetX + cheekWidth,
            cheekY + cheekHeight
        )
        canvas.drawOval(leftCheekRect, fillPaint)

        val rightCheekRect = RectF(
            cx + cheekOffsetX - cheekWidth,
            cheekY - cheekHeight,
            cx + cheekOffsetX + cheekWidth,
            cheekY + cheekHeight
        )
        canvas.drawOval(rightCheekRect, fillPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.reset()
                drawPath.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(x, y)
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                drawCanvas?.drawPath(drawPath, drawPaint)
                drawPath.reset()
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        invalidate()
        return true
    }

    // Borrado seguro
    fun clear() {
        drawPath.reset()
        drawCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun getBitmapAsString(): String {
        val bitmap = canvasBitmap ?: return ""
        if (bitmap.width == 0 || bitmap.height == 0) return ""

        return try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    fun setBitmapFromString(base64: String) {
        if (base64.isEmpty()) return

        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return

            canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            drawCanvas = Canvas(canvasBitmap!!)
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
