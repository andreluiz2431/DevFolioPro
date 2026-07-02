package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.PortfolioDao
import com.example.data.local.entities.*
import com.example.data.remote.*
import com.example.data.remote.models.GithubRepo
import com.example.data.remote.models.ResumeImprovements
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

    suspend fun suggestResumeImprovements(
        targetRole: String,
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>,
        repos: List<GithubRepo>
    ): ResumeImprovements? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Chave API do Gemini não configurada.")
        }

        val prompt = """
            Você é um especialista em recrutamento técnico de TI (Tech Recruiter) e coach de carreira.
            Sua missão é analisar o currículo/portfólio atual do usuário e gerar sugestões personalizadas de melhoria focadas especificamente na vaga alvo: "$targetRole".

            Aqui estão as informações atuais do usuário:
            
            1. Perfil Profissional:
               - Cargo Atual: ${profile.role}
               - Bio/Resumo: ${profile.bio}
            
            2. Habilidades (Skills) Atuais:
               ${skills.joinToString(", ") { "${it.name} (${it.category})" }}
            
            3. Experiência Profissional Atual:
               ${experiences.joinToString("\n") { "- ${it.role} na ${it.company} (${it.period}): ${it.description}" }}
            
            4. Repositórios do GitHub (Projetos do Portfólio):
               ${repos.joinToString("\n") { "- ${it.name} (Linguagem: ${it.language ?: "N/A"}, Estrelas: ${it.stargazersCount}): ${it.description ?: ""}" }}

            Seu objetivo é gerar um JSON com sugestões de otimização de acordo com as seguintes regras:
            
            1. `improvedProfile`: Reescreva o título profissional (`role`) e a biografia (`bio`) de modo a destacar palavras-chave e competências diretamente ligadas ao cargo alvo "$targetRole", mantendo a veracidade das experiências dele. A bio reescrita deve ser profissional, chamativa e de alto impacto (aproximadamente 3-4 linhas).
            
            2. `recommendedSkills`: Sugira de 3 a 5 tecnologias ou metodologias altamente recomendadas para esta vaga alvo "$targetRole" que o usuário ainda não possui na lista dele ou que seriam excelentes complementos. Indique para cada uma o nome, a categoria ("Desenvolvimento" ou "Infraestrutura") e uma breve justificativa de 1 frase explicando por que ela é valorizada para essa vaga.
            
            3. `improvedExperiences`: Para cada uma das experiências do usuário, reescreva a descrição para que fique focada em realizações e conquistas técnicas, usando termos relevantes para a vaga de "$targetRole". Mantenha exatamente o mesmo nome da empresa (`company`), cargo (`role`) e período (`period`) correspondentes às originais para permitir a associação.
            
            Retorne estritamente o seguinte formato JSON, sem marcações markdown ou blocos adicionais (responda APENAS o JSON puro):

            {
              "improvedProfile": {
                "role": "Título reescrito para o cargo alvo",
                "bio": "Bio profissional de alto impacto e focada no cargo alvo"
              },
              "recommendedSkills": [
                {
                  "name": "Nome da Habilidade sugerida",
                  "category": "Desenvolvimento",
                  "justification": "Por que esta habilidade é importante para a vaga alvo"
                }
              ],
              "improvedExperiences": [
                {
                  "company": "Nome da Empresa exatamente igual",
                  "role": "Cargo exatamente igual",
                  "period": "Período exatamente igual",
                  "improvedDescription": "Descrição otimizada com foco em realizações, tecnologias relevantes para o cargo alvo, metodologias ágeis, etc."
                }
              ]
            }

            Importante:
            - Responda apenas com o JSON válido.
            - Seja extremamente realista e encorajador.
            - Não invente empresas ou empregos falsos que o usuário não realizou. Apenas reescreva e otimize os dados existentes.
        """.trimIndent()

        val request = GeminiContentRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.3f
            )
        )

        val response = geminiApiService.generateContent(apiKey, request)
        val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: return null

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(ResumeImprovements::class.java)
        return adapter.fromJson(jsonText)
    }
}
