package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.PortfolioDao
import com.example.data.local.entities.*
import com.example.data.remote.*
import com.example.data.remote.models.GithubRepo
import com.example.data.remote.models.ResumeImprovements
import com.example.data.remote.models.CourseRecommendation
import com.example.data.remote.models.CourseRecommendationsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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

    // Certificates
    fun getCertificates(): Flow<List<CertificateEntity>> {
        return portfolioDao.getCertificates()
    }

    suspend fun insertCertificate(certificate: CertificateEntity) {
        portfolioDao.insertCertificate(certificate)
    }

    suspend fun deleteCertificate(id: Int) {
        portfolioDao.deleteCertificateById(id)
    }

    suspend fun clearAllCertificates() {
        portfolioDao.clearAllCertificates()
    }

    suspend fun checkAndPopulateIfEmpty() {
        try {
            val sections = portfolioDao.getSectionOrders().first()
            if (sections.isEmpty()) {
                // Preload Theme Settings
                portfolioDao.saveThemeSettings(ThemeSettingsEntity())

                // Preload Profile
                portfolioDao.saveProfile(ProfileEntity())

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
                portfolioDao.insertSkills(defaultSkills)

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
                portfolioDao.insertExperiences(defaultExperiences)

                // Preload Section Orders
                val defaultSections = listOf(
                    SectionOrderEntity("sobre", 1, "Sobre Mim"),
                    SectionOrderEntity("skills", 2, "Habilidades Técnicas"),
                    SectionOrderEntity("experiencia", 3, "Experiência Profissional"),
                    SectionOrderEntity("certificados", 4, "Certificados & Conquistas"),
                    SectionOrderEntity("projetos", 5, "Projetos GitHub"),
                    SectionOrderEntity("contato", 6, "Contato")
                )
                portfolioDao.insertSectionOrders(defaultSections)

                // Preload Default Certificates
                portfolioDao.insertCertificate(
                    CertificateEntity(
                        title = "Certificação Fortinet NSE 4 - Network Security Professional",
                        date = "Março de 2025"
                    )
                )
                portfolioDao.insertCertificate(
                    CertificateEntity(
                        title = "AWS Certified Solutions Architect - Associate",
                        date = "Janeiro de 2025"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        repos: List<GithubRepo>,
        jobDescription: String? = null
    ): ResumeImprovements? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Chave API do Gemini não configurada.")
        }

        val jobDescSection = if (!jobDescription.isNullOrBlank()) {
            "\n            5. Descrição Detalhada da Vaga Desejada (Oportunidade Alvo):\n            $jobDescription\n"
        } else {
            ""
        }

        val prompt = """
            Você é um especialista em recrutamento técnico de TI (Tech Recruiter) e coach de carreira de alta performance.
            Sua missão é analisar o currículo/portfólio atual do usuário e gerar sugestões personalizadas de melhoria focadas especificamente na vaga alvo: "$targetRole"${if (!jobDescription.isNullOrBlank()) " e nos requisitos e responsabilidades descritos para a vaga." else "."}

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
            $jobDescSection

            Seu objetivo é gerar um JSON com sugestões de otimização de acordo com as seguintes regras:
            
            1. `improvedProfile`: Reescreva o título profissional (`role`) e a biografia (`bio`) de modo a destacar palavras-chave e competências diretamente ligadas ao cargo alvo "$targetRole"${if (!jobDescription.isNullOrBlank()) " e à descrição da vaga" else ""}, mantendo a veracidade das experiências dele. A bio reescrita deve ser profissional, chamativa e de alto impacto (aproximadamente 3-4 linhas).
               - ATENÇÃO CRÍTICA (NÃO MINTA): Não altere o nível hierárquico real ou cargo do usuário para mentir sobre o seu nível atual. Por exemplo, se ele é "Analista 1" ou "Desenvolvedor Júnior" e a vaga alvo é "Analista 3" ou "Desenvolvedor Sênior", você NÃO DEVE mentir mudando o título dele para "Analista 3" ou "Sênior". Em vez disso, reescreva o título ou bio de forma a ressaltar que ele atua/tem competência com foco nas tecnologias e práticas buscadas (por exemplo, "Analista de Sistemas focado em [Tecnologia]") sem atribuir-lhe falsamente uma senioridade superior.
            
            2. `recommendedSkills`: Sugira de 3 a 5 tecnologias ou metodologias altamente recomendadas para esta vaga alvo "$targetRole"${if (!jobDescription.isNullOrBlank()) " e descrição de vaga" else ""} que o usuário ainda não possui na lista dele ou que seriam excelentes complementos. Indique para cada uma o nome, a categoria ("Desenvolvimento" ou "Infraestrutura") e uma breve justificativa de 1 frase explicando por que ela é valorizada para essa vaga.
            
            3. `improvedExperiences`: Para cada uma das experiências do usuário, reescreva a descrição para que fique focada em realizações e conquistas técnicas, usando termos relevantes para a vaga de "$targetRole"${if (!jobDescription.isNullOrBlank()) " e a descrição da vaga fornecida" else ""}. Mantenha exatamente o mesmo nome da empresa (`company`), cargo (`role`) e período (`period`) correspondentes às originais para permitir a associação.
               - ATENÇÃO CRÍTICA (NÃO MINTA): Nunca altere os cargos reais ou minta sobre as responsabilidades de experiências passadas. Dê forte ênfase nas atividades que o usuário já exerce ou exerceu que se comparam, assemelham ou são diretamente relevantes para a vaga de interesse, demonstrando maturidade técnica nas atividades reais exercidas.
            
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
            - Respeite rigorosamente a veracidade: NUNCA minta sobre cargos ou níveis que o usuário não possui.
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

    suspend fun suggestCertificates(
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>
    ): List<CourseRecommendation> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return getLocalCourseRecommendations(profile, skills, experiences)
        }

        val prompt = """
            Você é um especialista em desenvolvimento de carreira de TI e arquiteto de soluções educacionais.
            Sua tarefa é analisar o perfil do usuário, suas habilidades e suas experiências de trabalho, e recomendar de 3 a 5 cursos ou certificados técnicos altamente valiosos que impulsionariam a sua carreira atual ou ajudariam a preencher lacunas de habilidades.

            Aqui estão os dados atuais do usuário:
            1. Cargo/Bio: ${profile.role ?: "N/A"} - ${profile.bio ?: "N/A"}
            2. Habilidades: ${skills.joinToString(", ") { "${it.name} (${it.category})" }}
            3. Experiência Profissional:
               ${experiences.joinToString("\n") { "- ${it.role} na ${it.company}: ${it.description}" }}

            Para cada recomendação, forneça de forma muito realista e precisa:
            - Nome do curso/certificado (Ex: "Certificação Fortinet NSE 4 - Network Security Professional" ou "Google Associate Android Developer")
            - Tempo estimado para conclusão (Ex: "40 horas" ou "3 meses")
            - Custo estimado (Ex: "Grátis", "R$ 400" ou "US$ 150")
            - Uma breve descrição da serventia do curso/certificado para o mercado de trabalho e o que ele agrega.
            - Área de atuação principal (Ex: "Segurança de Redes", "Desenvolvimento Mobile", "DevOps")

            Retorne estritamente o seguinte formato JSON, sem marcações markdown ou blocos adicionais (responda APENAS o JSON puro):
            {
              "recommendations": [
                {
                  "title": "Nome do curso/certificado",
                  "estimatedTime": "Tempo estimado",
                  "estimatedCost": "Custo estimado",
                  "description": "Breve descrição sobre a serventia do curso e o que ele agrega",
                  "careerField": "Área de atuação"
                }
              ]
            }
        """.trimIndent()

        val request = GeminiContentRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.4f
            )
        )

        return try {
            val response = geminiApiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return getLocalCourseRecommendations(profile, skills, experiences)

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(CourseRecommendationsResponse::class.java)
            adapter.fromJson(jsonText)?.recommendations ?: getLocalCourseRecommendations(profile, skills, experiences)
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalCourseRecommendations(profile, skills, experiences)
        }
    }

    fun getLocalCourseRecommendations(
        profile: ProfileEntity,
        skills: List<SkillEntity>,
        experiences: List<ExperienceEntity>
    ): List<CourseRecommendation> {
        val list = mutableListOf<CourseRecommendation>()
        
        val skillsStr = skills.joinToString(" ") { it.name }.lowercase()
        val expStr = experiences.joinToString(" ") { "${it.role} ${it.description}" }.lowercase()
        val bioStr = (profile.role ?: "").lowercase() + " " + (profile.bio ?: "").lowercase()
        
        val fullContext = "$skillsStr $expStr $bioStr"

        // Infrastructure / NOC / Cloud check
        val isInfra = fullContext.contains("infra") || 
                      fullContext.contains("rede") || 
                      fullContext.contains("cisco") || 
                      fullContext.contains("fortinet") || 
                      fullContext.contains("mikrotik") || 
                      fullContext.contains("zabbix") || 
                      fullContext.contains("cloud") || 
                      fullContext.contains("aws") || 
                      fullContext.contains("docker") || 
                      fullContext.contains("kubernetes")

        // Mobile / Android check
        val isMobile = fullContext.contains("android") || 
                       fullContext.contains("kotlin") || 
                       fullContext.contains("compose") || 
                       fullContext.contains("mobile") || 
                       fullContext.contains("ios") || 
                       fullContext.contains("swift")

        // Front-end / Full-stack check
        val isWeb = fullContext.contains("react") || 
                    fullContext.contains("node") || 
                    fullContext.contains("typescript") || 
                    fullContext.contains("javascript") || 
                    fullContext.contains("full-stack") || 
                    fullContext.contains("python")

        if (isInfra) {
            list.add(
                CourseRecommendation(
                    title = "Certificação Fortinet NSE 4 - Network Security Professional",
                    estimatedTime = "40 horas",
                    estimatedCost = "US$ 150",
                    description = "Capacitação avançada em segurança de rede corporativa e administração de firewalls FortiGate de última geração. Altamente valorizado no mercado de infraestrutura de TI corporativa.",
                    careerField = "Segurança de Redes & Engenharia de Segurança"
                )
            )
            list.add(
                CourseRecommendation(
                    title = "AWS Certified Solutions Architect - Associate",
                    estimatedTime = "80 horas",
                    estimatedCost = "US$ 150",
                    description = "Valida o conhecimento técnico em projetar e implantar sistemas seguros, escaláveis e resilientes na infraestrutura em nuvem AWS. Essencial para profissionais de Cloud/DevOps.",
                    careerField = "Cloud Computing & Engenharia DevOps"
                )
            )
            list.add(
                CourseRecommendation(
                    title = "Cisco CCNA (200-301) - Certified Network Associate",
                    estimatedTime = "3 a 6 meses",
                    estimatedCost = "US$ 300",
                    description = "Fundamento indispensável em redes de computadores, cobrindo roteamento, comutação, segurança IP e automação de redes Cisco.",
                    careerField = "Administração de Redes & Infraestrutura"
                )
            )
        }

        if (isMobile) {
            list.add(
                CourseRecommendation(
                    title = "Google Associate Android Developer Certification",
                    estimatedTime = "2 a 3 meses",
                    estimatedCost = "US$ 149",
                    description = "Exame oficial de certificação do Google que valida a competência no desenvolvimento profissional de aplicativos Android usando Kotlin e arquitetura moderna do Android Jetpack.",
                    careerField = "Desenvolvimento de Software Mobile (Android)"
                )
            )
        }

        if (isWeb) {
            list.add(
                CourseRecommendation(
                    title = "Desenvolvedor de Software Full-Stack (React & Node.js)",
                    estimatedTime = "60 horas",
                    estimatedCost = "Grátis (Recursos Open Source / YouTube)",
                    description = "Curso intensivo focado em construir microsserviços modernos com Node.js, TypeScript e interfaces interativas e responsivas com React.",
                    careerField = "Engenharia de Software Web Full-Stack"
                )
            )
        }

        // Add standard premium generic recommendations if we need to reach at least 3-4 recommendations
        if (list.size < 3) {
            list.add(
                CourseRecommendation(
                    title = "Orquestração de Microsserviços com Docker & Kubernetes",
                    estimatedTime = "30 horas",
                    estimatedCost = "R$ 49,90",
                    description = "Curso abrangente sobre conteinerização de software, gerenciamento de clusters, escalabilidade horizontal e automação de esteiras de CI/CD.",
                    careerField = "Engenharia de Plataforma & DevOps"
                )
            )
            list.add(
                CourseRecommendation(
                    title = "Scrum Product Owner (PSPO I) ou Scrum Master (PSM I)",
                    estimatedTime = "16 horas",
                    estimatedCost = "US$ 150",
                    description = "Certificação profissional ágil da Scrum.org altamente reconhecida mundialmente para certificar a liderança de escopo, desenvolvimento de produtos de software e agilidade.",
                    careerField = "Gestão de Projetos & Liderança Ágil"
                )
            )
        }

        return list
    }
}

