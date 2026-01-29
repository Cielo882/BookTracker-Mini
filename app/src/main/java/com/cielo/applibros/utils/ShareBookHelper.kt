package com.cielo.applibros.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.cielo.applibros.R
import com.cielo.applibros.domain.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.graphics.scale


class ShareBookHelper(private val context: Context) {

    suspend fun createShareImage(book: Book): Uri? = withContext(Dispatchers.IO) {
        try {
            val width = 1080
            val height = 1920

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            drawBackground(canvas, width, height)
            drawBookCover(canvas, book.imageUrl, width)
            drawBookInfo(canvas, book, width, height)
            drawAppLogo(canvas, width, height)

            val file = saveImageToCache(bitmap)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int) {
        try {
            val backgroundBitmap = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.profile_pattern
            )

            val scaledBackground = Bitmap.createScaledBitmap(
                backgroundBitmap,
                width,
                height,
                true
            )

            canvas.drawBitmap(scaledBackground, 0f, 0f, null)

            val nightMode = context.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK

            val overlayPaint = Paint().apply {
                if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
                    color = Color.parseColor("#CC000000")
                } else {
                    color = Color.parseColor("#B3FFFDF5")
                }
            }

            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        } catch (e: Exception) {
            drawGradientBackground(canvas, width, height)
        }
    }

    private fun drawGradientBackground(canvas: Canvas, width: Int, height: Int) {
        val nightMode = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        val colors = if (nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            intArrayOf(
                Color.parseColor("#2B2421"),
                Color.parseColor("#3A332E")
            )
        } else {
            intArrayOf(
                Color.parseColor("#F9F5F0"),
                Color.parseColor("#E8D5C4")
            )
        }

        val gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            shader = gradient
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private suspend fun drawBookCover(canvas: Canvas, coverUrl: String?, width: Int) {
        try {
            val coverBitmap = withContext(Dispatchers.IO) {
                Glide.with(context)
                    .asBitmap()
                    .load(coverUrl)
                    .submit(400, 600)
                    .get()
            }

            val shadowPaint = Paint().apply {
                color = Color.BLACK
                alpha = 50
                maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            }

            val coverX = (width - coverBitmap.width) / 2f
            val coverY = 200f

            canvas.drawRoundRect(
                coverX - 10f,
                coverY + 10f,
                coverX + coverBitmap.width + 10f,
                coverY + coverBitmap.height + 10f,
                20f, 20f,
                shadowPaint
            )

            val path = Path().apply {
                addRoundRect(
                    coverX, coverY,
                    coverX + coverBitmap.width,
                    coverY + coverBitmap.height,
                    20f, 20f,
                    Path.Direction.CW
                )
            }

            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(coverBitmap, coverX, coverY, null)
            canvas.restore()

        } catch (e: Exception) {
            drawPlaceholder(canvas, width)
        }
    }

    private fun drawPlaceholder(canvas: Canvas, width: Int) {
        val placeholderPaint = Paint().apply {
            color = Color.parseColor("#D4A59A")
            style = Paint.Style.FILL
        }

        val placeholderRect = RectF(
            (width - 400) / 2f,
            200f,
            (width + 400) / 2f,
            800f
        )

        canvas.drawRoundRect(placeholderRect, 20f, 20f, placeholderPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 120f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawText("📚", width / 2f, 550f, textPaint)
    }

    private fun drawBookInfo(canvas: Canvas, book: Book, width: Int, height: Int) {
        val textColor = if (isDarkMode()) Color.parseColor("#E8DFD8")
        else Color.parseColor("#4A4039")

        val accentColor = Color.parseColor("#D4A59A")

        var yPosition = 900f

        val titlePaint = Paint().apply {
            color = textColor
            textSize = 56f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        drawMultilineText(
            canvas,
            book.title,
            width / 2f,
            yPosition,
            titlePaint,
            width - 120,
            2
        )

        yPosition += 140f

        val authorPaint = Paint().apply {
            color = textColor
            alpha = 200
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawText(book.authorsString, width / 2f, yPosition, authorPaint)
        yPosition += 100f

        val linePaint = Paint().apply {
            color = accentColor
            strokeWidth = 4f
            alpha = 150
        }

        canvas.drawLine(
            width / 2f - 150f,
            yPosition,
            width / 2f + 150f,
            yPosition,
            linePaint
        )

        yPosition += 80f

        drawRating(canvas, book.rating ?: 0, width / 2f, yPosition)
        yPosition += 100f

        drawStats(canvas, book, width / 2f, yPosition, textColor)
    }

    // ✅ MÉTODO CORREGIDO CON STRING RESOURCES
    private fun drawStats(canvas: Canvas, book: Book, centerX: Float, y: Float, textColor: Int) {
        val statPaint = Paint().apply {
            color = textColor
            textSize = 36f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        var currentY = y

        // ✅ Días de lectura con string resources
        book.startDate?.let { start ->
            book.finishDate?.let { finish ->
                val days = ((finish - start) / (1000 * 60 * 60 * 24)).toInt()

                // ✅ Usar string resource según si es 1 día o más
                val daysText = if (days <= 1) {
                    context.getString(R.string.stats_read_in_one_day)
                } else {
                    context.getString(R.string.stats_read_in_days, days)
                }

                canvas.drawText(daysText, centerX, currentY, statPaint)
                currentY += 60f
            }
        }

        // ✅ Fecha de término con string resource
        book.finishDate?.let { finish ->
            // ✅ IMPORTANTE: El formato de fecha también debe adaptarse al idioma
            val locale = if (context.resources.configuration.locales[0].language == "en") {
                Locale.ENGLISH
            } else {
                Locale("es")
            }

            val dateFormat = SimpleDateFormat("d MMM yyyy", locale)
            val dateText = dateFormat.format(Date(finish))

            val finishedText = context.getString(R.string.stats_finished_on, dateText)
            canvas.drawText(finishedText, centerX, currentY, statPaint)
        }
    }

    private fun drawAppLogo(canvas: Canvas, width: Int, height: Int) {
        val baseY = height - 140f

        try {
            val logoBitmap = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.playstore_logo
            )

            val logoSize = 80
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)

            val centerX = width / 2f
            val centerY = baseY - 10f
            val logoX = centerX - logoSize / 2f
            val logoY = centerY - logoSize / 2f

            val typedValue = android.util.TypedValue()
            val theme = context.theme

            theme.resolveAttribute(
                com.google.android.material.R.attr.colorSurface,
                typedValue,
                true
            )
            val surfaceColor = typedValue.data

            theme.resolveAttribute(
                com.google.android.material.R.attr.colorPrimary,
                typedValue,
                true
            )
            val primaryColor = typedValue.data

            val bgPaint = Paint().apply {
                color = surfaceColor
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            canvas.drawCircle(centerX, centerY, (logoSize / 2f) + 12f, bgPaint)

            val borderPaint = Paint().apply {
                color = primaryColor
                alpha = 100
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }

            canvas.drawCircle(centerX, centerY, (logoSize / 2f) + 12f, borderPaint)

            val path = Path().apply {
                addCircle(centerX, centerY, logoSize / 2f, Path.Direction.CW)
            }

            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(scaledLogo, logoX, logoY, null)
            canvas.restore()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnSurfaceVariant,
            typedValue,
            true
        )
        val textColor = typedValue.data

        val textPaint = Paint().apply {
            color = textColor
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        canvas.drawText("BookTracker Mini", width / 2f, baseY + 90f, textPaint)
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Int,
        maxLines: Int
    ) {
        val words = text.split(" ")
        var currentLine = ""
        var lineCount = 0
        var currentY = y

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)

            if (testWidth > maxWidth) {
                if (lineCount < maxLines - 1) {
                    canvas.drawText(currentLine, x, currentY, paint)
                    currentY += paint.textSize + 10f
                    currentLine = word
                    lineCount++
                } else {
                    canvas.drawText("$currentLine...", x, currentY, paint)
                    return
                }
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
        }
    }

    private fun saveImageToCache(bitmap: Bitmap): File {
        val imagesDir = File(context.cacheDir, "shared_images")
        imagesDir.mkdirs()

        val file = File(imagesDir, "book_share_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return file
    }

    private fun isDarkMode(): Boolean {
        return context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    // ✅ MÉTODO CORREGIDO CON STRING RESOURCES
    fun shareBook(imageUri: Uri, book: Book) {
        val shareText = buildString {
            // ✅ Texto inicial
            append(context.getString(R.string.share_finished_reading))
            append("\n\n")

            // Título del libro (no necesita traducción)
            append("\"${book.title}\"\n")

            // ✅ "por [autor]" traducido
            append(context.getString(R.string.share_by_author, book.authorsString))
            append("\n\n")

            // ✅ Rating traducido
            book.rating?.let {
                append(context.getString(R.string.share_my_rating, it))
                append("\n\n")
            }

            // ✅ Tiempo de lectura traducido
            book.startDate?.let { start ->
                book.finishDate?.let { finish ->
                    val days = ((finish - start) / (1000 * 60 * 60 * 24)).toInt()
                    append(context.getString(R.string.share_reading_time, days))
                    append("\n\n")
                }
            }

            // ✅ Hashtags traducidos
            append(context.getString(R.string.share_hashtags))
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // ✅ Título del diálogo traducido
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.share_dialog_title))
        )
    }

    private fun drawStar(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        radius: Float,
        paint: Paint
    ) {
        val path = Path()
        val innerRadius = radius * 0.5
        val angle = Math.PI / 5

        for (i in 0..9) {
            val r = if (i % 2 == 0) radius else innerRadius
            val a = i * angle - Math.PI / 2
            val x = (cx.toDouble() + cos(a) * r.toDouble()).toFloat()
            val y = (cy.toDouble() + sin(a) * r.toDouble()).toFloat()

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawRating(canvas: Canvas, rating: Int, centerX: Float, y: Float) {
        val starSize = 26f
        val spacing = 18f
        val totalWidth = 5 * starSize * 2 + 4 * spacing
        val startX = centerX - totalWidth / 2

        val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F4C430")
            style = Paint.Style.FILL
            setShadowLayer(10f, 0f, 4f, Color.parseColor("#66000000"))
        }

        val depthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B08A00")
            style = Paint.Style.FILL
        }

        val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CFC2B8")
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }

        for (i in 0 until 5) {
            val x = startX + i * (starSize * 2 + spacing)

            if (i < rating) {
                drawStar(canvas, x, y + 4f, starSize, depthPaint)
                drawStar(canvas, x, y, starSize, filledPaint)
            } else {
                drawStar(canvas, x, y, starSize, emptyPaint)
            }
        }
    }
}