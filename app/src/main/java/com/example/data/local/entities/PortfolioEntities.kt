package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theme_settings")
data class ThemeSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val primaryColorHex: String = "#6750A4",      // Sleek M3 Purple
    val secondaryColorHex: String = "#625B71",    // Sleek M3 Secondary
    val backgroundColorHex: String = "#FEFBFF",   // Sleek Soft Off-White
    val textColorHex: String = "#1C1B1F",         // Sleek Dark Charcoal
    val useSystemTheme: Boolean = true,
    val isDarkModeForced: Boolean = false
)

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alexandre Lucas Moreira",
    val role: String = "Sênior Android Dev & Especialista em Infraestrutura de TI",
    val bio: String = "Engenheiro de Software com mais de 8 anos de experiência desenvolvendo aplicações móveis nativas de alta performance e projetando redes seguras de TI. Especialista em unir soluções de software robustas com arquitetura de infraestrutura escalável, monitoramento avançado e segurança de redes.",
    val githubUsername: String = "alm28062001", // Default github username from metadata/email prefix
    val linkedinUrl: String = "https://www.linkedin.com/in/alexandrelucas-moreira",
    val email: String = "alm28062001@gmail.com",
    val phone: String = "+55 (11) 99999-9999",
    val location: String = "São Paulo, Brasil",
    val photoUrl: String? = null
)

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String // "Desenvolvimento" or "Infraestrutura"
)

@Entity(tableName = "experience")
data class ExperienceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val company: String,
    val role: String,
    val period: String,
    val description: String,
    val displayOrder: Int
)

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val attachmentPath: String? = null
)

@Entity(tableName = "section_order")
data class SectionOrderEntity(
    @PrimaryKey val sectionId: String, // "sobre", "skills", "experiencia", "projetos", "contato"
    val displayOrder: Int,
    val title: String
)
