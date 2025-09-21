package ar.edu.utn.frba.mobile.foodforall.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.roundToInt

fun BitmapDescriptorFromEmoji(
    context: Context,
    emoji: String,
    sizeDp: Float = 32f,
    withBackground: Boolean = false
): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).roundToInt()

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    if (withBackground) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x22000000
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.06f
        }
        val r = sizePx / 2f
        canvas.drawCircle(r, r, r, bg)
        canvas.drawCircle(r, r, r - stroke.strokeWidth / 2f, stroke)
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isSubpixelText = true
        textSize = sizePx * 0.8f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    val x = sizePx / 2f
    val y = sizePx / 2f - (paint.descent() + paint.ascent()) / 2
    canvas.drawText(emoji, x, y, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
