package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ThemeSettingsEntity::class,
        ProfileEntity::class,
        SkillEntity::class,
        ExperienceEntity::class,
        SectionOrderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "portfolio_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.portfolioDao())
                }
            }
        }

        private suspend fun populateDatabase(dao: PortfolioDao) {
            // Preload Theme Settings
            dao.saveThemeSettings(ThemeSettingsEntity())

            // Preload Profile
            dao.saveProfile(ProfileEntity())

            // Preload Skills
            val defaultSkills = listOf(
                SkillEntity(name = "Kotlin", category = "Desenvolvimento"),
                SkillEntity(name = "Jetpack Compose", category = "Desenvolvimento"),
                SkillEntity(name = "React", category = "Desenvolvimento"),
                SkillEntity(name = "Node.js", category = "Desenvolvimento"),
                SkillEntity(name = "Python", category = "Desenvolvimento"),
                SkillEntity(name = "TypeScript", category = "Desenvolvimento"),
                SkillEntity(name = "Fortinet", category = "Infraestrutura"),
                SkillEntity(name = "Zabbix", category = "Infraestrutura"),
                SkillEntity(name = "MikroTik", category = "Infraestrutura"),
                SkillEntity(name = "Cisco Routing & Switching", category = "Infraestrutura"),
                SkillEntity(name = "Docker & Kubernetes", category = "Infraestrutura"),
                SkillEntity(name = "AWS Cloud", category = "Infraestrutura")
            )
            dao.insertSkills(defaultSkills)

            // Preload Experience Timeline
            val defaultExperiences = listOf(
                ExperienceEntity(
                    company = "TechVanguard Solutions",
                    role = "Arquiteto Android Sênior & Engenheiro Cloud",
                    period = "2023 - Presente",
                    description = "Liderança técnica no desenvolvimento de aplicativos móveis escaláveis usando Jetpack Compose e Kotlin Multiplatform. Configuração e sustentação de ambientes AWS integrados a firewalls de segurança Fortinet FortiGate, monitoramento avançado com Zabbix.",
                    displayOrder = 1
                ),
                ExperienceEntity(
                    company = "CyberShield Corp",
                    role = "Desenvolvedor Full-Stack & Administrador de Redes",
                    period = "2020 - 2023",
                    description = "Criação de microsserviços seguros com Node.js/Python e interfaces ricas em React. Gestão de rede de alta disponibilidade corporativa com switches Cisco empilhados, roteadores MikroTik CCR e VPNs IPSec seguras.",
                    displayOrder = 2
                ),
                ExperienceEntity(
                    company = "Global Network Solutions",
                    role = "Analista de Suporte de Infraestrutura de TI & NOC",
                    period = "2018 - 2020",
                    description = "Monitoramento proativo em tempo real de servidores on-premise e cloud usando Zabbix, Prometheus e Grafana. Virtualização e gerenciamento de storage de dados via VMware ESXi, suporte técnico avançado L3.",
                    displayOrder = 3
                )
            )
            dao.insertExperiences(defaultExperiences)

            // Preload Section Orders
            val defaultSections = listOf(
                SectionOrderEntity("sobre", 1, "Sobre Mim"),
                SectionOrderEntity("skills", 2, "Habilidades Técnicas"),
                SectionOrderEntity("experiencia", 3, "Experiência Profissional"),
                SectionOrderEntity("projetos", 4, "Projetos GitHub"),
                SectionOrderEntity("contato", 5, "Contato")
            )
            dao.insertSectionOrders(defaultSections)
        }
    }
}
