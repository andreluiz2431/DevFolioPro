package com.example.ui.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ExperienceEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.SectionOrderEntity
import com.example.data.local.entities.SkillEntity
import com.example.ui.theme.toColor
import com.example.ui.viewmodel.PortfolioViewModel
import com.example.ui.viewmodel.LinkedInImportUiState

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1: Theme Customization (Motor de Temas)
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

        // SECTION: Export Portfolio & Resume (Exportação & Download)
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

        // SECTION: Import LinkedIn Data (Importação via IA)
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

        // SECTION 2: Layout Reordering (Gestão de Layout)
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

        // SECTION 3: Profile and Links (Perfil e Redes)
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

        // SECTION 4: Manage Skills (Gerenciar Habilidades)
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

        // SECTION 5: Experience Timeline Settings (Linha do Tempo)
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

    var name by remember { mutableStateOf(profile.name) }
    var role by remember { mutableStateOf(profile.role) }
    var bio by remember { mutableStateOf(profile.bio) }
    var github by remember { mutableStateOf(profile.githubUsername) }
    var linkedin by remember { mutableStateOf(profile.linkedinUrl) }
    var email by remember { mutableStateOf(profile.email) }
    var phone by remember { mutableStateOf(profile.phone) }
    var location by remember { mutableStateOf(profile.location) }

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

