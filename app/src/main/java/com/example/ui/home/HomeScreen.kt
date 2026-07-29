package com.example.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.CertificateEntity
import com.example.data.local.entities.ExperienceEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.SectionOrderEntity
import com.example.data.local.entities.SkillEntity
import com.example.data.remote.models.GithubRepo
import com.example.ui.theme.toColor
import com.example.ui.viewmodel.GithubReposUiState
import com.example.ui.viewmodel.LinkedInImportUiState
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.utils.ExportUtils

@Composable
fun HomeScreen(
    viewModel: PortfolioViewModel,
    modifier: Modifier = Modifier,
    onNavigateToEdit: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val experiences by viewModel.experiences.collectAsState()
    val certificates by viewModel.certificates.collectAsState()
    val sections by viewModel.sectionOrders.collectAsState()
    val githubReposState by viewModel.githubReposState.collectAsState()
    val themeSettings by viewModel.themeSettings.collectAsState()
    val linkedinImportState by viewModel.linkedinImportState.collectAsState()

    val primaryColor = themeSettings.primaryColorHex.toColor()
    val secondaryColor = themeSettings.secondaryColorHex.toColor()

    var isEditMode by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isFabExpanded by remember { mutableStateOf(false) }

    // Section Dialog States for Inline Editing
    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showAddSkillDialog by remember { mutableStateOf(false) }
    var showAddExperienceDialog by remember { mutableStateOf(false) }
    var showAddCertificateDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_box")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_scroll"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Edit Mode Indicator Banner
            if (isEditMode) {
                item(key = "edit_mode_banner") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f)),
                        border = BorderStroke(1.5.dp, primaryColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_mode_active_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Modo de Edição Ativo",
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Toque nas setas ▲ ▼ de cada seção para reordenar os tópicos ou toque nos botões para editar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                viewModel.cancelEditModeAndRevert {
                                    Toast.makeText(context, "Alterações descartadas.", Toast.LENGTH_SHORT).show()
                                }
                                isEditMode = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Sair do modo de edição",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Hero Header
            item {
                HeroHeaderCard(
                    profile = profile,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    onNavigateToEdit = {
                        viewModel.startEditModeSnapshot()
                        isEditMode = true
                    },
                    onOpenExport = { showExportDialog = true },
                    onOpenImport = { showImportDialog = true }
                )
            }

            // Render dynamic sections according to database order
            val sortedSections = sections.sortedBy { it.displayOrder }
            sortedSections.forEachIndexed { index, section ->
                val isFirst = index == 0
                val isLast = index == sortedSections.size - 1

                when (section.sectionId) {
                    "sobre" -> {
                        item(key = "sobre") {
                            SectionWrapper(
                                title = section.title,
                                icon = Icons.Default.Person,
                                primaryColor = primaryColor,
                                isEditMode = isEditMode,
                                isFirst = isFirst,
                                isLast = isLast,
                                onMoveUp = { viewModel.moveSectionUp(section) },
                                onMoveDown = { viewModel.moveSectionDown(section) }
                            ) {
                                AboutSection(
                                    profile = profile,
                                    isEditMode = isEditMode,
                                    onEditProfile = { showProfileEditDialog = true }
                                )
                            }
                        }
                    }
                    "skills" -> {
                        item(key = "skills") {
                            SectionWrapper(
                                title = section.title,
                                icon = Icons.Default.Terminal,
                                primaryColor = primaryColor,
                                isEditMode = isEditMode,
                                isFirst = isFirst,
                                isLast = isLast,
                                onMoveUp = { viewModel.moveSectionUp(section) },
                                onMoveDown = { viewModel.moveSectionDown(section) }
                            ) {
                                SkillsSection(
                                    skills = skills,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    isEditMode = isEditMode,
                                    onAddSkill = { showAddSkillDialog = true },
                                    onRemoveSkill = { id -> viewModel.removeSkill(id) }
                                )
                            }
                        }
                    }
                    "experiencia" -> {
                        item(key = "experiencia") {
                            SectionWrapper(
                                title = section.title,
                                icon = Icons.Default.WorkHistory,
                                primaryColor = primaryColor,
                                isEditMode = isEditMode,
                                isFirst = isFirst,
                                isLast = isLast,
                                onMoveUp = { viewModel.moveSectionUp(section) },
                                onMoveDown = { viewModel.moveSectionDown(section) }
                            ) {
                                ExperienceTimeline(
                                    experiences = experiences,
                                    primaryColor = primaryColor,
                                    isEditMode = isEditMode,
                                    onAddExperience = { showAddExperienceDialog = true },
                                    onRemoveExperience = { id -> viewModel.removeExperience(id) }
                                )
                            }
                        }
                    }
                    "certificados" -> {
                        item(key = "certificados") {
                            SectionWrapper(
                                title = section.title,
                                icon = Icons.Default.WorkspacePremium,
                                primaryColor = primaryColor,
                                isEditMode = isEditMode,
                                isFirst = isFirst,
                                isLast = isLast,
                                onMoveUp = { viewModel.moveSectionUp(section) },
                                onMoveDown = { viewModel.moveSectionDown(section) }
                            ) {
                                CertificatesSection(
                                    certificates = certificates,
                                    primaryColor = primaryColor,
                                    isEditMode = isEditMode,
                                    onAddCertificate = { showAddCertificateDialog = true },
                                    onRemoveCertificate = { id -> viewModel.removeCertificate(id) }
                                )
                            }
                        }
                    }
                    "projetos" -> {
                        item(key = "projetos") {
                            SectionWrapper(
                                title = section.title,
                                icon = Icons.Default.Source,
                                primaryColor = primaryColor,
                                isEditMode = isEditMode,
                                isFirst = isFirst,
                                isLast = isLast,
                                onMoveUp = { viewModel.moveSectionUp(section) },
                                onMoveDown = { viewModel.moveSectionDown(section) }
                            ) {
                                GithubReposSection(
                                    state = githubReposState,
                                    primaryColor = primaryColor,
                                    isEditMode = isEditMode,
                                    onRefresh = { viewModel.fetchGithubRepos(profile.githubUsername) },
                                    onEditProfile = { showProfileEditDialog = true }
                                )
                            }
                        }
                    }
                    "contato" -> {
                        item(key = "contato") {
                            SectionWrapper(
                                title = section.title,
                                icon = Icons.Default.ContactMail,
                                primaryColor = primaryColor,
                                isEditMode = isEditMode,
                                isFirst = isFirst,
                                isLast = isLast,
                                onMoveUp = { viewModel.moveSectionUp(section) },
                                onMoveDown = { viewModel.moveSectionDown(section) }
                            ) {
                                ContactSection(
                                    profile = profile,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    isEditMode = isEditMode,
                                    onEditProfile = { showProfileEditDialog = true }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Sticky Edit Action Control Bar when in Edit Mode
        if (isEditMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("inline_edit_bottom_bar"),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.cancelEditModeAndRevert {
                                Toast.makeText(context, "Alterações descartadas.", Toast.LENGTH_SHORT).show()
                            }
                            isEditMode = false
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancelar")
                    }

                    Button(
                        onClick = { showSaveConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirmar & Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Floating Action Bar / SpeedDial Overlay at bottom right
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Action 1: Editar Informações
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                viewModel.startEditModeSnapshot()
                                isEditMode = true
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = primaryColor,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp),
                            modifier = Modifier.testTag("fab_edit_data")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ativar Modo Edição", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Action 2: Exportar
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                showExportDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = primaryColor,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp),
                            modifier = Modifier.testTag("fab_export_data")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Exportar Currículo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Action 3: Importar Dados (PDF / Texto)
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                showImportDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = primaryColor,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp),
                            modifier = Modifier.testTag("fab_import_data")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importar Dados (PDF/Texto)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                // Main FAB toggle
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_toggle_quick_actions")
                ) {
                    Icon(
                        imageVector = if (isFabExpanded) Icons.Default.Close else Icons.Default.AutoAwesome,
                        contentDescription = if (isFabExpanded) "Fechar menu rápido" else "Ações rápidas do portfólio"
                    )
                }
            }
        }
    }

    // Modal Dialogs for Inline Editing
    if (showProfileEditDialog) {
        InlineEditProfileDialog(
            profile = profile,
            onDismiss = { showProfileEditDialog = false },
            onSave = { name, role, bio, email, phone, location, github, linkedin ->
                viewModel.updateProfile(name, role, bio, github, linkedin, email, phone, location)
            },
            primaryColor = primaryColor
        )
    }

    if (showAddSkillDialog) {
        InlineAddSkillDialog(
            existingSkills = skills,
            onDismiss = { showAddSkillDialog = false },
            onSave = { name, category ->
                viewModel.addSkill(name, category)
            },
            primaryColor = primaryColor
        )
    }

    if (showAddExperienceDialog) {
        InlineAddExperienceDialog(
            onDismiss = { showAddExperienceDialog = false },
            onSave = { company, role, period, description ->
                viewModel.addExperience(company, role, period, description)
            },
            primaryColor = primaryColor
        )
    }

    if (showAddCertificateDialog) {
        InlineAddCertificateDialog(
            onDismiss = { showAddCertificateDialog = false },
            onSave = { title, date ->
                viewModel.addCertificate(title, date)
            },
            primaryColor = primaryColor
        )
    }

    if (showSaveConfirmDialog) {
        SaveCurriculumConfirmDialog(
            onDismiss = { showSaveConfirmDialog = false },
            viewModel = viewModel,
            primaryColor = primaryColor,
            onConfirmed = {
                showSaveConfirmDialog = false
                isEditMode = false
            }
        )
    }

    // Export Options Dialog
    if (showExportDialog) {
        ExportOptionsDialog(
            onDismiss = { showExportDialog = false },
            profile = profile,
            skills = skills,
            experiences = experiences,
            certificates = certificates,
            themeSettings = themeSettings,
            primaryColor = primaryColor
        )
    }

    // Import Options Dialog (PDF + Text)
    if (showImportDialog) {
        ImportOptionsDialog(
            onDismiss = {
                showImportDialog = false
                viewModel.resetLinkedInImportState()
            },
            viewModel = viewModel,
            importState = linkedinImportState,
            primaryColor = primaryColor
        )
    }
}

@Composable
fun SectionWrapper(
    title: String,
    icon: ImageVector,
    primaryColor: Color,
    isEditMode: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            1.dp,
            if (isEditMode) primaryColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isEditMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = !isFirst,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Mover seção para cima",
                                tint = if (!isFirst) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(
                            onClick = onMoveDown,
                            enabled = !isLast,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Mover seção para baixo",
                                tint = if (!isLast) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun HeroHeaderCard(
    profile: ProfileEntity,
    primaryColor: Color,
    secondaryColor: Color,
    onNavigateToEdit: () -> Unit = {},
    onOpenExport: () -> Unit = {},
    onOpenImport: () -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_header_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Avatar/Icon Placeholder
                val initials = profile.name.split(" ")
                    .filter { it.trim().isNotEmpty() }
                    .map { it.first().uppercase() }
                    .take(2)
                    .joinToString("")
                    .ifEmpty { "AS" }

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profile.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.testTag("profile_name")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = profile.role,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.testTag("profile_role")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Localização",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = profile.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LinkedIn & Email Quick Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val uri = Uri.parse(profile.linkedinUrl)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Não foi possível abrir o LinkedIn", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("linkedin_button")
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LinkedIn", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${profile.email}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Nenhum aplicativo de email encontrado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("E-mail")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Portfolio Control Actions Row (Edit, Export, Import)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onNavigateToEdit,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("hero_edit_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Currículo", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Editar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        onClick = onOpenExport,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("hero_export_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.IosShare, contentDescription = "Exportar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        onClick = onOpenImport,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White,
                        modifier = Modifier.testTag("hero_import_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = "Importar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Importar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSection(
    profile: ProfileEntity,
    isEditMode: Boolean = false,
    onEditProfile: () -> Unit = {}
) {
    Column {
        Text(
            text = profile.bio.ifBlank { "Toque em editar para adicionar um resumo profissional." },
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isEditMode) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onEditProfile,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_bio_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Editar Perfil & Biografia", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SkillsSection(
    skills: List<SkillEntity>,
    primaryColor: Color,
    secondaryColor: Color,
    isEditMode: Boolean = false,
    onAddSkill: () -> Unit = {},
    onRemoveSkill: (Int) -> Unit = {}
) {
    val skillsByCategory = remember(skills) {
        skills.groupBy { it.category }
    }
    val categories = remember(skillsByCategory) {
        skillsByCategory.keys.toList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (isEditMode) {
            OutlinedButton(
                onClick = onAddSkill,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_skill_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adicionar Nova Skill", fontWeight = FontWeight.Bold)
            }
        }

        skillsByCategory.forEach { (category, categorySkills) ->
            if (categorySkills.isNotEmpty()) {
                val catIndex = categories.indexOf(category)
                val badgeColor = when (catIndex % 3) {
                    0 -> primaryColor
                    1 -> secondaryColor
                    else -> MaterialTheme.colorScheme.tertiary
                }
                Column {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorySkills.forEach { skill ->
                            SkillBadge(
                                skillName = skill.name,
                                badgeColor = badgeColor.copy(alpha = 0.12f),
                                textColor = badgeColor,
                                isEditMode = isEditMode,
                                onDelete = { onRemoveSkill(skill.id) }
                            )
                        }
                    }
                }
            }
        }
        
        if (skills.isEmpty() && !isEditMode) {
            Text(
                text = "Nenhuma habilidade cadastrada ainda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SkillBadge(
    skillName: String,
    badgeColor: Color,
    textColor: Color,
    isEditMode: Boolean = false,
    onDelete: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeColor)
            .border(BorderStroke(1.dp, textColor.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = skillName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = textColor
            )
            if (isEditMode) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remover skill",
                    tint = textColor,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
fun ExperienceTimeline(
    experiences: List<ExperienceEntity>,
    primaryColor: Color,
    isEditMode: Boolean = false,
    onAddExperience: () -> Unit = {},
    onRemoveExperience: (Int) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isEditMode) {
            OutlinedButton(
                onClick = onAddExperience,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("add_experience_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adicionar Experiência Profissional", fontWeight = FontWeight.Bold)
            }
        }

        if (experiences.isEmpty() && !isEditMode) {
            Text(
                text = "Nenhuma experiência profissional cadastrada ainda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        experiences.forEachIndexed { index, exp ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Timeline Line with Node Circle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                    if (index < experiences.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .drawBehind {
                                    val size = this.size
                                    var y = 0f
                                    val interval = 10f
                                    while (y < size.height) {
                                        drawLine(
                                            color = primaryColor.copy(alpha = 0.4f),
                                            start = Offset(size.width / 2, y),
                                            end = Offset(size.width / 2, y + 6f),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                        y += interval
                                    }
                                }
                        )
                    }
                }

                // Experience details
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, bottom = 24.dp)
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exp.role,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isEditMode) {
                            IconButton(
                                onClick = { onRemoveExperience(exp.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir experiência",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${exp.company} • ${exp.period}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = exp.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GithubReposSection(
    state: GithubReposUiState,
    primaryColor: Color,
    isEditMode: Boolean = false,
    onRefresh: () -> Unit,
    onEditProfile: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isEditMode) {
            OutlinedButton(
                onClick = onEditProfile,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_github_username_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adicionar / Alterar Usuário GitHub", fontWeight = FontWeight.Bold)
            }
        }

        when (state) {
            is GithubReposUiState.Idle -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Nenhum usuário do GitHub cadastrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cadastrar Usuário do GitHub", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is GithubReposUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            is GithubReposUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.message.contains("404") || state.message.contains("não encontrado", ignoreCase = true)) {
                            "Usuário do GitHub não encontrado (HTTP 404). Verifique se o nome está correto."
                        } else {
                            state.message
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRefresh,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Tentar novamente")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tentar Novamente")
                        }

                        OutlinedButton(
                            onClick = onEditProfile,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Corrigir usuário")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Corrigir Usuário")
                        }
                    }
                }
            }
            is GithubReposUiState.Success -> {
                val repos = state.repos
                if (repos.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Nenhum repositório público encontrado para este usuário.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isEditMode) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onEditProfile) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Alterar Usuário do GitHub")
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repos.take(4).forEach { repo ->
                            GithubRepoCard(repo = repo, primaryColor = primaryColor)
                        }

                        if (repos.size > 4) {
                            Text(
                                text = "E mais ${repos.size - 4} repositórios adicionais no GitHub.",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GithubRepoCard(
    repo: GithubRepo,
    primaryColor: Color
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repo.htmlUrl))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast
                        .makeText(context, "Não foi possível abrir o repositório", Toast.LENGTH_SHORT)
                        .show()
                }
            },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Stars Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stars",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = repo.stargazersCount.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main language tag
                repo.language?.let { lang ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(getLanguageColor(lang))
                        )
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Link symbol
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Ver repositório",
                        style = MaterialTheme.typography.bodySmall.copy(color = primaryColor, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> Color(0xFF7F52FF)
        "java" -> Color(0xFFB07219)
        "javascript" -> Color(0xFFF1E05A)
        "typescript" -> Color(0xFF3178C6)
        "python" -> Color(0xFF3572A5)
        "html" -> Color(0xFFE34C26)
        "css" -> Color(0xFF563D7C)
        "shell" -> Color(0xFF89E051)
        "c++" -> Color(0xFFF34B7D)
        "go" -> Color(0xFF00ADD8)
        else -> Color(0xFF9E9E9E)
    }
}

@Composable
fun ContactSection(
    profile: ProfileEntity,
    primaryColor: Color,
    secondaryColor: Color,
    isEditMode: Boolean = false,
    onEditProfile: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ContactRow(icon = Icons.Outlined.Email, label = "E-mail Profissional", value = profile.email, primaryColor = primaryColor)
        ContactRow(icon = Icons.Outlined.Phone, label = "Telefone / WhatsApp", value = profile.phone, primaryColor = primaryColor)
        ContactRow(icon = Icons.Outlined.LocationOn, label = "Endereço", value = profile.location, primaryColor = primaryColor)
        ContactRow(icon = Icons.Outlined.Link, label = "LinkedIn", value = profile.linkedinUrl, primaryColor = primaryColor)

        if (isEditMode) {
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onEditProfile,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("edit_contact_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Editar Dados de Contato", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    primaryColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Simple FlowRow implementation for older layouts
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Composable
fun CertificatesSection(
    certificates: List<CertificateEntity>,
    primaryColor: Color,
    isEditMode: Boolean = false,
    onAddCertificate: () -> Unit = {},
    onRemoveCertificate: (Int) -> Unit = {}
) {
    var selectedImageForDialog by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isEditMode) {
            OutlinedButton(
                onClick = onAddCertificate,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("add_certificate_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adicionar Certificado / Cursos", fontWeight = FontWeight.Bold)
            }
        }

        if (certificates.isEmpty() && !isEditMode) {
            Text(
                text = "Nenhum certificado cadastrado ainda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        certificates.forEach { cert ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("certificate_card_${cert.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Miniatura do anexo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.08f))
                            .border(1.dp, primaryColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .clickable(enabled = !cert.attachmentPath.isNullOrBlank()) {
                                selectedImageForDialog = cert.attachmentPath
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!cert.attachmentPath.isNullOrBlank()) {
                            AsyncImage(
                                model = cert.attachmentPath,
                                contentDescription = "Miniatura do certificado ${cert.title}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                primaryColor.copy(alpha = 0.2f),
                                                primaryColor.copy(alpha = 0.05f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Certificado sem imagem",
                                    tint = primaryColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Informações do certificado
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = cert.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = cert.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isEditMode) {
                        IconButton(onClick = { onRemoveCertificate(cert.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir certificado",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog to view enlarged certificate image
    selectedImageForDialog?.let { imagePath ->
        AlertDialog(
            onDismissRequest = { selectedImageForDialog = null },
            confirmButton = {
                TextButton(onClick = { selectedImageForDialog = null }) {
                    Text("Fechar")
                }
            },
            title = {
                Text(
                    text = "Visualização do Certificado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imagePath,
                        contentDescription = "Certificado Ampliado",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }
        )
    }
}

// Dialogs for Inline Editing
@Composable
fun InlineEditProfileDialog(
    profile: ProfileEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, bio: String, email: String, phone: String, location: String, github: String, linkedin: String) -> Unit,
    primaryColor: Color
) {
    var name by remember { mutableStateOf(profile.name) }
    var role by remember { mutableStateOf(profile.role) }
    var bio by remember { mutableStateOf(profile.bio) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }
    var location by remember { mutableStateOf(profile.location) }
    var github by remember { mutableStateOf(profile.githubUsername) }
    var linkedin by remember { mutableStateOf(profile.linkedinUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Perfil Profissional", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Cargo / Título Profissional") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Resumo / Biografia") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail Profissional") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefone / WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Localização (Cidade, Estado)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = github,
                        onValueChange = { github = it },
                        label = { Text("Usuário GitHub") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = linkedin,
                        onValueChange = { linkedin = it },
                        label = { Text("URL do LinkedIn") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, role, bio, email, phone, location, github, linkedin)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar Perfil", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun InlineAddSkillDialog(
    existingSkills: List<SkillEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, category: String) -> Unit,
    primaryColor: Color
) {
    var name by remember { mutableStateOf("") }

    val defaultPool = remember {
        listOf(
            "Desenvolvimento",
            "Frameworks & Libs",
            "Bancos de Dados",
            "DevOps & Cloud",
            "Design UX/UI",
            "Infraestrutura",
            "Ferramentas & Outros"
        )
    }

    val existingCategories = remember(existingSkills) {
        existingSkills
            .map { it.category.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
    }

    val newCategorySuggestions = remember(existingSkills) {
        defaultPool
            .filter { defaultCat ->
                existingCategories.none { existing -> existing.equals(defaultCat.trim(), ignoreCase = true) }
            }
            .take(2)
    }

    var category by remember {
        mutableStateOf(
            existingCategories.firstOrNull() ?: newCategorySuggestions.firstOrNull() ?: "Desenvolvimento"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Habilidade", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Skill (ex: Kotlin, Jetpack Compose)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_skill_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria (digite nova ou selecione abaixo)") },
                    placeholder = { Text("Ex: Inteligência Artificial") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inline_skill_category_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (existingCategories.isNotEmpty()) {
                    Text(
                        text = "Categorias já criadas:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        existingCategories.forEach { cat ->
                            val isSelected = category.trim().equals(cat.trim(), ignoreCase = true)
                            Surface(
                                onClick = { category = cat },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                if (newCategorySuggestions.isNotEmpty()) {
                    Text(
                        text = "Sugestões de novas categorias (máx. 2):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        newCategorySuggestions.forEach { cat ->
                            val isSelected = category.trim().equals(cat.trim(), ignoreCase = true)
                            Surface(
                                onClick = { category = cat },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && category.isNotBlank()) {
                        onSave(name.trim(), category.trim())
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && category.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Adicionar Skill", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun InlineAddExperienceDialog(
    onDismiss: () -> Unit,
    onSave: (company: String, role: String, period: String, description: String) -> Unit,
    primaryColor: Color
) {
    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkHistory, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Experiência", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Empresa / Organização") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Cargo (ex: Desenvolvedor Senior)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = period,
                    onValueChange = { period = it },
                    label = { Text("Período (ex: 2022 - Presente)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição das Atividades / Conquistas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (company.isNotBlank() && role.isNotBlank()) {
                        onSave(company.trim(), role.trim(), period.trim(), description.trim())
                        onDismiss()
                    }
                },
                enabled = company.isNotBlank() && role.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Adicionar Experiência", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun InlineAddCertificateDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, date: String) -> Unit,
    primaryColor: Color
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Certificado / Curso", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome do Certificado / Curso") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data de Emissão (ex: Jul/2024)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), date.trim())
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Adicionar Certificado", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SaveCurriculumConfirmDialog(
    onDismiss: () -> Unit,
    viewModel: PortfolioViewModel,
    primaryColor: Color,
    onConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val savedResumes by viewModel.savedResumes.collectAsState()
    val currentSelectedResume by viewModel.selectedResumeName.collectAsState()

    var selectedResumeName by remember { mutableStateOf(currentSelectedResume) }
    var isNewResumeVariant by remember { mutableStateOf(false) }
    var customResumeName by remember { mutableStateOf("") }
    var syncCloud by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Alterações no Currículo", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Em qual versão de currículo você deseja salvar as edições?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    savedResumes.forEach { name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isNewResumeVariant = false
                                    selectedResumeName = name
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (!isNewResumeVariant && selectedResumeName == name),
                                onClick = {
                                    isNewResumeVariant = false
                                    selectedResumeName = name
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Versão: $name", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isNewResumeVariant = true }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = isNewResumeVariant,
                            onClick = { isNewResumeVariant = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ Criar Nova Versão (ex: Android Lead)",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }

                    if (isNewResumeVariant) {
                        OutlinedTextField(
                            value = customResumeName,
                            onValueChange = { customResumeName = it },
                            label = { Text("Nome da Nova Versão") },
                            placeholder = { Text("Ex: Desenvolvedor Senior Mobile") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = syncCloud,
                        onCheckedChange = { syncCloud = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Sincronizar no Firebase Cloud",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Salva no Room offline e sincroniza ao conectar à rede.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalResumeName = if (isNewResumeVariant && customResumeName.isNotBlank()) {
                        customResumeName.trim()
                    } else {
                        selectedResumeName
                    }

                    if (syncCloud) {
                        viewModel.syncWithCloud(com.example.ui.viewmodel.ConflictResolution.PUSH_OVERWRITE, resumeId = finalResumeName)
                    }

                    Toast.makeText(
                        context,
                        "Currículo '$finalResumeName' salvo com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()

                    onConfirmed()
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar e Salvar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ExportOptionsDialog(
    onDismiss: () -> Unit,
    profile: ProfileEntity,
    skills: List<SkillEntity>,
    experiences: List<ExperienceEntity>,
    certificates: List<CertificateEntity>,
    themeSettings: com.example.data.local.entities.ThemeSettingsEntity,
    primaryColor: Color
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.IosShare,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Currículo", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Escolha o formato em que deseja exportar o seu portfólio:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: PDF ATS
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            ExportUtils.exportToAtsPdf(context, profile, skills, experiences)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Currículo ATS (PDF)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Formato limpo otimizado para sistemas de recrutamento.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.Download, contentDescription = "Baixar", tint = primaryColor)
                    }
                }

                // Option 2: Styled HTML Website
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            ExportUtils.exportToStyledHtml(context, profile, skills, experiences, themeSettings)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Site Portfólio (HTML)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Página HTML interativa e responsiva pronta para publicar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.Download, contentDescription = "Baixar", tint = primaryColor)
                    }
                }

                // Option 3: JSON Backup
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDismiss()
                            ExportUtils.exportToJson(context, profile, skills, experiences, certificates)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataObject,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Backup de Dados (JSON)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Estrutura completa de dados para cópia de segurança.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.Download, contentDescription = "Baixar", tint = primaryColor)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ImportOptionsDialog(
    onDismiss: () -> Unit,
    viewModel: PortfolioViewModel,
    importState: LinkedInImportUiState,
    primaryColor: Color
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPdfName by remember { mutableStateOf("") }
    var rawText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(false) }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            var name = "Curriculo.pdf"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            selectedPdfName = name
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar Currículo com IA", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = primaryColor
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("📁 Arquivo PDF", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("📝 Copiar Texto", fontWeight = FontWeight.Bold) }
                    )
                }

                when (importState) {
                    is LinkedInImportUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = primaryColor)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (selectedTabIndex == 0) "Lendo PDF e extraindo currículo com Gemini IA..." else "Processando texto com Gemini IA...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    is LinkedInImportUiState.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = BorderStroke(1.dp, Color(0xFF81C784)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(importState.message, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is LinkedInImportUiState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.dp, Color(0xFFE57373)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFC62828))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(importState.error, color = Color(0xFFB71C1C), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    else -> {
                        if (selectedTabIndex == 0) {
                            // PDF Import View
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Selecione um arquivo de currículo em PDF para extrair nome, cargo, biografia, habilidades e experiências automaticamente com a Gemini IA.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedButton(
                                    onClick = { pdfLauncher.launch("application/pdf") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = primaryColor)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (selectedPdfName.isNotBlank()) selectedPdfName else "Escolher Arquivo PDF",
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = replaceExisting,
                                        onCheckedChange = { replaceExisting = it }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Substituir dados atuais ao importar",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedPdfUri?.let { uri ->
                                            viewModel.importPdfData(context, uri, replaceExisting)
                                        } ?: Toast.makeText(context, "Selecione um arquivo PDF primeiro.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = selectedPdfUri != null
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analisar PDF com IA", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Text Import View
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Cole o texto do seu perfil do LinkedIn ou currículo abaixo:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = rawText,
                                    onValueChange = { rawText = it },
                                    placeholder = { Text("Cole aqui o texto do seu perfil ou currículo...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = replaceExisting,
                                        onCheckedChange = { replaceExisting = it }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Substituir dados atuais ao importar",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (rawText.isNotBlank()) {
                                            viewModel.importLinkedInData(rawText, replaceExisting)
                                        } else {
                                            Toast.makeText(context, "Cole algum texto antes de continuar.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = rawText.isNotBlank()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analisar Texto com IA", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    viewModel.resetLinkedInImportState()
                }
            ) {
                Text(if (importState is LinkedInImportUiState.Success) "Concluir" else "Fechar")
            }
        }
    )
}
