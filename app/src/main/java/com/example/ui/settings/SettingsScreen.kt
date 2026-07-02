package com.example.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.data.local.entities.ExperienceEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.SectionOrderEntity
import com.example.data.local.entities.SkillEntity
import com.example.ui.theme.toColor
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.viewmodel.LinkedInImportUiState
import com.example.utils.ImageUtils
import com.example.ui.viewmodel.FirebaseSyncUiState
import com.example.ui.viewmodel.ConflictResolution
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

enum class SettingsCategory(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    PROFILE_CONTENT(
        title = "Dados & Perfil",
        description = "Edite suas informações, habilidades e histórico profissional.",
        icon = Icons.Default.Badge
    ),
    DESIGN_THEME(
        title = "Tema & Design",
        description = "Personalize o motor de cores, temas e a ordem das seções.",
        icon = Icons.Default.Palette
    ),
    SYNC_AI(
        title = "Nuvem & IA",
        description = "Sincronize com Firebase ou importe dados do LinkedIn com IA.",
        icon = Icons.Default.SmartToy
    ),
    EXPORT(
        title = "Exportação",
        description = "Gere PDFs de alta qualidade ou baixe backups do currículo.",
        icon = Icons.Default.Download
    )
}

@Composable
fun CategoryCard(
    category: SettingsCategory,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .testTag("category_card_${category.name}"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: PortfolioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeSettings by viewModel.themeSettings.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val experiences by viewModel.experiences.collectAsState()
    val sections by viewModel.sectionOrders.collectAsState()

    val primaryColor = themeSettings.primaryColorHex.toColor()

    var currentCategory by remember { mutableStateOf<SettingsCategory?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentCategory == null) {
            // Dashboard Welcome Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Painel de Controle",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gerencie as configurações visuais, os dados do currículo e sincronizações na nuvem.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Categories Grid
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CategoryCard(
                            category = SettingsCategory.PROFILE_CONTENT,
                            primaryColor = primaryColor,
                            onClick = { currentCategory = SettingsCategory.PROFILE_CONTENT },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            category = SettingsCategory.DESIGN_THEME,
                            primaryColor = primaryColor,
                            onClick = { currentCategory = SettingsCategory.DESIGN_THEME },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CategoryCard(
                            category = SettingsCategory.SYNC_AI,
                            primaryColor = primaryColor,
                            onClick = { currentCategory = SettingsCategory.SYNC_AI },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryCard(
                            category = SettingsCategory.EXPORT,
                            primaryColor = primaryColor,
                            onClick = { currentCategory = SettingsCategory.EXPORT },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // App Update Section - Isolated on the main menu
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionCard(
                    title = "Atualizações do Aplicativo",
                    icon = Icons.Default.Update,
                    primaryColor = primaryColor
                ) {
                    AppUpdateSettings(
                        viewModel = viewModel,
                        primaryColor = primaryColor
                    )
                }
            }

            // Information Card footer
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Toque em uma das categorias do painel acima para acessar as ferramentas detalhadas de customização e conteúdo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Category Active Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    IconButton(
                        onClick = { currentCategory = null },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar ao painel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = currentCategory!!.icon,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = currentCategory!!.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = currentCategory!!.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Category detail settings
            when (currentCategory) {
                SettingsCategory.PROFILE_CONTENT -> {
                    // Profile Settings
                    item {
                        SettingsSectionCard(
                            title = "Perfil Profissional e Redes Sociais",
                            icon = Icons.Default.Badge,
                            primaryColor = primaryColor
                        ) {
                            ProfileSettings(
                                profile = profile,
                                viewModel = viewModel,
                                primaryColor = primaryColor
                            )
                        }
                    }
                    // Skills Settings
                    item {
                        SettingsSectionCard(
                            title = "Gerenciar Habilidades (Skills)",
                            icon = Icons.Default.Terminal,
                            primaryColor = primaryColor
                        ) {
                            SkillsSettings(
                                skills = skills,
                                viewModel = viewModel,
                                primaryColor = primaryColor
                            )
                        }
                    }
                    // Experience Settings
                    item {
                        SettingsSectionCard(
                            title = "Gerenciar Linha do Tempo (Experiências)",
                            icon = Icons.Default.WorkHistory,
                            primaryColor = primaryColor
                        ) {
                            ExperienceSettings(
                                experiences = experiences,
                                viewModel = viewModel,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
                SettingsCategory.DESIGN_THEME -> {
                    // Theme Engine Settings
                    item {
                        SettingsSectionCard(
                            title = "Motor de Temas (Customizar Cores)",
                            icon = Icons.Default.Palette,
                            primaryColor = primaryColor
                        ) {
                            ThemeEngineSettings(
                                themeSettings = themeSettings,
                                viewModel = viewModel,
                                primaryColor = primaryColor
                            )
                        }
                    }
                    // Layout reordering
                    item {
                        SettingsSectionCard(
                            title = "Gestão de Layout (Reordenar Seções)",
                            icon = Icons.Default.Layers,
                            primaryColor = primaryColor
                        ) {
                            LayoutManagerSettings(
                                sections = sections,
                                viewModel = viewModel,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
                SettingsCategory.SYNC_AI -> {
                    // Cloud Sync Settings
                    item {
                        SettingsSectionCard(
                            title = "Nuvem & Sincronização (Firebase)",
                            icon = Icons.Default.CloudSync,
                            primaryColor = primaryColor
                        ) {
                            CloudSyncSettings(
                                viewModel = viewModel,
                                primaryColor = primaryColor
                            )
                        }
                    }
                    // LinkedIn Import
                    item {
                        SettingsSectionCard(
                            title = "Importar Dados do LinkedIn (IA)",
                            icon = Icons.Default.SmartToy,
                            primaryColor = primaryColor
                        ) {
                            LinkedInImportSettings(
                                viewModel = viewModel,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
                SettingsCategory.EXPORT -> {
                    // Export & Download
                    item {
                        SettingsSectionCard(
                            title = "Exportar Portfólio & Download",
                            icon = Icons.Default.Download,
                            primaryColor = primaryColor
                        ) {
                            ExportOptionsSettings(
                                profile = profile,
                                skills = skills,
                                experiences = experiences,
                                themeSettings = themeSettings,
                                primaryColor = primaryColor
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        // Bottom space
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
fun ThemeEngineSettings(
    themeSettings: com.example.data.local.entities.ThemeSettingsEntity,
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    val context = LocalContext.current

    var primHex by remember { mutableStateOf(themeSettings.primaryColorHex) }
    var secHex by remember { mutableStateOf(themeSettings.secondaryColorHex) }
    var bgHex by remember { mutableStateOf(themeSettings.backgroundColorHex) }
    var txtHex by remember { mutableStateOf(themeSettings.textColorHex) }

    // Sync state when database updates
    LaunchedEffect(themeSettings) {
        primHex = themeSettings.primaryColorHex
        secHex = themeSettings.secondaryColorHex
        bgHex = themeSettings.backgroundColorHex
        txtHex = themeSettings.textColorHex
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Predefined Themes Buttons
        Text(
            text = "Paletas Prontas Recomendadas:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PaletteButton(
                name = "Indigo",
                primary = "#3F51B5",
                secondary = "#009688",
                bg = "#F5F5F5",
                txt = "#212121",
                onClick = { viewModel.updateThemeColors("#3F51B5", "#009688", "#F5F5F5", "#212121") }
            )
            PaletteButton(
                name = "Deep Sea",
                primary = "#006064",
                secondary = "#00E5FF",
                bg = "#E0F7FA",
                txt = "#004D40",
                onClick = { viewModel.updateThemeColors("#006064", "#00E5FF", "#E0F7FA", "#004D40") }
            )
            PaletteButton(
                name = "Sunset",
                primary = "#D32F2F",
                secondary = "#F57C00",
                bg = "#FFF3E0",
                txt = "#3E2723",
                onClick = { viewModel.updateThemeColors("#D32F2F", "#F57C00", "#FFF3E0", "#3E2723") }
            )
            PaletteButton(
                name = "Forest",
                primary = "#2E7D32",
                secondary = "#8BC34A",
                bg = "#F1F8E9",
                txt = "#1B5E20",
                onClick = { viewModel.updateThemeColors("#2E7D32", "#8BC34A", "#F1F8E9", "#1B5E20") }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Personalização Avançada (HEX):",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )

        OutlinedTextField(
            value = primHex,
            onValueChange = { primHex = it },
            label = { Text("Cor Primária (Hex)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.ColorLens, contentDescription = null, tint = primaryColor) }
        )

        OutlinedTextField(
            value = secHex,
            onValueChange = { secHex = it },
            label = { Text("Cor Secundária (Hex)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.ColorLens, contentDescription = null, tint = secHex.toColor()) }
        )

        OutlinedTextField(
            value = bgHex,
            onValueChange = { bgHex = it },
            label = { Text("Cor de Fundo (Hex)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Colorize, contentDescription = null) }
        )

        OutlinedTextField(
            value = txtHex,
            onValueChange = { txtHex = it },
            label = { Text("Cor do Texto (Hex)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    try {
                        Color(android.graphics.Color.parseColor(primHex))
                        Color(android.graphics.Color.parseColor(secHex))
                        Color(android.graphics.Color.parseColor(bgHex))
                        Color(android.graphics.Color.parseColor(txtHex))

                        viewModel.updateThemeColors(primHex, secHex, bgHex, txtHex)
                        Toast.makeText(context, "Cores salvas com sucesso!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Formatos HEX inválidos (Use #RRGGBB)", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Aplicar Cores")
            }

            OutlinedButton(
                onClick = {
                    viewModel.restoreDefaultColors()
                    Toast.makeText(context, "Cores restauradas para o padrão!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Restaurar Padrão")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Automatic / Forced Dark Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Usar Tema do Sistema", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Segue as configurações globais do Android", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = themeSettings.useSystemTheme,
                onCheckedChange = { viewModel.updateThemeModes(useSystem = it, forceDark = themeSettings.isDarkModeForced) },
                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
            )
        }

        if (!themeSettings.useSystemTheme) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Forçar Modo Escuro (Dark Mode)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Deixa o app escuro permanentemente", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = themeSettings.isDarkModeForced,
                    onCheckedChange = { viewModel.updateThemeModes(useSystem = false, forceDark = it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                )
            }
        }
    }
}

@Composable
fun PaletteButton(
    name: String,
    primary: String,
    secondary: String,
    bg: String,
    txt: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .width(76.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bg.toColor())
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(primary.toColor()))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(secondary.toColor()))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 10.sp,
                color = txt.toColor(),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LayoutManagerSettings(
    sections: List<SectionOrderEntity>,
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Use as setas para alterar a ordem das seções na tela inicial:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        sections.sortedBy { it.displayOrder }.forEachIndexed { index, section ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.moveSectionUp(section) },
                            enabled = index > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Subir seção",
                                tint = if (index > 0) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.moveSectionDown(section) },
                            enabled = index < sections.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Descer seção",
                                tint = if (index < sections.size - 1) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSettings(
    profile: ProfileEntity,
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    val context = LocalContext.current
    val currentUser by viewModel.firebaseSyncManager.currentUser.collectAsState()

    var name by remember { mutableStateOf(profile.name) }
    var role by remember { mutableStateOf(profile.role) }
    var bio by remember { mutableStateOf(profile.bio) }
    var github by remember { mutableStateOf(profile.githubUsername) }
    var linkedin by remember { mutableStateOf(profile.linkedinUrl) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }
    var location by remember { mutableStateOf(profile.location) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64Str = ImageUtils.uriToBase64(context, uri)
            if (base64Str != null) {
                viewModel.updateProfilePhoto(base64Str)
                Toast.makeText(context, "Foto da galeria adicionada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(profile) {
        name = profile.name
        role = profile.role
        bio = profile.bio
        github = profile.githubUsername
        linkedin = profile.linkedinUrl
        email = profile.email
        phone = profile.phone
        location = profile.location
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Foto de Perfil Controller
        Text(
            text = "Foto de Perfil",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            // Visualização da Foto Atual
            val initials = name.split(" ")
                .filter { it.trim().isNotEmpty() }
                .map { it.first().uppercase() }
                .take(2)
                .joinToString("")
                .ifEmpty { "AS" }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f))
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!profile.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profile.photoUrl,
                        contentDescription = "Foto Atual",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    )
                }
            }

            // Opções de Edição
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galeria", fontSize = 12.sp)
                    }

                    if (!profile.photoUrl.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateProfilePhoto(null)
                                Toast.makeText(context, "Foto de perfil removida.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remover", fontSize = 12.sp)
                        }
                    }
                }

                val googlePhotoUrl = currentUser?.photoUrl
                if (!googlePhotoUrl.isNullOrBlank() && googlePhotoUrl != profile.photoUrl) {
                    Button(
                        onClick = {
                            viewModel.updateProfilePhoto(googlePhotoUrl)
                            Toast.makeText(context, "Foto do Google sincronizada!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Usar Foto do Google", fontSize = 12.sp)
                    }
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
            singleLine = true
        )

        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Cargo / Especialidade") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Biografia / Resumo Profissional") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        OutlinedTextField(
            value = github,
            onValueChange = { github = it },
            label = { Text("Usuário GitHub (para buscar repositórios)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null) }
        )

        OutlinedTextField(
            value = linkedin,
            onValueChange = { linkedin = it },
            label = { Text("Link do LinkedIn") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail para Contato") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefone / WhatsApp") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Cidade, Estado, País") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
        )

        Button(
            onClick = {
                viewModel.updateProfile(
                    name = name,
                    role = role,
                    bio = bio,
                    githubUsername = github,
                    linkedinUrl = linkedin,
                    email = email,
                    phone = phone,
                    location = location
                )
                Toast.makeText(context, "Dados do perfil salvos com sucesso!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Salvar Perfil")
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SkillsSettings(
    skills: List<SkillEntity>,
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    val context = LocalContext.current
    var newSkillName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Desenvolvimento") } // "Desenvolvimento" or "Infraestrutura"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Form to Add Skill
        Text("Adicionar Habilidade:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = newSkillName,
            onValueChange = { newSkillName = it },
            label = { Text("Nome da Skill (Ex: Kotlin, AWS)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Categoria:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedCategory == "Desenvolvimento",
                    onClick = { selectedCategory = "Desenvolvimento" },
                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                )
                Text("Software Dev", fontSize = 13.sp, modifier = Modifier.clickable { selectedCategory = "Desenvolvimento" })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedCategory == "Infraestrutura",
                    onClick = { selectedCategory = "Infraestrutura" },
                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                )
                Text("IT Infra", fontSize = 13.sp, modifier = Modifier.clickable { selectedCategory = "Infraestrutura" })
            }
        }

        Button(
            onClick = {
                if (newSkillName.isNotBlank()) {
                    viewModel.addSkill(newSkillName.trim(), selectedCategory)
                    newSkillName = ""
                    Toast.makeText(context, "Skill cadastrada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Nome da Skill não pode ser vazio", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cadastrar Skill")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Skills List with deletion options
        Text("Habilidades Atuais (${skills.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

        com.example.ui.home.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            skills.forEach { skill ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (skill.category == "Desenvolvimento") primaryColor.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = skill.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (skill.category == "Desenvolvimento") primaryColor else MaterialTheme.colorScheme.secondary
                        )
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Excluir skill",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.removeSkill(skill.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExperienceSettings(
    experiences: List<ExperienceEntity>,
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    val context = LocalContext.current

    var company by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Form to add Experience
        Text("Cadastrar Nova Atuação:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Empresa / Instituição") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Cargo (Ex: Desenvolvedor Pleno)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = period,
            onValueChange = { period = it },
            label = { Text("Período (Ex: 2021 - 2023)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Descrição das Atividades") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = {
                if (company.isNotBlank() && role.isNotBlank() && period.isNotBlank() && desc.isNotBlank()) {
                    viewModel.addExperience(
                        company = company.trim(),
                        role = role.trim(),
                        period = period.trim(),
                        description = desc.trim()
                    )
                    company = ""
                    role = ""
                    period = ""
                    desc = ""
                    Toast.makeText(context, "Experiência cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Preencha todos os campos da experiência!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Adicionar Experiência")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Experience List to manage
        Text("Atuações Cadastradas (${experiences.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

        experiences.forEach { exp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = exp.role, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${exp.company} | ${exp.period}", fontSize = 12.sp, color = primaryColor)
                    }
                    IconButton(onClick = { viewModel.removeExperience(exp.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover experiência",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportOptionsSettings(
    profile: ProfileEntity,
    skills: List<SkillEntity>,
    experiences: List<ExperienceEntity>,
    themeSettings: com.example.data.local.entities.ThemeSettingsEntity,
    primaryColor: Color
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Gere e baixe seu portfólio nos seguintes formatos profissionais:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Option 1: PDF Model ATS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        com.example.utils.ExportUtils.exportToAtsPdf(context, profile, skills, experiences)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Currículo ATS (PDF)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Modelo limpo e estruturado em coluna única, ideal para sistemas ATS de recrutamento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Baixar PDF",
                    tint = primaryColor
                )
            }
        }

        // Option 2: Styled HTML Website
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        com.example.utils.ExportUtils.exportToStyledHtml(context, profile, skills, experiences, themeSettings)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Site Portfólio (HTML)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Site completo e responsivo, estilizado com o seu tema para publicação futura.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Baixar HTML",
                    tint = primaryColor
                )
            }
        }
    }
}

@Composable
fun LinkedInImportSettings(
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    val importState by viewModel.linkedinImportState.collectAsState()
    var rawText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(importState) {
        if (importState is LinkedInImportUiState.Success) {
            Toast.makeText(context, (importState as LinkedInImportUiState.Success).message, Toast.LENGTH_LONG).show()
            rawText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Copie o texto completo do seu perfil do LinkedIn (sobre, experiências, competências) ou o texto de um currículo PDF e cole abaixo. A Inteligência Artificial (Gemini) irá estruturar automaticamente suas informações.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = rawText,
            onValueChange = { rawText = it },
            label = { Text("Texto do LinkedIn / Currículo") },
            placeholder = { Text("Cole aqui o texto copiado...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("linkedin_import_input"),
            maxLines = 15,
            enabled = importState !is LinkedInImportUiState.Loading,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor
            )
        )

        // Switch to choose Replace vs Merge
        Surface(
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Substituir portfólio atual",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Apaga habilidades e experiências atuais para usar apenas os dados importados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = replaceExisting,
                    onCheckedChange = { replaceExisting = it },
                    enabled = importState !is LinkedInImportUiState.Loading,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = primaryColor,
                        checkedTrackColor = primaryColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("linkedin_replace_switch")
                )
            }
        }

        // Action states
        when (val state = importState) {
            is LinkedInImportUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                        Text(
                            text = "Analisando dados com Gemini AI...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            is LinkedInImportUiState.Error -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Erro na importação:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.resetLinkedInImportState() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Tentar Novamente", color = Color.White)
                        }
                    }
                }
            }
            is LinkedInImportUiState.Success -> {
                Surface(
                    color = Color(0xFFE8F5E9), // Light green background
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sucesso",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Importado com Sucesso!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1B5E20)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.resetLinkedInImportState() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = Color(0xFF1B5E20)
                            )
                        }
                    }
                }
            }
            else -> {
                Button(
                    onClick = {
                        if (rawText.isNotBlank()) {
                            viewModel.importLinkedInData(rawText, replaceExisting)
                        } else {
                            Toast.makeText(context, "Por favor, cole algum texto antes de importar.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("linkedin_import_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = rawText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar com Gemini AI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CloudSyncSettings(
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    val syncState by viewModel.syncState.collectAsState()
    val currentUser by viewModel.firebaseSyncManager.currentUser.collectAsState()
    val isFirebaseAvailable by viewModel.firebaseSyncManager.isFirebaseAvailable.collectAsState()
    val context = LocalContext.current
    var showSimulatedLogin by remember { mutableStateOf(false) }
    var showDeveloperErrorDialog by remember { mutableStateOf(false) }
    var developerErrorDetail by remember { mutableStateOf("") }

    val savedResumes by viewModel.savedResumes.collectAsState()
    val selectedResumeName by viewModel.selectedResumeName.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveResolution by remember { mutableStateOf<ConflictResolution?>(null) }

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Não foi possível obter a credencial do Google.", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            if (e is ApiException && (e.statusCode == 10 || e.statusCode == 12500)) {
                developerErrorDetail = "O erro ${e.statusCode} (DEVELOPER_ERROR) indica que as assinaturas do seu app (SHA-1/SHA-256) não estão cadastradas ou não combinam com a chave registrada no console Firebase.\n\n" +
                    "Siga estes passos para corrigir no Firebase Console:\n" +
                    "1. Acesse as Configurações do seu Projeto Firebase.\n" +
                    "2. Na seção 'Seus aplicativos', selecione este app Android.\n" +
                    "3. Clique em 'Adicionar impressão digital' e cole as chaves abaixo correspondentes ao debug.keystore deste ambiente."
                showDeveloperErrorDialog = true
            } else {
                Toast.makeText(context, "Erro no Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(syncState) {
        if (syncState is FirebaseSyncUiState.Success) {
            Toast.makeText(context, (syncState as FirebaseSyncUiState.Success).message, Toast.LENGTH_SHORT).show()
            viewModel.resetSyncState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentUser != null) {
            val user = currentUser!!
            // Profile Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!user.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.1f))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user.displayName ?: "U").take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName ?: "Usuário",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = user.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Conectado") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = Color(0xFF2E7D32)
                                )
                            )
                            val badgeText = if (user.isSimulated) "Modo Simulado" else "Firebase Cloud"
                            val badgeColor = if (user.isSimulated) Color(0xFFE65100) else Color(0xFF0D47A1)
                            SuggestionChip(
                                onClick = {},
                                label = { Text(badgeText) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = badgeColor
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.testTag("cloud_signout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sair",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Dropdown Selector of saved resumes
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Currículo Selecionado para Sincronização:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("select_resume_dropdown_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Article,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                                Text(
                                    text = selectedResumeName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        savedResumes.forEach { resumeName ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = resumeName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (resumeName == selectedResumeName) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (resumeName != "Principal") {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteResume(resumeName)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Excluir currículo",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.setSelectedResumeName(resumeName)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Text(
                    text = "Você pode alternar entre currículos salvos para diferentes propósitos de vagas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sync actions
            Text(
                text = "Escolha como sincronizar os dados locais com o currículo '$selectedResumeName':",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Button(
                onClick = { 
                    saveResolution = ConflictResolution.MERGE
                    showSaveDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sync_merge_button"),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(8.dp),
                enabled = syncState !is FirebaseSyncUiState.Loading
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sincronizar e Mesclar (Recomendado)", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.syncWithCloud(ConflictResolution.PULL_OVERWRITE, selectedResumeName) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sync_pull_button"),
                    border = BorderStroke(1.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    enabled = syncState !is FirebaseSyncUiState.Loading
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Baixar Nuvem", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { 
                        saveResolution = ConflictResolution.PUSH_OVERWRITE
                        showSaveDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sync_push_button"),
                    border = BorderStroke(1.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    enabled = syncState !is FirebaseSyncUiState.Loading
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enviar p/ Nuvem", fontSize = 12.sp)
                }
            }

            // Dialog for Saving Resume Options (Sobrescrever ou Criar Novo)
            if (showSaveDialog && saveResolution != null) {
                var saveAsNew by remember { mutableStateOf(false) }
                var newNameInput by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = {
                        Text(
                            text = "Opções de Envio à Nuvem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Como deseja salvar as informações do currículo atual no Firebase?",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { saveAsNew = false }
                                ) {
                                    RadioButton(
                                        selected = !saveAsNew,
                                        onClick = { saveAsNew = false }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Substituir currículo atual: '$selectedResumeName'",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { saveAsNew = true }
                                ) {
                                    RadioButton(
                                        selected = saveAsNew,
                                        onClick = { saveAsNew = true }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Criar novo currículo (separado)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            if (saveAsNew) {
                                OutlinedTextField(
                                    value = newNameInput,
                                    onValueChange = { newNameInput = it },
                                    label = { Text("Nome/Propósito do Currículo") },
                                    placeholder = { Text("Ex: Vaga Mobile, DevOps...") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("new_resume_name_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        focusedLabelColor = primaryColor
                                    )
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (saveAsNew && newNameInput.isBlank()) {
                                    Toast.makeText(context, "Por favor, digite um nome válido!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val targetId = if (saveAsNew) newNameInput.trim() else selectedResumeName
                                    viewModel.syncWithCloud(saveResolution!!, targetId)
                                    showSaveDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancelar", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                )
            }
        } else {
            // Logged out
            Text(
                text = "Mantenha seu portfólio salvo com segurança na nuvem! Faça login para sincronizar dados em tempo real ou transferir para outros dispositivos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                        val rIdStr = if (resId != 0) context.getString(resId) else ""
                        val webClientId = if (!rIdStr.isNullOrBlank() && rIdStr != "YOUR_GOOGLE_WEB_CLIENT_ID") {
                            rIdStr
                        } else {
                            BuildConfig.GOOGLE_WEB_CLIENT_ID
                        }

                        if (webClientId.isBlank() || webClientId == "YOUR_GOOGLE_WEB_CLIENT_ID") {
                            Toast.makeText(
                                context,
                                "Web Client ID do Google não configurado no .env ou no google-services.json! Use a Conta de Teste (Simulada) abaixo para testar.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            try {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                // Force sign out first so the Google Account chooser is always displayed
                                googleSignInClient.signOut()
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao iniciar Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = syncState !is FirebaseSyncUiState.Loading
                ) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entrar com Google (Firebase)", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showSimulatedLogin = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulated_login_button"),
                    border = BorderStroke(1.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = syncState !is FirebaseSyncUiState.Loading
                ) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entrar com Conta de Teste (Simulado)", fontWeight = FontWeight.SemiBold)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Text(
                        text = "O modo simulado permite experimentar a experiência de nuvem salvando seus backups de forma virtual, ideal para rodar no emulador imediatamente!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Show loading and status
        when (val state = syncState) {
            is FirebaseSyncUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                        Text(
                            text = "Aguardando sincronização da nuvem...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryColor
                        )
                    }
                }
            }
            is FirebaseSyncUiState.Error -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.resetSyncState() }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
            else -> {}
        }
    }

    if (showSimulatedLogin) {
        SimulatedLoginDialog(
            onDismiss = { showSimulatedLogin = false },
            onConfirm = { email, name ->
                showSimulatedLogin = false
                viewModel.signInSimulated(email, name)
            },
            primaryColor = primaryColor
        )
    }

    if (showDeveloperErrorDialog) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showDeveloperErrorDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(text = "Configurar Google Sign-In")
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = developerErrorDetail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Suas chaves de assinatura do app (SHA):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // SHA-1 Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SHA-1", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            TextButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("7D:4C:2D:D2:4F:1A:7E:B4:EA:AF:1F:C8:BE:E7:B7:AC:FF:4C:24:4C"))
                                    Toast.makeText(context, "SHA-1 copiado!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Copiar", fontSize = 11.sp, color = primaryColor)
                            }
                        }
                        Text(
                            "7D:4C:2D:D2:4F:1A:7E:B4:EA:AF:1F:C8:BE:E7:B7:AC:FF:4C:24:4C",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    // SHA-256 Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SHA-256", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            TextButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("B2:5F:95:8E:1B:A5:26:FA:25:45:79:45:11:55:65:59:D8:ED:0C:BE:69:2A:41:2D:D2:3B:F7:CF:92:3D:07:E5"))
                                    Toast.makeText(context, "SHA-256 copiado!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Copiar", fontSize = 11.sp, color = primaryColor)
                            }
                        }
                        Text(
                            "B2:5F:95:8E:1B:A5:26:FA:25:45:79:45:11:55:65:59:D8:ED:0C:BE:69:2A:41:2D:D2:3B:F7:CF:92:3D:07:E5",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeveloperErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Entendi", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun SimulatedLoginDialog(
    onDismiss: () -> Unit,
    onConfirm: (email: String, name: String) -> Unit,
    primaryColor: Color
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = primaryColor)
                Text(text = "Entrar com Conta de Teste")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Insira um e-mail e nome fictícios para simular uma conta autenticada na nuvem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Nome Completo") },
                    placeholder = { Text("Ex: Alexandre Lucas") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = false
                    },
                    label = { Text("E-mail") },
                    placeholder = { Text("Ex: dev@devfolio.pro") },
                    isError = emailError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) nameError = true
                    if (email.isBlank() || !email.contains("@")) emailError = true
                    
                    if (!nameError && !emailError) {
                        onConfirm(email, name)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Entrar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = primaryColor)
            }
        }
    )
}

@Composable
fun AppUpdateSettings(
    viewModel: PortfolioViewModel,
    primaryColor: Color
) {
    val context = LocalContext.current
    val updateState by viewModel.updateUiState.collectAsState()

    val currentVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Version Info Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Versão Instalada",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "v$currentVersion",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action & UI States
        when (val state = updateState) {
            is com.example.data.remote.UpdateUiState.Idle -> {
                Button(
                    onClick = { viewModel.checkAppUpdates() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verificar se Há Atualizações", fontWeight = FontWeight.Bold)
                }
            }
            is com.example.data.remote.UpdateUiState.Checking -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = primaryColor, modifier = Modifier.size(32.dp))
                    Text(
                        text = "Consultando a API do GitHub...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is com.example.data.remote.UpdateUiState.NoUpdateAvailable -> {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Você já está na versão mais recente!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "O aplicativo local confere perfeitamente com a última build lançada no GitHub.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = { viewModel.resetUpdateState() }) {
                            Text("Verificar Novamente", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            is com.example.data.remote.UpdateUiState.NewUpdateAvailable -> {
                val release = state.release
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF8E1)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Nova Versão Disponível!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Lançamento: ${release.name.ifBlank { release.tagName }}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "Tag: ${release.tagName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100).copy(alpha = 0.8f)
                            )
                        }

                        if (release.body.isNotBlank()) {
                            Text(
                                text = "Notas da Versão:\n${release.body}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF3E2723),
                                maxLines = 8,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }

                        Button(
                            onClick = { viewModel.downloadAndInstallApk(release.apkUrl) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Baixar e Instalar APK", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            is com.example.data.remote.UpdateUiState.Downloading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Baixando nova versão...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.progress}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = state.progress / 100f,
                        color = primaryColor,
                        trackColor = primaryColor.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
            is com.example.data.remote.UpdateUiState.DownloadCompleted -> {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Column {
                            Text(
                                text = "Download Concluído!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Abrindo o assistente de instalação do Android...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
            is com.example.data.remote.UpdateUiState.Error -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Erro ao Processar Atualização",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.resetUpdateState() },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            ) {
                                Text("Limpar", color = MaterialTheme.colorScheme.error)
                            }
                            Button(
                                onClick = { viewModel.checkAppUpdates() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Tentar Novamente", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

