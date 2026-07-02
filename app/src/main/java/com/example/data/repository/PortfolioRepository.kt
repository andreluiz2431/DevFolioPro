package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.PortfolioDao
import com.example.data.local.entities.*
import com.example.data.remote.*
import com.example.data.remote.models.GithubRepo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(
    private val portfolioDao: PortfolioDao,
    private val githubApiService: GithubApiService,
    private val geminiApiService: GeminiApiService = GeminiApiService.create()
) {

    // Theme Settings
    fun getThemeSettings(): Flow<ThemeSettingsEntity?> {
        return portfolioDao.getThemeSettings()
    }

    suspend fun saveThemeSettings(settings: ThemeSettingsEntity) {
        portfolioDao.saveThemeSettings(settings)
    }

    // Profile
    fun getProfile(): Flow<ProfileEntity?> {
        return portfolioDao.getProfile()
    }

    suspend fun saveProfile(profile: ProfileEntity) {
        portfolioDao.saveProfile(profile)
    }

    // Skills
    fun getSkills(): Flow<List<SkillEntity>> {
        return portfolioDao.getSkills()
    }

    suspend fun insertSkill(skill: SkillEntity) {
        portfolioDao.insertSkill(skill)
    }

    suspend fun insertSkills(skills: List<SkillEntity>) {
        portfolioDao.insertSkills(skills)
    }

    suspend fun deleteSkill(id: Int) {
        portfolioDao.deleteSkillById(id)
    }

    suspend fun clearAllSkills() {
        portfolioDao.clearAllSkills()
    }

    // Experience
    fun getExperiences(): Flow<List<ExperienceEntity>> {
        return portfolioDao.getExperiences()
    }

    suspend fun insertExperience(experience: ExperienceEntity) {
        portfolioDao.insertExperience(experience)
    }

    suspend fun insertExperiences(experiences: List<ExperienceEntity>) {
        portfolioDao.insertExperiences(experiences)
    }

    suspend fun deleteExperience(id: Int) {
        portfolioDao.deleteExperienceById(id)
    }

    suspend fun clearAllExperiences() {
        portfolioDao.clearAllExperiences()
    }

    // Section Order
    fun getSectionOrders(): Flow<List<SectionOrderEntity>> {
        return portfolioDao.getSectionOrders()
    }

    suspend fun saveSectionOrders(orders: List<SectionOrderEntity>) {
        portfolioDao.insertSectionOrders(orders)
    }

    suspend fun saveSectionOrder(order: SectionOrderEntity) {
        portfolioDao.saveSectionOrder(order)
    }

    // GitHub Integration
    suspend fun fetchGithubRepos(username: String): List<GithubRepo> {
        return githubApiService.getUserRepos(username)
    }

    suspend fun importFromLinkedIn(rawText: String): ImportedLinkedInPortfolio? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Chave API do Gemini não configurada.")
        }

        val prompt = """
            Você é um assistente de IA especialista em recrutamento técnico de TI (Desenvolvedores e profissionais de Infraestrutura).
            Sua tarefa é ler um texto não estruturado contendo informações de perfil/currículo copiadas do LinkedIn (ou currículo em geral) e mapear essas informações estritamente para o seguinte formato JSON:

            {
              "profile": {
                "name": "Nome Completo",
                "role": "Cargo atual / Título profissional",
                "bio": "Uma descrição profissional resumida extraída do texto",
                "email": "E-mail de contato, se houver",
                "phone": "Telefone de contato, se houver",
                "location": "Localização (ex: Cidade - Estado, Brasil)"
              },
              "skills": [
                {
                  "name": "Nome da Habilidade/Tecnologia",
                  "category": "Desenvolvimento" ou "Infraestrutura"
                }
              ],
              "experiences": [
                {
                  "company": "Nome da Empresa",
                  "role": "Cargo ocupado",
                  "period": "Período (ex: Jan 2020 - Presente)",
                  "description": "Breve resumo das responsabilidades e realizações"
                }
              ]
            }

            Regras de Mapeamento:
            1. Se houver habilidades técnicas, divida-as em "Desenvolvimento" (se envolver programação, linguagens como Kotlin, Java, Python, bancos de dados, etc.) ou "Infraestrutura" (se envolver redes, servidores, DevOps, Docker, Kubernetes, AWS, suporte de TI, etc.). Use apenas uma dessas duas categorias.
            2. Garanta que o JSON resultante seja válido e siga exatamente a estrutura indicada. Não inclua nenhuma outra chave ou explicações adicionais no JSON.
            3. Responda APENAS com o JSON válido.

            Texto do perfil/currículo:
            $rawText
        """.trimIndent()

        val request = GeminiContentRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        val response = geminiApiService.generateContent(apiKey, request)
        val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: return null

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(ImportedLinkedInPortfolio::class.java)
        return adapter.fromJson(jsonText)
    }
}
