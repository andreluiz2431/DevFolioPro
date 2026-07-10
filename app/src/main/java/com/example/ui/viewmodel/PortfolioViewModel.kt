package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.data.remote.models.GithubRepo
import com.example.data.remote.models.ResumeImprovements
import com.example.data.remote.models.RecommendedSkillSuggestion
import com.example.data.remote.models.ImprovedExperienceSuggestion
import com.example.data.remote.models.CourseRecommendation
import com.example.data.repository.PortfolioRepository
import com.example.data.remote.FirebaseSyncManager
import com.example.data.remote.UserSession
import com.example.data.remote.PortfolioSyncData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.example.data.remote.GitHubUpdateManager
import com.example.data.remote.UpdateUiState

sealed interface GithubReposUiState {
    object Idle : GithubReposUiState
    object Loading : GithubReposUiState
    data class Success(val repos: List<GithubRepo>) : GithubReposUiState
    data class Error(val message: String) : GithubReposUiState
}

sealed interface LinkedInImportUiState {
    object Idle : LinkedInImportUiState
    object Loading : LinkedInImportUiState
    data class Success(val message: String) : LinkedInImportUiState
    data class Error(val error: String) : LinkedInImportUiState
}

sealed interface FirebaseSyncUiState {
    object Idle : FirebaseSyncUiState
    object Loading : FirebaseSyncUiState
    data class Success(val message: String) : FirebaseSyncUiState
    data class Error(val error: String) : FirebaseSyncUiState
}

enum class ConflictResolution {
    PULL_OVERWRITE, PUSH_OVERWRITE, MERGE
}

sealed interface ResumeCoachUiState {
    object Idle : ResumeCoachUiState
    object Loading : ResumeCoachUiState
    data class Success(val improvements: ResumeImprovements) : ResumeCoachUiState
    data class Error(val error: String) : ResumeCoachUiState
}

sealed interface CertificateRecommendationsUiState {
    object Idle : CertificateRecommendationsUiState
    object Loading : CertificateRecommendationsUiState
    data class Success(val recommendations: List<CourseRecommendation>) : CertificateRecommendationsUiState
    data class Error(val error: String) : CertificateRecommendationsUiState
}

