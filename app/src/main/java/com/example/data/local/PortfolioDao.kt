package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    // Theme Settings
    @Query("SELECT * FROM theme_settings WHERE id = 1")
    fun getThemeSettings(): Flow<ThemeSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveThemeSettings(settings: ThemeSettingsEntity)

    // Profile
    @Query("SELECT * FROM profile WHERE id = 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: ProfileEntity)

    // Skills
    @Query("SELECT * FROM skills")
    fun getSkills(): Flow<List<SkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Query("DELETE FROM skills WHERE id = :id")
    suspend fun deleteSkillById(id: Int)

    @Query("DELETE FROM skills")
    suspend fun clearAllSkills()

    // Experience
    @Query("SELECT * FROM experience ORDER BY displayOrder ASC")
    fun getExperiences(): Flow<List<ExperienceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(experience: ExperienceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiences(experiences: List<ExperienceEntity>)

    @Query("DELETE FROM experience WHERE id = :id")
    suspend fun deleteExperienceById(id: Int)

    @Query("DELETE FROM experience")
    suspend fun clearAllExperiences()

    // Section Order
    @Query("SELECT * FROM section_order ORDER BY displayOrder ASC")
    fun getSectionOrders(): Flow<List<SectionOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectionOrders(orders: List<SectionOrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSectionOrder(order: SectionOrderEntity)

    // Certificates
    @Query("SELECT * FROM certificates ORDER BY id DESC")
    fun getCertificates(): Flow<List<CertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(certificate: CertificateEntity)

    @Query("DELETE FROM certificates WHERE id = :id")
    suspend fun deleteCertificateById(id: Int)

    @Query("DELETE FROM certificates")
    suspend fun clearAllCertificates()
}
