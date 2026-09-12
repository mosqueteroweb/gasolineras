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
     * Creates a distinctive GPS location dot (Google Maps style: blue dot with white outline and faint halo).
     */
    fun createUserLocationMarker(context: Context): Drawable {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer halo
        paint.color = Color.argb(60, 25, 118, 210) // Faint blue
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // White border
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size * 0.35f, paint)

        // Solid Blue core
        paint.color = Color.rgb(25, 118, 210)
        canvas.drawCircle(size / 2f, size / 2f, size * 0.26f, paint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Creates custom map pin markers for gas stations with specific highlight colors and badges.
     */
    fun createGasStationMarker(
        context: Context,
        pinColor: Int,
        label: String? = null
    ): Drawable {
        val width = 72
        val height = 90
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Pin shadow
        paint.color = Color.argb(50, 0, 0, 0)
        canvas.drawOval(width * 0.2f, height * 0.85f, width * 0.8f, height * 0.98f, paint)

        // Pin body path
        val path = Path()
        val radius = width * 0.45f
        val centerX = width / 2f
        val centerY = radius

        path.addCircle(centerX, centerY, radius, Path.Direction.CW)
        // Triangle tip pointing downwards
        path.moveTo(centerX - radius * 0.7f, centerY + radius * 0.6f)
        path.lineTo(centerX, height * 0.86f)
        path.lineTo(centerX + radius * 0.7f, centerY + radius * 0.6f)
        path.close()

        // Draw pin background
        paint.color = pinColor
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        // White outline for sharpness
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawPath(path, paint)

        // Inner white circle
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius * 0.65f, paint)

        // Draw badge/text/symbol in the center
        paint.color = pinColor
        paint.textSize = 22f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        val textToDraw = label ?: "⛽"
        val fontMetrics = paint.fontMetrics
        val textY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(textToDraw, centerX, textY, paint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
