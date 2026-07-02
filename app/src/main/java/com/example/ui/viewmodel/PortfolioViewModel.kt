package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.data.remote.models.GithubRepo
import com.example.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

class PortfolioViewModel(
    private val repository: PortfolioRepository
) : ViewModel() {

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

    // GitHub Repos State
    private val _githubReposState = MutableStateFlow<GithubReposUiState>(GithubReposUiState.Idle)
    val githubReposState: StateFlow<GithubReposUiState> = _githubReposState.asStateFlow()

    init {
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
                            val cat = if (skill.category == "Infraestrutura") "Infraestrutura" else "Desenvolvimento"
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
}

class PortfolioViewModelFactory(
    private val repository: PortfolioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            return PortfolioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
