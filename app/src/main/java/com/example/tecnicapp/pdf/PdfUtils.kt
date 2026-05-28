package com.example.tecnicapp.pdf

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import com.example.tecnicapp.R
import com.example.tecnicapp.data.TecnicasPorGrado
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val PDF_CHANNEL_ID = "pdf_download_channel"
private const val PDF_NOTIFICATION_ID = 1001

fun generateTecnicasPdf(
    context: Context,
    bloques: List<TecnicasPorGrado>
) {
    val document = PdfDocument()

    val pageWidth = 595
    val pageHeight = 842

    val titlePaint = Paint().apply {
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val bodyPaint = Paint().apply {
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    val margin = 40f
    val lineHeight = 16f
    val maxTextWidth = pageWidth - 2 * margin

    var currentPageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
    var page = document.startPage(pageInfo)
    var canvas = page.canvas
    var x = margin
    var y = margin

    fun newPage() {
        document.finishPage(page)
        currentPageNumber++
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        x = margin
        y = margin
    }

    fun drawWrappedText(text: String, paint: Paint) {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val count = paint.breakText(remaining, true, maxTextWidth, null)
            val line = remaining.substring(0, count)

            if (y > pageHeight - margin) {
                newPage()
            }

            canvas.drawText(line, x, y, paint)
            y += lineHeight
            remaining = remaining.substring(count)
        }
    }

    try {
        bloques.forEach { bloque ->
            if (y > pageHeight - margin) {
                newPage()
            }
            drawWrappedText("GRADO: ${bloque.grado.nombre}", titlePaint)
            y += lineHeight / 2

            bloque.tecnicas.forEach { tecnica ->
                if (y > pageHeight - margin) {
                    newPage()
                }
                drawWrappedText("• ${tecnica.nombre}", bodyPaint)

                if (tecnica.alias.isNotBlank()) {
                    drawWrappedText("   Alias: ${tecnica.alias}", bodyPaint)
                }
                if (tecnica.posicion.isNotBlank()) {
                    drawWrappedText("   Posición: ${tecnica.posicion}", bodyPaint)
                }
                if (tecnica.atacante.isNotBlank()) {
                    drawWrappedText("   Atacante: ${tecnica.atacante}", bodyPaint)
                }
                if (tecnica.defensor.isNotBlank()) {
                    drawWrappedText("   Defensor: ${tecnica.defensor}", bodyPaint)
                }
                if (tecnica.altura.isNotBlank()) {
                    drawWrappedText("   Altura: ${tecnica.altura}", bodyPaint)
                }
                if (tecnica.descripcion.isNotBlank()) {
                    drawWrappedText("   Descripción: ${tecnica.descripcion}", bodyPaint)
                }

                y += lineHeight
            }

            y += lineHeight
        }

        document.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (dir == null) {
            Toast.makeText(context, "No se pudo acceder a la carpeta de documentos", Toast.LENGTH_LONG).show()
            document.close()
            return
        }
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "tecnicas.pdf")

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }

        document.close()

        showPdfNotification(context, file)

    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error al guardar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        document.close()
    }
}

private fun showPdfNotification(context: Context, file: File) {
    // Authority fijo: debe coincidir con el AndroidManifest
    val uri: Uri = FileProvider.getUriForFile(
        context,
        "com.example.tecnicapp.fileprovider",
        file
    )

    val openPdfIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        openPdfIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    )

    val builder = NotificationCompat.Builder(context, PDF_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher) // icono seguro que existe
        .setContentTitle("PDF generado")
        .setContentText("Pulsa para abrir el PDF de técnicas")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    NotificationManagerCompat.from(context).notify(PDF_NOTIFICATION_ID, builder.build())
}