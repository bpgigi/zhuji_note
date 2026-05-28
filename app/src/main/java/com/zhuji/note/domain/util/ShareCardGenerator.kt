package com.zhuji.note.domain.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File

object ShareCardGenerator {
    fun generate(context: Context, title: String, content: String, appName: String = "ZhujiNote"): File {
        val w = 1080
        val padding = 80f
        val textWidth = w - (padding * 2).toInt()

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1A1A1A")
            textSize = 56f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3D3D3D")
            textSize = 40f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#999999")
            textSize = 32f
        }

        val titleLayout = StaticLayout.Builder.obtain(title.take(100), 0, title.take(100).length, titlePaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).setMaxLines(3).build()
        val bodyText = content.take(500)
        val bodyLayout = StaticLayout.Builder.obtain(bodyText, 0, bodyText.length, bodyPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).setMaxLines(15).build()

        val h = (padding + titleLayout.height + 40 + bodyLayout.height + 60 + 48 + padding).toInt()
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(), Color.parseColor("#FAF9F5"), Color.parseColor("#F0EDE6"), Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), 32f, 32f, bgPaint)

        val accentPaint = Paint().apply { color = Color.parseColor("#CC785C") }
        canvas.drawRoundRect(RectF(padding - 20, padding, padding - 12, padding + titleLayout.height), 4f, 4f, accentPaint)

        canvas.save()
        canvas.translate(padding, padding)
        titleLayout.draw(canvas)
        canvas.restore()

        canvas.save()
        canvas.translate(padding, padding + titleLayout.height + 40)
        bodyLayout.draw(canvas)
        canvas.restore()

        val footerY = padding + titleLayout.height + 40 + bodyLayout.height + 60
        canvas.drawText("$appName · 助记笔记", padding, footerY, footerPaint)

        val file = File(context.cacheDir, "share_card.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
        bitmap.recycle()
        return file
    }

    fun shareImage(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享笔记卡片"))
    }
}
