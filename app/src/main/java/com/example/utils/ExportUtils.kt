package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.local.entities.ExperienceEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.SkillEntity
import com.example.data.local.entities.ThemeSettingsEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ExportUtils {

    /**
     * Generates an ATS-friendly, clean text-based PDF resume.
     * ATS (Applicant Tracking System) require simple single-column layouts, standard fonts,
     * and clear, high-contrast text markers without complex graphical tables.
     */
    fun exportToAtsPdf(
        context: Context,
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>
    ) {
        val pdfDocument = PdfDocument()
        
        // A4 Page dimension: 595 x 842 points (72 points/inch)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paintNormal = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintTitle = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 20f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintHeading = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        var yPosition = 50f
        val leftMargin = 50f
        val rightMargin = 545f
        val contentWidth = rightMargin - leftMargin

        // Draw Name
        canvas.drawText(profile.name.uppercase(), leftMargin, yPosition, paintTitle)
        yPosition += 24f

        // Draw Subtitle / Role
        canvas.drawText(profile.role, leftMargin, yPosition, paintSubtitle)
        yPosition += 18f

        // Contact Info Line
        val contactLine = "${profile.email}  |  ${profile.phone}  |  ${profile.location}"
        canvas.drawText(contactLine, leftMargin, yPosition, paintNormal)
        yPosition += 8f
        
        // Line under header
        canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paintNormal.apply { strokeWidth = 1f })
        yPosition += 25f

        // Helper to draw paragraph with word wrapping
        fun drawParagraph(text: String, startY: Float, paint: Paint, spacing: Float = 14f): Float {
            var currentY = startY
            val words = text.split(" ")
            var line = StringBuilder()
            
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "${line} $word"
                val width = paint.measureText(testLine)
                if (width > contentWidth) {
                    canvas.drawText(line.toString(), leftMargin, currentY, paint)
                    currentY += spacing
                    line = StringBuilder(word)
                } else {
                    line.append(if (line.isEmpty()) word else " $word")
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line.toString(), leftMargin, currentY, paint)
                currentY += spacing
            }
            return currentY
        }

        // Section: Resume Summary / Profile
        canvas.drawText("RESUMO PROFISSIONAL", leftMargin, yPosition, paintHeading)
        yPosition += 16f
        yPosition = drawParagraph(profile.bio, yPosition, paintNormal)
        yPosition += 15f

        // Section: Technical Skills
        canvas.drawText("HABILIDADES TÉCNICAS", leftMargin, yPosition, paintHeading)
        yPosition += 16f
        val devSkillsStr = "Desenvolvimento de Software: " + skills.filter { it.category == "Desenvolvimento" }.joinToString(", ") { it.name }
        val infraSkillsStr = "Infraestrutura de TI & Redes: " + skills.filter { it.category == "Infraestrutura" }.joinToString(", ") { it.name }

        yPosition = drawParagraph(devSkillsStr, yPosition, paintNormal)
        yPosition = drawParagraph(infraSkillsStr, yPosition, paintNormal)
        yPosition += 15f

        // Section: Professional Experience
        canvas.drawText("EXPERIÊNCIA PROFISSIONAL", leftMargin, yPosition, paintHeading)
        yPosition += 16f

        experiences.sortedBy { it.displayOrder }.forEach { exp ->
            // Check page overflow
            if (yPosition > 750f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }

            canvas.drawText(exp.role, leftMargin, yPosition, paintBold)
            val periodWidth = paintNormal.measureText(exp.period)
            canvas.drawText(exp.period, rightMargin - periodWidth, yPosition, paintNormal)
            yPosition += 14f

            canvas.drawText(exp.company, leftMargin, yPosition, paintBold.apply { color = android.graphics.Color.DKGRAY })
            yPosition += 14f

            yPosition = drawParagraph(exp.description, yPosition, paintNormal)
            yPosition += 12f
        }

        // Section: Social & Professional Links
        if (yPosition > 750f) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = 50f
        }
        yPosition += 10f
        canvas.drawText("LINKS E PORTFÓLIO", leftMargin, yPosition, paintHeading)
        yPosition += 16f
        canvas.drawText("LinkedIn: ${profile.linkedinUrl}", leftMargin, yPosition, paintNormal)
        yPosition += 14f
        canvas.drawText("GitHub: https://github.com/${profile.githubUsername}", leftMargin, yPosition, paintNormal)

        pdfDocument.finishPage(page)

        saveAndShareFile(context, pdfDocument, "Curriculo_ATS_${profile.name.replace(" ", "_")}.pdf", "application/pdf")
    }

    /**
     * Generates a modern self-contained styled HTML file of the portfolio, matching the active design,
     * including custom fonts, Material Symbols, and responsive grids.
     */
    fun exportToStyledHtml(
        context: Context,
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>,
        themeSettings: ThemeSettingsEntity
    ) {
        val primaryColor = themeSettings.primaryColorHex
        val secondaryColor = themeSettings.secondaryColorHex
        val bgColor = themeSettings.backgroundColorHex
        val textColor = themeSettings.textColorHex

        val devSkillsList = skills.filter { it.category == "Desenvolvimento" }
        val infraSkillsList = skills.filter { it.category == "Infraestrutura" }

        val skillsHtml = StringBuilder()
        if (devSkillsList.isNotEmpty()) {
            skillsHtml.append("""
                <div class="mb-6">
                    <h3 class="text-xs font-bold uppercase tracking-wider mb-3" style="color: $primaryColor">Desenvolvimento de Software</h3>
                    <div class="flex flex-wrap gap-2">
            """.trimIndent())
            devSkillsList.forEach {
                skillsHtml.append("<span class=\"px-3 py-1 bg-gray-100 border border-gray-200 rounded-lg text-sm font-medium text-gray-800\">${it.name}</span>")
            }
            skillsHtml.append("</div></div>")
        }

        if (infraSkillsList.isNotEmpty()) {
            skillsHtml.append("""
                <div class="mb-4">
                    <h3 class="text-xs font-bold uppercase tracking-wider mb-3" style="color: $secondaryColor">Redes & Infraestrutura de TI</h3>
                    <div class="flex flex-wrap gap-2">
            """.trimIndent())
            infraSkillsList.forEach {
                skillsHtml.append("<span class=\"px-3 py-1 bg-gray-100 border border-gray-200 rounded-lg text-sm font-medium text-gray-800\">${it.name}</span>")
            }
            skillsHtml.append("</div></div>")
        }

        val experienceHtml = StringBuilder()
        experiences.sortedBy { it.displayOrder }.forEachIndexed { index, exp ->
            experienceHtml.append("""
                <div class="flex gap-4 mb-6">
                    <div class="flex flex-col items-center">
                        <div class="w-3 h-3 rounded-full" style="background-color: $primaryColor"></div>
                        ${if (index < experiences.size - 1) "<div class=\"w-0.5 flex-1 bg-gray-200\"></div>" else ""}
                    </div>
                    <div class="pb-2">
                        <h3 class="text-base font-bold text-gray-900">${exp.role}</h3>
                        <p class="text-sm font-medium" style="color: $primaryColor">${exp.company} • ${exp.period}</p>
                        <p class="text-sm mt-2 text-gray-600 leading-relaxed">${exp.description}</p>
                    </div>
                </div>
            """.trimIndent())
        }

        val htmlContent = """
            <!doctype html>
            <html lang="pt-BR">
              <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>Portfólio Profissional - ${profile.name}</title>
                <script src="https://cdn.tailwindcss.com"></script>
                <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet" />
                <style>
                  body {
                    font-family: 'Plus Jakarta Sans', sans-serif;
                    background-color: $bgColor;
                    color: $textColor;
                  }
                </style>
              </head>
              <body class="min-h-screen py-12 px-4 md:px-8">
                <div class="max-w-4xl mx-auto space-y-8">
                  
                  <!-- Main Hero Header -->
                  <header class="rounded-[28px] p-8 md:p-12 text-white shadow-xl relative overflow-hidden" style="background: linear-gradient(135deg, $primaryColor, $secondaryColor)">
                    <div class="absolute -right-12 -top-12 w-48 h-48 rounded-full bg-white opacity-10"></div>
                    <div class="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
                      <div>
                        <h1 class="text-3xl md:text-4xl font-extrabold tracking-tight">${profile.name}</h1>
                        <p class="text-lg md:text-xl font-medium mt-2 opacity-95">${profile.role}</p>
                        <div class="flex items-center gap-2 mt-4 text-sm opacity-80">
                          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                          <span>${profile.location}</span>
                        </div>
                      </div>
                      <div class="flex flex-wrap gap-3">
                        <a href="${profile.linkedinUrl}" target="_blank" class="px-6 py-3 bg-white text-gray-900 font-bold rounded-xl shadow-md hover:bg-gray-50 transition flex items-center gap-2">
                          LinkedIn
                        </a>
                        <a href="https://github.com/${profile.githubUsername}" target="_blank" class="px-6 py-3 bg-black/20 border border-white/30 text-white font-bold rounded-xl hover:bg-white/10 transition flex items-center gap-2">
                          GitHub
                        </a>
                      </div>
                    </div>
                  </header>

                  <!-- Professional Summary -->
                  <section class="bg-white border border-gray-100 p-8 rounded-[24px] shadow-sm">
                    <h2 class="text-xs font-bold uppercase tracking-wider mb-4" style="color: $primaryColor">Sobre Mim</h2>
                    <p class="text-gray-700 text-base leading-relaxed">${profile.bio}</p>
                  </section>

                  <!-- Skills Section -->
                  <section class="bg-white border border-gray-100 p-8 rounded-[24px] shadow-sm">
                    <h2 class="text-xs font-bold uppercase tracking-wider mb-6" style="color: $primaryColor">Habilidades e Especialidades</h2>
                    $skillsHtml
                  </section>

                  <!-- Professional Experiences -->
                  <section class="bg-white border border-gray-100 p-8 rounded-[24px] shadow-sm">
                    <h2 class="text-xs font-bold uppercase tracking-wider mb-6" style="color: $primaryColor">Trajetória Profissional</h2>
                    $experienceHtml
                  </section>

                  <!-- Contact / Footer Info -->
                  <section class="bg-white border border-gray-100 p-8 rounded-[24px] shadow-sm text-center">
                    <h2 class="text-xs font-bold uppercase tracking-wider mb-4" style="color: $primaryColor">Contato Profissional</h2>
                    <p class="text-gray-600 mb-6">Sinta-se à vontade para entrar em contato para novas oportunidades de projetos e conexões.</p>
                    <div class="flex flex-col md:flex-row justify-center items-center gap-6 text-sm font-semibold">
                      <div class="flex items-center gap-2">
                        <span class="text-gray-400">E-mail:</span>
                        <a href="mailto:${profile.email}" class="hover:underline" style="color: $primaryColor">${profile.email}</a>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-gray-400">Telefone:</span>
                        <span class="text-gray-800">${profile.phone}</span>
                      </div>
                    </div>
                  </section>

                </div>
              </body>
            </html>
        """.trimIndent()

        saveAndShareFile(context, htmlContent, "Portfolio_${profile.name.replace(" ", "_")}.html", "text/html")
    }

    private fun saveAndShareFile(context: Context, pdfDocument: PdfDocument, filename: String, mimeType: String) {
        try {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    pdfDocument.writeTo(outputStream)
                    outputStream.close()
                    pdfDocument.close()
                    Toast.makeText(context, "Salvo na pasta Downloads: $filename", Toast.LENGTH_LONG).show()
                    triggerShareIntent(context, uri, mimeType, "Compartilhar Currículo ATS")
                } else {
                    pdfDocument.close()
                    Toast.makeText(context, "Erro ao abrir fluxo de gravação", Toast.LENGTH_SHORT).show()
                }
            } else {
                pdfDocument.close()
                Toast.makeText(context, "Erro ao criar arquivo no MediaStore", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(context, "Erro ao exportar PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAndShareFile(context: Context, content: String, filename: String, mimeType: String) {
        try {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    outputStream.write(content.toByteArray())
                    outputStream.close()
                    Toast.makeText(context, "Salvo na pasta Downloads: $filename", Toast.LENGTH_LONG).show()
                    triggerShareIntent(context, uri, mimeType, "Compartilhar Código HTML")
                } else {
                    Toast.makeText(context, "Erro ao abrir fluxo de gravação", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Erro ao criar arquivo no MediaStore", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao exportar HTML: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerShareIntent(context: Context, uri: Uri, mimeType: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
}
