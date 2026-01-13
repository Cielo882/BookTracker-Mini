package com.cielo.applibros.presentation.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
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
    private val drawPaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val canvasPaint = Paint(Paint.DITHER_FLAG)
    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null

    // Base del avatar (silueta de persona)
    private val basePaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.FILL
        isAntiAlias = true
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
        // Dibujar fondo circular
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(width, height) / 2f * 0.9f

        // Fondo blanco circular
        canvas.drawCircle(centerX, centerY, radius, Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        })

        // Dibujar silueta base (opcional, como guía)
        drawBaseAvatar(canvas, centerX, centerY, radius)

        // Dibujar el bitmap con los trazos del usuario
        canvasBitmap?.let { canvas.drawBitmap(it, 0f, 0f, canvasPaint) }

        // Dibujar el path actual
        canvas.drawPath(drawPath, drawPaint)
    }

    private fun drawBaseAvatar(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        // Cabeza (círculo)
        canvas.drawCircle(cx, cy - radius * 0.2f, radius * 0.3f, basePaint)

        // Cuerpo (semicírculo inferior)
        val bodyRect = RectF(
            cx - radius * 0.4f,
            cy + radius * 0.1f,
            cx + radius * 0.4f,
            cy + radius * 0.8f
        )
        canvas.drawArc(bodyRect, 0f, 180f, true, basePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                drawCanvas?.drawPath(drawPath, drawPaint)
                drawPath.reset()
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        drawCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun getBitmapAsString(): String {
        val bitmap = canvasBitmap ?: return ""

        // ✅ VERIFICAR: Que el bitmap tenga contenido
        if (bitmap.width == 0 || bitmap.height == 0) {
            Log.d("DrawableAvatarView", "Bitmap has no size")
            return ""
        }

        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()

            Log.d("DrawableAvatarView", "Bitmap size: ${byteArray.size} bytes")

            return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("DrawableAvatarView", "Error encoding bitmap", e)
            return ""
        }
    }
    fun setBitmapFromString(base64: String) {
        try {
            if (base64.isEmpty()) return

            val decodedBytes = android.util.Base64.decode(
                base64,
                android.util.Base64.DEFAULT
            )

            val bitmap = BitmapFactory.decodeByteArray(
                decodedBytes,
                0,
                decodedBytes.size
            ) ?: return

            // 🔧 MUY IMPORTANTE: copiar a bitmap mutable
            canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            drawCanvas = Canvas(canvasBitmap!!)

            invalidate()

            Log.d("DrawableAvatarView", "Bitmap restored successfully")

        } catch (e: Exception) {
            Log.e("DrawableAvatarView", "Error restoring bitmap", e)
        }
    }


    fun loadFromString(base64: String) {
        try {
            val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            canvasBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            drawCanvas = Canvas(canvasBitmap!!)
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}