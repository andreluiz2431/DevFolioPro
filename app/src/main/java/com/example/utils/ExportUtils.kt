package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.local.entities.CertificateEntity
import com.example.data.local.entities.EducationEntity
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
        experiences: List<ExperienceEntity>,
        educations: List<EducationEntity> = emptyList()
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
        val skillsByCategory = skills.groupBy { it.category }
        skillsByCategory.forEach { (category, skillList) ->
            if (skillList.isNotEmpty()) {
                val categoryStr = "$category: " + skillList.joinToString(", ") { it.name }
                yPosition = drawParagraph(categoryStr, yPosition, paintNormal)
            }
        }
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

        // Section: Academic Experience / Education
        if (educations.isNotEmpty()) {
            if (yPosition > 750f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            yPosition += 8f
            canvas.drawText("FORMAÇÃO ACADÊMICA", leftMargin, yPosition, paintHeading)
            yPosition += 16f

            educations.sortedBy { it.displayOrder }.forEach { edu ->
                if (yPosition > 750f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }

                val titleLine = if (edu.fieldOfStudy.isNotBlank()) "${edu.degree} - ${edu.fieldOfStudy}" else edu.degree
                canvas.drawText(titleLine, leftMargin, yPosition, paintBold)
                val periodWidth = paintNormal.measureText(edu.period)
                canvas.drawText(edu.period, rightMargin - periodWidth, yPosition, paintNormal)
                yPosition += 14f

                canvas.drawText(edu.institution, leftMargin, yPosition, paintBold.apply { color = android.graphics.Color.DKGRAY })
                yPosition += 14f

                if (edu.description.isNotBlank()) {
                    yPosition = drawParagraph(edu.description, yPosition, paintNormal)
                    yPosition += 4f
                }
                yPosition += 8f
            }
        }

        // Section: Social & Professional Links
        val hasLinkedin = profile.linkedinUrl.isNotBlank()
        val hasGithub = profile.githubUsername.isNotBlank()
        if (hasLinkedin || hasGithub) {
            if (yPosition > 750f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 50f
            }
            yPosition += 10f
            canvas.drawText("LINKS E PORTFÓLIO", leftMargin, yPosition, paintHeading)
            yPosition += 16f
            if (hasLinkedin) {
                canvas.drawText("LinkedIn: ${profile.linkedinUrl.trim()}", leftMargin, yPosition, paintNormal)
                yPosition += 14f
            }
            if (hasGithub) {
                val githubLink = if (profile.githubUsername.trim().startsWith("http")) profile.githubUsername.trim() else "https://github.com/${profile.githubUsername.trim()}"
                canvas.drawText("GitHub: $githubLink", leftMargin, yPosition, paintNormal)
                yPosition += 14f
            }
        }

        pdfDocument.finishPage(page)

        saveAndShareFile(context, pdfDocument, "Curriculo_ATS_${profile.name.replace(" ", "_")}.pdf", "application/pdf")
    }

    /**
     * Generates a fully styled visual PDF resume matching the HTML layout and colors,
     * optimized for high-quality printing, PDF archiving, and digital sharing.
     */
    fun exportToStyledPdf(
        context: Context,
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>,
        themeSettings: ThemeSettingsEntity,
        educations: List<EducationEntity> = emptyList(),
        certificates: List<CertificateEntity> = emptyList()
    ) {
        val pdfDocument = PdfDocument()

        // A4 Page dimension: 595 x 842 points (72 points/inch)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var pageNumber = 1
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val primaryColorInt = parseColorSafe(themeSettings.primaryColorHex, Color.rgb(37, 99, 235))
        val secondaryColorInt = parseColorSafe(themeSettings.secondaryColorHex, Color.rgb(79, 70, 229))

        val leftMargin = 36f
        val rightMargin = 559f
        val contentWidth = rightMargin - leftMargin

        val paintNormal = Paint().apply {
            color = Color.rgb(55, 65, 81) // Gray 700
            textSize = 9.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = Color.rgb(17, 24, 39) // Gray 900
            textSize = 10f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSectionHeading = Paint().apply {
            color = primaryColorInt
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        // Helper to check page break and transition smoothly
        fun checkPageBreak(currentY: Float, requiredSpace: Float = 40f): Float {
            var y = currentY
            if (y + requiredSpace > 780f) {
                // Draw footer on current page
                val footerPaint = Paint().apply {
                    color = Color.rgb(156, 163, 175)
                    textSize = 8f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    isAntiAlias = true
                }
                val pageText = "Página $pageNumber"
                canvas.drawText(pageText, rightMargin - footerPaint.measureText(pageText), 815f, footerPaint)
                canvas.drawText("${profile.name} • Currículo Profissional", leftMargin, 815f, footerPaint)

                pdfDocument.finishPage(page)
                pageNumber++
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                // New page header line
                val headerRulePaint = Paint().apply {
                    color = Color.rgb(229, 231, 235)
                    strokeWidth = 1f
                }
                canvas.drawLine(leftMargin, 35f, rightMargin, 35f, headerRulePaint)
                val topNotePaint = Paint().apply {
                    color = primaryColorInt
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("${profile.name} — ${profile.role}", leftMargin, 28f, topNotePaint)
                y = 55f
            }
            return y
        }

        // Helper to draw wrapped text lines
        fun drawStyledParagraph(text: String, startY: Float, paint: Paint, spacing: Float = 13.5f): Float {
            var currentY = startY
            val words = text.split(" ")
            var line = StringBuilder()

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val width = paint.measureText(testLine)
                if (width > contentWidth) {
                    currentY = checkPageBreak(currentY, spacing)
                    canvas.drawText(line.toString(), leftMargin, currentY, paint)
                    currentY += spacing
                    line = StringBuilder(word)
                } else {
                    line.append(if (line.isEmpty()) word else " $word")
                }
            }
            if (line.isNotEmpty()) {
                currentY = checkPageBreak(currentY, spacing)
                canvas.drawText(line.toString(), leftMargin, currentY, paint)
                currentY += spacing
            }
            return currentY
        }

        // 1. Hero Header Banner (Top stylized card with gradient)
        val headerHeight = 115f
        val headerRect = RectF(leftMargin, 32f, rightMargin, 32f + headerHeight)
        val headerPaint = Paint().apply {
            isAntiAlias = true
            shader = LinearGradient(
                headerRect.left, headerRect.top,
                headerRect.right, headerRect.bottom,
                primaryColorInt, secondaryColorInt,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(headerRect, 18f, 18f, headerPaint)

        // Accent translucent circle on header
        val accentCirclePaint = Paint().apply {
            color = Color.WHITE
            alpha = 25
            isAntiAlias = true
        }
        canvas.drawCircle(rightMargin - 30f, 32f + 25f, 60f, accentCirclePaint)

        // Photo or Avatar on Hero
        val avatarSize = 64f
        val avatarX = leftMargin + 18f
        val avatarY = 32f + (headerHeight - avatarSize) / 2f
        val profileBmp = getProfileBitmap(context, profile.photoUrl)

        if (profileBmp != null) {
            drawCircularBitmap(canvas, profileBmp, avatarX, avatarY, avatarSize, Color.WHITE)
        } else {
            // Draw Monogram Initials Avatar
            val avatarBgPaint = Paint().apply {
                color = Color.WHITE
                alpha = 60
                isAntiAlias = true
            }
            canvas.drawCircle(avatarX + avatarSize / 2f, avatarY + avatarSize / 2f, avatarSize / 2f, avatarBgPaint)
            val borderPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawCircle(avatarX + avatarSize / 2f, avatarY + avatarSize / 2f, avatarSize / 2f, borderPaint)

            val initials = if (profile.name.isNotBlank()) profile.name.first().uppercase() else "P"
            val initialPaint = Paint().apply {
                color = Color.WHITE
                textSize = 26f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(initials, avatarX + avatarSize / 2f, avatarY + avatarSize / 2f + 9f, initialPaint)
        }

        // Header Text
        val textStartX = avatarX + avatarSize + 16f
        val textWidth = rightMargin - textStartX - 16f

        val headerNamePaint = Paint().apply {
            color = Color.WHITE
            textSize = 19f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val headerRolePaint = Paint().apply {
            color = Color.WHITE
            alpha = 235
            textSize = 11.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val headerInfoPaint = Paint().apply {
            color = Color.WHITE
            alpha = 210
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText(profile.name, textStartX, 32f + 36f, headerNamePaint)
        canvas.drawText(profile.role, textStartX, 32f + 54f, headerRolePaint)

        val contactLine1 = "${profile.email}   •   ${profile.phone}"
        canvas.drawText(contactLine1, textStartX, 32f + 73f, headerInfoPaint)

        val contactLine2 = StringBuilder(profile.location)
        if (profile.linkedinUrl.isNotBlank()) {
            contactLine2.append("   •   LinkedIn: ${profile.linkedinUrl.trim()}")
        }
        if (profile.githubUsername.isNotBlank()) {
            contactLine2.append("   •   GitHub: ${profile.githubUsername.trim()}")
        }
        // Truncate if too long for one line
        var contactStr = contactLine2.toString()
        if (headerInfoPaint.measureText(contactStr) > textWidth) {
            while (headerInfoPaint.measureText("$contactStr...") > textWidth && contactStr.length > 5) {
                contactStr = contactStr.substring(0, contactStr.length - 1)
            }
            contactStr = "$contactStr..."
        }
        canvas.drawText(contactStr, textStartX, 32f + 90f, headerInfoPaint)

        var yPosition = 32f + headerHeight + 24f

        // Helper to draw a Section Title with modern accent bar
        fun drawSectionHeader(title: String) {
            yPosition = checkPageBreak(yPosition, 30f)
            canvas.drawText(title.uppercase(), leftMargin, yPosition, paintSectionHeading)

            val textWidthMeasured = paintSectionHeading.measureText(title.uppercase())
            val linePaint = Paint().apply {
                color = Color.rgb(229, 231, 235) // Gray 200
                strokeWidth = 1.2f
            }
            val accentPaint = Paint().apply {
                color = primaryColorInt
                strokeWidth = 2.5f
                strokeCap = Paint.Cap.ROUND
            }
            // Accent bar under text
            canvas.drawLine(leftMargin, yPosition + 5f, leftMargin + textWidthMeasured + 10f, yPosition + 5f, accentPaint)
            // Lighter line continuation
            canvas.drawLine(leftMargin + textWidthMeasured + 14f, yPosition + 5f, rightMargin, yPosition + 5f, linePaint)

            yPosition += 18f
        }

        // 2. Sobre Mim / Resumo
        if (profile.bio.isNotBlank()) {
            drawSectionHeader("Sobre Mim")
            yPosition = drawStyledParagraph(profile.bio, yPosition, paintNormal)
            yPosition += 14f
        }

        // 3. Habilidades e Especialidades (Styled Pill Badges)
        if (skills.isNotEmpty()) {
            drawSectionHeader("Habilidades e Especialidades")
            val skillsByCategory = skills.groupBy { it.category }

            val badgeBgPaint = Paint().apply {
                color = Color.rgb(243, 244, 246) // Gray 100
                isAntiAlias = true
            }
            val badgeBorderPaint = Paint().apply {
                color = Color.rgb(229, 231, 235) // Gray 200
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
            val badgeTextPaint = Paint().apply {
                color = Color.rgb(31, 41, 55) // Gray 800
                textSize = 8.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            val catTitlePaint = Paint().apply {
                color = primaryColorInt
                textSize = 9f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                isAntiAlias = true
            }

            skillsByCategory.forEach { (category, skillList) ->
                if (skillList.isNotEmpty()) {
                    yPosition = checkPageBreak(yPosition, 24f)
                    canvas.drawText(category.uppercase(), leftMargin, yPosition, catTitlePaint)
                    yPosition += 12f

                    var currentX = leftMargin
                    val badgeHeight = 18f
                    val horizontalPadding = 8f

                    skillList.forEach { skill ->
                        val textWidthM = badgeTextPaint.measureText(skill.name)
                        val badgeWidth = textWidthM + (horizontalPadding * 2f)

                        if (currentX + badgeWidth > rightMargin) {
                            currentX = leftMargin
                            yPosition += badgeHeight + 6f
                            yPosition = checkPageBreak(yPosition, badgeHeight + 10f)
                        }

                        val badgeRect = RectF(currentX, yPosition - 11f, currentX + badgeWidth, yPosition - 11f + badgeHeight)
                        canvas.drawRoundRect(badgeRect, 6f, 6f, badgeBgPaint)
                        canvas.drawRoundRect(badgeRect, 6f, 6f, badgeBorderPaint)
                        canvas.drawText(skill.name, currentX + horizontalPadding, yPosition + 1.5f, badgeTextPaint)

                        currentX += badgeWidth + 6f
                    }
                    yPosition += badgeHeight + 10f
                }
            }
            yPosition += 4f
        }

        // 4. Trajetória Profissional (Timeline with colored markers)
        if (experiences.isNotEmpty()) {
            drawSectionHeader("Trajetória Profissional")

            experiences.sortedBy { it.displayOrder }.forEachIndexed { index, exp ->
                yPosition = checkPageBreak(yPosition, 45f)

                val timelineX = leftMargin + 6f
                val contentLeft = leftMargin + 20f

                // Node circle
                val circlePaint = Paint().apply {
                    color = primaryColorInt
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, yPosition - 2f, 4f, circlePaint)
                val whiteCenterPaint = Paint().apply {
                    color = Color.WHITE
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, yPosition - 2f, 1.8f, whiteCenterPaint)

                // Role title & period
                canvas.drawText(exp.role, contentLeft, yPosition, paintBold.apply { textSize = 10.5f })
                val periodPaint = Paint().apply {
                    color = Color.rgb(107, 114, 128)
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    isAntiAlias = true
                }
                val periodWidth = periodPaint.measureText(exp.period)
                canvas.drawText(exp.period, rightMargin - periodWidth, yPosition, periodPaint)
                yPosition += 13f

                // Company
                val companyPaint = Paint().apply {
                    color = primaryColorInt
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText(exp.company, contentLeft, yPosition, companyPaint)
                yPosition += 13f

                // Description
                val startDescY = yPosition
                yPosition = drawStyledParagraph(exp.description, yPosition, paintNormal.apply { textSize = 9f })
                yPosition += 10f

                // Timeline connecting line
                if (index < experiences.size - 1) {
                    val linePaint = Paint().apply {
                        color = Color.rgb(209, 213, 219)
                        strokeWidth = 1.2f
                    }
                    canvas.drawLine(timelineX, startDescY - 20f, timelineX, yPosition - 6f, linePaint)
                }
            }
            yPosition += 4f
        }

        // 5. Formação Acadêmica (Timeline with secondary color markers)
        if (educations.isNotEmpty()) {
            drawSectionHeader("Formação Acadêmica")

            educations.sortedBy { it.displayOrder }.forEachIndexed { index, edu ->
                yPosition = checkPageBreak(yPosition, 45f)

                val timelineX = leftMargin + 6f
                val contentLeft = leftMargin + 20f

                // Secondary color node circle
                val eduCirclePaint = Paint().apply {
                    color = secondaryColorInt
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, yPosition - 2f, 4f, eduCirclePaint)
                val whiteCenterPaint = Paint().apply {
                    color = Color.WHITE
                    isAntiAlias = true
                }
                canvas.drawCircle(timelineX, yPosition - 2f, 1.8f, whiteCenterPaint)

                // Degree and Field
                val titleDegree = if (edu.fieldOfStudy.isNotBlank()) "${edu.degree} — ${edu.fieldOfStudy}" else edu.degree
                canvas.drawText(titleDegree, contentLeft, yPosition, paintBold.apply { textSize = 10.5f })

                val periodPaint = Paint().apply {
                    color = Color.rgb(107, 114, 128)
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                    isAntiAlias = true
                }
                val periodWidth = periodPaint.measureText(edu.period)
                canvas.drawText(edu.period, rightMargin - periodWidth, yPosition, periodPaint)
                yPosition += 13f

                // Institution
                val instPaint = Paint().apply {
                    color = primaryColorInt
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText(edu.institution, contentLeft, yPosition, instPaint)
                yPosition += 13f

                // Description
                val startDescY = yPosition
                if (edu.description.isNotBlank()) {
                    yPosition = drawStyledParagraph(edu.description, yPosition, paintNormal.apply { textSize = 9f })
                }
                yPosition += 8f

                // Connecting line
                if (index < educations.size - 1) {
                    val linePaint = Paint().apply {
                        color = Color.rgb(209, 213, 219)
                        strokeWidth = 1.2f
                    }
                    canvas.drawLine(timelineX, startDescY - 20f, timelineX, yPosition - 4f, linePaint)
                }
            }
            yPosition += 4f
        }

        // 6. Certificações e Cursos
        if (certificates.isNotEmpty()) {
            drawSectionHeader("Certificações & Cursos")
            certificates.forEach { cert ->
                yPosition = checkPageBreak(yPosition, 20f)
                val bulletPaint = Paint().apply {
                    color = primaryColorInt
                    isAntiAlias = true
                }
                canvas.drawCircle(leftMargin + 6f, yPosition - 2.5f, 2.5f, bulletPaint)

                canvas.drawText(cert.title, leftMargin + 16f, yPosition, paintBold.apply { textSize = 9.5f })
                if (cert.date.isNotBlank()) {
                    val datePaint = Paint().apply {
                        color = Color.rgb(107, 114, 128)
                        textSize = 8.5f
                        isAntiAlias = true
                    }
                    val dateWidth = datePaint.measureText(cert.date)
                    canvas.drawText(cert.date, rightMargin - dateWidth, yPosition, datePaint)
                }
                yPosition += 14f
            }
            yPosition += 6f
        }

        // 7. Footer note on last page
        val footerPaint = Paint().apply {
            color = Color.rgb(156, 163, 175)
            textSize = 8f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }
        val pageText = "Página $pageNumber"
        canvas.drawText(pageText, rightMargin - footerPaint.measureText(pageText), 815f, footerPaint)
        canvas.drawText("${profile.name} • Currículo Estilizado (Pronto para Impressão)", leftMargin, 815f, footerPaint)

        pdfDocument.finishPage(page)

        saveAndShareFile(
            context,
            pdfDocument,
            "Curriculo_Estilizado_${profile.name.replace(" ", "_")}.pdf",
            "application/pdf"
        )
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
        themeSettings: ThemeSettingsEntity,
        educations: List<EducationEntity> = emptyList()
    ) {
        val primaryColor = themeSettings.primaryColorHex
        val secondaryColor = themeSettings.secondaryColorHex
        val bgColor = themeSettings.backgroundColorHex
        val textColor = themeSettings.textColorHex

        val imageBase64 = getProfileImageBase64(context, profile.photoUrl)

        val skillsByCategory = skills.groupBy { it.category }
        val skillsHtml = StringBuilder()
        skillsByCategory.forEach { (category, skillList) ->
            if (skillList.isNotEmpty()) {
                skillsHtml.append("""
                    <div class="mb-4">
                        <h3 class="text-xs font-bold uppercase tracking-wider mb-3" style="color: $primaryColor">${category}</h3>
                        <div class="flex flex-wrap gap-2">
                """.trimIndent())
                skillList.forEach {
                    skillsHtml.append("<span class=\"px-3 py-1 bg-gray-100 border border-gray-200 rounded-lg text-sm font-medium text-gray-800\">${it.name}</span>")
                }
                skillsHtml.append("</div></div>")
            }
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

        val educationHtml = StringBuilder()
        if (educations.isNotEmpty()) {
            educations.sortedBy { it.displayOrder }.forEachIndexed { index, edu ->
                educationHtml.append("""
                    <div class="flex gap-4 mb-6">
                        <div class="flex flex-col items-center">
                            <div class="w-3 h-3 rounded-full" style="background-color: $secondaryColor"></div>
                            ${if (index < educations.size - 1) "<div class=\"w-0.5 flex-1 bg-gray-200\"></div>" else ""}
                        </div>
                        <div class="pb-2">
                            <h3 class="text-base font-bold text-gray-900">${edu.degree}</h3>
                            <p class="text-sm font-medium" style="color: $primaryColor">${edu.institution} • ${edu.period}</p>
                            ${if (edu.fieldOfStudy.isNotBlank()) "<p class=\"text-xs text-gray-500 mt-0.5 font-medium\">${edu.fieldOfStudy}</p>" else ""}
                            ${if (edu.description.isNotBlank()) "<p class=\"text-sm mt-2 text-gray-600 leading-relaxed\">${edu.description}</p>" else ""}
                        </div>
                    </div>
                """.trimIndent())
            }
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
                      <div class="flex flex-col md:flex-row items-center gap-6 text-center md:text-left">
                        ${if (!imageBase64.isNullOrBlank()) """
                          <img src="$imageBase64" class="w-24 h-24 rounded-full object-cover border-4 border-white/20 shadow-md shrink-0 mx-auto md:mx-0" alt="Foto de perfil" />
                        """ else ""}
                        <div>
                          <h1 class="text-3xl md:text-4xl font-extrabold tracking-tight">${profile.name}</h1>
                          <p class="text-lg md:text-xl font-medium mt-2 opacity-95">${profile.role}</p>
                          <div class="flex items-center justify-center md:justify-start gap-2 mt-4 text-sm opacity-80">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                            <span>${profile.location}</span>
                          </div>
                        </div>
                      </div>
                      <div class="flex flex-wrap gap-3 justify-center md:justify-start">
                        ${if (profile.linkedinUrl.isNotBlank()) """
                          <a href="${profile.linkedinUrl.trim()}" target="_blank" class="px-6 py-3 bg-white text-gray-900 font-bold rounded-xl shadow-md hover:bg-gray-50 transition flex items-center gap-2">
                            LinkedIn
                          </a>
                        """.trimIndent() else ""}
                        ${if (profile.githubUsername.isNotBlank()) """
                          <a href="${if (profile.githubUsername.trim().startsWith("http")) profile.githubUsername.trim() else "https://github.com/${profile.githubUsername.trim()}"}" target="_blank" class="px-6 py-3 bg-black/20 border border-white/30 text-white font-bold rounded-xl hover:bg-white/10 transition flex items-center gap-2">
                            GitHub
                          </a>
                        """.trimIndent() else ""}
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

                  ${if (educations.isNotEmpty()) """
                  <!-- Academic Experience / Education -->
                  <section class="bg-white border border-gray-100 p-8 rounded-[24px] shadow-sm">
                    <h2 class="text-xs font-bold uppercase tracking-wider mb-6" style="color: $primaryColor">Formação & Experiência Acadêmica</h2>
                    $educationHtml
                  </section>
                  """.trimIndent() else ""}

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

    /**
     * Exports the portfolio data as a structured JSON backup file.
     */
    fun exportToJson(
        context: Context,
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>,
        certificates: List<CertificateEntity> = emptyList(),
        educations: List<EducationEntity> = emptyList()
    ) {
        val jsonObject = org.json.JSONObject().apply {
            put("profile", org.json.JSONObject().apply {
                put("name", profile.name)
                put("role", profile.role)
                put("bio", profile.bio)
                put("email", profile.email)
                put("phone", profile.phone)
                put("location", profile.location)
                put("githubUsername", profile.githubUsername)
                put("linkedinUrl", profile.linkedinUrl)
            })
            put("skills", org.json.JSONArray().apply {
                skills.forEach { s ->
                    put(org.json.JSONObject().apply {
                        put("name", s.name)
                        put("category", s.category)
                    })
                }
            })
            put("experiences", org.json.JSONArray().apply {
                experiences.forEach { e ->
                    put(org.json.JSONObject().apply {
                        put("company", e.company)
                        put("role", e.role)
                        put("period", e.period)
                        put("description", e.description)
                    })
                }
            })
            put("educations", org.json.JSONArray().apply {
                educations.forEach { ed ->
                    put(org.json.JSONObject().apply {
                        put("institution", ed.institution)
                        put("degree", ed.degree)
                        put("fieldOfStudy", ed.fieldOfStudy)
                        put("period", ed.period)
                        put("description", ed.description)
                    })
                }
            })
            put("certificates", org.json.JSONArray().apply {
                certificates.forEach { c ->
                    put(org.json.JSONObject().apply {
                        put("title", c.title)
                        put("date", c.date)
                        put("attachmentPath", c.attachmentPath ?: "")
                    })
                }
            })
        }

        saveAndShareFile(
            context,
            jsonObject.toString(2),
            "Backup_Curriculo_${profile.name.replace(" ", "_")}.json",
            "application/json"
        )
    }

    /**
     * Exports the portfolio data as a tabular CSV file compatible with Excel / Google Sheets.
     */
    fun exportToCsv(
        context: Context,
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>,
        certificates: List<CertificateEntity> = emptyList(),
        educations: List<EducationEntity> = emptyList()
    ) {
        val csv = StringBuilder()
        csv.append("TIPO,NOME/TITULO,CATEGORIA/EMPRESA,DESCRICAO,PERIODO/LINK\n")
        csv.append("\"PERFIL\",\"${profile.name.replace("\"", "\"\"")}\",\"${profile.role.replace("\"", "\"\"")}\",\"${profile.bio.replace("\"", "\"\"")}\",\"Email: ${profile.email}, Tel: ${profile.phone}, Local: ${profile.location}\"\n")

        if (profile.linkedinUrl.isNotBlank()) {
            csv.append("\"LINK\",\"LinkedIn\",\"Redes Sociais\",\"Link de Perfil Professional\",\"${profile.linkedinUrl.trim()}\"\n")
        }
        if (profile.githubUsername.isNotBlank()) {
            val githubLink = if (profile.githubUsername.trim().startsWith("http")) profile.githubUsername.trim() else "https://github.com/${profile.githubUsername.trim()}"
            csv.append("\"LINK\",\"GitHub\",\"Redes Sociais\",\"Repositórios de Código\",\"$githubLink\"\n")
        }

        skills.forEach { s ->
            csv.append("\"HABILIDADE\",\"${s.name.replace("\"", "\"\"")}\",\"${s.category.replace("\"", "\"\"")}\",\"\",\"\"\n")
        }
        experiences.forEach { e ->
            csv.append("\"EXPERIENCIA\",\"${e.role.replace("\"", "\"\"")}\",\"${e.company.replace("\"", "\"\"")}\",\"${e.description.replace("\"", "\"\"")}\",\"${e.period.replace("\"", "\"\"")}\"\n")
        }
        educations.forEach { ed ->
            val desc = if (ed.fieldOfStudy.isNotBlank()) "${ed.fieldOfStudy} - ${ed.description}" else ed.description
            csv.append("\"EDUCACAO\",\"${ed.degree.replace("\"", "\"\"")}\",\"${ed.institution.replace("\"", "\"\"")}\",\"${desc.replace("\"", "\"\"")}\",\"${ed.period.replace("\"", "\"\"")}\"\n")
        }
        certificates.forEach { c ->
            csv.append("\"CERTIFICADO\",\"${c.title.replace("\"", "\"\"")}\",\"Certificação/Curso\",\"${c.date}\",\"${c.attachmentPath ?: ""}\"\n")
        }

        saveAndShareFile(
            context,
            csv.toString(),
            "Portfolio_${profile.name.replace(" ", "_")}.csv",
            "text/csv"
        )
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

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            }
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

            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            }
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

    private fun getProfileImageBase64(context: Context, photoUrlStr: String?): String? {
        if (photoUrlStr.isNullOrBlank()) return null
        try {
            val uri = Uri.parse(photoUrlStr)
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bytes = inputStream.readBytes()
                inputStream.close()
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                return "data:image/jpeg;base64,$base64"
            }
        } catch (e: Exception) {
            android.util.Log.e("ExportUtils", "Erro ao converter imagem de perfil para base64: ${e.localizedMessage}")
        }

        // Fallback for direct remote URLs
        if (photoUrlStr.startsWith("http://") || photoUrlStr.startsWith("https://")) {
            return photoUrlStr
        }
        return null
    }

    private fun parseColorSafe(hex: String?, fallback: Int): Int {
        if (hex.isNullOrBlank()) return fallback
        return try {
            val cleanHex = if (hex.startsWith("#")) hex else "#$hex"
            Color.parseColor(cleanHex)
        } catch (e: Exception) {
            fallback
        }
    }

    private fun getProfileBitmap(context: Context, photoUrlStr: String?): Bitmap? {
        if (photoUrlStr.isNullOrBlank()) return null
        try {
            val uri = Uri.parse(photoUrlStr)
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                return bitmap
            }
        } catch (e: Exception) {
            android.util.Log.e("ExportUtils", "Erro ao carregar bitmap de perfil: ${e.localizedMessage}")
        }
        return null
    }

    private fun drawCircularBitmap(
        canvas: Canvas,
        bitmap: Bitmap,
        x: Float,
        y: Float,
        size: Float,
        borderColor: Int = Color.WHITE
    ) {
        try {
            val output = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888)
            val bmCanvas = Canvas(output)
            val paint = Paint().apply { isAntiAlias = true }
            val rect = Rect(0, 0, size.toInt(), size.toInt())
            val rectF = RectF(rect)

            bmCanvas.drawOval(rectF, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            bmCanvas.drawBitmap(bitmap, null, rect, paint)

            canvas.drawBitmap(output, x, y, null)

            // Draw border
            val borderPaint = Paint().apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawCircle(x + size / 2f, y + size / 2f, size / 2f, borderPaint)
        } catch (e: Exception) {
            android.util.Log.e("ExportUtils", "Erro ao desenhar avatar circular: ${e.localizedMessage}")
        }
    }
}
