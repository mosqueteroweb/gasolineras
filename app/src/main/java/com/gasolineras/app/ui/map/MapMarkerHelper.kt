package com.gasolineras.app.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

object MapMarkerHelper {

    /**
     * Creates a distinctive GPS location dot (Google Maps style: blue dot with white outline and faint halo),
     * properly scaled to screen density.
     */
    fun createUserLocationMarker(context: Context): Drawable {
        val density = context.resources.displayMetrics.density
        val size = (32 * density).toInt().coerceAtLeast(64)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer halo
        paint.color = Color.argb(55, 33, 150, 243)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // White border
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size * 0.36f, paint)

        // Solid Blue core
        paint.color = Color.rgb(25, 118, 210)
        canvas.drawCircle(size / 2f, size / 2f, size * 0.26f, paint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Creates custom map pin markers for gas stations with specific highlight colors and badges,
     * scaled to screen density so they are large, sharp and easily readable.
     */
    fun createGasStationMarker(
        context: Context,
        pinColor: Int,
        label: String? = null
    ): Drawable {
        val density = context.resources.displayMetrics.density
        val width = (36 * density).toInt().coerceAtLeast(80)
        val height = (48 * density).toInt().coerceAtLeast(106)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Pin shadow
        paint.color = Color.argb(60, 0, 0, 0)
        canvas.drawOval(width * 0.15f, height * 0.84f, width * 0.85f, height * 0.98f, paint)

        // Pin body path
        val path = Path()
        val radius = width * 0.44f
        val centerX = width / 2f
        val centerY = radius + 2 * density

        path.addCircle(centerX, centerY, radius, Path.Direction.CW)
        // Triangle tip pointing downwards
        path.moveTo(centerX - radius * 0.72f, centerY + radius * 0.58f)
        path.lineTo(centerX, height * 0.88f)
        path.lineTo(centerX + radius * 0.72f, centerY + radius * 0.58f)
        path.close()

        // Draw pin background
        paint.color = pinColor
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        // White outline for sharpness
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * density
        canvas.drawPath(path, paint)

        // Inner white circle
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius * 0.65f, paint)

        // Draw badge/text/symbol in the center
        paint.color = pinColor
        paint.textSize = 15f * density
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        val textToDraw = label ?: "⛽"
        val fontMetrics = paint.fontMetrics
        val textY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(textToDraw, centerX, textY, paint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