class PortfolioViewModel(
    private val repository: PortfolioRepository,
    val firebaseSyncManager: FirebaseSyncManager,
    val gitHubUpdateManager: GitHubUpdateManager
) : ViewModel() {

    // App Updates UI State
    val updateUiState: StateFlow<UpdateUiState> = gitHubUpdateManager.updateState

    fun checkAppUpdates() {
        viewModelScope.launch {
            gitHubUpdateManager.checkForUpdates()
        }
    }

    fun downloadAndInstallApk(url: String) {
        viewModelScope.launch {
            gitHubUpdateManager.downloadAndInstallApk(url)
        }
    }

    fun saveGitHubConfig(owner: String, repo: String) {
        gitHubUpdateManager.saveGitHubConfig(owner, repo)
    }

    fun getGitHubOwner(): String = gitHubUpdateManager.getGitHubOwner()
    fun getGitHubRepo(): String = gitHubUpdateManager.getGitHubRepo()

    fun resetUpdateState() {
        gitHubUpdateManager.resetState()
    }

    // Theme Settings
    val themeSettings: StateFlow<ThemeSettingsEntity> = repository.getThemeSettings()
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSettingsEntity()
        )

    // Profile
    val profile: StateFlow<ProfileEntity> = repository.getProfile()
        .filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfileEntity()
        )

    // Skills
    val skills: StateFlow<List<SkillEntity>> = repository.getSkills()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Experiences
    val experiences: StateFlow<List<ExperienceEntity>> = repository.getExperiences()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Section Order
    val sectionOrders: StateFlow<List<SectionOrderEntity>> = repository.getSectionOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Certificates
    val certificates: StateFlow<List<CertificateEntity>> = repository.getCertificates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // GitHub Repos State
    private val _githubReposState = MutableStateFlow<GithubReposUiState>(GithubReposUiState.Idle)
    val githubReposState: StateFlow<GithubReposUiState> = _githubReposState.asStateFlow()

    // Firebase Sync States & Flows (Declared before init to prevent initialization order bugs)
    private val _syncState = MutableStateFlow<FirebaseSyncUiState>(FirebaseSyncUiState.Idle)
    val syncState: StateFlow<FirebaseSyncUiState> = _syncState.asStateFlow()

    private val _savedResumes = MutableStateFlow<List<String>>(listOf("Principal"))
    val savedResumes: StateFlow<List<String>> = _savedResumes.asStateFlow()

    private val _selectedResumeName = MutableStateFlow("Principal")
    val selectedResumeName: StateFlow<String> = _selectedResumeName.asStateFlow()

    // Course Suggestion Feature Purchase State
    private val _isCoursesFeatureUnlockedState = MutableStateFlow(false)
    val isCoursesFeatureUnlockedState: StateFlow<Boolean> = _isCoursesFeatureUnlockedState.asStateFlow()

    // Mercado Pago Checkout States
    private val _mercadoPagoCheckoutUrl = MutableStateFlow<String?>(null)
    val mercadoPagoCheckoutUrl: StateFlow<String?> = _mercadoPagoCheckoutUrl.asStateFlow()

    private val _mercadoPagoLoading = MutableStateFlow(false)
    val mercadoPagoLoading: StateFlow<Boolean> = _mercadoPagoLoading.asStateFlow()

    private val _mercadoPagoError = MutableStateFlow<String?>(null)
    val mercadoPagoError: StateFlow<String?> = _mercadoPagoError.asStateFlow()

    private var checkoutPollingJob: kotlinx.coroutines.Job? = null

    init {
        // Automatically check and populate database if empty (e.g., after destructive migration)
        viewModelScope.launch {
            repository.checkAndPopulateIfEmpty()
        }

        // Automatically fetch GitHub repos when the username changes and is not empty
        viewModelScope.launch {
            profile.collect { prof ->
                if (prof.githubUsername.isNotBlank()) {
                    fetchGithubRepos(prof.githubUsername)
                } else {
                    _githubReposState.value = GithubReposUiState.Idle
                }
            }
        }
        // Listen to currentUser to load saved resumes list automatically and update purchase status
        viewModelScope.launch {
            firebaseSyncManager.currentUser.collect { user ->
                if (user != null) {
                    loadSavedResumes()
                    _isCoursesFeatureUnlockedState.value = firebaseSyncManager.isCoursesFeatureUnlocked(user.uid)
                    checkLicenseStatusFromRailway()
                    checkMercadoPagoPaymentStatusDirectly()
                } else {
                    _savedResumes.value = listOf("Principal")
                    _selectedResumeName.value = "Principal"
                    _isCoursesFeatureUnlockedState.value = false
                }
            }
        }
    }

    fun fetchGithubRepos(username: String) {
        if (username.isBlank()) {
            _githubReposState.value = GithubReposUiState.Idle
            return
        }
        viewModelScope.launch {
            _githubReposState.value = GithubReposUiState.Loading
            try {
                val repos = repository.fetchGithubRepos(username)
                // Filter out forks or sort by stars
                val sortedRepos = repos.sortedByDescending { it.stargazersCount }
                _githubReposState.value = GithubReposUiState.Success(sortedRepos)
            } catch (e: Exception) {
                _githubReposState.value = GithubReposUiState.Error(
                    e.localizedMessage ?: "Erro desconhecido ao carregar repositórios."
                )
            }
        }
    }

    // Theme Operations
    fun updateThemeColors(primary: String, secondary: String, background: String, text: String) {
        viewModelScope.launch {
            val current = themeSettings.value
            repository.saveThemeSettings(
                current.copy(
                    primaryColorHex = primary,
                    secondaryColorHex = secondary,
                    backgroundColorHex = background,
                    textColorHex = text
                )
            )
        }
    }

    fun updateThemeModes(useSystem: Boolean, forceDark: Boolean) {
        viewModelScope.launch {
            val current = themeSettings.value
            repository.saveThemeSettings(
                current.copy(
                    useSystemTheme = useSystem,
                    isDarkModeForced = forceDark
                )
            )
        }
    }

    fun restoreDefaultColors() {
        viewModelScope.launch {
            val current = themeSettings.value
            repository.saveThemeSettings(
                current.copy(
                    primaryColorHex = "#6750A4",
                    secondaryColorHex = "#625B71",
                    backgroundColorHex = "#FEFBFF",
                    textColorHex = "#1C1B1F"
                )
            )
        }
    }

    // Profile Operations
    fun updateProfile(
        name: String,
        role: String,
        bio: String,
        githubUsername: String,
        linkedinUrl: String,
        email: String,
        phone: String,
        location: String
    ) {
        viewModelScope.launch {
            val current = profile.value
            repository.saveProfile(
                current.copy(
                    name = name,
                    role = role,
                    bio = bio,
                    githubUsername = githubUsername,
                    linkedinUrl = linkedinUrl,
                    email = email,
                    phone = phone,
                    location = location
                )
            )
        }
    }

    fun updateProfilePhoto(photoUrl: String?) {
        viewModelScope.launch {
            val current = profile.value
            repository.saveProfile(current.copy(photoUrl = photoUrl))
        }
    }

    // Skills Operations
    fun addSkill(name: String, category: String) {
        viewModelScope.launch {
            repository.insertSkill(SkillEntity(name = name, category = category))
        }
    }

    fun removeSkill(id: Int) {
        viewModelScope.launch {
            repository.deleteSkill(id)
        }
    }

    // Certificates Operations
    fun addCertificate(title: String, date: String, attachmentPath: String? = null) {
        viewModelScope.launch {
            repository.insertCertificate(
                CertificateEntity(
                    title = title,
                    date = date,
                    attachmentPath = attachmentPath
                )
            )
        }
    }

    fun removeCertificate(id: Int) {
        viewModelScope.launch {
            repository.deleteCertificate(id)
        }
    }

    // Experience Operations
    fun addExperience(company: String, role: String, period: String, description: String) {
        viewModelScope.launch {
            val nextOrder = (experiences.value.maxOfOrNull { it.displayOrder } ?: 0) + 1
            repository.insertExperience(
                ExperienceEntity(
                    company = company,
                    role = role,
                    period = period,
                    description = description,
                    displayOrder = nextOrder
                )
            )
        }
    }

    fun removeExperience(id: Int) {
        viewModelScope.launch {
            repository.deleteExperience(id)
        }
    }

    // Layout/Section Reordering Operations
    fun moveSectionUp(section: SectionOrderEntity) {
        viewModelScope.launch {
            val list = sectionOrders.value.toMutableList()
            val index = list.indexOfFirst { it.sectionId == section.sectionId }
            if (index > 0) {
                // Swap with the previous section
                val prev = list[index - 1]
                list[index - 1] = section.copy(displayOrder = prev.displayOrder)
                list[index] = prev.copy(displayOrder = section.displayOrder)
                repository.saveSectionOrders(list)
            }
        }
    }

    fun moveSectionDown(section: SectionOrderEntity) {
        viewModelScope.launch {
            val list = sectionOrders.value.toMutableList()
            val index = list.indexOfFirst { it.sectionId == section.sectionId }
            if (index != -1 && index < list.size - 1) {
                // Swap with the next section
                val next = list[index + 1]
                list[index + 1] = section.copy(displayOrder = next.displayOrder)
                list[index] = next.copy(displayOrder = section.displayOrder)
                repository.saveSectionOrders(list)
            }
        }
    }

    // LinkedIn Import State
    private val _linkedinImportState = MutableStateFlow<LinkedInImportUiState>(LinkedInImportUiState.Idle)
    val linkedinImportState: StateFlow<LinkedInImportUiState> = _linkedinImportState.asStateFlow()

    fun resetLinkedInImportState() {
        _linkedinImportState.value = LinkedInImportUiState.Idle
    }

    fun importLinkedInData(rawText: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            _linkedinImportState.value = LinkedInImportUiState.Loading
            try {
                val imported = repository.importFromLinkedIn(rawText)
                if (imported != null) {
                    // Update Profile if parsed
                    imported.profile?.let { ip ->
                        val current = profile.value
                        repository.saveProfile(
                            current.copy(
                                name = ip.name ?: current.name,
                                role = ip.role ?: current.role,
                                bio = ip.bio ?: current.bio,
                                email = ip.email ?: current.email,
                                phone = ip.phone ?: current.phone,
                                location = ip.location ?: current.location
                            )
                        )
                    }

                    // Handle skills
                    if (replaceExisting) {
                        repository.clearAllSkills()
                    }
                    imported.skills?.mapNotNull { skill ->
                        if (!skill.name.isNullOrBlank()) {
                            val cat = if (!skill.category.isNullOrBlank()) skill.category else "Desenvolvimento"
                            SkillEntity(name = skill.name, category = cat)
                        } else null
                    }?.let { skillsToInsert ->
                        if (skillsToInsert.isNotEmpty()) {
                            repository.insertSkills(skillsToInsert)
                        }
                    }

                    // Handle experiences
                    if (replaceExisting) {
                        repository.clearAllExperiences()
                    }
                    var order = if (replaceExisting) 1 else (experiences.value.maxOfOrNull { it.displayOrder } ?: 0) + 1
                    imported.experiences?.mapNotNull { exp ->
                        if (!exp.role.isNullOrBlank() && !exp.company.isNullOrBlank()) {
                            ExperienceEntity(
                                company = exp.company,
                                role = exp.role,
                                period = exp.period ?: "",
                                description = exp.description ?: "",
                                displayOrder = order++
                            )
                        } else null
                    }?.let { experiencesToInsert ->
                        if (experiencesToInsert.isNotEmpty()) {
                            repository.insertExperiences(experiencesToInsert)
                        }
                    }

                    _linkedinImportState.value = LinkedInImportUiState.Success("Dados importados com sucesso!")
                } else {
                    _linkedinImportState.value = LinkedInImportUiState.Error("Não foi possível extrair os dados do texto fornecido.")
                }
            } catch (e: Exception) {
                _linkedinImportState.value = LinkedInImportUiState.Error(
                    e.localizedMessage ?: "Erro ao processar importação."
                )
            }
        }
    }

    // Firebase Sync Functions
    fun resetSyncState() {
        _syncState.value = FirebaseSyncUiState.Idle
    }

    fun loadSavedResumes() {
        val user = firebaseSyncManager.currentUser.value
        if (user == null) {
            _savedResumes.value = listOf("Principal")
            return
        }
        viewModelScope.launch {
            try {
                val list = firebaseSyncManager.listSavedResumes(user.uid, user.isSimulated)
                _savedResumes.value = list
            } catch (e: Exception) {
                // Keep default list
            }
        }
    }

    fun setSelectedResumeName(name: String) {
        _selectedResumeName.value = name
    }

    fun deleteResume(resumeId: String) {
        val user = firebaseSyncManager.currentUser.value ?: return
        viewModelScope.launch {
            _syncState.value = FirebaseSyncUiState.Loading
            try {
                firebaseSyncManager.deletePortfolio(user.uid, user.isSimulated, resumeId)
                loadSavedResumes()
                if (_selectedResumeName.value == resumeId) {
                    _selectedResumeName.value = "Principal"
                }
                _syncState.value = FirebaseSyncUiState.Success("Currículo '$resumeId' excluído com sucesso!")
            } catch (e: Exception) {
                _syncState.value = FirebaseSyncUiState.Error(e.localizedMessage ?: "Erro ao excluir currículo.")
            }
        }
    }

    fun signInSimulated(email: String, name: String) {
        viewModelScope.launch {
            _syncState.value = FirebaseSyncUiState.Loading
            try {
                val session = firebaseSyncManager.signInSimulated(email, name)
                _syncState.value = FirebaseSyncUiState.Success("Logado no modo simulado como ${session.displayName}!")
                loadSavedResumes()
            } catch (e: Exception) {
                _syncState.value = FirebaseSyncUiState.Error(e.localizedMessage ?: "Erro ao fazer login simulado.")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _syncState.value = FirebaseSyncUiState.Loading
            try {
                val session = firebaseSyncManager.signInWithGoogleIdToken(idToken)
                _syncState.value = FirebaseSyncUiState.Success("Autenticado via Google como ${session.displayName}!")
                loadSavedResumes()
            } catch (e: Exception) {
                _syncState.value = FirebaseSyncUiState.Error(e.localizedMessage ?: "Erro ao autenticar com Firebase.")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                repository.clearAllSkills()
                repository.clearAllExperiences()
                repository.clearAllCertificates()
                repository.saveProfile(
                    ProfileEntity(
                        id = 1,
                        name = "",
                        role = "",
                        bio = "",
                        githubUsername = "",
                        linkedinUrl = "",
                        email = "",
                        phone = "",
                        location = "",
                        photoUrl = null
                    )
                )
            } catch (e: Exception) {
                // Ignore exceptions during DB clearing
            }
        }
        firebaseSyncManager.clearSession()
        _savedResumes.value = listOf("Principal")
        _selectedResumeName.value = "Principal"
        _syncState.value = FirebaseSyncUiState.Idle
    }

    fun syncWithCloud(resolution: ConflictResolution, resumeId: String = _selectedResumeName.value) {
        val user = firebaseSyncManager.currentUser.value ?: run {
            _syncState.value = FirebaseSyncUiState.Error("Você precisa estar logado para sincronizar.")
            return
        }
        viewModelScope.launch {
            _syncState.value = FirebaseSyncUiState.Loading
            try {
                when (resolution) {
                    ConflictResolution.PUSH_OVERWRITE -> {
                        val syncData = PortfolioSyncData(
                            profile = profile.value,
                            skills = skills.value,
                            experiences = experiences.value,
                            themeSettings = themeSettings.value,
                            sectionOrders = sectionOrders.value,
                            certificates = certificates.value
                        )
                        firebaseSyncManager.uploadPortfolio(user.uid, user.isSimulated, syncData, resumeId)
                        loadSavedResumes()
                        _selectedResumeName.value = resumeId
                        _syncState.value = FirebaseSyncUiState.Success("Dados enviados para a nuvem com sucesso no currículo '$resumeId'!")
                    }
                    ConflictResolution.PULL_OVERWRITE -> {
                        val cloudData = firebaseSyncManager.downloadPortfolio(user.uid, user.isSimulated, resumeId)
                        if (cloudData != null) {
                            applySyncData(cloudData)
                            _selectedResumeName.value = resumeId
                            _syncState.value = FirebaseSyncUiState.Success("Dados baixados do currículo '$resumeId' e aplicados localmente!")
                        } else {
                            _syncState.value = FirebaseSyncUiState.Error("Nenhum dado encontrado na nuvem para o currículo '$resumeId'.")
                        }
                    }
                    ConflictResolution.MERGE -> {
                        val cloudData = firebaseSyncManager.downloadPortfolio(user.uid, user.isSimulated, resumeId)
                        if (cloudData != null) {
                            val localData = PortfolioSyncData(
                                profile = profile.value,
                                skills = skills.value,
                                experiences = experiences.value,
                                themeSettings = themeSettings.value,
                                sectionOrders = sectionOrders.value,
                                certificates = certificates.value
                            )
                            val mergedData = mergeSyncData(local = localData, cloud = cloudData)
                            applySyncData(mergedData)
                            firebaseSyncManager.uploadPortfolio(user.uid, user.isSimulated, mergedData, resumeId)
                            loadSavedResumes()
                            _selectedResumeName.value = resumeId
                            _syncState.value = FirebaseSyncUiState.Success("Dados locais e do currículo '$resumeId' mesclados com sucesso!")
                        } else {
                            // Equivalent to push if cloud has nothing
                            val syncData = PortfolioSyncData(
                                profile = profile.value,
                                skills = skills.value,
                                experiences = experiences.value,
                                themeSettings = themeSettings.value,
                                sectionOrders = sectionOrders.value,
                                certificates = certificates.value
                            )
                            firebaseSyncManager.uploadPortfolio(user.uid, user.isSimulated, syncData, resumeId)
                            loadSavedResumes()
                            _selectedResumeName.value = resumeId
                            _syncState.value = FirebaseSyncUiState.Success("Nenhum dado encontrado na nuvem. Currículo '$resumeId' criado com sucesso!")
                        }
                    }
                }
            } catch (e: Exception) {
                _syncState.value = FirebaseSyncUiState.Error(e.localizedMessage ?: "Erro na sincronização.")
            }
        }
    }

    private suspend fun applySyncData(data: PortfolioSyncData) {
        data.profile?.let { repository.saveProfile(it) }
        
        // Apply skills (clear first, then insert)
        repository.clearAllSkills()
        data.skills?.let { repository.insertSkills(it) }
        
        // Apply experiences (clear first, then insert)
        repository.clearAllExperiences()
        data.experiences?.let { repository.insertExperiences(it) }

        // Apply certificates (clear first, then insert)
        repository.clearAllCertificates()
        data.certificates?.let { certs ->
            certs.forEach { repository.insertCertificate(it) }
        }
        
        // Apply theme if available
        data.themeSettings?.let { repository.saveThemeSettings(it) }
        
        // Apply section orders if available
        data.sectionOrders?.let { repository.saveSectionOrders(it) }
    }

    private fun mergeSyncData(local: PortfolioSyncData, cloud: PortfolioSyncData): PortfolioSyncData {
        val mergedProfile = ProfileEntity(
            id = 1,
            name = if (local.profile?.name.isNullOrBlank() || local.profile?.name == "Alexandre Lucas Moreira") cloud.profile?.name ?: local.profile?.name ?: "Alexandre Lucas Moreira" else local.profile?.name ?: "Alexandre Lucas Moreira",
            role = if (local.profile?.role.isNullOrBlank() || local.profile?.role == "Sênior Android Dev & Especialista em Infraestrutura de TI") cloud.profile?.role ?: local.profile?.role ?: "Sênior Android Dev & Especialista em Infraestrutura de TI" else local.profile?.role ?: "Sênior Android Dev & Especialista em Infraestrutura de TI",
            bio = if (local.profile?.bio.isNullOrBlank() || local.profile?.bio?.startsWith("Engenheiro de Software com mais de") == true) cloud.profile?.bio ?: local.profile?.bio ?: "" else local.profile?.bio ?: "",
            githubUsername = if (local.profile?.githubUsername.isNullOrBlank() || local.profile?.githubUsername == "alm28062001") cloud.profile?.githubUsername ?: local.profile?.githubUsername ?: "alm28062001" else local.profile?.githubUsername ?: "alm28062001",
            linkedinUrl = if (local.profile?.linkedinUrl.isNullOrBlank() || local.profile?.linkedinUrl?.contains("alexandrelucas-moreira") == true) cloud.profile?.linkedinUrl ?: local.profile?.linkedinUrl ?: "" else local.profile?.linkedinUrl ?: "",
            email = if (local.profile?.email.isNullOrBlank() || local.profile?.email == "alm28062001@gmail.com") cloud.profile?.email ?: local.profile?.email ?: "" else local.profile?.email ?: "",
            phone = if (local.profile?.phone.isNullOrBlank() || local.profile?.phone == "+55 (11) 99999-9999") cloud.profile?.phone ?: local.profile?.phone ?: "" else local.profile?.phone ?: "",
            location = if (local.profile?.location.isNullOrBlank() || local.profile?.location == "São Paulo, Brasil") cloud.profile?.location ?: local.profile?.location ?: "" else local.profile?.location ?: ""
        )

        val localSkills = local.skills ?: emptyList()
        val cloudSkills = cloud.skills ?: emptyList()
        val mergedSkills = (localSkills + cloudSkills).distinctBy { it.name.trim().lowercase() }

        val localExperiences = local.experiences ?: emptyList()
        val cloudExperiences = cloud.experiences ?: emptyList()
        val mergedExperiences = (localExperiences + cloudExperiences).distinctBy { "${it.company.trim().lowercase()}_${it.role.trim().lowercase()}" }
            .mapIndexed { index, exp -> exp.copy(displayOrder = index + 1) }

        val localCerts = local.certificates ?: emptyList()
        val cloudCerts = cloud.certificates ?: emptyList()
        val mergedCerts = (localCerts + cloudCerts).distinctBy { it.title.trim().lowercase() }

        return PortfolioSyncData(
            profile = mergedProfile,
            skills = mergedSkills,
            experiences = mergedExperiences,
            themeSettings = local.themeSettings ?: cloud.themeSettings,
            sectionOrders = local.sectionOrders ?: cloud.sectionOrders,
            certificates = mergedCerts
        )
    }

    // Resume Coach / Melhorias Screen States & Operations
    private val _resumeCoachState = MutableStateFlow<ResumeCoachUiState>(ResumeCoachUiState.Idle)
    val resumeCoachState: StateFlow<ResumeCoachUiState> = _resumeCoachState.asStateFlow()

    fun resetResumeCoachState() {
        _resumeCoachState.value = ResumeCoachUiState.Idle
    }

    fun generateResumeImprovements(targetRole: String, jobDescription: String? = null) {
        val currentProfile = profile.value
        val currentSkills = skills.value
        val currentExperiences = experiences.value
        val githubRepos = when (val s = githubReposState.value) {
            is GithubReposUiState.Success -> s.repos
            else -> emptyList()
        }

        viewModelScope.launch {
            _resumeCoachState.value = ResumeCoachUiState.Loading
            try {
                val results = repository.suggestResumeImprovements(
                    targetRole = targetRole,
                    profile = currentProfile,
                    skills = currentSkills,
                    experiences = currentExperiences,
                    repos = githubRepos,
                    jobDescription = jobDescription
                )
                if (results != null) {
                    _resumeCoachState.value = ResumeCoachUiState.Success(results)
                } else {
                    _resumeCoachState.value = ResumeCoachUiState.Error("Não foi possível gerar sugestões do servidor de IA.")
                }
            } catch (e: Exception) {
                _resumeCoachState.value = ResumeCoachUiState.Error(e.localizedMessage ?: "Erro ao gerar melhorias de currículo.")
            }
        }
    }

    fun applyProfileImprovement(role: String, bio: String) {
        viewModelScope.launch {
            val current = profile.value
            repository.saveProfile(current.copy(role = role, bio = bio))
        }
    }

    fun addRecommendedSkill(name: String, category: String) {
        viewModelScope.launch {
            val trimmedCat = category.trim()
            val normalizedCat = if (trimmedCat.isBlank()) "Desenvolvimento" else trimmedCat
            // Avoid duplicates
            val exists = skills.value.any { it.name.trim().lowercase() == name.trim().lowercase() }
            if (!exists) {
                repository.insertSkill(SkillEntity(name = name.trim(), category = normalizedCat))
            }
        }
    }

    fun applyExperienceImprovement(company: String, role: String, improvedDescription: String) {
        viewModelScope.launch {
            val localList = experiences.value
            val match = localList.find {
                it.company.trim().equals(company.trim(), ignoreCase = true) &&
                it.role.trim().equals(role.trim(), ignoreCase = true)
            }
            if (match != null) {
                repository.insertExperience(match.copy(description = improvedDescription))
            } else {
                // Try fallback matching by company
                val matchCompany = localList.find {
                    it.company.trim().equals(company.trim(), ignoreCase = true)
                }
                if (matchCompany != null) {
                    repository.insertExperience(matchCompany.copy(description = improvedDescription))
                }
            }
        }
    }

    // Certificate Recommendations State & Operation
    private val _certificateRecommendationsState = MutableStateFlow<CertificateRecommendationsUiState>(CertificateRecommendationsUiState.Idle)
    val certificateRecommendationsState: StateFlow<CertificateRecommendationsUiState> = _certificateRecommendationsState.asStateFlow()

    fun resetCertificateRecommendationsState() {
        _certificateRecommendationsState.value = CertificateRecommendationsUiState.Idle
    }

    fun generateCertificateRecommendations() {
        viewModelScope.launch {
            _certificateRecommendationsState.value = CertificateRecommendationsUiState.Loading
            try {
                val currentProfile = profile.value
                val currentSkills = skills.value
                val currentExperiences = experiences.value

                val recommendations = repository.suggestCertificates(
                    profile = currentProfile,
                    skills = currentSkills,
                    experiences = currentExperiences
                )
                _certificateRecommendationsState.value = CertificateRecommendationsUiState.Success(recommendations)
            } catch (e: Exception) {
                _certificateRecommendationsState.value = CertificateRecommendationsUiState.Error(
                    e.localizedMessage ?: "Erro ao gerar recomendações de certificados."
                )
            }
        }
    }

    fun resetMercadoPagoCheckout() {
        checkoutPollingJob?.cancel()
        checkoutPollingJob = null
        _mercadoPagoCheckoutUrl.value = null
        _mercadoPagoError.value = null
        _mercadoPagoLoading.value = false
    }

    fun unlockCoursesFeature() {
        val user = firebaseSyncManager.currentUser.value
        if (user != null) {
            firebaseSyncManager.setCoursesFeatureUnlocked(user.uid, true)
        }
        _isCoursesFeatureUnlockedState.value = true
    }

    fun checkLicenseStatusFromRailway() {
        val user = firebaseSyncManager.currentUser.value
        if (user != null) {
            viewModelScope.launch {
                try {
                    val response = com.example.data.remote.LicenseClient.api.checkLicense(user.uid)
                    if (response.unlocked) {
                        unlockCoursesFeature()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PortfolioViewModel", "Erro ao verificar licença no Railway: ${e.localizedMessage}")
                }
            }
        }
    }

    fun checkMercadoPagoPaymentStatusDirectly() {
        val user = firebaseSyncManager.currentUser.value
        if (user != null) {
            viewModelScope.launch {
                try {
                    val token = com.example.BuildConfig.MERCADO_PAGO_ACCESS_TOKEN
                    if (token.isNotBlank() && !token.contains("YOUR_MERCADO_PAGO_ACCESS_TOKEN") && !token.contains("MERCADO_PAGO_ACCESS_TOKEN")) {
                        // Check by external_reference (user.uid)
                        val responseByRef = com.example.data.remote.MercadoPagoClient.api.searchPayments(
                            authorization = "Bearer $token",
                            externalReference = user.uid,
                            payerEmail = null
                        )
                        val hasApprovedRef = responseByRef.results?.any { it.status == "approved" } == true
                        if (hasApprovedRef) {
                            unlockCoursesFeature()
                            return@launch
                        }

                        // Check by user email
                        val email = user.email
                        if (!email.isNullOrBlank()) {
                            val responseByEmail = com.example.data.remote.MercadoPagoClient.api.searchPayments(
                                authorization = "Bearer $token",
                                externalReference = null,
                                payerEmail = email
                            )
                            val hasApprovedEmail = responseByEmail.results?.any { it.status == "approved" } == true
                            if (hasApprovedEmail) {
                                unlockCoursesFeature()
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PortfolioViewModel", "Erro ao verificar pagamentos diretamente no Mercado Pago: ${e.localizedMessage}")
                }
            }
        }
    }

    fun startMercadoPagoCheckout(email: String) {
        viewModelScope.launch {
            _mercadoPagoLoading.value = true
            _mercadoPagoError.value = null
            try {
                val token = com.example.BuildConfig.MERCADO_PAGO_ACCESS_TOKEN
                if (token.isBlank() || token.contains("YOUR_MERCADO_PAGO_ACCESS_TOKEN") || token.contains("MERCADO_PAGO_ACCESS_TOKEN")) {
                    // Fallback to high-fidelity Simulator checkout
                    val simulatedUrl = "https://sandbox-mercadopago-simulator.vercel.app/checkout?email=$email&amount=19.90&title=Sugest%C3%A3o%20de%20Cursos%20IA"
                    _mercadoPagoCheckoutUrl.value = simulatedUrl
                } else {
                    val userUid = firebaseSyncManager.currentUser.value?.uid
                    val request = com.example.data.remote.PreferenceRequest(
                        items = listOf(
                            com.example.data.remote.PreferenceItem(
                                title = "Desbloqueio de Sugestão de Cursos IA",
                                quantity = 1,
                                unit_price = 19.90,
                                currency_id = "BRL"
                            )
                        ),
                        payer = com.example.data.remote.PreferencePayer(email = email),
                        back_urls = com.example.data.remote.PreferenceBackUrls(
                            success = "https://www.mercadopago.com/success",
                            pending = "https://www.mercadopago.com/pending",
                            failure = "https://www.mercadopago.com/failure"
                        ),
                        auto_return = "approved",
                        external_reference = userUid
                    )
                    
                    val response = com.example.data.remote.MercadoPagoClient.api.createPreference(
                        authorization = "Bearer $token",
                        request = request
                    )

                    val url = response.init_point ?: response.sandbox_init_point
                    if (url != null) {
                        _mercadoPagoCheckoutUrl.value = url
                    } else {
                        throw Exception("Resposta do Mercado Pago não possui URL de checkout.")
                    }
                }

                // Start polling Railway license status in background while checkout is open
                startLicensePolling()

            } catch (e: Exception) {
                _mercadoPagoError.value = "Falha ao gerar pagamento Mercado Pago: ${e.localizedMessage ?: "Tente novamente."}"
            } finally {
                _mercadoPagoLoading.value = false
            }
        }
    }

    private fun startLicensePolling() {
        checkoutPollingJob?.cancel()
        val user = firebaseSyncManager.currentUser.value ?: return
        checkoutPollingJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(4000)
                try {
                    val response = com.example.data.remote.LicenseClient.api.checkLicense(user.uid)
                    if (response.unlocked) {
                        unlockCoursesFeature()
                        resetMercadoPagoCheckout()
                        break
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PortfolioViewModel", "Erro de polling da licença: ${e.localizedMessage}")
                }
            }
        }
    }
}

class PortfolioViewModelFactory(
    private val repository: PortfolioRepository,
    private val firebaseSyncManager: FirebaseSyncManager,
    private val gitHubUpdateManager: GitHubUpdateManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            return PortfolioViewModel(repository, firebaseSyncManager, gitHubUpdateManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
